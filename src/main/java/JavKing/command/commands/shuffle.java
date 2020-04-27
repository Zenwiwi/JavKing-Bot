package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.LinkedList;
import java.util.Random;

public class shuffle extends AbstractCommand {
    private static Random rng = new Random();

    @Override
    public String getDescription() {
        return "shuffles the queue";
    }

    @Override
    public String getCommand() {
        return "shuffle";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"mix"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(inputMessage.getGuild(), bot);
        if (!playerManager.isInVoiceWith(inputMessage.getGuild(), author)) {
            return Templates.command.x_mark.formatFull("**I am currently not connected to a voice channel**," +
                    "Use the join command to summon me");
        }
        if (!playerManager.authorInVoice(inputMessage.getGuild(), author)) {
            return Templates.command.x_mark.formatFull("**You must be in a voice channel to use this command!**");
        }
        if (playerManager.getLinkedQueue().size() <= 0) {
            return Templates.command.x_mark.formatFull("**No elements in queue to shuffle!**");
        }

        Object[] queue = playerManager.getLinkedQueue().toArray();
        int size = queue.length;
        while (size > 1) {
            size--;
            int k = rng.nextInt(size + 1);
            Object temp = queue[k];
            queue[k] = queue[size];
            queue[size] = temp;
        }
        LinkedList<OMusic> newQueue = new LinkedList<>();
        newQueue.add(playerManager.getLinkedQueue().get(0));
        for (Object object : queue) {
            newQueue.add((OMusic) object);
        }
        playerManager.replaceLinkedQueue(newQueue);
        return Templates.music.shuffle_queue.formatFull("**Shuffled queue!**");
    }
}
