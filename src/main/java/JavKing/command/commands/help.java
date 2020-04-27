package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.CommandHandler;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Arrays;
import java.util.Calendar;

public class help extends AbstractCommand {
    @Override
    public String getDescription() {
        return "displays the full list of commands";
    }

    @Override
    public String getCommand() {
        return "help";
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
        StringBuilder sb = new StringBuilder();
        String[] toSort = new String[CommandHandler.getCommands().keySet().size()];
        int count = 0;
        for (Object cmd : CommandHandler.getCommands().keySet()) {
            toSort[count++] = cmd.toString();
        }
        Arrays.sort(toSort);
        for (String cmdSorted : toSort) {
            sb.append("[»] ").append(cmdSorted).append("\n");
        }
        EmbedBuilder embedBuilder = new EmbedTemplate()
                .setAuthor(bot.getJDA().getSelfUser().getName() + " Help { JDA v" + JDAInfo.VERSION + " - JVM v" +
                        System.getProperty("java.version") + " }", null, author.getEffectiveAvatarUrl())
                .setTitle("\uD83D\uDD20 Commands")
                .setDescription("```ini\n" + sb + "```")
                .setThumbnail(bot.getJDA().getSelfUser().getEffectiveAvatarUrl())
                .setFooter("JavKing©️ from 2020 - " + Calendar.getInstance().get(Calendar.YEAR));
        inputMessage.getTextChannel().sendMessage(embedBuilder.build()).queue();
        return null;
    }
}
