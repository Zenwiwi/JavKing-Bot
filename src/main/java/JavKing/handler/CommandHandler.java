package JavKing.handler;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.DiscordBot;
import JavKing.util.DisUtil;
import net.dv8tion.jda.api.entities.*;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Set;

public class CommandHandler {
    private static HashMap<String, AbstractCommand> commands = new HashMap<>();
    private static HashMap<String, AbstractCommand> commandsAliases = new HashMap<>();

    public static void initialize() {
        loadCommands();
        loadAliases();
    }

    public static HashMap getCommands() {
        return commands;
    }

    public static HashMap getAliases() {
        return commandsAliases;
    }

    /**
     * checks if the the message in channel is a command
     *
     * @param channel the channel the message came from
     * @param msg     the message
     * @param input   the args
     *                // @param mentionMe      the user mention string
     *                // @param mentionMeAlias the nickname
     * @return whether or not the message is a command
     */
    public static boolean isCommand(TextChannel channel, String msg, String[] input /*, String mentionMe*/) {
        String prefix = DisUtil.getCommandPrefix(channel);
//        if (input.length > 0) {
//            input[0] = input[0].startsWith(prefix) ? input[0].substring(prefix.length()).toLowerCase() : input[0];
//            return msg.startsWith(prefix) && (commands.containsKey(input[0]) || commandsAliases.containsKey(input[0]));
//        }
        return msg.startsWith(prefix)/*|| msg.startsWith(mentionMe)*/;
    }

    /**
     * directs the command to the right class
     *
     * @param bot             The bot instance
     * @param channel         which channel
     * @param author          author
     * @param incomingMessage message
     */
    public static void process(DiscordBot bot, MessageChannel channel, User author, Message incomingMessage) {
        String inputMessage = incomingMessage.getContentRaw();
        if (channel instanceof TextChannel) {
            if (!((TextChannel) channel).canTalk()) {
                return;
            }
        }

        String[] input = inputMessage.split("\\s+", 2);
        String[] args = input.length == 2 ? input[1].split("\\s+") : new String[0];
        input[0] = DisUtil.filterPrefix(input[0], channel).toLowerCase();
        AbstractCommand command = commands.containsKey(input[0]) ? commands.get(input[0]) : commandsAliases.get(input[0]);

//            if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
//                commandOutput = commands.get("help").execute(bot, new String[]{input[0]}, channel, author, incomingMessage);
        String output = null;
        try {
            output = command.execute(bot, args, channel, author, incomingMessage);
        } catch (IllegalArgumentException ignored) {

        }
        if (output != null) {
            channel.sendMessage(output).queue();
        }
//            }
    }

    private static void loadCommands() {
        Reflections reflections = new Reflections("JavKing.command.commands");
        Set<Class<? extends AbstractCommand>> classes = reflections.getSubTypesOf(AbstractCommand.class);
        for (Class<? extends AbstractCommand> s : classes) {
            try {
                if (Modifier.isAbstract(s.getModifiers())) {
                    continue;
                }
                AbstractCommand c = s.getConstructor().newInstance();
                if (!commands.containsKey(c.getCommand())) commands.put(c.getCommand(), c);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    private static void loadAliases() {
        for (AbstractCommand command : commands.values()) {
            for (String alias : command.getAlias()) {
                if (!commandsAliases.containsKey(alias)) {
                    commandsAliases.put(alias, command);
                }
            }
        }
    }
}
