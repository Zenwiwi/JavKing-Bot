package JavKing.handler.discord;

import JavKing.handler.GuildSettings;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.Templates;
import JavKing.util.Util;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class awaitJDAEvents extends ListenerAdapter {
    private final long channelId, authorId;

    public awaitJDAEvents(MessageChannel channel, User author) {
        this.channelId = channel.getIdLong();
        this.authorId = author.getIdLong();
    }

    @Override
    public synchronized void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        if (event.getAuthor().isBot() || event.getChannel().getIdLong() != channelId) return;
        MessageChannel channel = event.getChannel();
        String content = event.getMessage().getContentRaw();

        if (event.getAuthor().getIdLong() == authorId && (content.equalsIgnoreCase("y") || content.equalsIgnoreCase("yes"))) {
            BotContainer.mongoDbAdapter.resetGuild((TextChannel) channel);
            GuildSettings.get(channel).resetSettings();
            Util.sendMessage(Templates.command.blue_check_mark.formatFull("**JavKing settings have been restored to defaults**"), channel);
        } else Util.sendMessage(Templates.command.x_mark.formatFull("**Reset arborted**"), channel);

        event.getJDA().removeEventListener(this);
    }
}
