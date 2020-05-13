package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.CommandHandler;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import JavKing.util.DisUtil;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class clean extends AbstractCommand {
    @Override
    public String getDescription() {
        return "clears bot's commands/messages";
    }

    @Override
    public String getCommand() {
        return "clean";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        List<Message> messageList = channel.getHistory().retrievePast(100).complete();
        List<Message> toDelete = new ArrayList<>();

        String perms = DisUtil.discordBotPermsCHANNEL(channel, new Permission[]{Permission.MESSAGE_MANAGE});
        if (perms != null) return perms;

        for (Message message : messageList) {
            String content = message.getContentDisplay().toLowerCase();
            if (content.startsWith(DisUtil.getCommandPrefix(channel))) {
                String cmd = DisUtil.filterPrefix(content, channel);
                cmd = cmd.contains(" ") ? cmd.split("\\s+")[0] : cmd;
                if (CommandHandler.getCommands().containsKey(cmd) || CommandHandler.getAliases().containsKey(cmd)) {
                    toDelete.add(message);
                }
            }
            if (message.getAuthor().getId().equals(bot.getJDA().getSelfUser().getId())) {
                toDelete.add(message);
            }
        }

        if (toDelete.size() > 1) {
            ((TextChannel) channel).deleteMessages(toDelete).queue();
            Message deleted = channel.sendMessage(Templates.command.check_mark.formatFull("**Cleared `" + (toDelete.size() - 1) + "` messages!**")).complete();
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    deleted.delete().queue();
                }
            }, 3000);
        } else {
            return Templates.command.x_mark.formatFull("**No messages to clear!**");
        }
        return null;
    }
}
