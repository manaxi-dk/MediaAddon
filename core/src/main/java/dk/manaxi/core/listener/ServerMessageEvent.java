package dk.manaxi.core.listener;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dk.manaxi.core.MediaAddon;
import dk.manaxi.core.OggHelper.OggUtils;
import java.util.Base64;
import net.labymod.api.event.Subscribe;
import net.labymod.api.event.client.network.server.NetworkPayloadEvent;
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
              System.out.println("spil 1");
              byte[] backToBytes = Base64.getDecoder().decode(jsonObject.get("data").getAsString());
              mediaAddon.getPlayerSpeaker().addSound(backToBytes, jsonObject.get("id").getAsString());
            } else {
              if(bytes == null) return;
              System.out.println("spil 2");
              mediaAddon.getPlayerSpeaker().addSound(bytes, jsonObject.get("id").getAsString());
              bytes = null;
            }
            mediaAddon.getPlayerSpeaker().play();
            break;
          case "addsound":
            if(jsonObject.get("data") == null) return;
            byte[] backToBytes = Base64.getDecoder().decode(jsonObject.get("data").getAsString());
            if(bytes == null) {
              System.out.println("resetter");
              bytes = backToBytes;
            } else {
              System.out.println("tilføjer");
              try {
                bytes = OggUtils.joinOggData(bytes, backToBytes);
              } catch (Exception e) {
                throw new RuntimeException(e);
              }
            }
            System.out.println("Længde1: " + backToBytes.length);
            System.out.println("Længde2: " + bytes.length);
            break;
          default:
            throw new IllegalStateException("Unexpected value: " + jsonObject.get("type").getAsString());
        }
      }
    }
  }

}
