package JavKing.exceptions;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class UserException extends RuntimeException {
    public UserException() {
        super();
    }

    public UserException(String message) {
        super(shortenMessage(message));
    }

    public UserException(String message, Throwable cause) {
        super(shortenMessage(message), cause);
    }

    public UserException(Throwable cause) {
        super(cause);
    }

    private static String shortenMessage(String message) {
        if (message.length() > 1000) {
            return message.substring(0, 1000) + "...";
        }

        return message;
    }

    public EmbedBuilder buildEmbed() {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.setTitle("Error");

        StringBuilder builder = new StringBuilder(getMessage());

//        if (this instanceof AdditionalInformationException) {
//            String additionalInformation = ((AdditionalInformationException) this).getAdditionalInformation();
//            builder.append(System.lineSeparator()).append(System.lineSeparator())
//                    .append("_").append(additionalInformation).append("_");
//        }

        embedBuilder.setDescription(builder.toString());
        return embedBuilder;
    }
}
