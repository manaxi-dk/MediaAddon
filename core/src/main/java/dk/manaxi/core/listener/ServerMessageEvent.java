package dk.manaxi.core.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dk.manaxi.core.MediaAddon;
import dk.manaxi.core.OggHelper.OggUtils;
import java.util.Base64;
import java.util.UUID;
import dk.manaxi.core.Speaker;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkDisconnectEvent;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
import net.labymod.api.event.client.network.server.NetworkServerSwitchEvent;
import net.labymod.api.event.client.network.server.ServerSwitchEvent;
import net.labymod.serverapi.protocol.payload.io.PayloadReader;

public class ServerMessageEvent {
  private MediaAddon mediaAddon;
  private static final JsonParser jsonParser = new JsonParser();
  private static byte[] bytes;
  public ServerMessageEvent(MediaAddon mediaAddon) {
    this.mediaAddon = mediaAddon;
  }
  @Subscribe
  public void onReceive(NetworkPayloadEvent event) {
    if(event.identifier().getNamespace().equals("labymod3") && event.identifier().getPath().equals("media")) {
      PayloadReader reader = new PayloadReader(event.getPayload());
      String messageKey = reader.readString();
      String messageContent = reader.readString();
      JsonElement parsedServerMessage = jsonParser.parse(messageContent);
      if(messageKey.equals("sound")) {
        if(!parsedServerMessage.isJsonObject()) return;
        JsonObject jsonObject = parsedServerMessage.getAsJsonObject();
        switch (jsonObject.get("type").getAsString()) {
          case "play":
            if(jsonObject.get("data") != null) {
              byte[] backToBytes = Base64.getDecoder().decode(jsonObject.get("data").getAsString());
              mediaAddon.getPlayerSpeaker().addSound(backToBytes, jsonObject.get("id").getAsString());
            } else {
              if(bytes == null) return;
              mediaAddon.getPlayerSpeaker().addSound(bytes, jsonObject.get("id").getAsString());
              bytes = null;
            }
            mediaAddon.getPlayerSpeaker().play();
            break;
          case "addsound":
            if(jsonObject.get("data") == null) return;
            byte[] backToBytes = Base64.getDecoder().decode(jsonObject.get("data").getAsString());
            if(bytes == null) {
              bytes = backToBytes;
            } else {
              try {
                bytes = OggUtils.joinOggData(bytes, backToBytes);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
            break;
          default:
            throw new IllegalStateException("Unexpected value: " + jsonObject.get("type").getAsString());
        }
      }
    }
  }

  @Subscribe
  public void onServerSwitch(NetworkServerSwitchEvent event) {
    mediaAddon.getPlayerSpeaker().cleanup();
  }

  @Subscribe
  public void onServerLeave(NetworkDisconnectEvent event) {
    mediaAddon.getPlayerSpeaker().cleanup();
  }

}
