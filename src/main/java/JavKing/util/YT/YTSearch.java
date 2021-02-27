package JavKing.util.YT;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.util.LPUtil;
import JavKing.util.Util;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Joiner;
import com.sedmelluq.discord.lavaplayer.tools.DataFormatTools;
import com.sedmelluq.discord.lavaplayer.tools.JsonBrowser;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpClientTools;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterface;
import com.sedmelluq.discord.lavaplayer.tools.io.HttpInterfaceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTSearch {

    private final HttpInterfaceManager httpInterfaceManager;
    private final Pattern polymerInitialDataRegex = Pattern.compile("(window\\[\"ytInitialData\"]|var ytInitialData)\\s*=\\s*(.*);");

    public YTSearch() {
        this.httpInterfaceManager = HttpClientTools.createCookielessThreadLocalManager();
    }

    @Nullable
    public String getVideoId(String input) {
        return YTUri.getVideoCode(input);
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
        String id = YTUri.getVideoCode(uri);
        OMusic music = BotContainer.mongoDbAdapter.loadMusic(uri, author);
        if (music == null) {
            music = searchVideo(id, author);
            BotContainer.mongoDbAdapter.updateMusic("videoId", Util.musicKeys(), Util.oMusicArray(music));
        }
        return music;
    }

    public synchronized void resolvePlaylist(MusicPlayerManager musicPlayerManager, String uri, User author, Message message) {
//        System.out.println(uri);
        String plId = YTUri.getPlaylistCode(uri);
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

    public OMusic loadSearchResult(String args, User author, OMusic selected) {
        try (HttpInterface httpInterface = httpInterfaceManager.getInterface()) {
            URI url = new URIBuilder(BotContainer.getDotenv("YTRES"))
                    .addParameter("search_query", args)
                    .addParameter("hl", "en")
                    .addParameter("persist_hl", "1").build();

            try (CloseableHttpResponse response = httpInterface.execute(new HttpGet(url))) {
                HttpClientTools.assertSuccessWithContent(response, "search response");

                Document document = Jsoup.parse(response.getEntity().getContent(), StandardCharsets.UTF_8.name(), "");
                return extractSearchResult(document, author, selected);
            }
        } catch (Exception e) {
            System.out.println("YTSearch results timed out: YTSearch#loadSearchResult - " + selected.title);
        }
        return null;
    }

    private OMusic extractSearchResult(Document document, User author, OMusic selected) {
        Elements resultSelection = document.select("#page > #content #results");
        if (!resultSelection.isEmpty()) {
            for (Element results : resultSelection) {
                for (Element result : results.select(".yt-lockup-video")) {
                    if (!result.hasAttr("data-ad-impressions") && result.select(".standalone-ypc-badge-renderer-label").isEmpty()) {
                        return extractTrackFromResultEntry(result, author, selected);
                    }
                }
            }
        } else {
            try {
                return polymerExtrackTracks(document, author, selected);
            } catch (IOException e) {
                e.printStackTrace();
                throw new Error("Error polyextracting results");
            }
        }
        return null;
    }

    private OMusic extractTrackFromResultEntry(Element element, User requestedBy, OMusic selected) {
        OMusic oMusic = new OMusic();

        Element durationElement = element.select("[class^=video-time]").first();
        Element contentElement = element.select(".yt-lockup-content").first();
        String videoId = element.attr("data-context-item-id");

        if (durationElement == null || contentElement == null || videoId.isEmpty()) {
            return null;
        }

        long duration = DataFormatTools.durationTextToMillis(durationElement.text());

        String title = contentElement.select(".yt-lockup-title > a").text();
        String author = contentElement.select(".yt-lockup-byline > a").text();

        oMusic.thumbnail = selected.thumbnail == null ? "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg" : selected.thumbnail;
        oMusic.uri = BotContainer.getDotenv("YTVID") + videoId;
        oMusic.id = videoId;
        oMusic.author = author;
        oMusic.title = selected.title == null ? title : selected.title;
        oMusic.duration = duration;
        oMusic.requestedBy = requestedBy.getName();
        return oMusic;
    }

    private OMusic polymerExtrackTracks(Document document, User author, OMusic selected) throws IOException {
        Matcher matcher = polymerInitialDataRegex.matcher(document.outerHtml());
        if (!matcher.find()) return null;

        JsonBrowser jsonBrowser = JsonBrowser.parse(matcher.group(2));
        try {
            return extractPolymerData(jsonBrowser.get("contents")
                    .get("twoColumnSearchResultsRenderer")
                    .get("primaryContents")
                    .get("sectionListRenderer")
                    .get("contents")
                    .index(0)
                    .get("itemSectionRenderer")
                    .get("contents")
                    .values()
                    .get(0), author, selected);
//                .forEach(json -> {
//                    OMusic track = extractPolymerData(json, author);
//                    if (track != null) list.add(track);
//                });
        } catch (IndexOutOfBoundsException ignored) {
            return null;
        }
    }

    private OMusic extractPolymerData(JsonBrowser json, User requestedBy, OMusic selected) {
        JsonBrowser renderer = json.get("videoRenderer");

        // not a track
        if (renderer.isNull()) return null;

        String title = renderer.get("title").get("runs").index(0).get("text").text();
        String author = renderer.get("ownerText").get("runs").index(0).get("text").text();
        String lengthText = renderer.get("lengthText").get("simpleText").text();

        // null length = livestream
        if (lengthText == null) return null;

        long duration = DataFormatTools.durationTextToMillis(lengthText);
        String videoId = renderer.get("videoId").text();

        OMusic oMusic = new OMusic();
        oMusic.author = author;
        oMusic.requestedBy = requestedBy.getName();
        oMusic.id = videoId;
        oMusic.uri = BotContainer.getDotenv("YTVID") + videoId;
        oMusic.duration = duration;
        oMusic.thumbnail = selected.thumbnail == null ? "https://i.youtube.com/vi/" + videoId + "/hqdefault.jpg" : selected.thumbnail;
        oMusic.title = selected.title == null ? title : selected.title;
        return oMusic;
    }
}
