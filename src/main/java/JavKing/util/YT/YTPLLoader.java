package JavKing.util.YT;

import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.Units;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import static com.sedmelluq.discord.lavaplayer.tools.FriendlyException.Severity.COMMON;

public class YTPLLoader {
    private volatile int playlistPageCount = 10;
    private static final String REQUEST_URL = "https://www.youtube.com/youtubei/v1/browse?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8";
    private static final String REQUEST_PAYLOAD = "{\"context\":{\"client\":{\"clientName\":\"WEB\",\"clientVersion\":\"2.20210302.07.01\"}},\"continuation\":\"%s\"}";

    public void setPlaylistPageCount(int playlistPageCount) {
        this.playlistPageCount = playlistPageCount;
    }

    public AudioPlaylist load(HttpInterface httpInterface, String playlistId, String selectedVideoId,
                              Function<AudioTrackInfo, AudioTrack> trackFactory) {

        HttpGet request = new HttpGet(getPlaylistUrl(playlistId) + "&pbj=1&hl=en");

        try (CloseableHttpResponse response = httpInterface.execute(request)) {
            HttpClientTools.assertSuccessWithContent(response, "playlist response");
            HttpClientTools.assertJsonContentType(response);

            JsonBrowser json = JsonBrowser.parse(response.getEntity().getContent());

            return buildPlaylist(httpInterface, json, selectedVideoId, trackFactory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private AudioPlaylist buildPlaylist(HttpInterface httpInterface, JsonBrowser json, String selectedVideoId,
                                        Function<AudioTrackInfo, AudioTrack> trackFactory) throws IOException {

        JsonBrowser jsonResponse = json.index(1).get("response");

        JsonBrowser alerts = jsonResponse.get("alerts");

        if (!alerts.isNull()) {
            throw new FriendlyException(alerts.index(0).get("alertRenderer").get("text").get("simpleText").text(), COMMON, null);
        }

        JsonBrowser info = jsonResponse
                .get("sidebar")
                .get("playlistSidebarRenderer")
                .get("items")
                .index(0)
                .get("playlistSidebarPrimaryInfoRenderer");

        String playlistName = info
                .get("title")
                .get("runs")
                .index(0)
                .get("text")
                .text();

        JsonBrowser playlistVideoList = jsonResponse
                .get("contents")
                .get("twoColumnBrowseResultsRenderer")
                .get("tabs")
                .index(0)
                .get("tabRenderer")
                .get("content")
                .get("sectionListRenderer")
                .get("contents")
                .index(0)
                .get("itemSectionRenderer")
                .get("contents")
                .index(0)
                .get("playlistVideoListRenderer")
                .get("contents");

        List<AudioTrack> tracks = new ArrayList<>();
        String continuationsToken = extractPlaylistTracks(playlistVideoList, tracks, trackFactory);
        int loadCount = 0;
        int pageCount = playlistPageCount;

        // Also load the next pages, each result gives us a JSON with separate values for list html and next page loader html
        while (continuationsToken != null && ++loadCount < pageCount) {
            HttpPost post = new HttpPost(REQUEST_URL);
            StringEntity payload = new StringEntity(String.format(REQUEST_PAYLOAD, continuationsToken), "UTF-8");
            post.setEntity(payload);
            try (CloseableHttpResponse response = httpInterface.execute(post)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (!HttpClientTools.isSuccessWithContent(statusCode)) {
                    throw new IOException("Invalid status code for playlist response: " + statusCode);
                }

                JsonBrowser continuationJson = JsonBrowser.parse(response.getEntity().getContent());

                JsonBrowser playlistVideoListPage = continuationJson.index(1)
                        .get("response")
                        .get("continuationContents")
                        .get("playlistVideoListContinuation");

                if (playlistVideoListPage.isNull()) {
                    playlistVideoListPage = continuationJson.get("onResponseReceivedActions")
                            .index(0)
                            .get("appendContinuationItemsAction")
                            .get("continuationItems");
                }

                continuationsToken = extractPlaylistTracks(playlistVideoListPage, tracks, trackFactory);
            }
        }

        return new BasicAudioPlaylist(playlistName, tracks, findSelectedTrack(tracks, selectedVideoId), false);
    }

    private AudioTrack findSelectedTrack(List<AudioTrack> tracks, String selectedVideoId) {
        if (selectedVideoId != null) {
            for (AudioTrack track : tracks) {
                if (selectedVideoId.equals(track.getIdentifier())) {
                    return track;
                }
            }
        }

        return null;
    }

    private String extractPlaylistTracks(JsonBrowser playlistVideoList, List<AudioTrack> tracks,
                                         Function<AudioTrackInfo, AudioTrack> trackFactory) {

        if (playlistVideoList.isNull()) return null;

        final List<JsonBrowser> playlistTrackEntries = playlistVideoList.values();
        for (JsonBrowser track : playlistTrackEntries) {
            JsonBrowser item = track.get("playlistVideoRenderer");

            JsonBrowser shortBylineText = item.get("shortBylineText");

            // If the isPlayable property does not exist, it means the video is removed or private
            // If the shortBylineText property does not exist, it means the Track is Region blocked
            if (!item.get("isPlayable").isNull() && !shortBylineText.isNull()) {
                String videoId = item.get("videoId").text();
                String title = Optional.ofNullable(item.get("title").get("simpleText"))
                        .map(JsonBrowser::text)
                        .orElse(item.get("title").get("runs").index(0).get("text").text());
                String author = shortBylineText.get("runs").index(0).get("text").text();
                JsonBrowser lengthSeconds = item.get("lengthSeconds");
                long duration = Units.secondsToMillis(lengthSeconds.asLong(Units.DURATION_SEC_UNKNOWN));

                AudioTrackInfo info = new AudioTrackInfo(title, author, duration, videoId, false,
                        "https://www.youtube.com/watch?v=" + videoId);

                tracks.add(trackFactory.apply(info));
            }
        }

        JsonBrowser continuations = playlistTrackEntries.get(playlistTrackEntries.size() - 1)
                .get("continuationItemRenderer")
                .get("continuationEndpoint")
                .get("continuationCommand");

        String continuationsToken;
        if (!continuations.isNull()) {
            continuationsToken = continuations.get("token").text();
            return continuationsToken;
        }

        return null;
    }

    private static String getPlaylistUrl(String playlistId) {
        return "https://www.youtube.com/playlist?list=" + playlistId;
    }
}
