package JavKing.util;

import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioTrack;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeHttpContextFilter;
import com.sedmelluq.discord.lavaplayer.tools.ExceptionTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.*;

public class YTPLSearch extends YoutubeAudioSourceManager {
    private final HttpInterfaceManager httpInterfaceManager;
    private final YTPLLoader playlistLoader;

    public YTPLSearch() {
        httpInterfaceManager = HttpClientTools.createDefaultThreadLocalManager();
        httpInterfaceManager.setHttpContextFilter(new YoutubeHttpContextFilter());
        this.playlistLoader = new YTPLLoader();
    }

    public HttpInterface getHttpInterface() {
        return httpInterfaceManager.getInterface();
    }

    public AudioPlaylist playlist(String playlistId, String selectedVideoId) {
//        System.out.println("Starting to load playlist with ID " + playlistId);
        try (HttpInterface httpInterface = getHttpInterface()) {
            return playlistLoader.load(httpInterface, playlistId, selectedVideoId,
                    YTPLSearch.this::buildTrackFromInfo);
        } catch (Exception e) {
            throw ExceptionTools.wrapUnfriendlyExceptions(e);
        }
    }

    public YoutubeAudioTrack buildTrackFromInfo(AudioTrackInfo info) {
        return new YoutubeAudioTrack(info, this);
    }
}
