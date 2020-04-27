package JavKing.main;

import JavKing.handler.CommandHandler;
import JavKing.handler.discord.JDAEventManager;
import JavKing.handler.discord.JDAEvents;
import net.dv8tion.jda.api.AccountType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.concurrent.atomic.AtomicReference;

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
            } catch (LoginException e) {
                try {
                    Thread.sleep(5_00L);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void restartJDA() throws LoginException {
        jda.set(new JDABuilder(AccountType.BOT)
                .setToken(BotContainer.getDotenv("TOKEN"))
                .setActivity(Activity.watching("Beni"))
                .setEventManager(new JDAEventManager(this))
                .setStatus(OnlineStatus.DO_NOT_DISTURB)
                .build());
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
        if (CommandHandler.isCommand(channel, message.getContentRaw().trim())) {
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
