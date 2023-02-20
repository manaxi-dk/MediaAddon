package dk.manaxi.mediaapi.Classes;


import dk.manaxi.mediaapi.OggShit.OggInputStream;
import dk.manaxi.mediaapi.OggShit.OggPlayer;
import lombok.Getter;
import org.lwjgl.openal.AL10;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.UUID;

import static org.lwjgl.openal.AL10.AL_POSITION;
import static org.lwjgl.openal.AL10.alGenSources;

public class Speaker {
    private static AudioFormat format =
            new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, 48000, 16, 1, 2, 48000, false);

    @Getter
    private final int bufferId;
    @Getter
    private UUID uuid;
    private OggPlayer ogg;

    public Speaker(UUID uuid) {
        this.uuid = uuid;
        this.bufferId = alGenSources();
        ogg = new OggPlayer();
    }

    public void cleanup() {
        ogg.release();
    }

    public void setLocation(float x, float y, float z) {
        ogg.setPosition(x, y, z);
    }

    public void play(byte[] data) {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ogg.open(new OggInputStream(input));
        ogg.playInNewThread(5);
    }

}
