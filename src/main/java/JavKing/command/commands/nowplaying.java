package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.templates.Templates;
import JavKing.util.TimeUtil;
import JavKing.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public class nowplaying extends AbstractCommand {
    private String button = "\uD83D\uDD18", trackLine = "▬";

    @Override
    public String getDescription() {
        return "Gets currently playing song";
    }

    @Override
    public String getCommand() {
        return "nowplaying";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"np"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager musicPlayerManager = MusicPlayerManager.getFor(inputMessage.getGuild(), bot);
        if (musicPlayerManager.getLinkedQueue().isEmpty()) {
            return Templates.command.o_mark.formatFull(Util.surround("Currently not playing a song", "**"));
        }
        OMusic music = musicPlayerManager.getLinkedQueue().get(0);
        long intervals = music.duration / 3000;
        long currentTime = musicPlayerManager.getTime();
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (int i = 1; i <= 30; i++) {
            if (count < 1) {
                if ((i * 100L) * intervals >= currentTime) {
                    builder.append(button);
                    count++;
                } else builder.append(trackLine);
            } else builder.append(trackLine);
        }
        String description = String.format("[%s](%s)\n\n`%s`\n\n`%s / %s`\n\n`Requested By:` %s", music.title, music.uri,
                builder.toString(), TimeUtil.millisecondsToHHMMSS(currentTime), TimeUtil.millisecondsToHHMMSS(music.duration),
                music.requestedBy);
        EmbedBuilder embedTemplate = new EmbedTemplate()
                .setAuthor("Now Playing ♪", BotContainer.getDotenv("HEROKU_SITE"), bot.getJDA().getSelfUser().getAvatarUrl())
                .setThumbnail(music.thumbnail)
                .setDescription(description);
        Util.sendMessage(embedTemplate, inputMessage);
        return null;
    }
}
