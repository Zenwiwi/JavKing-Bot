package JavKing.util;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Joiner;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;
import java.io.IOException;
import java.time.Duration;
import java.util.LinkedList;
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

    public synchronized void resolvePlaylist(MusicPlayerManager musicPlayerManager, String uri, User author, Message message) {
//        System.out.println(uri);
        String plId = YTUtil.getPlaylistCode(uri);
        AudioPlaylist items = new YTPLSearch().playlist(plId, null);
        int index = 0;
        for (AudioItem item : items.getTracks()) {
            AudioTrack track = (AudioTrack) item;
            OMusic music = new OMusic();
            music.id = track.getIdentifier();
            music.thumbnail = "https://i.ytimg.com/vi/" + music.id + "/hqdefault.jpg";
            music.duration = track.getDuration();
            music.requestedBy = author.getAsTag();
            music.title = track.getInfo().title;
            music.uri = track.getInfo().uri;
            music.author = track.getInfo().author;
            musicPlayerManager.addToQueue(music);
            if (index == 0) {
                index++;
                LinkedList<OMusic> lqueue = musicPlayerManager.getLinkedQueue();
                Util.sendMessage(musicPlayerManager.playSendYTSCMessage(lqueue.get(lqueue.size() - 1), author, BotContainer.getDotenv("YOUTUBE"), true), message);
                LPUtil.updateLPURI(plId, "https://www.youtube.com/playlist?list=" + plId,
                        items.getName(), lqueue.get(lqueue.size() - 1).thumbnail, message.getGuild().getId());
            }
        }
        musicPlayerManager.playCheckVoice(message, author);
    }
}
