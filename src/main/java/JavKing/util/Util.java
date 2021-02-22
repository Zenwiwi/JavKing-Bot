package JavKing.util;

import JavKing.command.model.OGuild;
import JavKing.command.model.OMusic;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.util.SC.SCUri;
import JavKing.util.YT.YTSearch;
import com.mongodb.client.model.Filters;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.bson.Document;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class Util {
    /**
     * -based on boolean method is this true-
     *
     * @return True for uri (SC), False for id (YT)
     */
    public static String idOrURI(OMusic trackToAdd) {
        return idOrURI(trackToAdd.uri) ? trackToAdd.uri : trackToAdd.id;
    }

    public static boolean idOrURI(String uri) {
        return !SCUri.SCisURI(uri);
    }

    public static String resolveThumbnail(AudioTrack track, Message message) {
        String thumbnail = null;
        Document doc = BotContainer.mongoDbAdapter.getCollection("playlistThumbnail").find(Filters.eq("uri", track.getInfo().uri)).first();
        try {
            if (doc != null && doc.getString("id").equals(track.getIdentifier()) && doc.getString("thumbnail") != null) {
                thumbnail = doc.getString("thumbnail");
            } else {
                String id = track.getIdentifier();
                if (track.getSourceManager().getSourceName().equals("youtube")) {
                    OMusic music = new YTSearch().resolveVideoParameters(track.getInfo().uri, null);
                    thumbnail = music == null || music.thumbnail == null || music.thumbnail.equals("")
                            ? "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg"
                            : music.thumbnail;
                } else {
                    do {
                        MessageEmbed.Thumbnail thumbnailMessage = message.getEmbeds().get(0).getThumbnail();
                        assert thumbnailMessage != null;
                        thumbnail = thumbnailMessage.getUrl() == null ? thumbnailMessage.getProxyUrl() : thumbnailMessage.getUrl();
                    } while (thumbnail == null);
                    return thumbnail;
                }
                try {
                    BotContainer.mongoDbAdapter.getCollection("playlistThumbnail").findOneAndUpdate(Filters.eq("id", id),
                            new Document("guildId", message.getGuild().getId()).append("uri", track.getInfo().uri).append("id", id)
                                    .append("thumbnail", thumbnail));
                } catch (Exception ignored) {
                    BotContainer.mongoDbAdapter.getCollection("playlistThumbnail").insertOne(new Document("id", track.getIdentifier())
                            .append("uri", track.getInfo().uri).append("thumbnail", thumbnail));
                }
            }
        } catch (Exception ignored) {
        }
        return thumbnail;
    }

    public static boolean checkStr(String str) {
        return str == null || str.isEmpty();
    }

    public static String capitalize(String str, boolean bool) {
        if (checkStr(str)) return str;

        str = str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
        return bool ? str.replaceAll("[dDjJ]{2}", "DJ") : str;
    }

    public static String capitalizeAll(String str, boolean bool) {
        if (checkStr(str)) return str;

        StringBuilder sb = new StringBuilder();
        for (String s : str.split("\\s+")) {
            sb.append(s.substring(0, 1).toUpperCase()).append(s.substring(1).toLowerCase()).append(" ");
        }
        return bool ? sb.toString().replaceAll("[DdJj]{2}", "DJ") : sb.toString();
    }

    public static String camelCase(String str, String regex, String delimiter) {
        if (checkStr(str)) return str;

        List<String> sList = Arrays.asList(str.split(regex));
        sList.set(0, sList.get(0).toLowerCase());
        for (int i = 1; i < sList.size(); i++) sList.set(i, capitalize(sList.get(i), false));
        return String.join(delimiter, sList);
    }

    public static String surround(String str, String toSur) {
        return toSur + str + toSur;
    }

    public static void removeDupes(Object[] objects, int length) {
        if (length < 1) return;
        int j = 0;
        for (int i = 0; i < length - 1; i++) {
            if (objects[i] != (objects[i + 1])) {
                objects[j++] = objects[i];
            }
        }
        objects[j++] = objects[length - 1];
    }

    public static void sendMessage(Object toSend, String channelId, DiscordBot bot) {
        sendMessage(toSend, bot.getJDA().getTextChannelById(channelId));
    }

    public static void sendMessage(Object toSend, Message message) {
        sendMessage(toSend, message.getTextChannel());
    }

    public static void sendMessage(Object toSend, MessageChannel channel) {
        sendMessage(toSend, (TextChannel) channel);
    }

    public static void sendMessage(Object toSend, TextChannel channel) {
        if (toSend != null) {
            if (toSend instanceof EmbedBuilder) {
                channel.sendMessage(((EmbedBuilder) toSend).build()).queue();
            } else if (toSend instanceof File) {
                channel.sendFile((File) toSend).queue();
            } else if (toSend instanceof EmbedTemplate) {
                channel.sendMessage(((EmbedTemplate) toSend).getEmbedBuilder()).queue();
            } else {
                channel.sendMessage(toSend.toString()).queue();
            }
        }
    }

    public static String[] musicKeys() {
        return new String[]{"id", "thumbnail", "uri", "channel", "title", "duration"};
    }

    public static Object[] oMusicArray(OMusic music) {
        return new Object[]{music.id, music.thumbnail, music.uri, music.author, music.title, music.duration};
    }

    public static String[] lastPlayedKeys() {
        return new String[]{"guildId", "uri", "id", "thumbnail", "title"};
    }

    public static String[] guildKeys() {
        return new String[]{"announceSongs", "djOnly", "djRole", "prefix", "volume", "reset"};
    }

    public static String[] oGuildArray(OGuild guild) {
        return new String[]{guild.announceSongs, guild.djOnly, guild.djRole, guild.prefix, guild.volume, null};
    }
}
