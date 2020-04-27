package JavKing.util;

import JavKing.guildSettings.GSetting;
import JavKing.handler.DefaultGuildSettings;
import JavKing.handler.GuildSettings;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.internal.utils.PermissionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisUtil {
    private static final Pattern mentionUserPattern = Pattern.compile("<@!?([0-9]{8,})>");
    private static final Pattern channelPattern = Pattern.compile("<#!?([0-9]{4,})>");
    private static final Pattern rolePattern = Pattern.compile("<@&([0-9]{4,})>");
    private static final Pattern anyMention = Pattern.compile("<[@#][&!]?([0-9]{4,})>");
    private static final Pattern discordId = Pattern.compile("(\\d{9,})");

    /**
     * find a text channel by name
     *
     * @param guild       the guild to search in
     * @param channelName the channel to search for
     * @return TextChannel || null
     */
    public static TextChannel findChannel(Guild guild, String channelName) {
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                return channel;
            }
        }
        return null;
    }

    public static String extractId(String id) {
        Matcher matcher = discordId.matcher(id);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * check if a string has any mention
     *
     * @param search the text to match
     * @return contains mention?
     */
    public static boolean hasMention(String search) {
        return anyMention.matcher(search).matches();
    }

    public static boolean hasPermission(MessageChannel channel, User user, Permission permission) {
        if (channel == null) {
            return false;
        }
        switch (channel.getType()) {
            case PRIVATE:
                return true;
            case TEXT:
                TextChannel textChannel = (TextChannel) channel;
                return PermissionUtil.checkPermission(textChannel, textChannel.getGuild().getMember(user), permission);
            default:
                return false;
        }
    }

    /**
     * find a voice channel by name
     *
     * @param guild       the guild to search in
     * @param channelName the channel to search for
     * @return VoiceChannel || null
     */
    public static VoiceChannel findVoiceChannel(Guild guild, String channelName) {
        for (VoiceChannel channel : guild.getVoiceChannels()) {
            if (channel.getName().equalsIgnoreCase(channelName)) {
                return channel;
            }
        }
        return null;
    }

    /**
     * Gets the first channel in a guild where the bot has permission to write
     *
     * @param guild the guild to search in
     * @return TextChannel || null
     */
    public static TextChannel findFirstWriteableChannel(Guild guild) {
        for (TextChannel channel : guild.getTextChannels()) {
            if (channel.canTalk()) {
                return channel;
            }
        }
        return null;
    }

    /**
     * Checks if the string contains a mention for a role
     *
     * @param input string to check for mentions
     * @return found a mention
     */
    public static boolean isUserMention(String input) {
        return mentionUserPattern.matcher(input).find();
    }

    public static boolean isRoleMention(String input) {
        return rolePattern.matcher(input).find();
    }

    /**
     * Attempts to find a user in a channel, first look for account name then for nickname
     *
     * @param channel    the channel to look in
     * @param searchText the name to look for
     * @return IUser | null
     */
    public static Member findUserIn(TextChannel channel, String searchText) {
        List<Member> users = channel.getGuild().getMembers();
        List<Member> potential = new ArrayList<>();
        int smallestDiffIndex = 0, smallestDiff = -1;
        for (Member u : users) {
            String nick = u.getEffectiveName();
            if (nick.equalsIgnoreCase(searchText)) {
                return u;
            }
            if (nick.toLowerCase().contains(searchText)) {
                potential.add(u);
                int d = Math.abs(nick.length() - searchText.length());
                if (d < smallestDiff || smallestDiff == -1) {
                    smallestDiff = d;
                    smallestDiffIndex = potential.size() - 1;
                }
            }
        }
        if (!potential.isEmpty()) {
            return potential.get(smallestDiffIndex);
        }
        return null;
    }

    /**
     * Attempts to find a user from mention, if that fails see {@link DisUtil#findUserIn(TextChannel, String)}
     *
     * @param channel    the channel context
     * @param searchText the search argument
     * @return user || null
     */
    public static User findUser(TextChannel channel, String searchText) {
        if (DisUtil.isUserMention(searchText)) {
            return channel.getJDA().getUserById(DisUtil.mentionToId(searchText));
        } else {
            Member member = DisUtil.findUserIn(channel, searchText);
            if (member != null) {
                return member.getUser();
            }
        }
        return null;
    }

    /**
     * Converts any mention to an id
     *
     * @param mention the mention to filter
     * @return a stripped down version of the mention
     */
    public static String mentionToId(String mention) {
        String id = "";
        Matcher matcher = anyMention.matcher(mention);
        if (matcher.find()) {
            id = matcher.group(1);
        }
        return id;
    }

    /**
     * Filters the command prefix from the string
     *
     * @param command the text to filter form
     * @param channel the channel where the text came from
     * @return text with the prefix filtered
     */
    public static String filterPrefix(String command, MessageChannel channel) {
        String prefix = getCommandPrefix(channel);
        if (command.startsWith(prefix)) {
            return command.substring(prefix.length());
        }
        return command;
    }

    /**
     * gets the command prefix for specified channel
     *
     * @param channel channel to check the prefix for
     * @return the command prefix
     */
    public static String getCommandPrefix(MessageChannel channel) {
        if (channel instanceof TextChannel) {
            return getCommandPrefix(((TextChannel) channel).getGuild());
        }
        return DefaultGuildSettings.getDefault(GSetting.PREFIX);
    }

    public static String getCommandPrefix(Guild guild) {
        return getCommandPrefix(guild.getIdLong());
    }

    public static String getCommandPrefix(long guildId) {
        return GuildSettings.get(guildId).getOrDefault(GSetting.PREFIX);
    }

    /**
     * attempts to find a role within a guild
     *
     * @param guild    the guild to search in
     * @param roleName the role name to search for
     * @return role or null
     */
    public static Role findRole(Guild guild, String roleName) {
        List<Role> roles = guild.getRoles();
        Role containsRole = null;
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return role;
            }
            if (containsRole == null && role.getName().contains(roleName)) {
                containsRole = role;
            }
        }
        return containsRole;
    }

    public static Role hasRole(Guild guild, String roleName) {
        List<Role> roles = guild.getRoles();
        Role containsRole = null;
        for (Role role : roles) {
            if (role.getName().equalsIgnoreCase(roleName)) {
                return role;
            }
            if (containsRole == null && role.getName().contains(roleName)) {
                containsRole = role;
            }
        }
        return containsRole;
    }

    /**
     * @param input string to check for mentions
     * @return found a mention
     */
    public static boolean isChannelMention(String input) {
        return channelPattern.matcher(input).matches();
    }
}
