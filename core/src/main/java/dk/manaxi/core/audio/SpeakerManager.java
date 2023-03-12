package dk.manaxi.core.audio;

import dk.manaxi.core.MediaAddon;
import dk.manaxi.core.listener.ServerMessageEvent;
import org.lwjgl.LWJGLException;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC11;
import paulscode.sound.SoundSystem;
import javax.sound.midi.Soundbank;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class SpeakerManager {
  private HashMap<UUID, Speaker> speakers = new HashMap<>();
  private Speaker playerSpeaker;
  private MediaAddon mediaAddon;

  public SpeakerManager(MediaAddon mediaAddon) {
    this.mediaAddon = mediaAddon;
    playerSpeaker = new Speaker(UUID.randomUUID(), mediaAddon);
    playerSpeaker.setPlayer(true);
  }

  public Speaker getPlayerSpeaker() {
    return playerSpeaker;
  }

  public Speaker getSpeaker(UUID uuid) {
    return speakers.get(uuid);
  }

  public void addSpeaker(Speaker speaker) {
    speakers.put(speaker.getUuid(), speaker);
  }

  public void cleanup() {
    Collection<Speaker> collection = this.speakers.values();
    for (Speaker speaker : collection) {
      Speaker removeSpeaker = this.speakers.remove(speaker.getUuid());
      removeSpeaker.cleanup();
    }
  }
}
