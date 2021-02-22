package JavKing.util.SP;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SPSearch {

    public static void resolveSPURI(String args, User author, Message message, MusicPlayerManager playerManager) throws Exception {
        SPUri uri = new SPUri(args);
        List<OMusic> oMusicList = uri.getType().loadTracks(uri, author, message);

        // multithreading to pull songs from yt quicker
        final ExecutorService executorService = Executors.newFixedThreadPool(8);

        for (int i = 1; i <= 8; i++) {
            executorService.execute(new SPTask(author, message, playerManager, oMusicList, i, uri));
        }

//        oMusicList = new ArrayList<>();
//        int i = 0;
//        while (i <= 8) {
//            for (Future<List<OMusic>> future : futuresList) {
//                List<OMusic> oMusics = future.get();
//                if (oMusics.get(0).index == i) {
//                    oMusicList.addAll(oMusics);
//                    i++;
//                }
//            }
//        }
//        playerManager.getLinkedQueue().addAll(oMusicList);

//        oMusicList.parallelStream().collect(Collectors.toList()).forEach(oMusic -> {
//            OMusic oMusicSearch = new YTSearch().loadSearchResult(String.join("+", oMusic.title.split("\\s+")), author);
//            if (oMusicSearch != null) playerManager.addToQueue(oMusicSearch);
//        });

//        for (OMusic oMusic : oMusicList) {
//            if (oMusic != null) playerManager.addToQueue(oMusic);
//            String join = oMusic.title /*+ " " + oMusic.author*/;
//            String[] split = join.split("\\s+");
//            join = String.join("+", split);
//            oMusic = new YTSearch().loadSearchResult(String.join("+", oMusic.title.split("\\s+")), author);
//            if (oMusic != null) playerManager.addToQueue(oMusic);
//        }
    }
}

