package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.templates.Templates;
import JavKing.util.DisUtil;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.awt.*;
import java.util.List;
import java.util.regex.Pattern;

public class lyrics extends AbstractCommand {
    @Override
    public String getDescription() {
        return "searches for lyrics of song";
    }

    @Override
    public String getCommand() {
        return "lyrics";
    }

    @Override
    public String[] getUsage() {
        return new String[]{"<song>"};
    }

    @Override
    public String[] getAlias() {
        return new String[]{"l"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        String perms = DisUtil.discordBotPermsCHANNEL(channel, new Permission[]{Permission.MESSAGE_EMBED_LINKS, Permission.MESSAGE_WRITE});
        if (perms != null) return perms;

        if (args.length > 0 && !Pattern.compile("https?://").matcher(args[0]).find()) {
            channel.sendMessage(Templates.command.mag.formatFull("**Searching lyrics for** `" + String.join(" ", args) + "`")).queue();
            try {
                HttpResponse<String> response = Unirest.get("https://genius.p.rapidapi.com/search?q=" + String.join("%20", args))
                        .header("x-rapidapi-host", "genius.p.rapidapi.com")
                        .header("x-rapidapi-key", BotContainer.getDotenv("X_RAPIDAPI_KEY"))
                        .asString();
                JSONObject jsonObject = new JSONObject(response.getBody()).getJSONObject("response").getJSONArray("hits").getJSONObject(0).getJSONObject("result");
                String thumbnail = jsonObject.getString("song_art_image_url");

                WebClient client = new WebClient();
                client.getOptions().setJavaScriptEnabled(false);
                client.getOptions().setCssEnabled(false);
                client.getOptions().setUseInsecureSSL(false);

                HtmlPage page = client.getPage(jsonObject.getString("url"));

                List<HtmlElement> lyricsList = page.getByXPath("//div[@class='lyrics']");
                List<HtmlElement> aboutList = page.getByXPath("//div[@class='header_with_cover_art-primary_info']");
//                List<HtmlElement> thumbnailList = page.getByXPath("//div[@class='cover_art']");

                HtmlParagraph paragraph = (HtmlParagraph) lyricsList.get(0).getByXPath(".//p").get(0);
                HtmlHeading1 heading1 = (HtmlHeading1) aboutList.get(0).getByXPath(".//h1").get(0);
                String title = heading1.asText().trim();
                HtmlHeading2 heading2 = (HtmlHeading2) aboutList.get(0).getByXPath(".//h2").get(0);
                String artist = heading2.asText().trim();
//                HtmlImage thumbnail = (HtmlImage) thumbnailList.get(0).getByXPath(".//img").get(0);

                String lyrics = paragraph.asText().replaceAll("[\\[].*?[]]\\s*", "");
                client.close();

                EmbedBuilder embedBuilder = new EmbedTemplate()
                        .clearEmbed()
                        .setTitle(title + " - " + artist)
                        .setThumbnail(thumbnail)
                        .setFooter("Requested by " + author.getName(), author.getEffectiveAvatarUrl());
                if (lyrics.length() > 2048) {
                    for (int i = 0; i < lyrics.length(); i += 2048) {
                        if (i > 0) {
                            embedBuilder.clear().setColor(Color.decode(BotContainer.getDotenv("HEX")));
                        }
                        embedBuilder.setDescription(lyrics.substring(i, Math.min(i + 2048, lyrics.length())))
                                .setFooter("Requested by " + author.getName(), author.getEffectiveAvatarUrl());
                        channel.sendMessage(embedBuilder.build()).queue();
                    }
                } else channel.sendMessage(embedBuilder.setDescription(lyrics).build()).queue();
            } catch (Exception e) {
                e.printStackTrace();
                return Templates.command.x_mark.formatFull("**Error searching for lyrics!**");
            }
        } else return Templates.command.x_mark.formatFull("**Please provide a song to search for!**");
        return null;
    }
}
