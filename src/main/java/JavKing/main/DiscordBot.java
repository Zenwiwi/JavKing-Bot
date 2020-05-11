package JavKing.main;

import JavKing.handler.CommandHandler;
import JavKing.handler.discord.JDAEventManager;
import JavKing.handler.discord.JDAEvents;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

import static net.dv8tion.jda.api.requests.GatewayIntent.*;

public class DiscordBot {
    public static final Logger LOGGER = LogManager.getLogger(DiscordBot.class);
    private final AtomicReference<JDA> jda;
    private final int totShards;
    private int shardId;
    private BotContainer botContainer;
    private volatile boolean isReady = false;

    public DiscordBot(int shardId, int numShards, BotContainer botContainer) {
        jda = new AtomicReference<>();
        this.shardId = shardId;
        this.totShards = numShards;
        setContainer(botContainer);
        while (true) {
            try {
                restartJDA();
                break;
            } catch (LoginException | InterruptedException e) {
                try {
                    Thread.sleep(5_00L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void restartJDA() throws LoginException, InterruptedException {
        GatewayIntent[] intents = new GatewayIntent[]{GUILD_MESSAGES, GUILD_VOICE_STATES, DIRECT_MESSAGES, GUILD_MEMBERS, GUILD_PRESENCES,
        GUILD_EMOJIS};
        jda.set(JDABuilder.create(BotContainer.getDotenv("TOKEN"), Arrays.asList(intents))
                .setActivity(Activity.streaming("Loli Hentai", BotContainer.getDotenv("TWITCH_URI")))
                .setEventManager(new JDAEventManager(this))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build()
                .awaitReady());
        jda.get().addEventListener(new JDAEvents(this));
    }

    public JDA getJDA() {
        return jda.get();
    }

    public void updateJDA(JDA jda) {
        this.jda.compareAndSet(this.jda.get(), jda);
    }

    public int getGuilds() {
        return getJDA().getGuilds().size();
    }

    public void handleMessage(TextChannel channel, User author, Message message) {
        if (author == null || author.isBot() || channel.getType() != ChannelType.TEXT) {
            return;
        }
        if (CommandHandler.isCommand(channel, message.getContentRaw().trim(), message.getContentRaw().split("\\s+"))) {
            CommandHandler.process(this, channel, author, message);
        }
    }

    public int getShardId() {
        return shardId;
    }

    public void setContainer(BotContainer botContainer) {
        this.botContainer = botContainer;
    }

    public boolean isReady() {
        return isReady;
    }

    public void markReady() {
        if (isReady) {
            return;
        }
        isReady = true;
    }
}
