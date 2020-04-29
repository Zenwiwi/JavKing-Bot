package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.CommandHandler;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class aliases extends AbstractCommand {
    @Override
    public String getDescription() {
        return "returns a list of aliases";
    }

    @Override
    public String getCommand() {
        return "aliases";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"al"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        StringBuilder sb = new StringBuilder();
        String[] toSort = new String[CommandHandler.getAliases().keySet().size()];
        int count = 0;
        for (Object cmd : CommandHandler.getAliases().keySet()) {
            toSort[count++] = cmd.toString();
        }
        Arrays.sort(toSort);
        List<String> added = new ArrayList<>();
        for (String cmdSorted : toSort) {
            AbstractCommand cmd = ((AbstractCommand) CommandHandler.getAliases().get(cmdSorted));
            if (!added.contains(cmd.getCommand())) {
                added.add(cmd.getCommand());
                sb.append("[»] ").append(cmd.getCommand()).append(" - ")
                        .append(String.join(", ", (cmd.getAlias())))
                        .append("\n");
            }
        }
        EmbedBuilder embedBuilder = new EmbedTemplate()
                .setAuthor(bot.getJDA().getSelfUser().getName() + " Aliases", null, bot.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setDescription("```ini\n" + sb + "```")
                .setFooter("JavKing©️ from 2020 - " + Calendar.getInstance().get(Calendar.YEAR), bot.getJDA().getSelfUser().getEffectiveAvatarUrl());
        author.openPrivateChannel().flatMap(pm -> pm.sendMessage(embedBuilder.build())).queue();
        return null;
    }
}
