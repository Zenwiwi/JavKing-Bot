package JavKing.util.SP;

import JavKing.command.model.OMusic;
import JavKing.main.BotContainer;
import JavKing.util.LPUtil;
import com.google.common.collect.Lists;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.IPlaylistItem;
import com.wrapper.spotify.model_objects.specification.*;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SPPLLoader {

    private static final SpotifyApi spotifyApi = BotContainer.spUtil.getSpotifyApi();

    public static List<OMusic> getTrack(String id, User author, Message message) throws ParseException, SpotifyWebApiException, IOException {
        Track track = spotifyApi.getTrack(id).build().execute();
        LPUtil.updateLPURI(id, SPUri.parseFromId(id, "track"), track.getName(), "https://open.scdn.co/cdn/images/favicon32.a19b4f5b.png",
                message.getGuild().getId());
        OMusic oMusic = new OMusic();
        oMusic.thumbnail = "https://open.scdn.co/cdn/images/favicon32.a19b4f5b.png";
        oMusic.id = track.getId();
        oMusic.title = track.getName();
        oMusic.duration = track.getDurationMs();
        oMusic.uri = SPUri.parseToUri(track.getUri());
        oMusic.requestedBy = author.getName();
        oMusic.author = track.getArtists()[0].getName();
        return Lists.newArrayList(oMusic);
    }

    private static String getTrackArtist(String id) throws ParseException, SpotifyWebApiException, IOException {
        Track track = spotifyApi.getTrack(id).build().execute();
        return track.getArtists()[0].getName();
    }

    public static List<OMusic> getAlbumTrack(String id, User author, Message message) throws ParseException, SpotifyWebApiException, IOException {
        List<OMusic> oMusicList = Lists.newArrayList();
        int limit = 50;
        int offset = 0;
        String nextPage;
        Album album = spotifyApi.getAlbum(id).build().execute();
        String title = album.getName();
        String thumbnail = album.getImages()[0].getUrl();
        LPUtil.updateLPURI(id, SPUri.parseFromId(id, "album"), title, thumbnail, message.getGuild().getId());

        do {
            Paging<TrackSimplified> paging = spotifyApi.getAlbumsTracks(id).offset(offset).limit(limit).build().execute();
            TrackSimplified[] items = paging.getItems();
            Track[] albumTracks = spotifyApi.getSeveralTracks(Arrays.stream(items).filter(Objects::nonNull).map(TrackSimplified::getId).toArray(String[]::new))
                    .build().execute();
            for (Track track : albumTracks) {
                OMusic oMusic = new OMusic();
                oMusic.author = track.getArtists()[0].getName();
                oMusic.requestedBy = author.getName();
                oMusic.duration = track.getDurationMs();
                oMusic.id = track.getId();
                oMusic.uri = SPUri.parseToUri(track.getUri());
                oMusic.title = track.getName();
                oMusic.thumbnail = thumbnail;
                oMusicList.add(oMusic);
            }
            offset = offset + limit;
            nextPage = paging.getNext();
        } while (nextPage != null);
        return oMusicList;
    }

    public static List<OMusic> getPlaylistTracks(String id, User author, Message message) throws ParseException, SpotifyWebApiException, IOException {
//        List<Track> tracks = Lists.newArrayList();
        List<OMusic> oMusicList = Lists.newArrayList();
        int limit = 100;
        int offset = 0;
        String nextPage;
        Playlist playlist = spotifyApi.getPlaylist(id).build().execute();
        String title = playlist.getName();
        String thumbnail = playlist.getImages()[0].getUrl();
        LPUtil.updateLPURI(id, SPUri.parseFromId(id, "playlist"), title, thumbnail, message.getGuild().getId());
        do {
            Paging<PlaylistTrack> paging = spotifyApi.getPlaylistsItems(id).offset(offset).limit(limit).build().execute();
            PlaylistTrack[] items = paging.getItems();
            for (PlaylistTrack item : items) {
                IPlaylistItem track = item.getTrack();
                OMusic oMusic = new OMusic();
                oMusic.thumbnail = thumbnail;
                oMusic.author = getTrackArtist(track.getId());
                oMusic.duration = track.getDurationMs();
                oMusic.id = track.getId();
                oMusic.requestedBy = author.getName();
                oMusic.title = track.getName();
                oMusic.uri = SPUri.parseToUri(track.getUri());
                oMusicList.add(oMusic);
            }
//            tracks.addAll(Arrays.stream(items).map(PlaylistTrack::getTrack).filter(Objects::nonNull).collect(Collectors.toList()));
            offset = offset + limit;
            nextPage = paging.getNext();
        } while (nextPage != null);
        return oMusicList;
    }
}
