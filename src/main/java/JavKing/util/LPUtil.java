package JavKing.util;

import JavKing.command.commands.play;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.templates.Templates;
import JavKing.util.SC.SCUri;
import JavKing.util.SP.SPUri;
import JavKing.util.YT.YTSearch;
import JavKing.util.YT.YTUri;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

public class LPUtil {

    public static Object resolveLPURI(User author, Message message, MusicPlayerManager playerManager) throws Exception {
        OMusic music = BotContainer.mongoDbAdapter.loadMusic(null, author, message.getTextChannel());
        if (music != null) {
            String uri = music.uri;
            String id = music.id;
            if (SPUri.SPisURI(uri)) {
                playerManager.addSPToQueue(uri, author, message);
            } else if (SCUri.SCisURI(uri)) {
                playerManager.addSCToQueue(new String[]{id}, author, message);
            } else if (YTUri.isPlaylistCode(uri)) {
                playerManager.playlistAdd(uri, author, message);
            } else if (YTUri.isVideoCode(uri)) {
                OMusic search = new YTSearch().resolveVideoParameters(uri, author);
                play.processTrack(search, playerManager);
                return playerManager.playSendYTSCMessage(search, author, BotContainer.getDotenv("YOUTUBE"), true);
            } else
                return Templates.command.o_mark.formatFull("**Error retrieving last played song(s) for this server!**");
        } else
            return Templates.command.o_mark.formatFull("**No last played song(s) found for this server!**");
        return null;
    }

    public static int updateLPURI(String id, String uri, String title, String thumbnail, String guildId) {
        return BotContainer.mongoDbAdapter.update("lastPlayed", guildId, Util.lastPlayedKeys(),
                new Object[]{guildId, uri, id, thumbnail, title});
    }
}
