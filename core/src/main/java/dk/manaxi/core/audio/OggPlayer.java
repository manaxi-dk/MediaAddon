package dk.manaxi.core.audio;

import dk.manaxi.core.audio.runnable.AudioDistanceChecker;
import net.labymod.api.client.entity.player.Player;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.AL11;
import paulscode.sound.SoundSystem;
import paulscode.sound.Vector3D;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import static org.lwjgl.openal.AL10.AL_DISTANCE_MODEL;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_MAX_DISTANCE;
import static org.lwjgl.openal.AL10.AL_REFERENCE_DISTANCE;

/**
 * Plays ogg files using lwjgl's openal.
 * <p>
 * First open the file by calling open(OggInputStream). Then play it either by
 * using play(), or playInNewThread(long). If you use Play() you must also call
 * update() at an interval, to feed OpenAL with data.
 */
public class OggPlayer {

  // temporary buffer
  private ByteBuffer dataBuffer = ByteBuffer.allocateDirect(4096*8);

  // front and back buffers
  private IntBuffer buffers = createIntBuffer(2);

  // audio source
  private IntBuffer source = createIntBuffer(1);

  // is used to unpack ogg file.
  private OggInputStream oggInputStream;

  // set to true when player is initialized.
  private boolean initalized = false;

  private Thread thread;

  private Speaker speaker;


  /**
   * Opens the specified ogg file in the classpath.
   */
  public void open(OggInputStream input, Speaker speaker) {
    oggInputStream = input;

    ((Buffer) buffers).rewind();
    AL10.alGenBuffers(buffers);
    check();

    ((Buffer) source).rewind();
    AL10.alGenSources(source);
    check();

    initalized = true;

    AL10.alSourcei(source.get(0), AL10.AL_SOURCE_RELATIVE, AL10.AL_TRUE);

    this.speaker = speaker;
    Player player = speaker.getMediaAddon().labyAPI().minecraft().getClientPlayer();

    if(!speaker.isPlayer()) {
      AL10.alDistanceModel(AL11.AL_EXPONENT_DISTANCE);
      AL10.alSourcei(source.get(0), AL_REFERENCE_DISTANCE, speaker.getDistance());
      AL10.alSourcei(source.get(0), AL_MAX_DISTANCE, 100);
      thread = new Thread(new AudioDistanceChecker(speaker, source, speaker.getDistance(), speaker.getGain()));
      thread.start();

      Vector3D selfPlayerLocation = new Vector3D(player.getPosX(), player.getPosY(), player.getPosZ());
      Vector3D otherLocation = new Vector3D(speaker.getPositionX(), speaker.getPositionY(), speaker.getPositionZ());
      Vector3D differenceLocation = otherLocation.clone().subtract(selfPlayerLocation);
      differenceLocation = rotateVectorCC(differenceLocation, new Vector3D(0.0F, 1.0F, 0.0F), Math.toRadians(player.getRotationYaw()));
      differenceLocation.x = -differenceLocation.x;
      differenceLocation.y = -differenceLocation.y;
      if (Math.abs(differenceLocation.x) < 0.1D)
        differenceLocation.x = 0.0F;
      if (Math.abs(differenceLocation.z) < 0.1D)
        differenceLocation.z = 0.0F;
      AL10.alSource3f(source.get(0), AL10.AL_POSITION, differenceLocation.x, differenceLocation.y, differenceLocation.z);
    }
    AL10.alSource3f(source.get(0), AL10.AL_POSITION, 0, 0, 0);
    AL10.alSourcef(source.get(0), AL10.AL_ROLLOFF_FACTOR, 3.4f);
  }

  public void setGain() {
    if(initalized) {
      setPosition(speaker.getPositionX(), speaker.getPositionY(), speaker.getPositionZ());
    }
  }

  public void setDistance() {
    if(initalized) {
      setPosition(speaker.getPositionX(), speaker.getPositionY(), speaker.getPositionZ());
    }
  }

  public void setPosition(float x, float y, float z) {
    if(initalized && !speaker.isPlayer()) {
      thread.stop();
      Player player = speaker.getMediaAddon().labyAPI().minecraft().getClientPlayer();

      thread = new Thread(new AudioDistanceChecker(speaker, source, speaker.getDistance(), speaker.getGain()));
      thread.start();

      Vector3D selfPlayerLocation = new Vector3D(player.getPosX(), player.getPosY(), player.getPosZ());
      Vector3D otherLocation = new Vector3D(speaker.getPositionX(), speaker.getPositionY(), speaker.getPositionZ());
      Vector3D differenceLocation = otherLocation.clone().subtract(selfPlayerLocation);
      differenceLocation = rotateVectorCC(differenceLocation, new Vector3D(0.0F, 1.0F, 0.0F), Math.toRadians(player.getRotationYaw()));
      differenceLocation.x = -differenceLocation.x;
      differenceLocation.y = -differenceLocation.y;
      if (Math.abs(differenceLocation.x) < 0.1D)
        differenceLocation.x = 0.0F;
      if (Math.abs(differenceLocation.z) < 0.1D)
        differenceLocation.z = 0.0F;
      AL10.alSource3f(source.get(0), AL10.AL_POSITION, differenceLocation.x, differenceLocation.y, differenceLocation.z);
    }
  }


