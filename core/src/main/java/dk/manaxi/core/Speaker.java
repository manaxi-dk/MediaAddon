package dk.manaxi.core;

import com.google.gson.JsonObject;
import dk.manaxi.core.ogghelper.OggInputStream;
import dk.manaxi.core.ogghelper.OggPlayer;
import lombok.Getter;
import lombok.Setter;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.serverapi.protocol.payload.io.PayloadWriter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class Speaker {
  @Getter
  private UUID uuid;
  private OggPlayer ogg;
  @Getter @Setter
  private Queue<OggInputStream> oggInputStreamQueue;
  private MediaAddon mediaAddon;

  public Speaker(UUID uuid, MediaAddon mediaAddon) {
    this.uuid = uuid;
    ogg = new OggPlayer();
    oggInputStreamQueue = new LinkedList<>();
    this.mediaAddon = mediaAddon;
  }

  public void cleanup() {
    ogg.release();
    ogg = new OggPlayer();
  }

  public void setLocation(float x, float y, float z) {
    ogg.setPosition(x, y, z);
  }

  public void addSound(byte[] data, String id) {
    ByteArrayInputStream input = new ByteArrayInputStream(data);
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

        PayloadWriter payloadWriter = new PayloadWriter();
        payloadWriter.writeString("done");
        payloadWriter.writeString(jsonObject.toString());
        mediaAddon.labyAPI().serverController().sendPayload(ResourceLocation.create("labymod3", "media"), payloadWriter.toByteArray());

        ogg.release();
      }
    }).start();
  }

}

