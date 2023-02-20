package dk.manaxi.mediaapi;

import dk.manaxi.mediaapi.Classes.Speaker;
import lombok.Getter;
import lombok.Setter;
import net.labymod.api.LabyModAddon;
import net.labymod.settings.elements.SettingsElement;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
/*import uk.co.caprica.vlcj.factory.MediaPlayerFactory;
import uk.co.caprica.vlcj.factory.discovery.NativeDiscovery; */

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class Main extends LabyModAddon {
    @Getter
    private static Main instance;
    //private MediaPlayerFactory mediaPlayerFactory;
    @Getter
    private SpeakerManager speakerManager;
    @Getter @Setter
    private Speaker playerSpeaker;
    @Override
    public void onEnable() {
        instance = this;
        //new NativeDiscovery().discover();
        this.speakerManager = new SpeakerManager();
        playerSpeaker = new Speaker(UUID.randomUUID());
    }

    @Override
    public void loadConfig() {

    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {

    }
}
