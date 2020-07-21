package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class repeat extends AbstractCommand {
    @Override
    public String getDescription() {
        return "loops the current song";
    }

    @Override
    public String getCommand() {
        return "repeat";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"loop"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);

        if (!playerManager.authorInVoice(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**You must be in a voice channel to use this command!**");

        if (!playerManager.isInVoiceWith(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**I am currently not connected to your voice channel**," +
                    "Use the join command to summon me");

        playerManager.setRepeat(!playerManager.isInRepeatMode());
        return Templates.music.repeat_song.formatFull("**" + (playerManager.isInRepeatMode() ? "Enabled" : "Disabled") + "!**");
    }
}
