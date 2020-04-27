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

public class move extends AbstractCommand {
    @Override
    public String getDescription() {
        return "moves song to desired position";
    }

    @Override
    public String getCommand() {
        return "move";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"move [song position] [new position]"};
    }

    @Override
    public String[] getAlias() {
        return new String[0];
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
        int size = playerManager.getLinkedQueue().size();
        if (args.length < 3 && args.length > 0) {
            if (Integer.parseInt(args[0]) < size && Integer.parseInt(args[0]) > 1) {
                int arg0 = Integer.parseInt(args[0]);
                if (Integer.parseInt(args[1]) < size && Integer.parseInt(args[1]) > 0) {
                    int arg1 = Integer.parseInt(args[1]);
                    LinkedList<OMusic> queue = playerManager.getLinkedQueue();
                    OMusic temp = queue.get(arg0);
                    queue.remove(arg0);
                    queue.add(arg1, temp);
                    return Templates.command.blue_check_mark.formatFull("**Moved `" + temp.title + "` to position `" + arg1 + "`!**");
                } else return Templates.command.x_mark.formatFull("**New song position must be between 1 and " + (size - 1) + "!**");
            } else return Templates.command.x_mark.formatFull("**Selected song position must be between 2 and " + (size - 1) + "!**");
        }
        return null;
    }
}
