package JavKing.util.SP;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.util.DisUtil;
import JavKing.util.Util;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SPSearch {

    public static void resolveSPURI(String args, User author, Message message, MusicPlayerManager playerManager) throws Exception {
        SPUri uri = new SPUri(args);
        List<OMusic> oMusicList = uri.getType().loadTracks(uri, author, message);
        final int previousQueue = playerManager.getLinkedQueue().size();
        final long ET = playerManager.getTotTimeSeconds();

        if (oMusicList.size() > 1) {
            // multithreading to pull songs from yt quicker
            int containers = (int) Math.floor((double) oMusicList.size() / 5) + 1;
//            System.out.println("Queue: " + oMusicList);
            final ExecutorService executorService = Executors.newFixedThreadPool(containers);
            for (int i = 1; i <= containers; i++) {
                executorService.submit(new SPTask(author, message, playerManager, oMusicList, i, uri, ET));
            }
            executorService.shutdown();
        } else {
            SPTask.Task.execute(0, author, message, oMusicList, playerManager, true);
            startSpotify(previousQueue, ET, author, message, playerManager);
        }
    }

    public static void startSpotify(int previousQueue, long ET, User author, Message message, MusicPlayerManager playerManager) {
        try {
            while (playerManager.getLinkedQueue().isEmpty()) {}
            OMusic oMusic = playerManager.getLinkedQueue().get(previousQueue);
            Util.sendMessage(playerManager.playSendYTSCMessage(oMusic, author, BotContainer.getDotenv("SPOTIFY"),
                    previousQueue > 0, previousQueue, ET), message);
            if (!playerManager.isInVoiceWith(message.getGuild(), author) && !playerManager.getLinkedQueue().isEmpty()) {
                VoiceChannel vc = message.getGuild().getMember(author).getVoiceState().getChannel();

                String perms = DisUtil.discordBotPermsVOICE(vc, new Permission[]{Permission.VOICE_SPEAK, Permission.VOICE_CONNECT}) +
                        DisUtil.discordBotPermsCHANNEL(message.getChannel(), new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS});
                if (!perms.equals("nullnull")) {
                    System.out.println(perms);
                    return;
                }

                if (playerManager.isConnected()) {
                    playerManager.leave();
                }
                playerManager.connectTo(vc);
                BotContainer.mongoDbAdapter.update("guildSettings", (TextChannel) message.getChannel(), "channelId", message.getChannel().getId());
            }

            playerManager.setVolume(BotContainer.mongoDbAdapter.loadGuild((TextChannel) message.getChannel()).volume);
            playerManager.startPlaying();
        } catch (Exception e) {
            System.out.println("Error Location: SPSearch#startSpotify " + e.getMessage());
        }
    }
}

