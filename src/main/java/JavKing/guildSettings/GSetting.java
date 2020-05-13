package JavKing.guildSettings;

import JavKing.guildSettings.types.AnnounceSongsSettingType;
import JavKing.guildSettings.types.DJOnlySettingType;
import JavKing.guildSettings.types.DJRoleSettingType;
import JavKing.guildSettings.types.PrefixSettingType;
import JavKing.main.BotContainer;
import net.dv8tion.jda.api.entities.Guild;

public enum GSetting {
    ANNOUNCE_SONGS("🔔", "off", new AnnounceSongsSettingType(), "Announces title of each song upon playing", "on/off"),
    DJ_ONLY("🚷", "off", new DJOnlySettingType(), "DJs only mode", "on/off"),
    DJ_ROLE("\uD83D\uDCC3", BotContainer.getDotenv("DJ"), new DJRoleSettingType(true), "Changes DJ role", "role name"),
    PREFIX("❗", BotContainer.getDotenv("PREFIX"), new PrefixSettingType(1, 4), "Changes prefix for JavKing", "Any text, max of 4 characters"),
    RESET("♻️", null, null, null, null);

    private final String icon;
    private final String defaultValue;
    private final IGuildSettingType settingType;
    private final String description;
    private final String validSetting;

    GSetting(String icon, String defaultValue, IGuildSettingType settingType, String description, String validSetting) {
        this.icon = icon;
        this.defaultValue = defaultValue;
        this.settingType = settingType;
        this.description = description;
        this.validSetting = validSetting;
    }

    public String getIcon() {
        return icon;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public IGuildSettingType getSettingType() {
        return settingType;
    }

    public String getDescription() {
        return description;
    }

    public String getValidSetting() { return validSetting; }

    public boolean isValidValue(Guild guild, String input) {
        return settingType.validate(guild, input);
    }

    public String getValue(Guild guild, String input) {
        return settingType.fromInput(guild, input);
    }

    public String toDisplay(Guild guild, String value) {
        return settingType.toDisplay(guild, value);
    }
}
