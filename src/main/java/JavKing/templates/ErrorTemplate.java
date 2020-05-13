package JavKing.templates;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.CommandHandler;
import JavKing.main.DiscordBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class ErrorTemplate {
    public static String formatFull(String vars, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(vars).append("```java\n");
        for (StackTraceElement element : e.getStackTrace()) sb.append(element).append("\n");
        sb.append("\n```");
        return sb.toString();
    }

    public static String formatFull(DiscordBot bot, String cmd, MessageChannel channel, User author, Message inputMessage) {
        return ((AbstractCommand) CommandHandler.getCommands().get("help")).execute(bot, new String[]{cmd}, channel, author, inputMessage);
    }
}
