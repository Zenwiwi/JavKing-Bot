package JavKing.main;

import JavKing.db.MongoDbAdapter;
import JavKing.handler.CommandHandler;
import JavKing.util.SP.SPUtil;
import JavKing.util.YT.YTUtil;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.exceptions.RateLimitedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLongArray;

public class BotContainer {
    public static final Logger LOGGER = LogManager.getLogger(DiscordBot.class);
    public static MongoDbAdapter mongoDbAdapter = null;
    public static YTUtil ytUtil = null;
    public static SPUtil spUtil = null;
    public static Dotenv dotenv = Dotenv.configure().ignoreIfMalformed().ignoreIfMissing().load();
    private final DiscordBot[] shards;
    private final int numShards;
    private final AtomicInteger numGuilds;
    private final AtomicLongArray lastActions;
    private volatile boolean allShardsReady = false;

    public BotContainer(int numGuilds) throws LoginException, InterruptedException, RateLimitedException {
        this.numGuilds = new AtomicInteger(numGuilds);
        this.numShards = getRecommendedShards();
        mongoDbAdapter = new MongoDbAdapter();
        ytUtil = new YTUtil();
        spUtil = new SPUtil();
        shards = new DiscordBot[numShards];
        lastActions = new AtomicLongArray(numShards);
        initHandlers();
        initShards();
    }

    public static void main(String[] args) throws LoginException, InterruptedException, RateLimitedException {
        BotContainer botContainer = new BotContainer(2);
        System.out.println("      _             _  ___             \n" +
                "     | | __ ___   _| |/ (_)_ __   __ _ \n" +
                "  _  | |/ _` \\ \\ / / ' /| | '_ \\ / _` |\n" +
                " | |_| | (_| |\\ V /| . \\| | | | | (_| |\n" +
                "  \\___/ \\__,_| \\_/ |_|\\_\\_|_| |_|\\__, |\n" +
                "                                 |___/ ");
    }

    public static String getDotenv(String env) {
        return System.getenv(env) == null ? dotenv.get(env) : System.getenv(env);
    }

    public int getRecommendedShards() {
        try {
            HttpResponse<JsonNode> request = Unirest.get("https://discordapp.com/api/gateway/bot")
                    .header("Authorization", "Bot " + getDotenv("TOKEN"))
                    .header("Content-Type", "application/json")
                    .asJson();
            return Integer.parseInt(request.getBody().getObject().get("shards").toString());
        } catch (UnirestException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * {@link BotContainer#getShardFor(long)}
     */
    public DiscordBot getShardFor(String discordGuildId) {
        if (numShards == 1) {
            return shards[0];
        }
        return getShardFor(Long.parseLong(discordGuildId));
    }

    /**
     * Retrieves the right shard for the guildId
     *
     * @param discordGuildId the discord guild id
     * @return the instance responsible for the guild
     */
    public DiscordBot getShardFor(long discordGuildId) {
        if (numShards == 1) {
            return shards[0];
        }
        return shards[calcShardId(discordGuildId)];
    }

    /**
     * calculate to which shard the guild goes to
     *
     * @param discordGuildId discord guild id
     * @return shard number
     */
    public int calcShardId(long discordGuildId) {
        return (int) ((discordGuildId >> 22) % numShards);
    }

    private void initShards() throws LoginException, InterruptedException, RateLimitedException {
        for (int i = 0; i < shards.length; i++) {
            shards[i] = new DiscordBot(i, shards.length, this);
            CommandHandler.initialize();
            LOGGER.info("Starting shard #{} of {}", i, shards.length);
            Thread.sleep(5_000L);
        }
        for (DiscordBot shard : shards) {
            setLastAction(shard.getShardId(), System.currentTimeMillis());
        }
    }

    public void setLastAction(int shard, long timestamp) {
        lastActions.set(shard, timestamp);
    }

    public long getLastAction(int shard) {
        return lastActions.get(shard);
    }

    private void initHandlers() {

    }

    public boolean allShardsReady() {
        if (allShardsReady) {
            return allShardsReady;
        }
        for (DiscordBot shard : shards) {
            if (shard == null || !shard.isReady()) {
                return false;
            }
        }
        allShardsReady = true;
//        onAllShardsReady(); resets db on all guilds
        return true;
    }
}
