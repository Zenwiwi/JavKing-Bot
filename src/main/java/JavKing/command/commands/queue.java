package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.util.TimeUtil;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class queue extends AbstractCommand {
    @Override
    public String getDescription() {
        return "returns the server's queue";
    }

    @Override
    public String getCommand() {
        return "queue";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"queue [int]"};
    }

    @Override
    public String[] getAlias() {
        return new String[0];
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager musicPlayerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);

        List<OMusic> queue = musicPlayerManager.getLinkedQueue();
        if (queue.isEmpty()) {
            return "❌ **No songs in current queue!**";
        }

        int index = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        int items = 10;
        int start = (index - 1) * items;
        int end = start + items + 1;

        EmbedTemplate embedTemplate = new EmbedTemplate();
        embedTemplate.clearEmbed();

        OMusic nP = queue.get(0);

        String repeat = musicPlayerManager.isInRepeatMode() ? " | **REPEAT ENABLED 🔂**" : "";
        String nowPlaying = "[" + nP.title + "](" + nP.uri + ") | `" + TimeUtil.millisecondsToHHMMSS(nP.duration) +
                " Requested By: " + nP.requestedBy + "`\n\n";

        StringBuilder sb = new StringBuilder();
        String title = "[Queue for " + ((TextChannel) channel).getGuild().getName() + "](https://www.youtube.com)" + repeat +
            "\n\n__Now Playing__:\n" + nowPlaying;

        long totalDuration = 0;
        for (OMusic oMusic : queue) totalDuration += oMusic.duration;

        for (int i = start; i < end; i++) {
            if (i > 0) {
                try {
                    sb.append("`").append(i).append(".` [").append(queue.get(i).title).append("](").append(queue.get(i).uri)
                            .append(") | `").append(TimeUtil.millisecondsToHHMMSS(queue.get(i).duration)).append(" Requested By: ")
                            .append(queue.get(i).requestedBy).append("`\n\n");
                } catch (Exception e) {
                    break;
                }
                embedTemplate.setDescription(title + "\n__Queue__:\n" + sb + "\n**" + (queue.size() - 1) + " songs. Total Length: " + TimeUtil.millisecondsToHHMMSS(totalDuration) + "**");
                int tabs = (int) Math.ceil((double) queue.size() / items);
                embedTemplate.setFooter("Page " + index + " of " + tabs, author.getEffectiveAvatarUrl());
            } else {
                embedTemplate.setDescription(title);
            }
        }
        channel.sendMessage(embedTemplate.getEmbedBuilder()).queue();
        return null;
    }
}
