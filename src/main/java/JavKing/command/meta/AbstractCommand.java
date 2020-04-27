package JavKing.command.meta;

import JavKing.main.DiscordBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

public abstract class AbstractCommand {
    public AbstractCommand() {

    }

    /**
     * A short discription of the method
     *
     * @return description
     */
    public abstract String getDescription();

    /**
     * What should be typed to trigger this command (Without prefix)
     *
     * @return command
     */
    public abstract String getCommand();

    /**
     * How to use the command?
     *
     * @return command usage
     */
    public abstract String[] getUsage();

    /**
     * aliases to call the command
     *
     * @return array of aliases
     */
    public abstract String[] getAlias();

    /**
     * @param bot          the shard where its executing on
     * @param args         arguments for the command
     * @param channel      channel where the command is executed
     * @param author       who invoked the command
     * @param inputMessage the incoming message object
     * @return the message to output or an empty string for nothing
     */
    public abstract String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage);
}
