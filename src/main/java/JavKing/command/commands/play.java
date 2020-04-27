package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import JavKing.util.LPUtil;
import JavKing.util.SCUtil;
import JavKing.util.YTSearch;
import JavKing.util.YTUtil;
import com.google.common.base.Joiner;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.Objects;

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

        channel.sendMessage("<:YT:605943277706936324> **Searching for **\uD83D\uDD0E `" + Joiner.on(" ").join(args) + "`").queue();
        Object toSend = null;

        if (args.length > 0) {
            if (YTUtil.isPlaylistCode(args[0])) {
//                List<YTSearch.SimpleResult> playlist = ytSearch.searchPlaylist(args[0], inputMessage.getGuild().getId());
//                int count = 0;
//                for (YTSearch.SimpleResult track : playlist) {
//                    OMusic music = ytSearch.resolveVideoParameters(BotConfig.YTVID + track.getCode(), author);
//                    processTrack(music, player);
//                    if (count++ == 0) {
//                        toSend = player.playSendYTSCMessage(music, author, BotConfig.YOUTUBE);
//                    }
//                }
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
                playerManager.startPlaying();
            }
        }

        if (toSend != null) {
            if (toSend instanceof EmbedBuilder) {
                channel.sendMessage(((EmbedBuilder) toSend).build()).queue();
            } else {
                channel.sendMessage(toSend.toString()).queue();
            }
        }
        return null;
    }
}
