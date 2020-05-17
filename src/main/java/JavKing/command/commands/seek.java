package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import JavKing.util.TimeUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

public class seek extends AbstractCommand {
    @Override
    public String getDescription() {
        return "Seeks to specified point in track";
    }

    @Override
    public String getCommand() {
        return "seek";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"<[hh]:[mm]:[ss]>", "<seconds>"};
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        if (args.length > 0) {
            MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);
            OMusic music = playerManager.getLinkedQueue().get(0);
            long maxSeekTo = music.duration;
            long seekTo = String.join(" ", args).contains(":") ? TimeUtil.HHMMSStoSeconds(String.join(" ", args)) : Long.parseLong(args[0]);

            if (seekTo > maxSeekTo)
                return Templates.command.x_mark.formatFull("**Seek to time must not exceed `" + maxSeekTo + "` seconds!**");

            playerManager.goToTime(seekTo * 1000);
            return Templates.command.blue_check_mark.formatFull("**Playing `" + music.title + "` from `" + TimeUtil.secondsToHHMMSS(seekTo) + "`** - Now!");
        } else return ErrorTemplate.formatFull(bot, getCommand(), channel, author, inputMessage);
    }
}
