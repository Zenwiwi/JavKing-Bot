package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.templates.Templates;
import JavKing.util.DisUtil;
import JavKing.util.TimeUtil;
import JavKing.util.Util;
import net.dv8tion.jda.api.Permission;
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
        return new String[]{"[page/int]"};
    }

    @Override
    public String[] getAlias() {
        return new String[]{"q"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager musicPlayerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);

        String perms = DisUtil.discordBotPermsCHANNEL(channel, Permission.MESSAGE_EMBED_LINKS);
        if (perms != null) return perms;

        List<OMusic> queue = musicPlayerManager.getLinkedQueue();
        if (queue.isEmpty())
            return Templates.command.x_mark.formatFull(Util.surround("No songs in current queue!", "**"));

        int index = args.length == 0 ? 1 : Integer.parseInt(args[0]);
        int items = 10;
        int start = (index - 1) * items + 1;
        int end = start + items;

        OMusic nP = queue.get(0);

        String repeat = musicPlayerManager.isInRepeatMode() ? " | **REPEAT ENABLED 🔂**" : "";
        String nowPlaying = "[" + nP.title + "](" + nP.uri + ") | `" + TimeUtil.millisecondsToHHMMSS(nP.duration) +
                " Requested By: " + nP.requestedBy + "`\n\n";

        StringBuilder sb = new StringBuilder();
        String title = "[Queue for " + ((TextChannel) channel).getGuild().getName() + "](" + BotContainer.getDotenv("HEROKU_SITE") + ")" + repeat +
                "\n\n__Now Playing__:\n" + nowPlaying;

        EmbedTemplate embedTemplate = new EmbedTemplate();
        embedTemplate.clearEmbed();

        long totalDuration = 0;
        for (OMusic oMusic : queue) totalDuration += oMusic.duration;

        if (queue.size() > 1) {
            for (int i = start; i < end; i++) {
                try {
                    OMusic queueGet = queue.get(i);
                    sb.append("`").append(i).append(".` [").append(queueGet.title).append("](").append(queueGet.uri)
                            .append(") | `").append(TimeUtil.millisecondsToHHMMSS(queueGet.duration)).append(" Requested By: ")
                            .append(queue.get(i).requestedBy).append("`\n\n");
                } catch (IndexOutOfBoundsException e) {
                    break;
                }
            }
            embedTemplate.setDescription(title + "\n__Queue__:\n" + sb + "\n**" + (queue.size() - 1) + " songs. Total Length: " + TimeUtil.millisecondsToHHMMSS(totalDuration) + "**");
        } else {
            embedTemplate.setDescription(title);
        }
        int tabs = (int) Math.ceil((double) queue.size() / items);
        embedTemplate.setFooter("Page " + index + " of " + tabs, author.getEffectiveAvatarUrl());
        Util.sendMessage(embedTemplate, inputMessage);
        return null;
    }
}
