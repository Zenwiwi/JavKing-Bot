package JavKing.util;

import JavKing.command.model.OMusic;
import JavKing.main.BotContainer;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Joiner;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.List;

public class YTSearch {
    @Nullable
    public String getVideoId(String input) {
        return YTUtil.getVideoCode(input);
    }

    @Nullable
    public OMusic searchVideo(String[] args, User author) {
        String input = Joiner.on(" ").join(args);
        if (getVideoId(input) != null) {
            return resolveVideoParameters(input, author);
        }
        try {
            List<SearchResult> results = BotContainer.ytUtil.getYoutube().search().list("id").setQ(input)
                    .setMaxResults(1L).setType("video").setFields("items(id/videoId)")
                    .setKey(BotContainer.getDotenv("GOOGLE_API_KEY")).execute().getItems();
            if (!results.isEmpty()) {
                return searchVideo(results.get(0).getId().getVideoId(), author);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private OMusic searchVideo(String id, @Nullable User author) {
        try {
            List<Video> list = BotContainer.ytUtil.getYoutube().videos().list("snippet,id,contentDetails").setId(id).setKey(BotContainer.getDotenv("GOOGLE_API_KEY"))
                    .execute().getItems();
            return searchVideo(list.get(0), author);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private OMusic searchVideo(Video video, @Nullable User author) {
        try {
            OMusic music = new OMusic();
            music.duration = Duration.parse(video.getContentDetails().getDuration()).getSeconds() * 1000L;
            music.author = video.getSnippet().getChannelTitle();
            music.title = video.getSnippet().getTitle();
            music.uri = "https://www.youtube.com/watch?v=" + video.getId();
            if (author != null) music.requestedBy = author.getAsTag();
            music.thumbnail = video.getSnippet().getThumbnails().getHigh().getUrl();
            music.id = video.getId();
            return music;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public OMusic resolveVideoParameters(String uri, @Nullable User author) {
        String id = YTUtil.getVideoCode(uri);
        Document doc = (Document) BotContainer.mongoDbAdapter.getCollection("videoId").find(Filters.eq("id", id)).first();
        OMusic music;
        if (doc != null) {
            music = Util.fillOMusic(doc, author);
        } else {
            music = searchVideo(id, author);
            BotContainer.mongoDbAdapter.getCollection("videoId").insertOne(new Document("id", music.id).append("thumbnail", music.thumbnail)
                    .append("uri", music.uri).append("channel", music.author).append("title", music.title).append("duration", music.duration));
        }
        return music;
    }

/*
    @Nullable
    public String getPlaylistId(String[] args) {
        return YTUtil.getPlaylistCode(Joiner.on(" ").join(args));
    }
*/

//    @Nullable
//    public List<SimpleResult> searchPlaylist(String uri, String guildId) {
//        String id = YTUtil.getPlaylistCode(uri);
//        List<SimpleResult> list = new ArrayList<>();
//        try {
//            PlaylistListResponse listResponse = BotContainer.ytUtil.getYoutube().playlists().list("snippet").setId(id)
//                    .setKey(BotConfig.GOOGLE_API_KEY).execute();
//            YouTube.PlaylistItems.List playlistRequest = BotContainer.ytUtil.getYoutube().playlistItems().list("snippet,contentDetails")
//                    .setMaxResults(50L).setPlaylistId(id).setKey(BotConfig.GOOGLE_API_KEY);
//
//
//            String nextToken = "";
//            int count = 0;
//            do {
//                playlistRequest.setPageToken(nextToken);
//                PlaylistItemListResponse itemListResponse = playlistRequest.execute();
//                if (count++ == 0) {
//                    LPUtil.updateLPURI(id, uri, listResponse.getItems().get(0).getSnippet().getTitle(),
//                            itemListResponse.getItems().get(0).getSnippet().getThumbnails().getHigh().getUrl(), guildId);
//                }
//                list.addAll(itemListResponse.getItems().stream().map(playlistItem -> new SimpleResult(playlistItem.getContentDetails().getVideoId(), playlistItem.getSnippet().getTitle())).collect(Collectors.toList()));
//                nextToken = itemListResponse.getNextPageToken();
//            } while (nextToken != null);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return list;
//    }
//
//    public class SimpleResult {
//        private final String code;
//        private final String title;
//
//        public SimpleResult(String code, String title) {
//            this.code = code;
//            this.title = title;
//        }
//
//        public String getTitle() {
//            return title;
//        }
//
//        public String getCode() {
//            return code;
//        }
//    }
}
