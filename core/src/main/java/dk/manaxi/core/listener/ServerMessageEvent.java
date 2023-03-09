package dk.manaxi.core.listener;

import com.google.gson.JsonParser;
import dk.manaxi.core.MediaAddon;
import net.labymod.api.client.resources.ResourceLocation;
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
      System.out.println(reader.readString());
      System.out.println(reader.readString());
    }
  }

}
