package dk.manaxi.core;

import dk.manaxi.core.listener.ServerMessageEvent;
import dk.manaxi.core.packet.AddSoundPacket;
import dk.manaxi.core.packet.MediaProtocol;
import net.labymod.api.Laby;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class MediaAddon extends LabyAddon<MediaConfiguration> {


  @Override
  protected void enable() {
    this.registerSettingCategory();
    this.registerListener(new ServerMessageEvent(this));

    this.logger().info("Enabled the Addon");
  }

  @Override
  protected Class<MediaConfiguration> configurationClass() {
    return MediaConfiguration.class;
  }
}
