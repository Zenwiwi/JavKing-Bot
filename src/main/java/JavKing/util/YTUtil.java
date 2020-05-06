package JavKing.util;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTUtil {
    public final static Pattern ytPlURI = Pattern.compile("list=(.*?)(?:&|$)", Pattern.CASE_INSENSITIVE);
    public final static Pattern ytViURI = Pattern.compile("v=([^#&\\n\\r]+)", Pattern.CASE_INSENSITIVE);
    private final YouTube youtube;

    public YTUtil() {
        YouTube temp = null;

        try {
            temp = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
                    .setApplicationName("JavKing").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        youtube = temp;
    }

    public static boolean isURI(String uri) {
        return ytViURI.matcher(uri).find() || ytPlURI.matcher(uri).find();
    }

    public static boolean isVideoCode(String uri) {
        return ytViURI.matcher(uri).find();
    }

    public static String getVideoCode(String uri) {
        Matcher matcher = ytViURI.matcher(uri);
        if (matcher.find()) {
            if (matcher.groupCount() == 1) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public static boolean isPlaylistCode(String uri) {
        return ytPlURI.matcher(uri).find();
    }

    public static String getPlaylistCode(String uri) {
        Matcher matcher = ytPlURI.matcher(uri);
        if (matcher.find()) {
            if (matcher.groupCount() == 1) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public YouTube getYoutube() {
        return youtube;
    }
}
