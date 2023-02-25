package dk.manaxi.mediaapi.Classes;


import com.google.gson.JsonObject;
import dk.manaxi.mediaapi.Main;
import dk.manaxi.mediaapi.OggShit.OggInputStream;
import dk.manaxi.mediaapi.OggShit.OggPlayer;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.network.PacketBuffer;
import org.lwjgl.Sys;

import javax.sound.sampled.AudioFormat;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

import static org.lwjgl.openal.AL10.alGenSources;

public class Speaker {
    @Getter
    private UUID uuid;
    private OggPlayer ogg;
    @Getter @Setter
    private Queue<OggInputStream> oggInputStreamQueue;

    public Speaker(UUID uuid) {
        this.uuid = uuid;
        ogg = new OggPlayer();
        oggInputStreamQueue = new LinkedList<>();
    }

    public void cleanup() {
        ogg.release();
        ogg = new OggPlayer();
    }

    public void setLocation(float x, float y, float z) {
        ogg.setPosition(x, y, z);
    }

    public void addSound(byte[] data, String id) {
        System.out.println("Længde af sounden " + data.length);
        ByteArrayInputStream input = new ByteArrayInputStream(data);
        System.out.println("Længde af sounden " + input.available());
        oggInputStreamQueue.add(new OggInputStream(input, id));
    }

    public void play() {
        if(ogg.playing()) {
            return;
        }
        new Thread(() -> {
            while (!oggInputStreamQueue.isEmpty()) {
                OggInputStream oggInputStream = oggInputStreamQueue.poll();
                ogg.open(oggInputStream);
                ogg.play();
                while (true) {
                    try {
                        if (!ogg.update()) {
                            break;
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                // execute the callback when the sound is finished
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("id", oggInputStream.getId());

                PacketBuffer packetBuffer = new PacketBuffer(Unpooled.buffer());
                packetBuffer.writeString("done");
                packetBuffer.writeString(jsonObject.toString());
                Main.getInstance().getApi().sendPluginMessage("labymod3:media", new PacketBuffer(packetBuffer.copy()));

                ogg.release();
            }
        }).start();
    }

}
