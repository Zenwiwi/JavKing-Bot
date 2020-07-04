package JavKing.util;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import com.google.common.base.Joiner;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;

import javax.annotation.Nullable;

public class SCSearch {
    public void resolveSCURI(String[] args, User author, Message message, MusicPlayerManager playerManager) {
        resolveSCURI(Joiner.on(" ").join(args), author, message, playerManager);
    }

    public void resolveSCURI(String uri, User author, Message message, MusicPlayerManager playerManager) {
        if (SCUtil.SCisPlaylistURI(uri)) playerManager.playlistAdd(uri, author, message);
        else resolveSCVideoParameters(uri, author, message, playerManager);
    }

    private synchronized void searchSC(Message message, String uri, User author, @Nullable String thumbnail, MusicPlayerManager playerManager) {
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

                LPUtil.updateLPURI(music.id, music.uri, music.title, music.thumbnail, message.getGuild().getId());
                playerManager.getLinkedQueue().offer(music);
                Util.sendMessage(playerManager.playSendYTSCMessage(music, author, BotContainer.getDotenv("SOUNDCLOUD"), true), message);
                playerManager.updateTotTimeSeconds(music.duration);

                playerManager.playCheckVoice(message, author);
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
    }

    public void resolveSCVideoParameters(String[] args, User author, Message message, MusicPlayerManager playerManager) {
        resolveSCVideoParameters(args[0], author, message, playerManager);
    }

    private synchronized void resolveSCVideoParameters(String id, User author, Message message, MusicPlayerManager playerManager) {
        OMusic music = BotContainer.mongoDbAdapter.loadMusic(id, author);
        if (music == null) {
            String thumbnailURI;
            while (true) {
                try {
                    MessageEmbed.Thumbnail thumbnail = message.getEmbeds().get(0).getThumbnail();
                    if (thumbnail != null) {
                        thumbnailURI = thumbnail.getUrl() == null ? thumbnail.getProxyUrl() : thumbnail.getUrl();
                        break;
                    }
                } catch (IndexOutOfBoundsException ignored) {
                }
            }
            searchSC(message, id, author, thumbnailURI, playerManager);
        } else {
            playerManager.addToQueue(music);
            playerManager.playCheckVoice(message, author);
        }
    }
}
