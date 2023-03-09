package dk.manaxi.core;

import dk.manaxi.core.listener.ServerMessageEvent;
import dk.manaxi.core.packet.AddSoundPacket;
import dk.manaxi.core.packet.MediaProtocol;
import lombok.Getter;
import lombok.Setter;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;
import java.util.UUID;

@AddonMain
public class MediaAddon extends LabyAddon<MediaConfiguration> {
  private Speaker playerSpeaker;


  @Override
  protected void enable() {
    this.registerSettingCategory();
    playerSpeaker = new Speaker(UUID.randomUUID(), this);
    this.registerListener(new ServerMessageEvent(this));

    this.logger().info("Enabled the Addon");
  }

  public Speaker getPlayerSpeaker() {
    return playerSpeaker;
  }

  public void setPlayerSpeaker(Speaker playerSpeaker) {
    this.playerSpeaker = playerSpeaker;
  }

  @Override
  protected Class<MediaConfiguration> configurationClass() {
    return MediaConfiguration.class;
  }
}
