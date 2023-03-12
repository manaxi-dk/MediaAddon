package dk.manaxi.core.audio;

import com.google.gson.JsonObject;
import dk.manaxi.core.MediaAddon;
import dk.manaxi.core.audio.OggInputStream;
import dk.manaxi.core.audio.OggPlayer;
import lombok.Getter;
import lombok.Setter;
import net.labymod.api.client.resources.ResourceLocation;
import net.labymod.serverapi.protocol.payload.io.PayloadWriter;
import org.lwjgl.Sys;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.UUID;

public class Speaker {
  private UUID uuid;
  private OggPlayer ogg;
  private Queue<OggInputStream> oggInputStreamQueue;
  private MediaAddon mediaAddon;
  @Getter @Setter
  private byte[] bytes;
  @Getter
  private float positionX = 0;
  @Getter
  private float positionY = 0;
  @Getter
  private float positionZ = 0;
  @Getter
  private float velocityX = 0;
  @Getter
  private float velocityY = 0;
  @Getter
  private float velocityZ = 0;
  @Getter
  private float directionX = 0;
  @Getter
  private float directionY = 0;
  @Getter
  private float directionZ = 0;
  @Getter
  private float rollOff = 0;
  @Getter
  private float gain = 1;
  @Getter
  private boolean relative = true;

  public Speaker(UUID uuid, MediaAddon mediaAddon) {
    this.uuid = uuid;
    ogg = new OggPlayer();
    oggInputStreamQueue = new LinkedList<>();
    this.mediaAddon = mediaAddon;
  }

  public UUID getUuid() {
    return uuid;
  }

  public void cleanup() {
    ogg.release();
    oggInputStreamQueue.clear();
    ogg = new OggPlayer();
    bytes = null;
  }

  public void setGain(float gain) {
    this.gain = gain;
    ogg.setGain(gain);
  }

  public void setLocation(float x, float y, float z) {
    positionX = x;
    positionY = y;
    positionZ = z;
    ogg.setPosition(x, y, z);
  }

  public void setVelocity(float x, float y, float z) {
    velocityX = x;
    velocityY = y;
    velocityZ = z;
    ogg.setVelocity(x, y, z);
  }

  public void setDirection(float x, float y, float z) {
    directionX = x;
    directionY = y;
    directionZ = z;
    ogg.setDirection(x, y, z);
  }

  public void setRollOff(float value) {
    rollOff = value;
    ogg.setRollOff(value);
  }

  public void setRelative(boolean value) {
    relative = value;
    ogg.setRelative(value);
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
        ogg.open(oggInputStream, this);
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

