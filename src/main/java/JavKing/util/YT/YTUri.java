package JavKing.util.YT;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTUri {
    public final static Pattern ytPlURI = Pattern.compile("list=(.*?)(?:&|$)", Pattern.CASE_INSENSITIVE);
    public final static Pattern ytViURI = Pattern.compile("v=([^#&\\n\\r]+)", Pattern.CASE_INSENSITIVE);

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
}
