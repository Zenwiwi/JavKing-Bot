package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import JavKing.util.*;
import JavKing.util.SC.SCUri;
import JavKing.util.SP.SPUri;
import JavKing.util.YT.YTSearch;
import JavKing.util.YT.YTUri;
import com.google.common.base.Joiner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class play extends AbstractCommand {
    private YTSearch ytSearch;

    public play() {
        super();
        ytSearch = new YTSearch();
    }

    public static void processTrack(OMusic music, MusicPlayerManager musicPlayerManager) {
        try {
            musicPlayerManager.addToQueue(music);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "plays a YouTube or SoundCloud song";
    }

    @Override
    public String getCommand() {
        return "play";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"<url/keywords>"};
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);
        TextChannel txt = (TextChannel) channel;
        Guild guild = txt.getGuild();
        if (!playerManager.authorInVoice(guild, author))
            return Templates.command.x_mark.formatFull("**You must be in a voice channel first!**");

        Object toSend = null;
        if (args.length > 0) {
            if (YTUri.isPlaylistCode(args[0])) {
                channel.sendMessage("<:youtube:797267941824659486> **Searching for **\uD83D\uDD0E " + Util.surround(Joiner.on(" ").join(args), "`")).queue();
                playerManager.playlistAdd(args[0], author, inputMessage);
                return null;
            } else if (SCUri.SCisURI(args[0])) {
                channel.sendMessage("<:soundcloud:782829150670553119> **Searching for **\uD83D\uDD0E " + Util.surround(Joiner.on(" ").join(args), "`")).queue();
                try {
                    playerManager.addSCToQueue(args, author, inputMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Templates.command.x_mark.formatFull("**Unable to load playlist/track(s)**");
                }
            } else if (SPUri.SPisURI(args[0])) {
                channel.sendMessage("<:spotify:811735540604862484> **Searching for **\uD83D\uDD0E " + Util.surround(Joiner.on(" ").join(args), "`")).queue();
                try {
                    playerManager.addSPToQueue(args[0], author, inputMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Templates.command.x_mark.formatFull("**Unable to load playlist/track(s)**");
                }
            } else if (Joiner.on(" ").join(args).equalsIgnoreCase("lastplayed")) {
                channel.sendMessage("<:akariangry:811738629658378262> **Searching for **\uD83D\uDD0E " + Util.surround(Joiner.on(" ").join(args), "`")).queue();
                try {
                    toSend = playerManager.addLastPlayedToQueue(author, inputMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Templates.command.x_mark.formatFull("**Unable to load playlist/track(s)**");
                }
            } else {
                channel.sendMessage("<:youtube:797267941824659486> **Searching for **\uD83D\uDD0E " + Util.surround(Joiner.on(" ").join(args), "`")).queue();
                try {
                    OMusic music = ytSearch.searchVideo(args, author);
                    if (music != null) {
                        processTrack(music, playerManager);
                        toSend = playerManager.playSendYTSCMessage(music, author, BotContainer.getDotenv("YOUTUBE"), true);
                        LPUtil.updateLPURI(music.id, music.uri, music.title, music.thumbnail, inputMessage.getGuild().getId());
                    } else {
                        return Templates.command.x_mark.formatFull("**No results found for:** `" + Joiner.on(" ").join(args) + "`");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ErrorTemplate.formatFull("❌ **Unable to load audio track::**", e);
                }
            }
            if (!playerManager.isInVoiceWith(guild, author) && !playerManager.getLinkedQueue().isEmpty()) {
                VoiceChannel vc = inputMessage.getGuild().getMember(author).getVoiceState().getChannel();

                String perms = DisUtil.discordBotPermsVOICE(vc, new Permission[]{Permission.VOICE_SPEAK, Permission.VOICE_CONNECT}) +
                        DisUtil.discordBotPermsCHANNEL(channel, new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS});
                if (!perms.equals("nullnull")) return perms;

                try {
                    if (playerManager.isConnected()) {
                        playerManager.leave();
                    }
                    playerManager.connectTo(vc);
                    BotContainer.mongoDbAdapter.update("guildSettings", (TextChannel) channel, "channelId", channel.getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    return Templates.command.x_mark.formatFull("**Can't connect to voice channel, please try again!**");
                }
            }
            playerManager.setVolume(BotContainer.mongoDbAdapter.loadGuild((TextChannel) channel).volume);
            playerManager.startPlaying();
        } else
            return ErrorTemplate.formatFull(bot, getCommand(), channel, author, inputMessage);

        if (BotContainer.mongoDbAdapter.loadGuild((TextChannel) channel).announceSongs.equalsIgnoreCase("on")) {
            if (toSend instanceof EmbedBuilder) Util.sendMessage(toSend, inputMessage);
        } else Util.sendMessage(toSend, inputMessage);
        return null;
    }
}
