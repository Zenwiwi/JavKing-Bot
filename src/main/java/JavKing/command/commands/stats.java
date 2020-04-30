package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;

import java.lang.management.ManagementFactory;

import com.sun.management.OperatingSystemMXBean;

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

    public static String getProcessCpuLoad() throws Exception {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
        return NumberFormat.getInstance().format(operatingSystemMXBean.getSystemCpuLoad());
    }

//    public static String getMemoryUsage() {
//        Runtime runtime = Runtime.getRuntime();
//
//        NumberFormat format = NumberFormat.getInstance();
//        long allocatedMemory = runtime.totalMemory();
//        return format.format(allocatedMemory / 1024);
//    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        try {
            EmbedBuilder embedBuilder = new EmbedTemplate()
                    .setAuthor(bot.getJDA().getSelfUser().getName() + " Bot { v" + BotContainer.getDotenv("VERSION") + " }",
                            null, bot.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .addField("Bot", "```ini\n[Developer]\nZenpai\n[Latency]\n" + (int) Math.floor(bot.getJDA().getGatewayPing()) +
                            "ms\n[Shard]\n" + bot.getShardId() + "```", false)
                    .addField("JVM Process", "```ini\n[CPU Usage]\n" + getProcessCpuLoad() + "%\n[Memory Usage]\n" +
                            NumberFormat.getInstance().format((double) Runtime.getRuntime().totalMemory() / Runtime.getRuntime().maxMemory()) +
                            "%```", false)
                    .setThumbnail(bot.getJDA().getSelfUser().getEffectiveAvatarUrl())
                    .setFooter("JavKing©️ from 2020 - " + Calendar.getInstance().get(Calendar.YEAR), bot.getJDA().getSelfUser().getEffectiveAvatarUrl());
            channel.sendMessage(embedBuilder.build()).queue();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