  /**
   * release the file handle
   */
  public void release() {
    if (initalized) {
      AL10.alSourceStop(source);
      empty();
      AL10.alDeleteSources(source);
      check();
      AL10.alDeleteBuffers(buffers);
      check();
    }
  }


  /**
   * Plays the Ogg stream. update() must be called regularly so that the data
   * is copied to OpenAl
   */
  public boolean play() {
    if (playing()) {
      return true;
    }

    for (int i=0; i<buffers.capacity(); i++) {
      if (!stream(buffers.get(i))) {
        return false;
      }
    }

    AL10.alSourceQueueBuffers(source.get(0), buffers);
    AL10.alSourcePlay(source.get(0));

    return true;
  }


  /**
   * check if the source is playing
   */
  public boolean playing() {
    return (AL10.alGetSourcei(source.get(0), AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING);
  }


  /**
   * Copies data from the ogg stream to openal. Must be called often.
   * @return true if sound is still playing, false if the end of file is reached.
   */
  public synchronized boolean update(Speaker speaker) throws IOException {
    boolean active = true;
    int processed = AL10.alGetSourcei(source.get(0), AL10.AL_BUFFERS_PROCESSED);
    while (processed-- > 0) {
      IntBuffer buffer = createIntBuffer(1);
      AL10.alSourceUnqueueBuffers(source.get(0), buffer);
      check();

      active = stream(buffer.get(0));
      ((Buffer) buffer).rewind();
      if(!speaker.isPlayer()) {
        Player player = speaker.getMediaAddon().labyAPI().minecraft().getClientPlayer();

        Vector3D selfPlayerLocation = new Vector3D(player.getPosX(), player.getPosY(), player.getPosZ());
        Vector3D otherLocation = new Vector3D(speaker.getPositionX(), speaker.getPositionY(), speaker.getPositionZ());
        Vector3D differenceLocation = otherLocation.clone().subtract(selfPlayerLocation);
        differenceLocation = rotateVectorCC(differenceLocation, new Vector3D(0.0F, 1.0F, 0.0F), Math.toRadians(player.getRotationYaw()));
        differenceLocation.x = -differenceLocation.x;
        differenceLocation.y = -differenceLocation.y;
        if (Math.abs(differenceLocation.x) < 0.1D)
          differenceLocation.x = 0.0F;
        if (Math.abs(differenceLocation.z) < 0.1D)
          differenceLocation.z = 0.0F;
        AL10.alSource3f(source.get(0), AL10.AL_POSITION, differenceLocation.x, differenceLocation.y, differenceLocation.z);
      }

      AL10.alSourceQueueBuffers(source.get(0), buffer);
      check();
    }

    return active;
  }

  public static Vector3D rotateVectorCC(Vector3D vec, Vector3D axis, double theta) {
    double x = vec.x;
    double y = vec.y;
    double z = vec.z;
    double u = axis.x;
    double v = axis.y;
    double w = axis.z;
    double xPrime = u * (u * x + v * y + w * z) * (1.0D - Math.cos(theta)) + x * Math.cos(theta) + (-w * y + v * z) * Math.sin(theta);
    double yPrime = v * (u * x + v * y + w * z) * (1.0D - Math.cos(theta)) + y * Math.cos(theta) + (w * x - u * z) * Math.sin(theta);
    double zPrime = w * (u * x + v * y + w * z) * (1.0D - Math.cos(theta)) + z * Math.cos(theta) + (-v * x + u * y) * Math.sin(theta);
    return new Vector3D((float)xPrime, (float)yPrime, (float)zPrime);
  }


  /**
   * reloads a buffer
   * @return true if success, false if read failed or end of file.
   */
  protected boolean stream(int buffer) {
    try {
      int bytesRead = oggInputStream.read(dataBuffer, 0, dataBuffer.capacity());
      if (bytesRead >= 0) {
        ((Buffer) dataBuffer).rewind();
        boolean mono = (oggInputStream.getFormat() == OggInputStream.FORMAT_MONO16);
        int format = (mono ? AL_FORMAT_MONO16 : AL10.AL_FORMAT_STEREO16);
        AL10.alBufferData(buffer, format, dataBuffer, oggInputStream.getRate());
        check();
        return true;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }


  /**
   * empties the queue
   */
  protected void empty() {
    int queued = AL10.alGetSourcei(source.get(0), AL10.AL_BUFFERS_QUEUED);
    while (queued-- > 0) {
      IntBuffer buffer = createIntBuffer(1);
      AL10.alSourceUnqueueBuffers(source.get(0), buffer);
      check();
    }
  }


  /**
   * checks OpenAL error state
   */
  protected void check() {
    int error = AL10.alGetError();
    if (error != AL10.AL_NO_ERROR) {
      System.out.println("OpenAL error was raised. errorCode="+error);
    }
  }


  /**
   * Creates an integer buffer to hold specified ints
   * - strictly a utility method
   *
   * @param size how many int to contain
   * @return created IntBuffer
   */
  protected static IntBuffer createIntBuffer(int size) {
    ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
    temp.order(ByteOrder.nativeOrder());
    return temp.asIntBuffer();
  }
}
