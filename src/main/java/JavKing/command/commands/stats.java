package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.text.NumberFormat;
import java.util.Calendar;

public class stats extends AbstractCommand {
    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getCommand() {
        return "stats";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"bi", "botinfo"};
    }

    public static double getProcessCpuLoad() throws Exception {

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

        if (list.isEmpty()) return Double.NaN;

        Attribute att = (Attribute) list.get(0);
        Double value = (Double) att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0) return Double.NaN;
        // returns a percentage value with 1 decimal point precision
        return ((int) (value * 1000) / 10.0);
    }

    public static String getMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();

        NumberFormat format = NumberFormat.getInstance();
        long allocatedMemory = runtime.totalMemory();
        return format.format(allocatedMemory / 1024);
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        try {
            EmbedBuilder embedBuilder = new EmbedTemplate()
                    .setAuthor(bot.getJDA().getSelfUser().getName() + " Bot { v" + BotContainer.getDotenv("VERSION") + " }",
                            null, bot.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .addField("Bot", "```ini\n[Developer]\nZenpai\n[Latency]\n" + (int) Math.floor(bot.getJDA().getGatewayPing()) + "ms```", false)
                    .addField("JVM Process", "```ini\n[CPU Usage]\n" + getProcessCpuLoad() + "%\n[Memory Usage]\n" + getMemoryUsage() + "```", false)
                    .setThumbnail(bot.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setFooter("JavKing©️ from 2020 - " + Calendar.getInstance().get(Calendar.YEAR), bot.getJDA().getSelfUser().getEffectiveAvatarUrl());
            channel.sendMessage(embedBuilder.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
