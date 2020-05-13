package JavKing.util;

import JavKing.command.model.OMusic;
import JavKing.main.BotContainer;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Joiner;
import net.dv8tion.jda.api.entities.User;

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
        OMusic music = BotContainer.mongoDbAdapter.loadMusic(uri, author);
        if (music == null) {
            music = searchVideo(id, author);
            BotContainer.mongoDbAdapter.updateMusic("videoId", Util.musicKeys(), Util.oMusicArray(music));
        }
        return music;
    }
}
