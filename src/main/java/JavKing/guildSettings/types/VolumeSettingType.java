package JavKing.guildSettings.types;

import JavKing.guildSettings.IGuildSettingType;
import JavKing.main.BotContainer;
import net.dv8tion.jda.api.entities.Guild;

public class VolumeSettingType implements IGuildSettingType {
    private final int[] acceptedVolume;

    public VolumeSettingType() {
        int maxVolume = Integer.parseInt(BotContainer.getDotenv("MAX_VOLUME"));
        int[] acceptedVolume = new int[maxVolume + 1];
        for (int i = 0; i <= maxVolume; i++) acceptedVolume[i] = i;
        this.acceptedVolume = acceptedVolume;
    }

    @Override
    public String typeName() {
        return "volume-type";
    }

    @Override
    public boolean validate(Guild guild, String value) {
        for (int validVolume : acceptedVolume)
            if (validVolume == Integer.parseInt(value))
                return true;
        return false;
    }

    @Override
    public String fromInput(Guild guild, String value) {
        return validate(guild, value) ? value : "0";
    }

    @Override
    public String toDisplay(Guild guild, String value) {
        return fromInput(guild, value);
    }
}
