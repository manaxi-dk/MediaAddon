package dk.manaxi.mediaapi.Classes;


import com.google.gson.JsonObject;
import dk.manaxi.mediaapi.Main;
import dk.manaxi.mediaapi.OggShit.OggInputStream;
import dk.manaxi.mediaapi.OggShit.OggPlayer;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import net.minecraft.network.PacketBuffer;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.util.UUID;

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

    public void play(byte[] data, String id) {
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        ogg.open(new OggInputStream(input));
        ogg.playInNewThread(5);
        new Thread(() -> {
            while (ogg.playing()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ogg.release();
            ogg = new OggPlayer();

            // execute the callback when the sound is finished
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("id", id);

            PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
            packetBuffer.writeString("done");
            packetBuffer.writeString(jsonObject.toString());
            Main.getInstance().getApi().sendPluginMessage("labymod3:media", new PacketBuffer(packetBuffer.copy()));
        }).start();
    }

}
