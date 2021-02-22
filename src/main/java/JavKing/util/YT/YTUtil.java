package JavKing.util.YT;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;

public class YTUtil {

    private final YouTube youtube;

    public YTUtil() {
        YouTube temp = null;

        try {
            temp = new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(), null)
                    .setApplicationName("JavKing").build();
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.youtube = temp;
    }

    public YouTube getYoutube() {
        return youtube;
    }
}
