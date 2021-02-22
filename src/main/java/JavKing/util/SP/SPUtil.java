package JavKing.util.SP;

import JavKing.main.BotContainer;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.ClientCredentials;
import com.wrapper.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class SPUtil {
    //    private static SPLogin spLogin;
    private final SpotifyApi spotifyApi;
    private final URI redirectUri = SpotifyHttpManager.makeUri("https://javking.com/spotify-redirect");

    public SPUtil() {
//        spLogin = new SPLogin(new SpotifyApi.Builder());
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(BotContainer.getDotenv("SPOTIFY_ID_KEY"))
                .setClientSecret(BotContainer.getDotenv("SPOTIFY_SECRET_KEY"))
                .setRedirectUri(redirectUri)
                .build();
        ClientCredentialsRequest credentialsRequest = this.spotifyApi.clientCredentials().build();
        try {
            final ClientCredentials clientCredentials = credentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            int expiresIn = clientCredentials.getExpiresIn();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd");
            System.out.println("Spotify access token initialized: " + dateTimeFormatter.format(LocalDateTime.now()));
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            e.printStackTrace();
        }
    }

    public SpotifyApi getSpotifyApi() {
//        return spLogin.spotifyApi;
        return spotifyApi;
    }
}
