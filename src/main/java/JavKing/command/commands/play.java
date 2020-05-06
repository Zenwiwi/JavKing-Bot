package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.CommandHandler;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import JavKing.util.*;
import com.google.common.base.Joiner;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class play extends AbstractCommand {
    private YTSearch ytSearch;

    public play() {
        super();
        ytSearch = new YTSearch();
    }

    public static void processTrack(OMusic music, MusicPlayerManager musicPlayerManager) {
        try {
            musicPlayerManager.addToQueue(music);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getDescription() {
        return "plays a YouTube or SoundCloud song";
    }

    @Override
    public String getCommand() {
        return "play";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"play <string>"};
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);
        TextChannel txt = (TextChannel) channel;
        Guild guild = txt.getGuild();
        if (!playerManager.authorInVoice(guild, author)) {
            return Templates.command.x_mark.formatFull("**You must be in a voice channel first!**");
        }

        Object toSend = null;

        if (args.length > 0) {
            channel.sendMessage("<:YT:605943277706936324> **Searching for **\uD83D\uDD0E `" + Joiner.on(" ").join(args) + "`").queue();

            if (YTUtil.isPlaylistCode(args[0])) {
                playerManager.playlistAdd(args[0], author, inputMessage);
            } else if (SCUtil.SCisURI(args[0])) {
                try {
                    playerManager.addSCToQueue(args, author, inputMessage, playerManager);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Templates.command.x_mark.formatFull("**Unable to load playlist/track(s)**");
                }
            } else if (Joiner.on(" ").join(args).equalsIgnoreCase("lastplayed")) {
                try {
                    toSend = playerManager.addLastPlayedToQueue(author, inputMessage);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Templates.command.x_mark.formatFull("**Unable to load playlist/track(s)**");
                }
            } else {
                try {
                    OMusic music = ytSearch.searchVideo(args, author);
                    if (music != null) {
                        processTrack(music, playerManager);
                        toSend = playerManager.playSendYTSCMessage(music, author, BotContainer.getDotenv("YOUTUBE"));
                        LPUtil.updateLPURI(music.id, music.uri, music.title, music.thumbnail, inputMessage.getGuild().getId());
                    } else {
                        return Templates.command.x_mark.formatFull("**No results found for:** `" + Joiner.on(" ").join(args) + "`");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return ErrorTemplate.formatFull("❌ **Unable to load audio track::**", e);
                }
            }
            if (!playerManager.isInVoiceWith(guild, author) && !playerManager.getLinkedQueue().isEmpty()) {
                VoiceChannel vc = inputMessage.getGuild().getMember(author).getVoiceState().getChannel();
                if (!playerManager.checkDiscordBotVCPerms(vc, Permission.VOICE_CONNECT)) {
                    return Templates.command.triumph.formatFull("**No permission to connect to `" + vc.getName() + "`**");
                }
                if (!playerManager.checkDiscordBotVCPerms(vc, Permission.VOICE_SPEAK)) {
                    return Templates.command.triumph.formatFull("**No permission to speak in `" + vc.getName() + "`**");

                }
                if (!(playerManager.checkDiscordBotPerms(channel, Permission.MESSAGE_WRITE) &&
                        playerManager.checkDiscordBotPerms(channel, Permission.MESSAGE_EMBED_LINKS))) {
                    return Templates.command.triumph.formatFull("**No permission to send message in `" + channel.getName() + "`**");
                }
                try {
                    if (playerManager.isConnected()) {
                        playerManager.leave();
                    }
                    playerManager.connectTo(vc);
                } catch (Exception e) {
                    e.printStackTrace();
                    return Templates.command.x_mark.formatFull("**Can't connect to voice channel, please try again!**");
                }
            }
            playerManager.startPlaying();
        } else
            return ((AbstractCommand) CommandHandler.getCommands().get("help")).execute(bot, new String[]{getCommand()}, channel, author, inputMessage);

        if (toSend != null) Util.sendMessage(toSend, inputMessage);
        return null;
    }
}
