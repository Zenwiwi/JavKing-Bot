package JavKing.util.SP;

import JavKing.command.model.OMusic;
import com.wrapper.spotify.exceptions.detailed.NotFoundException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;

import java.util.List;
import java.util.regex.Pattern;

public class SPUri {
    public static final Pattern spURI = Pattern.compile("^(spotify:|https://[a-z]+\\.spotify\\.com/)");
    public static final Pattern spTrackURI = Pattern.compile("spotify\\.com/track/[a-zA-Z0-9]{22}");
    public static final Pattern spAlbumURI = Pattern.compile("spotify\\.com/album/[a-zA-Z0-9]{22}");
    public static final Pattern spPlaylistURI = Pattern.compile("spotify\\.com/playlist/[a-zA-Z0-9]{22}");
    private final String id, uri;
    private Type type = null;

    public SPUri(String uri) {
        for (Type type : Type.values()) {
            if (type.getPattern().matcher(uri).find()) {
                this.type = type;
                break;
            }
        }
        if (this.type == null) {
            throw new Error("Unsupported URI! Supported: spotify:track, spotify:album, spotify:playlist");
        }
        this.uri = uri;
        id = parseId(uri);
    }

    public String getUri() {
        return uri;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public String parseId(String uri) {
        String[] split = uri.split("/");
        return split[split.length - 1].substring(0, 22);
    }

    public static String parseToUri(String uri) {
        String[] split = uri.split(":");
        return "https://open.spotify.com/" + split[split.length - 2] + "/" + split[split.length - 1];
    }

    public static String parseFromUri(String uri) {
        String[] split = uri.split("/");
        return "spotify:" + split[split.length - 2] + ":" + split[split.length - 1];
    }

    public static String parseFromId(String id, String type) {
        return "https://open.spotify.com/" + type + "/" + id;
    }

    public static boolean SPisURI(String uri) {
        return spURI.matcher(uri).find();
    }

    public static boolean SPisPlaylistURI(String uri) {
        return spAlbumURI.matcher(uri).find() ||
                spPlaylistURI.matcher(uri).find();
    }

    public enum Type {
        TRACK(spTrackURI) {
            @Override
            public List<OMusic> loadTracks(SPUri uri, User author, Message message) throws Exception {
                List<OMusic> oMusicList;
                try {
                    oMusicList = SPPLLoader.getTrack(uri.getId(), author, message);
                } catch (NotFoundException e) {
                    throw new Error("No results found for id " + uri.getId());
                }
                return oMusicList;
            }
        },
        ALBUM(spAlbumURI) {
            @Override
            public List<OMusic> loadTracks(SPUri uri, User author, Message message) throws Exception {
                List<OMusic> oMusicList;
                try {
                    oMusicList = SPPLLoader.getAlbumTrack(uri.getId(), author, message);
                } catch (NotFoundException e) {
                    throw new Error("No results found for id " + uri.getId());
                }
                return oMusicList;
            }
        },
        PLAYLIST(spPlaylistURI) {
            @Override
            public List<OMusic> loadTracks(SPUri uri, User author, Message message) throws Exception {
                List<OMusic> oMusicList;
                try {
                    oMusicList = SPPLLoader.getPlaylistTracks(uri.getId(), author, message);
                } catch (NotFoundException e) {
                    throw new Error("No results found for id " + uri.getId());
                }
                return oMusicList;
            }
        };

        private final Pattern pattern;

        Type(Pattern pattern) {
            this.pattern = pattern;
        }

        public Pattern getPattern() {
            return pattern;
        }

        public abstract List<OMusic> loadTracks(SPUri uri, User author, Message message) throws Exception;
    }
}
