package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.templates.Templates;
import JavKing.util.DisUtil;
import JavKing.util.SC.SCUri;
import JavKing.util.SP.SPUri;
import JavKing.util.YT.YTUri;
import com.mongodb.client.model.Filters;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.bson.Document;

public class lastplayed extends AbstractCommand {
    @Override
    public String getDescription() {
        return "returns latest playlist/track played in your server";
    }

    @Override
    public String getCommand() {
        return "lastplayed";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"lp"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        Document doc = (Document) BotContainer.mongoDbAdapter.getCollection("lastPlayed").find(Filters.eq("guildId", inputMessage.getGuild().getId())).first();

        if (doc != null && doc.getString("guildId").equals(inputMessage.getGuild().getId())) {
            String playlist, uri = doc.getString("uri");
            if (YTUri.isPlaylistCode(uri) || SCUri.SCisPlaylistURI(uri) || SPUri.SPisPlaylistURI(uri)) {
                playlist = "Yes";
            } else playlist = "No";
            EmbedBuilder embedTemplate = new EmbedTemplate()
                    .clearEmbed().setAuthor("Last Played in Queue for " + inputMessage.getGuild().getName(), null, author.getEffectiveAvatarUrl())
                    .setDescription(String.format("[%s](%s)", doc.getString("title"), doc.getString("uri")))
                    .setThumbnail(doc.getString("thumbnail"))
                    .addField("**How to play:**", "Type `" + DisUtil.getCommandPrefix(channel) + "play lastplayed` to play!", false)
                    .addField("**Playlist?**", playlist, false);
            channel.sendMessage(embedTemplate.build()).queue();
        } else {
            return Templates.command.o_mark.formatFull("**No `lastplayed` playlist/track(s) found for this server!**");
        }
        return null;
    }
}
