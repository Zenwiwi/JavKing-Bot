package JavKing.command.commands;

import JavKing.command.meta.AbstractCommand;
import JavKing.guildSettings.GSetting;
import JavKing.handler.GuildSettings;
import JavKing.handler.discord.awaitJDAEvents;
import JavKing.main.BotContainer;
import JavKing.main.DiscordBot;
import JavKing.templates.EmbedTemplate;
import JavKing.templates.Templates;
import JavKing.util.DisUtil;
import JavKing.util.Util;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class settings extends AbstractCommand {
    @Override
    public String getDescription() {
        return "Server settings for the bot";
    }

    @Override
    public String getCommand() {
        return "settings";
    }

    @Override
    public String[] getUsage() {
        return new String[0];
    }

    @Override
    public String[] getAlias() {
        return new String[]{"s"};
    }

    @Override
    public String execute(DiscordBot bot, String[] args, MessageChannel channel, User author, Message inputMessage) {
        EmbedBuilder builder = new EmbedTemplate().setTitle("JavKing Settings").setDescription("Use `" + DisUtil.getCommandPrefix(channel) +
                getCommand() + " <option>` to view more info about an option");

        for (GSetting gsetting : GSetting.values()) {
            String setting = String.join(" ", gsetting.toString().split("_")).toLowerCase();

            if (args.length > 0) {
                String fsetting = String.join("", setting.split("\\s+"));
                if (fsetting.equalsIgnoreCase(args[0])) {
                    builder.clearFields();
                    String updated = String.join(" ", args).substring(String.join(" ", args).indexOf(" ") + 1);
                    if (args[0].equalsIgnoreCase("reset")) {
                        if (!DisUtil.hasPermission(channel, author, Permission.MANAGE_SERVER)) {
                            return Templates.command.x_mark.formatFull("**You must have the permission `" + Permission.MANAGE_SERVER.getName() + "` to reset settings**");
                        }
                        bot.getJDA().addEventListener(new awaitJDAEvents(channel, author));
                        return Templates.command.warning.formatFull("**You are about to reset all JavKing's settings to default. Continue? (yes/no)**");
                    } else if (args.length > 1 && gsetting.getSettingType().validate(((TextChannel) channel).getGuild(), updated)) {
                        BotContainer.mongoDbAdapter.update("guildSettings", (TextChannel) channel, Util.camelCase(gsetting.toString(), "_", ""), updated);
                        GuildSettings.get(channel).reloadSettings();
                        return Templates.command.blue_check_mark.formatFull("**" + Util.capitalize(setting, true) + " updated to** `" + updated + "`");
                    } else {
                        builder.setTitle("JavKing Settings - " + gsetting.getIcon() + " " + Util.capitalizeAll(setting, true))
                                .setDescription(gsetting.getDescription())
                                .addField(Templates.command.check_mark.formatFull("Current Setting:"), Util.surround(GuildSettings.getFor(channel, gsetting).toString(), "`"), false)
                                .addField(Templates.command.pencil.formatFull("Update:"), Util.surround(
                                        DisUtil.getCommandPrefix(channel) + getCommand() + " " + fsetting + " [valid setting]", "`"), false)
                                .addField(Templates.command.blue_check_mark.formatFull("Valid Settings:"), Util.surround(gsetting.getValidSetting(), "`"), false);
                    }
                    break;
                }
            }
            builder.addField(gsetting.getIcon() + " " + Util.capitalizeAll(setting, true),
                    "`" + DisUtil.getCommandPrefix(channel) + "settings " + String.join("", setting.split(" ")) + "`", true);
        }
        Util.sendMessage(builder, inputMessage);
        return null;
    }
}
