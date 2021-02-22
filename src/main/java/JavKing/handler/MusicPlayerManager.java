package JavKing.handler;

import JavKing.command.model.OGuild;
import JavKing.command.model.OMusic;
import JavKing.handler.audio.AudioPlayerSendHandler;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import JavKing.util.LPUtil;
import JavKing.util.SC.SCSearch;
import JavKing.util.SC.SCUri;
import JavKing.util.SP.SPSearch;
import JavKing.util.TimeUtil;
import JavKing.util.Util;
import JavKing.util.YT.YTSearch;
import JavKing.util.YT.YTUri;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.managers.AudioManager;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class MusicPlayerManager {
    private final static DefaultAudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final static Map<Long, MusicPlayerManager> musicManagers = new ConcurrentHashMap<>();
    private final TrackScheduler scheduler;
    private final long guildId;
    private final DiscordBot bot;
    private volatile boolean inRepeatMode = false;
    private volatile int currentlyPlaying = 0;
    private volatile int currentSongStartTimeInSeconds = 0;
    private volatile long pauseStart = 0;
    private long totTimeSeconds = 0;
    private volatile LinkedList<OMusic> queue;
    private List<OMusic> tempQueue;

    public final AudioPlayer player;

    public MusicPlayerManager(Guild guild, DiscordBot bot) {
        this.bot = bot;
        this.guildId = guild.getIdLong();

        player = playerManager.createPlayer();
        scheduler = new TrackScheduler(player);
        queue = new LinkedList<>();
        tempQueue = new ArrayList<>();
        // ytSearch = new YTSearch();
        AudioManager guildManager = guild.getAudioManager();
        guildManager.setSendingHandler(new AudioPlayerSendHandler(player));
        player.addListener(scheduler);
        musicManagers.put(guild.getIdLong(), this);
        init();
    }

    private static void init() {
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
        playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
        playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
    }

    public static MusicPlayerManager getFor(Guild guild) {
        return musicManagers.get(guild.getIdLong());
    }

    public static MusicPlayerManager getFor(Guild guild, DiscordBot bot) {
        if (musicManagers.containsKey(guild.getIdLong())) {
            return getFor(guild);
        } else {
            return new MusicPlayerManager(guild, bot);
        }
    }

    public AudioPlayer getAudioPlayer() {
        return player;
    }

    public DefaultAudioPlayerManager getDefaultAudioPlayerManager() {
        return playerManager;
    }

    public void goToTime(Long millis) {
        player.getPlayingTrack().setPosition(millis);
    }

    public long getTime() {
        return player.getPlayingTrack().getPosition();
    }

    // public long getGuild() {
    // return guildId;
    // }

    public synchronized Object playSendYTSCMessage(OMusic music, @Nullable User author, @Nullable String hex,
                                                   boolean bool) {
        if (queue.size() > 1 && bool) {
            EmbedBuilder embed = Templates.music.added_to_queue.clearEmbed()
                    .setColor(Color.decode(BotContainer.getDotenv("YOUTUBE")))
                    .setDescription(String.format("[%s](%s)", music.title, music.uri)).setThumbnail(music.thumbnail)
                    .addField("Channel", music.author, true)
                    .addField("Song Duration", TimeUtil.millisecondsToHHMMSS(music.duration), true)
                    .addField("ET Until Playing",
                            TimeUtil.millisecondsToHHMMSS(totTimeSeconds - queue.get(queue.size() - 1).duration), true)
                    .addField("Position In Queue", String.valueOf(queue.size() - 1), true)
                    .setColor(Color.decode(hex != null ? hex : BotContainer.getDotenv("HEX")));
            if (author != null)
                embed.setAuthor("Added to Queue!",
                        null, author.getEffectiveAvatarUrl());
            return embed;
        } else {
            return Templates.music.playing_now.formatFull("**Playing** :notes: `" + music.title + "` - Now!");
        }
    }

    public synchronized LinkedList<OMusic> getLinkedQueue() {
        return queue;
    }

    public synchronized void replaceLinkedQueue(LinkedList<OMusic> queue) {
        this.queue = queue;
    }

    public synchronized void replaceTotTimeSeconds(long duration) {
        totTimeSeconds = duration;
    }

    public synchronized void updateTotTimeSeconds(long duration) {
        totTimeSeconds += duration;
    }

    public synchronized boolean isPlaying() {
        return player.getPlayingTrack() != null;
    }

    public synchronized void startPlaying() {
        if (!isPlaying()) {
            if (player.isPaused()) {
                player.setPaused(false);
            } else {
                scheduler.skipTrack();
            }
        }
    }

    public synchronized boolean togglePause() {
        if (!player.isPaused()) {
            pauseStart = System.currentTimeMillis() / 1000L;
            player.setPaused(true);
        } else {
            currentSongStartTimeInSeconds += (System.currentTimeMillis() / 1000L) - pauseStart;
            player.setPaused(false);
        }
        return player.isPaused();
    }

    public synchronized boolean isPaused() {
        return player.isPaused();
    }

    public synchronized boolean setVolume(String volume) {
        player.setVolume(Integer.parseInt(volume));
        return true;
    }

    public synchronized java.util.List<Member> getUsersInVoiceChannel() {
        ArrayList<Member> userList = new ArrayList<>();
        VoiceChannel currentChannel = bot.getJDA().getGuildById(guildId).getAudioManager().getConnectedChannel();
        if (currentChannel != null) {
            List<Member> connectedUsers = currentChannel.getMembers();
            userList.addAll(connectedUsers.stream()
                    .filter(user -> !user.getUser().isBot() && !user.getVoiceState().isDeafened())
                    .collect(Collectors.toList()));
        }
        return userList;
    }

    public synchronized void connectTo(VoiceChannel channel) {
        if (channel != null && !isConnectedTo(channel)) {
            Guild guild = channel.getJDA().getGuildById(guildId);
            guild.getAudioManager().openAudioConnection(channel);
        }
    }

    public boolean isConnectedTo(VoiceChannel channel) {
        return channel != null
                && channel.equals(channel.getJDA().getGuildById(guildId).getAudioManager().getConnectedChannel());
    }

    public boolean isConnected() {
        Guild guildById = bot.getJDA().getGuildById(guildId);
        return guildById != null && guildById.getAudioManager().getConnectedChannel() != null;
    }

    public synchronized void playCheckVoice(Message message, User author) {
        String inVoiceCheck = inVoice(message, author);
        if (inVoiceCheck == null) {
            startPlaying();
        } else Util.sendMessage(inVoiceCheck, message);
    }

    public synchronized String inVoice(Message message, User author) {
        if (!isInVoiceWith(message.getGuild(), author) && !getLinkedQueue().isEmpty()) {
            VoiceChannel vc = Objects
                    .requireNonNull(
                            Objects.requireNonNull(message.getGuild().getMember(author)).getVoiceState())
                    .getChannel();
            if (vc == null) {
                return Templates.command.x_mark.formatFull("**You must be in a voice channel first!**");
            }
            try {
                if (isConnected()) leave();
                connectTo(vc);
            } catch (Exception e) {
                return Templates.command.x_mark.formatFull("**Can't connect to voice channel, please try again!**");
            }
        }
        return null;
    }

    public boolean isInVoiceWith(Guild guild, User author) {
        try {
            VoiceChannel channel = guild.getMember(author).getVoiceState().getChannel();
            if (channel == null)
                return false;
            for (Member user : channel.getMembers()) {
                if (user.getUser().getId().equals(guild.getJDA().getSelfUser().getId()))
                    return true;
            }
            return false;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean authorInVoice(Guild guild, User author) {
        try {
            VoiceChannel channel = guild.getMember(author).getVoiceState().getChannel();
            return channel != null;
        } catch (NullPointerException e) {
            return false;
        }
    }

    public boolean leave() {
        if (!queue.isEmpty()) {
            totTimeSeconds = 0;
            queue.clear();
        }
        if (isConnected())
            stopMusic();

        Guild guild = bot.getJDA().getGuildById(guildId);
        if (guild != null)
            guild.getAudioManager().closeAudioConnection();

        if (inRepeatMode)
            setRepeat(!inRepeatMode);
        return true;
    }

    public synchronized void stopMusic() {
        player.destroy();
    }

    public synchronized boolean isInRepeatMode() {
        return inRepeatMode;
    }

    public synchronized void setRepeat(boolean repeatMode) {
        inRepeatMode = repeatMode;
    }

    public synchronized void addToQueue(OMusic music) {
        if (queue.offer(music)) totTimeSeconds += music.duration;
    }

//    public void addToTempQueue(OMusic music) {
//        tempQueue.add(music);
//    }
//
//    public void sortTempQueue() {
//        int interval = 0, limit = 8;
//        LinkedList<OMusic> currentQueue = getLinkedQueue();
//        List<OMusic> currentTempQueue = tempQueue;
//        while (interval <= 8) {
//            for (OMusic music : tempQueue) {
//                if (music.index == interval) {
//                    currentQueue.offer(music);
//                    currentTempQueue.remove(music);
//                    totTimeSeconds += music.duration;
//                    limit--;
//                }
//                if (limit == 0) {
//                    interval++;
//                    limit = 8;
//                }
//            }
//        }
//        tempQueue = new ArrayList<>();
//        replaceLinkedQueue(currentQueue);
//    }

    public synchronized void addSCToQueue(String[] args, User author, Message message) {
        SCSearch.resolveSCURI(args, author, message, this);
    }

    public synchronized void addSPToQueue(String args, User author, Message message) throws Exception {
        SPSearch.resolveSPURI(args, author, message, this);
    }

    public synchronized Object addLastPlayedToQueue(User author, Message message) throws Exception {
        return LPUtil.resolveLPURI(author, message, this);
    }

    public synchronized void playlistAdd(String uri, User author, Message message) {
        if (YTUri.getPlaylistCode(uri) != null) {
            new YTSearch().resolvePlaylist(this, uri, author, message);
        } else {
            playerManager.loadItemOrdered(player, uri, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack audioTrack) {

                }

                @Override
                public synchronized void playlistLoaded(AudioPlaylist playlist) {
                    int count = 0;
                    try {
                        for (AudioTrack track : playlist.getTracks()) {
                            OMusic music = new OMusic();
                            music.title = track.getInfo().title;
                            music.id = track.getInfo().uri;
                            music.requestedBy = author.getAsTag();
                            music.uri = track.getInfo().uri;
                            music.author = track.getInfo().author;
                            music.duration = track.getDuration();
                            if (count == 0) {
                                music.thumbnail = Util.resolveThumbnail(track, message);
                                LPUtil.updateLPURI(SCUri.SCisURI(music.uri) ? uri : YTUri.getPlaylistCode(uri), uri,
                                        playlist.getName(), music.thumbnail, message.getGuild().getId());
                            }
                            addToQueue(music);
                            if (count++ == 0) {
                                if (track.getSourceManager().getSourceName().equalsIgnoreCase("youtube")) {
                                    Util.sendMessage(playSendYTSCMessage(
                                            queue.isEmpty() ? queue.get(0) : queue.get(queue.size() - 1), author,
                                            BotContainer.getDotenv("YOUTUBE"), false),
                                            BotContainer.mongoDbAdapter.loadGuild(message.getGuild()).channelId, bot);
                                }
                            }
                        }
                    } catch (Exception e) {
                        Util.sendMessage(Templates.command.x_mark.formatFull(Util.surround("Unable to load SoundCloud playlist: " + e.getMessage(), "**")), message);
                    }
                    playCheckVoice(message, author);
                }

                @Override
                public void noMatches() {
                    Util.sendMessage(Templates.command.x_mark.formatFull(Util.surround("No playlist found for: " + uri, "**")), message);
                }

                @Override
                public void loadFailed(FriendlyException e) {
                    Util.sendMessage(Templates.command.x_mark.formatFull(Util.surround("Unable to load playlist: " + e.getMessage(), "**")), message);
                }
            });
        }
    }

    public synchronized void trackEnded() {
        if (queue.isEmpty() /*|| getUsersInVoiceChannel().size() < 1*/) {
            player.destroy();
            return;
        }
        final OMusic trackToAdd = queue.get(0);
        if (trackToAdd == null) return;
        OGuild guild = BotContainer.mongoDbAdapter.loadGuild(String.valueOf(guildId));
//        System.out.println(trackToAdd.uri);
//        System.out.println(SPUri.SPisURI(trackToAdd.uri));
//        if (SPUri.SPisURI(trackToAdd.uri)) {
//            try {
//                BotContainer.spUtil.spotifyApi.startResumeUsersPlayback().context_uri(SPUri.parseFromUri(trackToAdd.uri))
//                        .build().execute();
//            } catch (ParseException | SpotifyWebApiException | IOException e) {
//                e.printStackTrace();
//                Util.sendMessage(Templates.command.x_mark.formatFull(Util.surround("No matches foundx!", "**")),
//                        guild.channelId, bot);
//            }
//        } else
        playerManager.loadItemOrdered(player, Util.idOrURI(trackToAdd), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                scheduler.queue(track);
                startPlaying();
                if (guild.announceSongs.equalsIgnoreCase("on")) {
                    Util.sendMessage(playSendYTSCMessage(trackToAdd, null, null, false),
                            guild.channelId, bot);
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {

            }

            @Override
            public void noMatches() {
                Util.sendMessage(Templates.command.x_mark.formatFull(Util.surround("No matches found!", "**")), guild.channelId, bot);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                trackEnded();
            }
        });
    }

    public void queuePoll() {
        queue.poll();
    }

    public synchronized String skipTrack(@Nullable String toIndex) {
        if (queue.size() == 0) return Templates.command.x_mark.formatFull("**Nothing playing in this server**");

        final boolean finalInRepeatMode = inRepeatMode;

        if (finalInRepeatMode) setRepeat(false);

        long duration = 0;
        for (int i = 0; i < (toIndex == null ? 1 : Integer.parseInt(toIndex)); i++) {
            duration += queue.get(0).duration;
            queuePoll();
        }
        totTimeSeconds -= duration;
        scheduler.skipTrack();
        if (finalInRepeatMode) setRepeat(true);
        return toIndex != null && Integer.parseInt(toIndex) > 1 ? Templates.music.skipped_song.formatFull("***Skipped to*** " + Util.surround(getLinkedQueue().get(0).title, "`"))
                : Templates.music.skipped_song.formatFull("***Skipped!***");
    }

    public class TrackScheduler extends AudioEventAdapter {
        private final AudioPlayer player;
        private final BlockingQueue<AudioTrack> queue;
//        private long interval;
//        private Timer timer;
//        private volatile String lastRequester = "";

        /**
         * @param player The audio player this scheduler uses
         */
        public TrackScheduler(AudioPlayer player) {
            this.player = player;
            this.queue = new LinkedBlockingQueue<>();
        }

        /**
         * Add the next track to queue or play right away if nothing is in the queue.
         *
         * @param track The track to play or add to queue.
         */
        public void queue(AudioTrack track) {
            // Calling startTrack with the noInterrupt set to true will start the track only
            // if nothing is currently playing. If
            // something is playing, it returns false and does nothing. In that case the
            // player was already playing so this
            // track goes to the queue instead.
            if (!player.startTrack(track, true)) {
                queue.offer(track);
            }
        }

        @Override
        public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
            // super.onTrackStuck(player, track, thresholdMs);
            skipTrack();
        }

        @Override
        public void onTrackStart(AudioPlayer player, AudioTrack track) {
//            stopTimer();
        }


        public void skipTrack() {
            trackEnded();
            if (isInRepeatMode() && player.getPlayingTrack() != null) {
                player.startTrack(player.getPlayingTrack().makeClone(), false);
                return;
            }
            player.stopTrack();
            AudioTrack poll = queue.poll();
            if (poll != null) {
                player.startTrack(poll, false);
            }
        }

        @Override
        public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            // Only start the next track if the end reason is suitable for it (FINISHED or
            // LOAD_FAILED)
            if (endReason.mayStartNext) {
                if (isInRepeatMode()) {
                    player.startTrack(track.makeClone(), false);
                    return;
                }
                queuePoll();
                totTimeSeconds -= track.getDuration();
                skipTrack();
            }
        }

        @Override
        public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
            skipTrack();
        }
    }
}
