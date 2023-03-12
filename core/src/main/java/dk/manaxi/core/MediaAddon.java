package dk.manaxi.core;

import dk.manaxi.core.audio.Speaker;
import dk.manaxi.core.audio.SpeakerManager;
import dk.manaxi.core.listener.ServerMessageEvent;
import java.util.UUID;
import net.labymod.api.addon.LabyAddon;
import net.labymod.api.models.addon.annotation.AddonMain;

@AddonMain
public class MediaAddon extends LabyAddon<MediaConfiguration> {
  private SpeakerManager speakerManager;

  @Override
  protected void enable() {
    this.registerSettingCategory();
    this.speakerManager = new SpeakerManager(this);
    this.registerListener(new ServerMessageEvent(this));

    this.logger().info("Enabled the Addon");
  }

  public SpeakerManager getSpeakerManager() {
    return speakerManager;
  }

  @Override
  protected Class<MediaConfiguration> configurationClass() {
    return MediaConfiguration.class;
  }
}
