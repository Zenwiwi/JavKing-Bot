package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;
import com.jagrosh.jlyrics.Lyrics;
import com.jagrosh.jlyrics.LyricsClient;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import jdk.nashorn.internal.parser.JSONParser;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.json.JSONObject;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
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
        return new String[]{"lyrics <song>"};
    }

    @Override
    public String[] getAlias() {
        return new String[]{"l"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
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
                String lyrics = paragraph.asText().replaceAll("\\n+", "");
                Matcher matcher = Pattern.compile("[\\[].*?[]]\\W *", Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(lyrics);
                while (matcher.find()) {
                    lyrics = matcher.replaceAll("");
                }
                EmbedBuilder embedBuilder = new EmbedTemplate()
                        .clearEmbed()
                        .setTitle(title + " - " + artist)
                        .setThumbnail(thumbnail)
                        .setFooter("Requested by " + author.getName(), author.getEffectiveAvatarUrl());
                int finalCount = 2048 - 3 - title.length() - artist.length();
                if (lyrics != null && lyrics.length() > finalCount) {
                    for (int i = 0; i < lyrics.length(); i += finalCount) {
                        if (i > 0) {
                            embedBuilder.clear().setColor(Color.decode(BotContainer.getDotenv("HEX")));
                        }
                        embedBuilder.setDescription(lyrics.substring(i, Math.min(i + finalCount, lyrics.length())))
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
