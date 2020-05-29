package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import JavKing.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.LinkedList;

public class clear extends AbstractCommand {
    @Override
    public String getDescription() {
        return "Clears the queue";
    }

    @Override
    public String getCommand() {
        return "clear";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"cl"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);
        if (playerManager.getLinkedQueue().size() == 0)
            return Templates.command.x_mark.formatFull(Util.surround("No songs in current queue!", "**"));

        LinkedList<OMusic> newLinkedQueue = new LinkedList<>();
        OMusic nowPlaying = playerManager.getLinkedQueue().get(0);

        newLinkedQueue.add(nowPlaying);

        playerManager.replaceLinkedQueue(newLinkedQueue);
        playerManager.replaceTotTimeSeconds(nowPlaying.duration);

        return Templates.command.boom.formatFull(Util.surround("Queue cleared!", "**"));
    }
}
