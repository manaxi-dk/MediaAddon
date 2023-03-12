package dk.manaxi.core.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dk.manaxi.core.MediaAddon;
import dk.manaxi.core.audio.OggUtils;
import java.util.Base64;
import java.util.UUID;
import dk.manaxi.core.audio.Speaker;
import net.labymod.api.Laby;
import net.labymod.api.client.entity.player.Player;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.lifecycle.GameTickEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.ServerDisconnectEvent;
import net.labymod.api.event.client.network.server.ServerJoinEvent;
import net.labymod.api.event.client.network.server.SubServerSwitchEvent;
import net.labymod.api.util.UUIDHelper;
import net.labymod.api.util.io.web.request.WebResolver;
import net.labymod.serverapi.protocol.payload.io.PayloadReader;
import org.lwjgl.Sys;
import org.lwjgl.openal.AL10;

public class ServerMessageEvent {
  private final MediaAddon mediaAddon;
  public ServerMessageEvent(MediaAddon mediaAddon) {
    this.mediaAddon = mediaAddon;
  }
  @Subscribe
  public void onReceive(NetworkPayloadEvent event) {
    if(event.identifier().getNamespace().equals("labymod3") && event.identifier().getPath().equals("media")) {
      PayloadReader reader = new PayloadReader(event.getPayload());
      String messageKey = reader.readString();
      String messageContent = reader.readString();

      JsonElement parsedServerMessage = WebResolver.GSON.fromJson(messageContent, JsonElement.class);
      if(messageKey.equals("sound")) {
        if(!parsedServerMessage.isJsonObject()) return;
        JsonObject jsonObject = parsedServerMessage.getAsJsonObject();
        Speaker speaker = mediaAddon.getSpeakerManager().getPlayerSpeaker();
        if(jsonObject.get("speakerId") != null) {
          try {
            UUID speakerUUID = UUID.fromString(jsonObject.get("speakerId").getAsString());
            Speaker newspeaker = mediaAddon.getSpeakerManager().getSpeaker(speakerUUID);
            if(newspeaker != null) {
              speaker = newspeaker;
            }
          } catch (IllegalArgumentException e) {

          }
        }
        switch (jsonObject.get("type").getAsString()) {
          case "play":
            System.out.println("UUID: " + speaker.getUuid());
            if(jsonObject.get("data") != null) {
              byte[] backToBytes = Base64.getDecoder().decode(jsonObject.get("data").getAsString());
              speaker.addSound(backToBytes, jsonObject.get("id").getAsString());
            } else {
              if(speaker.getBytes() == null) return;
              speaker.addSound(speaker.getBytes(), jsonObject.get("id").getAsString());
              speaker.setBytes(null);
            }
            speaker.play();
            break;
          case "addsound":
            if(jsonObject.get("data") == null) return;
            byte[] backToBytes = Base64.getDecoder().decode(jsonObject.get("data").getAsString());
            System.out.println("UUID: " + speaker.getUuid());
            if(speaker.getBytes() == null) {
              speaker.setBytes(backToBytes);
            } else {
              try {
                speaker.setBytes(OggUtils.joinOggData(speaker.getBytes(), backToBytes));
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
            break;
          case "addspeaker":
            if(jsonObject.get("speakerId") == null) return;
            Speaker speaker1;
            try {
              UUID speakerId = UUID.fromString(jsonObject.get("speakerId").getAsString());
              if(!speakerId.toString().equals(jsonObject.get("speakerId").getAsString())) return;
              speaker1 = mediaAddon.getSpeakerManager().getSpeaker(speakerId);
              if(speaker1 == null) {
                speaker1 = new Speaker(speakerId, mediaAddon);
              }
            } catch (IllegalArgumentException e) {
              return;
            }
            if(jsonObject.get("location") != null) {
              JsonObject locationObject = jsonObject.getAsJsonObject("location");
              try {
                float x = locationObject.get("x").getAsFloat();
                float y = locationObject.get("y").getAsFloat();
                float z = locationObject.get("z").getAsFloat();
                speaker1.setLocation(x, y, z);
              } catch (NullPointerException ex) {

              }
            }
            if(jsonObject.get("velocity") != null) {
              JsonObject velocityObject = jsonObject.get("velocity").getAsJsonObject();
              try {
                float x = velocityObject.get("x").getAsFloat();
                float y = velocityObject.get("y").getAsFloat();
                float z = velocityObject.get("z").getAsFloat();
                speaker1.setVelocity(x, y, z);
              } catch (NullPointerException ex) {

              }
            }
            if(jsonObject.get("direction") != null) {
              JsonObject velocityObject = jsonObject.get("direction").getAsJsonObject();
              try {
                float x = velocityObject.get("x").getAsFloat();
                float y = velocityObject.get("y").getAsFloat();
                float z = velocityObject.get("z").getAsFloat();
                speaker1.setDirection(x, y, z);
              } catch (NullPointerException ex) {

              }
            }
            if(jsonObject.get("rolloff") != null) {
              JsonObject velocityObject = jsonObject.get("rolloff").getAsJsonObject();
              try {
                float value = velocityObject.get("value").getAsFloat();
                speaker1.setRollOff(value);
              } catch (NullPointerException ex) {

              }
            }
            if(jsonObject.get("relative") != null) {
              speaker1.setRelative(jsonObject.get("relative").getAsBoolean());
            }
            if(jsonObject.get("gain") != null) {
              speaker1.setGain(jsonObject.get("gain").getAsFloat());
            }
            System.out.println("UUID: " + speaker1.getUuid());
            mediaAddon.getSpeakerManager().addSpeaker(speaker1);
            break;
          default:
            throw new IllegalStateException("Unexpected value: " + jsonObject.get("type").getAsString());
        }
      }
    }
  }

  @Subscribe
  public void onServerSwitch(SubServerSwitchEvent event) {
    mediaAddon.logger().info("ServerSwitch cleanup");
    mediaAddon.getSpeakerManager().getPlayerSpeaker().cleanup();
    mediaAddon.getSpeakerManager().cleanup();
  }

  @Subscribe
  public void onServerLeave(ServerDisconnectEvent event) {
    mediaAddon.logger().info("ServerDisconnect cleanup");
    mediaAddon.getSpeakerManager().getPlayerSpeaker().cleanup();
    mediaAddon.getSpeakerManager().cleanup();
  }
}
