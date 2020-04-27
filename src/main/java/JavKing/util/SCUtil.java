package JavKing.util;

import java.util.regex.Pattern;

public class SCUtil {
    public final static Pattern scURI = Pattern.compile("(snd\\.sc|soundcloud\\.com)", Pattern.CASE_INSENSITIVE);
    public final static Pattern scURI_ = Pattern.compile("\\?in=", Pattern.CASE_INSENSITIVE);
    public final static Pattern scPlURI = Pattern.compile("https?://soundcloud\\.com/\\S*/sets/\\S*", Pattern.CASE_INSENSITIVE);
    public final static Pattern scViURI = Pattern.compile("https?://(?:w\\.|www\\.|)(?:soundcloud\\.com/)(?:(?:player/\\?url=https%3A//api.soundcloud.com/tracks/)|)(((\\w|-)[^A-z]{7})|([A-Za-z0-9]+(?:[-_][A-Za-z0-9]+)*(?!/sets(?:/|$))(?:/[A-Za-z0-9]+(?:[-_][A-Za-z0-9]+)*){1,2}))", Pattern.CASE_INSENSITIVE);

//    public static boolean SCisPlaylistURI(String uri) {
//        return scPlURI.matcher(uri).find();
//    }

    public static boolean SCisURI(String uri) {
        return scURI.matcher(uri).find();
    }

    public static boolean SCisPlaylistURI(String uri) {
        return scPlURI.matcher(uri).find() && !scURI_.matcher(uri).find();
    }

    public static boolean SCisVideoURI(String uri) {
        return scViURI.matcher(uri).find();
    }
}
