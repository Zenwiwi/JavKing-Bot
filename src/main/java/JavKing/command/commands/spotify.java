package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.templates.ErrorTemplate;
import JavKing.templates.Templates;
import JavKing.util.SP.login.Login;
import JavKing.util.SP.login.LoginManager;
import JavKing.util.Util;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class spotify extends AbstractCommand {
    @Override
    public String getDescription() {
        return "Allows bot to access Spotify";
    }

    @Override
    public String getCommand() {
        return "spotify";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"sp"};
    }

    public CompletableFuture<Message> send(User user, MessageEmbed messageEmbed) {
        return executeMessageAction(user, messageChannel -> messageChannel.sendMessage(messageEmbed));
    }

//    protected CompletableFuture<Message> sendMessage(User user, Message message) {
//
//    }

    public CompletableFuture<Message> executeMessageAction(MessageChannel channel, Function<MessageChannel, MessageAction> function) {
        CompletableFuture<Message> futureMessage = new CompletableFuture<>();
        MessageAction messageAction = function.apply(channel);
        messageAction.queue(futureMessage::complete, futureMessage::completeExceptionally);
        return futureMessage;
    }

    public CompletableFuture<Message> executeMessageAction(User user, Function<MessageChannel, MessageAction> function) {
        return executeForUser(user, privateChannel -> executeMessageAction(privateChannel, function));
    }

    public CompletableFuture<Message> executeForUser(User user, Function<PrivateChannel, CompletableFuture<Message>> action) {
        CompletableFuture<Message> futureMessage = new CompletableFuture<>();
        user.openPrivateChannel().queue(channel -> {
            CompletableFuture<Message> future = action.apply(channel);
            future.whenComplete((msg, e) -> {
                if (e != null) {
                    futureMessage.completeExceptionally(e);
                } else {
                    futureMessage.complete(msg);
                }
            });
        }, futureMessage::completeExceptionally);

        return futureMessage;
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
//        final URI redirectUri = SpotifyHttpManager.makeUri(BotContainer.getDotenv("SPOTIFY_REDIRECT_KEY"));
//        final SpotifyApi spotifyApi = new SpotifyApi.Builder()
//                .setClientId(BotContainer.getDotenv("SPOTIFY_ID_KEY"))
//                .setClientSecret(BotContainer.getDotenv("SPOTIFY_SECRET_KEY"))
//                .setRedirectUri(redirectUri)
//                .build();
//
//        final AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
//                .scope("playlist-read-private")
//                .show_dialog(true)
//                .build();
//        final URI uri = authorizationCodeUriRequest.execute();
//
//
//        EmbedBuilder embedTemplate = new EmbedTemplate().clearEmbed()
//                .setAuthor(String.format("%s Spotify Authorization", bot.getJDA().getSelfUser().getName()),
//                        BotContainer.getDotenv("HEROKU_SITE"),  author.getEffectiveAvatarUrl())
//                .setDescription(String.format("[Link to User Auth](%s)", uri.toString()));
//        Util.sendMessage(embedTemplate, inputMessage);

        User user = inputMessage.getAuthor();
        AuthorizationCodeUriRequest authorizationCodeUriRequest = BotContainer.spUtil.getSpotifyApi().authorizationCodeUri()
                .scope("playlist-read-private")
                .show_dialog(true)
                .build();

        LoginManager loginManager = BotContainer.loginManager;
        CompletableFuture<Login> pendingLogin = new CompletableFuture<>();
        loginManager.expectLogin(user, pendingLogin);

        String loginUri = authorizationCodeUriRequest.execute().toString();
        EmbedBuilder embedLoginTemplate = new EmbedTemplate().clearEmbed()
                .setAuthor(String.format("%s Spotify Authorization", bot.getJDA().getSelfUser().getName()),
                        BotContainer.getDotenv("HEROKU_SITE"), author.getEffectiveAvatarUrl())
                .setDescription(String.format("Click [here](%s) to be redirected to Spotify", loginUri))
                .setColor(Color.decode(BotContainer.getDotenv("HEX")));

        CompletableFuture<Message> futurePrivateMessage = send(user, embedLoginTemplate.build());
        CompletableFuture<Login> futureLogin = pendingLogin.orTimeout(10, TimeUnit.MINUTES);
        futureLogin.whenComplete((login, throwable) -> {
            try {
                futurePrivateMessage.thenAccept(message -> message.delete().queue());
                if (login != null)
                    Util.sendMessage(Templates.command.check_mark.formatFull("You have successfully connected to Spotify", user), inputMessage);

                if (throwable != null) {
                    loginManager.removePendingLogin(user);

                    Util.sendMessage(Templates.command.x_mark.formatFull("Error logging in: " + throwable.getMessage()), inputMessage);
                }
            } catch (Exception e) {
                Util.sendMessage(ErrorTemplate.formatFull("Caught exception.", e), inputMessage);
            }
        });
        return null;
    }
}
