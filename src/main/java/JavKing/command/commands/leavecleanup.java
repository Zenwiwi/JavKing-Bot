package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.command.model.OMusic;
import JavKing.handler.MusicPlayerManager;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import net.dv8tion.jda.api.entities.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class leavecleanup extends AbstractCommand {
    @Override
    public String getDescription() {
        return "removes songs requested by absent users from queue";
    }

    @Override
    public String getCommand() {
        return "leavecleanup";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"leavec", "lc"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        MusicPlayerManager playerManager = MusicPlayerManager.getFor(((TextChannel) channel).getGuild(), bot);

        if (!playerManager.authorInVoice(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**You must be in a voice channel to use this command!**");

        if (!playerManager.isInVoiceWith(inputMessage.getGuild(), author))
            return Templates.command.x_mark.formatFull("**I am currently not connected to your voice channel**," +
                    "Use the join command to summon me");

        try {
            List<Member> memberList = playerManager.getUsersInVoiceChannel();
            LinkedList<OMusic> queue = playerManager.getLinkedQueue(), newQueue = new LinkedList<>();
            List<String> stringList = new ArrayList<>();
            int counter = 0;

            for (Member member : memberList) {
                stringList.add(member.getUser().getAsTag());
            }
            newQueue.add(queue.get(0));
            for (int i = 1; i < queue.size(); i++) {
                if (stringList.contains(queue.get(i).requestedBy)) {
                    newQueue.add(queue.get(i));
                } else counter++;
            }
            playerManager.replaceLinkedQueue(newQueue);
            return Templates.command.blue_check_mark.formatFull("**Removed `" + counter + "` songs**");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
