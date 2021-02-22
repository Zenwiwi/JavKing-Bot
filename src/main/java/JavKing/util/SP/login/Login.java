package JavKing.util.SP.login;

import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.exceptions.SpotifyWebApiException;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import net.dv8tion.jda.api.entities.User;
import org.apache.hc.core5.http.ParseException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class Login {

    private static final ScheduledExecutorService REFRESH_SERVICE = Executors.newScheduledThreadPool(3, new ThreadFactory() {
        private final AtomicLong threadId = new AtomicLong(1);

        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("login-refresh-thread-" + threadId.getAndIncrement());
            System.out.println("Refresh Service Thread: " + threadId);
//            thread.setUncaughtExceptionHandler();
            return thread;
        }
    });

    private final User user;
    private ScheduledFuture<?> nextRefresh;
    private String accessToken;
    private String refreshToken;
    private boolean expired = false;

    public Login(User user, String accessToken, String refreshToken, int expiresIn, SpotifyApi spotifyApi) {
        this.user = user;
        nextRefresh = REFRESH_SERVICE.schedule(new AutoRefreshTask(spotifyApi), expiresIn, TimeUnit.SECONDS);
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    public User getUser() {
        return user;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public boolean isExpired() {
        return expired;
    }

    public void expire() {
        expired = true;
    }

    public void cancel() {
        nextRefresh.cancel(false);
        expire();
    }


    private class AutoRefreshTask implements Runnable {
        private final SpotifyApi spotifyApi;

        private AutoRefreshTask(SpotifyApi spotifyApi) {
            this.spotifyApi = spotifyApi;
        }

        @Override
        public void run() {
            try {
                spotifyApi.setRefreshToken(getRefreshToken());
                AuthorizationCodeCredentials refreshCredentials = spotifyApi.authorizationCodeRefresh().build().execute();
                setAccessToken(refreshCredentials.getAccessToken());

                nextRefresh = REFRESH_SERVICE.schedule(new AutoRefreshTask(spotifyApi), refreshCredentials.getExpiresIn(), TimeUnit.SECONDS);
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss MM/dd");
                System.out.printf("Refreshed client credentials! (%s) %n", dateTimeFormatter.format(LocalDateTime.now()));
            } catch (IOException | SpotifyWebApiException | ParseException e) {
                System.out.println("Could not refresh client credentials!");
                expire();
            }
        }
    }
}
