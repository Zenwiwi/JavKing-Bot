package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class skip extends AbstractCommand {
    @Override
    public String getDescription() {
        return "skips the current track/multiple tracks";
    }

    @Override
    public String getCommand() {
        return "skip";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"<int>"};
    }

    @Override
    public String[] getAlias() {
        return new String[]{"next"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);

        if (!playerManager.authorInVoice(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**You must be in a voice channel to use this command!**");

        if (!playerManager.isInVoiceWith(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**I am currently not connected to your voice channel**," +
                    "Use the join command to summon me");

        if (args.length > 0) {
            if (Integer.parseInt(args[0]) > 1) {
                return playerManager.skipTrack(args[0]);
            }
        }
        return playerManager.skipTrack(null);
    }
}
