package dk.manaxi.mediaapi;

import dk.manaxi.mediaapi.Classes.Speaker;
import dk.manaxi.mediaapi.Listeners.ServerSwitchListener;
import lombok.Getter;
import net.labymod.main.Source;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import paulscode.sound.SoundSystem;

import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class SpeakerManager {
    @Getter
    private SoundHandler soundHandler;
    @Getter
    private SoundManager soundManager;
    @Getter
    private SoundSystem sndSystem;
    @Getter
    private HashMap<UUID, Speaker> speakers = new HashMap<>();
    public SpeakerManager() {
        this.soundHandler = Minecraft.getMinecraft().getSoundHandler();
        try {
            this.soundManager = (SoundManager) ReflectionHelper.getPrivateValue(SoundHandler.class, soundHandler, 5);
            if (this.soundManager != null)
                this.sndSystem = (SoundSystem) ReflectionHelper.getPrivateValue(SoundManager.class, this.soundManager, Source.ABOUT_MC_VERSION.startsWith("1.8") ? 4 : 5);
        } catch (Exception error) {
            error.printStackTrace();
        }
        Main.getInstance().getApi().getEventManager().register(new ServerSwitchListener());
    }

    public void addSpeaker(Speaker speaker) {
        speakers.put(speaker.getUuid(), speaker);
    }

    public void cleanup() {
        Collection<Speaker> collection = this.speakers.values();
        for (Speaker speaker : collection) {
            Speaker removeSpeaker = this.speakers.remove(speaker.getUuid());
            removeSpeaker.cleanup();
        }
    }
}
