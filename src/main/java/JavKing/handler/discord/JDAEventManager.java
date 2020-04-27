package JavKing.handler.discord;

import JavKing.main.DiscordBot;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.IEventManager;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class JDAEventManager implements IEventManager {
    private final DiscordBot bot;
    private final ThreadPoolExecutor threadExecutor;
    private List<Object> listeners = new LinkedList<>();

    public JDAEventManager(DiscordBot bot) {
        ThreadFactoryBuilder threadBuilder = new ThreadFactoryBuilder();
        threadBuilder.setNameFormat("1");
        this.threadExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool(threadBuilder.build());
        this.bot = bot;
    }

    @Override
    public void register(Object listener) {
        if (!(listener instanceof EventListener)) {
            throw new IllegalArgumentException("Listener must implement EventListener");
        }
        listeners.add(listener);
    }

    @Override
    public void unregister(Object listener) {
        listeners.remove(listener);
    }

    @Override
    public void handle(@Nonnull GenericEvent event) {
        threadExecutor.submit(() -> {
            bot.updateJDA(event.getJDA());
            if (!(event.getJDA().getStatus() == JDA.Status.CONNECTED)) {
                return;
            }
            List<Object> listenerCopy = new LinkedList<>(listeners);
            for (Object listener : listenerCopy) {
                try {
                    ((EventListener) listener).onEvent(event);
                } catch (PermissionException throwable) {
                    System.out.println("FATAL :: Unchecked permission error!");
                    System.out.println(throwable);
                } catch (Throwable throwable) {
                    System.out.println(throwable);
                }
            }
        });
    }
//    @Override
//    public void handle(Event event) {
//
//    }

    @Override
    public List<Object> getRegisteredListeners() {
        return this.listeners;
    }
}
