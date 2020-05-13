package JavKing.guildSettings.types;

import JavKing.guildSettings.IGuildSettingType;
import net.dv8tion.jda.api.entities.Guild;

public class PrefixSettingType implements IGuildSettingType {
    private final int min, max;

    public PrefixSettingType(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public String typeName() {
        return "string-length";
    }

    @Override
    public boolean validate(Guild guild, String value) {
        return value != null && value.length() >= min && value.length() <= max;
    }

    @Override
    public String fromInput(Guild guild, String value) {
        return value;
    }

    @Override
    public String toDisplay(Guild guild, String value) {
        return value;
    }
}
