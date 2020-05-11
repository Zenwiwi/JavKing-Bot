package JavKing.util;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import com.google.common.base.Joiner;
import com.mongodb.client.model.Filters;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

import javax.annotation.Nullable;

public class SCSearch {
    public void resolveSCURI(String[] args, User author, Message message, MusicPlayerManager playerManager) {
        resolveSCURI(Joiner.on(" ").join(args), author, message, playerManager);
    }

    public void resolveSCURI(String uri, User author, Message message, MusicPlayerManager playerManager) {
        if (SCUtil.SCisPlaylistURI(uri)) playerManager.playlistAdd(uri, author, message);
        else {
            OMusic music = resolveSCVideoParameters(uri, author, message, playerManager);
            LPUtil.updateLPURI(music.id, music.uri, music.title, music.thumbnail, message.getGuild().getId());
            playerManager.getLinkedQueue().offer(music);
            Util.sendMessage(playerManager.playSendYTSCMessage(music, author, BotContainer.getDotenv("SOUNDCLOUD")), message);
        }
        playerManager.startPlaying();
    }

    private OMusic searchSC(String uri, User author, @Nullable String thumbnail, MusicPlayerManager playerManager) {
        OMusic music = new OMusic();
        playerManager.getDefaultAudioPlayerManager().loadItemOrdered(playerManager.getAudioPlayer(), uri, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                music.duration = track.getDuration();
                music.author = track.getInfo().author;
                music.uri = uri;
                music.id = track.getIdentifier();
                music.requestedBy = author.getAsTag();
                music.title = track.getInfo().title;
                music.thumbnail = thumbnail;
                BotContainer.mongoDbAdapter.updateMusic("SCvideoId", Util.musicKeys(), Util.oMusicArray(music));
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException exception) {

            }
        });
        return music;
    }

    @Nullable
    public OMusic resolveSCVideoParameters(String[] args, User author, Message message, MusicPlayerManager playerManager) {
        return resolveSCVideoParameters(args[0], author, message, playerManager);
    }

    private OMusic resolveSCVideoParameters(String id, User author, Message message, MusicPlayerManager playerManager) {
        OMusic music = BotContainer.mongoDbAdapter.loadMusic(id, author);
        if (music != null) {
            String thumbnailURI = null;
            while (true) {
                MessageEmbed.Thumbnail thumbnail = message.getEmbeds().get(0).getThumbnail();
                if (thumbnail != null) {
                    thumbnailURI = thumbnail.getUrl() == null ? thumbnail.getProxyUrl() : thumbnail.getUrl();
                    break;
                }
            }
            music = searchSC(id, author, thumbnailURI, playerManager);
        }
        playerManager.updateTotTimeSeconds(music.duration);
        return music;
    }
}
