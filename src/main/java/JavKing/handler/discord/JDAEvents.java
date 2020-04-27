package JavKing.handler.discord;

import JavKing.main.DiscordBot;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class JDAEvents extends ListenerAdapter {
    private final DiscordBot discordBot;

    public JDAEvents(DiscordBot bot) {
        this.discordBot = bot;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        discordBot.handleMessage(event.getChannel(), event.getAuthor(), event.getMessage());
    }
}
