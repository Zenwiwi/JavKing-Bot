package JavKing.util.SP;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.util.YT.YTSearch;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class SPTask implements Runnable {
    private final User author;
    private final MusicPlayerManager playerManager;
    private final List<OMusic> oMusicList;
    private final int index;
    private final Message message;
    private final SPUri uri;
    private final long ET;

    SPTask(User author, Message message, MusicPlayerManager playerManager, List<OMusic> oMusicList, int index,
           SPUri uri,long ET) {
        this.author = author;
        this.playerManager = playerManager;
        this.oMusicList = oMusicList;
        this.index = index;
        this.message = message;
        this.uri = uri;
        this.ET = ET;
    }

    @Override
    public void run() {
        int load = 5;
        int forkStart = load * index - load;
        int forkEnd = load * index;
        int count = playerManager.getLinkedQueue().size();

        for (int i = forkStart; i < forkEnd; ++i) {
            if (i >= oMusicList.size()) {
//              playerManager.sortIndexTempQueue();
//              playerManager.sortIndexQueue();
                SPSearch.startSpotify(count, ET, author, message, playerManager);
                break;
            }
            Task.execute(i, author, message, oMusicList, playerManager, false);
        }
    }

    static class Task {

        public static void execute(int index, User author, Message message, List<OMusic> oMusicList, MusicPlayerManager playerManager, boolean sendMessage) throws IndexOutOfBoundsException {
            OMusic selected = oMusicList.get(index);
            String split = selected.title + " " + (selected.author.startsWith("[Artist]") ? "song" : selected.author);
            String join = String.join(" ", split.split("\\s+"));
            OMusic oMusic = new YTSearch().loadSearchResult(join, author, selected);
            int attempts = 5;
            while (oMusic == null) {
                oMusic = new YTSearch().loadSearchResult(join, author, selected);
                if (--attempts == 0) break;
            }
            if (oMusic != null) {
                playerManager.addToQueue(oMusic);
            }
        }
//          oMusic.index = index;
//          playerManager.addToTempQueue(oMusic);
//          if (sendMessage) {
//              playerManager.sortIndexTempQueue();
//          }
    }
}
