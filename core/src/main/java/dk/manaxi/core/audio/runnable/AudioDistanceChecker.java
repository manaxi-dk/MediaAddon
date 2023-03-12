package dk.manaxi.core.audio.runnable;

import dk.manaxi.core.audio.Speaker;
import net.labymod.api.client.entity.player.Player;
import org.lwjgl.openal.AL10;
import paulscode.sound.Vector3D;
import java.nio.IntBuffer;

public class AudioDistanceChecker implements Runnable {
  private final Speaker speaker;
  private final IntBuffer source;
  private final float maxDistance;
  private final float volume;

  public AudioDistanceChecker(Speaker speaker, IntBuffer source, float maxDistance, float volume) {
    this.speaker = speaker;
    this.source = source;
    this.maxDistance = maxDistance;
    this.volume = volume;
  }

  public void run() {
    while (true) {
      try {
        Thread.sleep(1000); // Check distance every second
      } catch (InterruptedException e) {
        break;
      }
      Player player = speaker.getMediaAddon().labyAPI().minecraft().getClientPlayer();
      Vector3D selfPlayerLocation = new Vector3D(player.getPosX(), player.getPosY(), player.getPosZ());
      Vector3D otherLocation = new Vector3D(speaker.getPositionX(), speaker.getPositionY(), speaker.getPositionZ());
      double distance = selfPlayerLocation.subtract(otherLocation).length();
      if (distance > maxDistance) {
        AL10.alSourcef(source.get(0), AL10.AL_GAIN, 0);
      } else {
        AL10.alSourcef(source.get(0), AL10.AL_GAIN, volume);
      }
    }
  }
}
