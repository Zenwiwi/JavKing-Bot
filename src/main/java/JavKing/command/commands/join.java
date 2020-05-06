package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

public class join extends AbstractCommand {
    @Override
    public String getDescription() {
        return "summons the bot to your voice channel";
    }

    @Override
    public String getCommand() {
        return "join";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"summon"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(inputMessage.getGuild(), bot);

        if (!playerManager.isInVoiceWith(inputMessage.getGuild(), author)) {
            VoiceChannel vc = inputMessage.getGuild().getMember(author).getVoiceState().getChannel();
            if (vc == null) {
                return Templates.command.x_mark.formatFull("**You must be in a voice channel first!**");
            }
            if (!playerManager.checkDiscordBotVCPerms(vc, Permission.VOICE_CONNECT)) {
                return Templates.command.triumph.formatFull("**No permission to connect to `" + vc.getName() + "`**");
            }
            if (!playerManager.checkDiscordBotVCPerms(vc, Permission.VOICE_SPEAK)) {
                return Templates.command.triumph.formatFull("**No permission to speak in `" + vc.getName() + "`**");
            }
            if (!playerManager.checkDiscordBotPerms(channel, Permission.MESSAGE_WRITE)) {
                return Templates.command.triumph.formatFull("**No permission to send message in `" + channel.getName() + "`**");
            }
            try {
                playerManager.connectTo(vc);
                return Templates.command.check_mark.formatFull("**Connected to `" + vc.getName() + "` and bound to `" + channel.getName() + "`!**");
            } catch (Exception e) {
                e.printStackTrace();
                return Templates.command.x_mark.formatFull("**Can't connect to voice channel, please try again!**");
            }
        }
        return null;
    }
}
