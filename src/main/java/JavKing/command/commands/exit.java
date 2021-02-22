package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.DiscordBot;
import JavKing.util.Util;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.Random;

public class exit extends AbstractCommand {
    @Override
    public String getDescription() {
        return "Re:Start";
    }

    @Override
    public String getCommand() {
        return "exit";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"sh", "shutdown", "re"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (!author.getId().equals("257214680823627777")) {
            String[] emotes = new String[]{"sovaWhat:787301408927645726", "sakuraAh:739075795263160452",
                    "nessacringe:757806405657952267"};
            inputMessage.addReaction(emotes[new Random().nextInt(emotes.length)]);
            return null;
        } else {
            Util.sendMessage("\uD83D\uDECC", inputMessage);
            System.exit(0);
        }
        return null;
    }
}
