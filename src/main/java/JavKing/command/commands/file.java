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
        return new String[]{"file <filename>"};
    }

    @Override
    public String[] getAlias() {
        return new String[]{"f"};
    }

//    public static File findFiles(File[] files, String filename) {
//        for (File file : files) {
//            if (file.isDirectory()) {
//                findFiles(Objects.requireNonNull(file.listFiles()), filename);
//            } else {
//                if (file.getName().equalsIgnoreCase(filename)) {
//                    return file;
//                }
//            }
//        }
//        return null;
//    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (!author.getId().equals("257214680823627777") || args.length == 0)
            return ErrorTemplate.formatFull(bot, getCommand(), channel, author, inputMessage);
        File file = new File(args[0]);
//        if (!file.exists()) {
//            File[] files = new File(System.getProperty("user.dir")).listFiles();
//            file = findFiles(files, args[0]);
//        }
//        System.out.println(file);
        if (file != null && file.exists()) {
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
