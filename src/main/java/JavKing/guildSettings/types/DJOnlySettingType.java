package JavKing.guildSettings.types;

import JavKing.guildSettings.IGuildSettingType;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Arrays;

public class DJOnlySettingType implements IGuildSettingType {
    private final String[] accepted;

    public DJOnlySettingType() {
        accepted = new String[]{"on", "off"};
    }
    @Override
    public String typeName() {
        return "dj-type";
    }

    @Override
    public boolean validate(Guild guild, String value) {
        return value != null && Arrays.asList(accepted).contains(value);
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
