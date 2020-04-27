package JavKing.handler;

import JavKing.guildSettings.GSetting;

public class DefaultGuildSettings {

    public static GSetting get(String key) {
        return GSetting.valueOf(key.toUpperCase());
    }

    public static String getDefault(String key) {
        return GSetting.valueOf(key).getDefaultValue();
    }

    public static String getDefault(GSetting setting) {
        return setting.getDefaultValue();
    }
}
