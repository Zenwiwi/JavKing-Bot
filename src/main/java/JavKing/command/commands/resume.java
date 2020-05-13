package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class resume extends AbstractCommand {
    @Override
    public String getDescription() {
        return "resumes the queue if paused";
    }

    @Override
    public String getCommand() {
        return "resume";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"continue", "go"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(inputMessage.getGuild(), bot);
        if (!playerManager.isInVoiceWith(inputMessage.getGuild(), author)) {
            return Templates.command.x_mark.formatFull("**I am currently not connected to a voice channel**, " +
                    "Use the join command to summon me");
        }
        if (!playerManager.authorInVoice(inputMessage.getGuild(), author)) {
            return Templates.command.x_mark.formatFull("**You must be in a voice channel to use this command!**");
        }
        if (playerManager.isPaused()) {
            playerManager.togglePause();
            return Templates.music.resumed_queue.formatFull("**Resuming**");
        } else return Templates.command.x_mark.formatFull("**The player is not paused**");
    }
}
