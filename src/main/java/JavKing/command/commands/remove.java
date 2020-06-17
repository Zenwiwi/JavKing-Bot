package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Iterator;

public class remove extends AbstractCommand {
    @Override
    public String getDescription() {
        return "Removes song at given position";
    }

    @Override
    public String getCommand() {
        return "remove";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"<song position> [end position]"};
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);
        if (args.length >= 1) {
            int pos = Integer.parseInt(args[0]), end = 0, count = 0;
            try {
                end = Integer.parseInt(args[1]);
            } catch (Exception ignored) {}
            if (pos > 0 && pos < playerManager.getLinkedQueue().size() && end < playerManager.getLinkedQueue().size()) {
                if (end != 0) {
                    pos = Math.min(pos, end);
                    end = Math.max(pos, end);
                    for (Iterator<OMusic> iterator = playerManager.getLinkedQueue().iterator(); iterator.hasNext();) {
                        if (count >= pos && count < end) {
                            iterator.remove();
                        }
                        count++;
                    }
                } else
                    playerManager.getLinkedQueue().remove(pos);
            } else
                return Templates.command.x_mark.formatFull("**Song position and/or end position must be between `1` and `" + playerManager.getLinkedQueue().size() + "`**");
        } else
            return Templates.command.x_mark.formatFull("**Song position must be between `1` and `" + playerManager.getLinkedQueue().size() + "`**");
        return ErrorTemplate.formatFull(bot, getCommand(), channel, author, inputMessage);
    }
}
