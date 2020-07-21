package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.entities.*;

public class leave extends AbstractCommand {

    @Override
    public String getDescription() {
        return "disconnects the bot from voice channel";
    }

    @Override
    public String getCommand() {
        return "leave";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"dismiss"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Guild guild = ((TextChannel) channel).getGuild();
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(guild, bot);

        if (!playerManager.authorInVoice(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**You must be in a voice channel to use this command!**");

        if (!playerManager.isInVoiceWith(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**I am currently not connected to your voice channel**, " +
                    "Use the join command to summon me");

        playerManager.leave();
        return Templates.command.check_mark.formatFull("**Successfully left voice channel!**");
    }
}
