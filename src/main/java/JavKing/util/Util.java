package JavKing.util;

import JavKing.command.model.OMusic;
import JavKing.main.BotContainer;
import JavKing.templates.ErrorTemplate;
import com.mongodb.client.model.Filters;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import org.bson.Document;

import java.util.Objects;

public class Util {
    public static String idOrURI(OMusic trackToAdd) {
        try {
            return !SCUtil.SCisURI(trackToAdd.uri) ? trackToAdd.uri : trackToAdd.id;
        } catch (Exception e) {
            return ErrorTemplate.formatFull("**Error comparing URI to ID::**", e);
        }
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
}
