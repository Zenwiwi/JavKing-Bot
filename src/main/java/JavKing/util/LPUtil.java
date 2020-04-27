package JavKing.util;

import JavKing.command.commands.play;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.templates.Templates;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

public class LPUtil {

    public static Object resolveLPURI(User author, Message message, MusicPlayerManager playerManager) {
        Document doc = (Document) BotContainer.mongoDbAdapter.getCollection("lastPlayed").find(Filters.eq("guildId", message.getGuild().getId()))
                .first();
        if (doc != null && doc.getString("guildId").equals(message.getGuild().getId())) {
            String uri = doc.getString("uri");
            String id = doc.getString("id");
            if (SCUtil.SCisURI(uri)) {
                new SCSearch().resolveSCURI(id, author, message, playerManager);
            } else if (YTUtil.isVideoCode(uri)) {
                OMusic music = new YTSearch().resolveVideoParameters(uri, author);
                play.processTrack(music, playerManager);
                return playerManager.playSendYTSCMessage(music, author, BotContainer.getDotenv("YOUTUBE"));
            } else if (YTUtil.isPlaylistCode(uri)) {
                playerManager.playlistAdd(uri, author, message);
                return null;
            } else
                return Templates.command.o_mark.formatFull("**Error retrieving last played song(s) for this server!**");
        } else
            return Templates.command.o_mark.formatFull("**No last played song(s) found for this server!**");
        return null;
    }

    public static void updateLPURI(String id, String uri, String title, String thumbnail, String guildId) {
        Document doc = (Document) BotContainer.mongoDbAdapter.getCollection("lastPlayed").find(Filters.eq("guildId", guildId))
                .first();
        if (doc != null && doc.getString("guildId").equals(guildId)) {
            BotContainer.mongoDbAdapter.getCollection("lastPlayed").updateOne(Filters.eq("guildId", guildId), new Document("$set",
                    new Document("guildId", guildId).append("uri", uri).append("id", id).append("thumbnail", thumbnail).append("title", title)));
        } else {
            BotContainer.mongoDbAdapter.getCollection("lastPlayed").insertOne(new Document("guildId", guildId).append("uri", uri).append("id", id)
                    .append("thumbnail", thumbnail).append("title", title));
        }
    }
}
