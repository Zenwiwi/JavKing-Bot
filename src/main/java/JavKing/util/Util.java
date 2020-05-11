package JavKing.util;

import JavKing.command.model.OMusic;
import JavKing.main.BotContainer;
import JavKing.templates.ErrorTemplate;
import com.mongodb.client.model.Filters;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import org.bson.Document;

import java.util.Objects;

import javax.annotation.Nullable;

public class Util {
    /**
     * -based on boolean method is this true-
     * @return True for uri (SC), False for id (YT)
     */
    public static String idOrURI(OMusic trackToAdd) {
        return idOrURI(trackToAdd.uri) ? trackToAdd.uri : trackToAdd.id;
    }

    public static boolean idOrURI(String uri) {
        return !SCUtil.SCisURI(uri);
    }

    public static String resolveThumbnail(AudioTrack track, Message message) {
        String thumbnail = null;
        Document doc = (Document) BotContainer.mongoDbAdapter.getCollection("playlistThumbnail").find(Filters.eq("uri", track.getInfo().uri)).first();
        try {
            if (doc != null && doc.getString("id").equals(track.getIdentifier()) && doc.getString("thumbnail") != null) {
                thumbnail = doc.getString("thumbnail");
            } else {
                if (track.getSourceManager().getSourceName().equals("youtube")) {
                    thumbnail = Objects.requireNonNull(new YTSearch().resolveVideoParameters(track.getInfo().uri, null)).thumbnail;
                } else thumbnail = message.getEmbeds().get(0).getThumbnail().getUrl();
                try {
                    String id = track.getIdentifier();
                    thumbnail = "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg";
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

    public static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
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
}
