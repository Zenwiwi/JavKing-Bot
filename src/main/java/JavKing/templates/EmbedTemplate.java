package JavKing.templates;

import JavKing.main.BotContainer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import javax.annotation.Nullable;
import java.awt.*;

public class EmbedTemplate {
    private final EmbedBuilder embedBuilder;

    public EmbedTemplate() {
        embedBuilder = new EmbedBuilder().setColor(Color.decode(BotContainer.getDotenv("HEX")));
    }

    public MessageEmbed getEmbedBuilder() {
        return embedBuilder.build();
    }

    public EmbedBuilder clearEmbed() {
        embedBuilder.clear();
        return setColor(Color.decode(BotContainer.getDotenv("HEX")));
    }

    public EmbedBuilder addField(String name, String value, boolean inline) {
        return embedBuilder.addField(name, value, inline);
    }

    public EmbedBuilder addField(String name, String value) {
        return addField(name, value, false);
    }

    public EmbedBuilder setTitle(String title, String url) {
        return embedBuilder.setTitle(title, url);
    }

    public EmbedBuilder setTitle(String title) {
        return embedBuilder.setTitle(title);
    }

    public EmbedBuilder setDescription(@Nullable String description) {
        return embedBuilder.setDescription(description);
    }

    public EmbedBuilder setAuthor(@Nullable String name) {
        return setAuthor(name, null, null);
    }

    public EmbedBuilder setAuthor(@Nullable String name, @Nullable String iconURI) {
        return setAuthor(name, null, iconURI);
    }

    public EmbedBuilder setAuthor(@Nullable String name, @Nullable String uri, @Nullable String iconURI) {
        return embedBuilder.setAuthor(name, uri, iconURI);
    }

    public EmbedBuilder setThumbnail(@Nullable String uri) {
        return embedBuilder.setThumbnail(uri);
    }

    public EmbedBuilder setColor(int color) {
        return embedBuilder.setColor(color);
    }

    public EmbedBuilder setColor(@Nullable Color color) {
        return embedBuilder.setColor(color);
    }

    public EmbedBuilder setColor(String hexColor) {
        return setColor(Color.decode(hexColor));
    }

    public EmbedBuilder setFooter(@Nullable String text) {
        return setFooter(text, null);
    }

    public EmbedBuilder setFooter(@Nullable String text, @Nullable String iconURI) {
        return embedBuilder.setFooter(text, iconURI);
    }
}
