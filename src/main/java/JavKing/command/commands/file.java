package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.DiscordBot;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import JavKing.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class file extends AbstractCommand {

    @Override
    public String getDescription() {
        return "sends this projects files";
    }

    @Override
    public String getCommand() {
        return "file";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"<filename>"};
    }

    @Override
    public String[] getAlias() {
        return new String[]{"f"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (!author.getId().equals("257214680823627777") || args.length == 0)
            return ErrorTemplate.formatFull(bot, getCommand(), channel, author, inputMessage);
        File file = new File(args[0]);

        if (file.exists()) {
            try {
                Util.sendMessage(file, inputMessage);
                return null;
            } catch (Exception e) {
                e.printStackTrace();
                return Templates.command.boom.formatFull("**Error occurred while sending file**");
            }
        }
        return Templates.command.boom.formatFull("(404 err) file not found");
    }
}
