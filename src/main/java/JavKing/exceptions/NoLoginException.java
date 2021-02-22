package JavKing.exceptions;

import net.dv8tion.jda.api.entities.User;

public class NoLoginException extends UserException {

    public NoLoginException(User user) {
        super(String.format("User %s is not logged in.", user.getName()));
    }

}
