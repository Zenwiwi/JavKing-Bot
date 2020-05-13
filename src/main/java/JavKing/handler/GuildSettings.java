package JavKing.handler;

import JavKing.guildSettings.GSetting;
import JavKing.main.BotContainer;
import JavKing.util.Util;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GuildSettings {
    private final static Map<Long, GuildSettings> settingInstance = new ConcurrentHashMap<>();
    private final String[] settings;
    private final String guildId;
    private boolean initialized = false;

    private GuildSettings(long guildId) {
        this.settings = new String[GSetting.values().length];
        this.guildId = String.valueOf(guildId);
        settingInstance.put(guildId, this);
        loadSettings();
    }

    private void loadSettings() {
        if (initialized) {
            return;
        }
        String[] guild = Util.oGuildArray(BotContainer.mongoDbAdapter.loadGuild(guildId));
        for (GSetting setting : GSetting.values()) {
            settings[setting.ordinal()] = guild[setting.ordinal()];
        }
        initialized = true;
    }

    public void reloadSettings() {
        initialized = false;
        loadSettings();
    }

    public void resetSettings() {
        Arrays.fill(settings, null);
        reloadSettings();
    }

    public static String getFor(MessageChannel channel, GSetting setting) {
        if (channel instanceof TextChannel) {
            return GuildSettings.get(((TextChannel) channel).getGuild()).getOrDefault(setting);
        }
        return DefaultGuildSettings.getDefault(setting);
    }

    public static GuildSettings get(MessageChannel channel) {
        if (channel instanceof TextChannel) {
            return GuildSettings.get(((TextChannel) channel).getGuild());
        }
        return null;
    }

    public static GuildSettings get(Guild guild) {
        return get(guild.getIdLong());
    }

    public static GuildSettings get(long guild) {
        if (settingInstance.containsKey(guild)) {
            return settingInstance.get(guild);
        } else {
            return new GuildSettings(guild);
        }
    }

    public String getOrDefault(GSetting setting) {
        return settings[setting.ordinal()] == null ? setting.getDefaultValue() : settings[setting.ordinal()];
    }
}
