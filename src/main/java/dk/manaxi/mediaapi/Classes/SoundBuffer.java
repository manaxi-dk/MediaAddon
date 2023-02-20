package dk.manaxi.mediaapi.Classes;

import dk.manaxi.mediaapi.OggShit.OggInputStream;
import dk.manaxi.mediaapi.OggShit.OggPlayer;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.util.WaveData;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alGenBuffers;

public class SoundBuffer {

    public static void createSoundBuffer(int bufferId, File file) {
        WaveData data = null;
        try {
            data = WaveData.create(AudioSystem.getAudioInputStream(file));
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        alGenBuffers(buffer);

        alBufferData(buffer.get(0), data.format, data.data, data.samplerate);
        data.dispose();

        AL10.alSourceQueueBuffers(bufferId, buffer);
    }

    public static void createSoundBuffer(int bufferId, byte[] data) {
        OggPlayer ogg = new OggPlayer();
        try {
            ByteArrayInputStream input = new ByteArrayInputStream(data);
            ogg.open(new OggInputStream(input));
            ogg.play();
        } finally {
            ogg.release();
        }
    }

}
