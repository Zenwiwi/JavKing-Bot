package JavKing.guildSettings;

import JavKing.guildSettings.types.RoleSettingType;
import JavKing.guildSettings.types.StringLengthSettingType;
import JavKing.main.BotContainer;
import net.dv8tion.jda.api.entities.Guild;

public enum GSetting {
    PREFIX(BotContainer.getDotenv("PREFIX"), new StringLengthSettingType(1, 4), "Prefix for commands (between 1 and 4 characters)"),
    DJ(BotContainer.getDotenv("DJ"), new RoleSettingType(true), "Role for music commands"),;

    private final String defaultValue;
    private final IGuildSettingType settingType;
    private final String description;

    GSetting(String defaultValue, IGuildSettingType settingType, String description) {
        this.defaultValue = defaultValue;
        this.settingType = settingType;
        this.description = description;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public String getDescription() {
        return description;
    }

    public boolean isValidValue(Guild guild, String input) {
        return settingType.validate(guild, input);
    }

    public String getValue(Guild guild, String input) {
        return settingType.fromInput(guild, input);
    }

    public String toDisplay(Guild guild, String value) {
        return settingType.toDisplay(guild, value);
    }

    public IGuildSettingType getSettingType() {
        return settingType;
    }
}
