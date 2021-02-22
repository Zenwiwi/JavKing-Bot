package JavKing.util.SP;

import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.templates.Templates;
import JavKing.util.DisUtil;
import JavKing.util.Util;
import JavKing.util.YT.YTSearch;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;

import java.util.List;

public class SPTask implements Runnable {
    private final User author;
    private final MusicPlayerManager playerManager;
    private final List<OMusic> oMusicList;
    private final int index;
    private final Message message;
    private final SPUri uri;

    SPTask(User author, Message message, MusicPlayerManager playerManager, List<OMusic> oMusicList, int index,
           SPUri uri) {
        this.author = author;
        this.playerManager = playerManager;
        this.oMusicList = oMusicList;
        this.index = index;
        this.message = message;
        this.uri = uri;
    }

    @Override
    public void run() {
        int interval = (int) Math.round((double) oMusicList.size() / 8);
        int forkStart = interval * index - interval;
        int forkEnd = interval * index;
        int count = playerManager.getLinkedQueue().size();

        for (int i = forkStart; i < forkEnd; i++) {
            try {
                OMusic selected = oMusicList.get(i);
                String split = selected.title + " " + (selected.author.startsWith("[Artist]") ? "" : selected.author);
                String join = String.join("+", split.split("\\s+"));
                OMusic oMusic = new YTSearch().loadSearchResult(join, author, selected);
                if (oMusic != null) {
                    playerManager.addToQueue(oMusic);
//                oMusic.index = index;
//                playerManager.addToTempQueue(oMusic);
                }
            } catch (IndexOutOfBoundsException ignored) {}
        }

        if (forkStart == 0) {
            OMusic oMusic = playerManager.getLinkedQueue().get(0);
            Util.sendMessage(playerManager.playSendYTSCMessage(oMusic, author, BotContainer.getDotenv("SPOTIFY"),
                    count > 0), message);

            if (!playerManager.isInVoiceWith(message.getGuild(), author) && !playerManager.getLinkedQueue().isEmpty()) {
                VoiceChannel vc = message.getGuild().getMember(author).getVoiceState().getChannel();

                String perms = DisUtil.discordBotPermsVOICE(vc, new Permission[]{Permission.VOICE_SPEAK, Permission.VOICE_CONNECT}) +
                        DisUtil.discordBotPermsCHANNEL(message.getChannel(), new Permission[]{Permission.MESSAGE_WRITE, Permission.MESSAGE_EMBED_LINKS});
                if (!perms.equals("nullnull")) {
                    Util.sendMessage(perms, message);
                    return;
                }

                try {
                    if (playerManager.isConnected()) {
                        playerManager.leave();
                    }
                    playerManager.connectTo(vc);
                    BotContainer.mongoDbAdapter.update("guildSettings", (TextChannel) message.getChannel(), "channelId", message.getChannel().getId());
                } catch (Exception e) {
                    e.printStackTrace();
                    Util.sendMessage(Templates.command.x_mark.formatFull("**Can't connect to voice channel, please try again!**"), message);
                    return;
                }
            }
            playerManager.setVolume(BotContainer.mongoDbAdapter.loadGuild((TextChannel) message.getChannel()).volume);
//        playerManager.sortTempQueue();
            playerManager.startPlaying();
        }
    }
}
