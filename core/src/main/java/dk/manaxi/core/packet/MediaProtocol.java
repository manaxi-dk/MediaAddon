package dk.manaxi.core.packet;

import net.labymod.serverapi.protocol.packet.protocol.AddonProtocol;
import net.labymod.serverapi.protocol.packet.protocol.Protocol;
import net.labymod.serverapi.protocol.payload.identifier.PayloadChannelIdentifier;

public class MediaProtocol extends Protocol {
  public MediaProtocol() {
    super(PayloadChannelIdentifier.create("labymod3", "media"));

    this.registerPacket(0, new PlaySoundPacket());
    this.registerPacket(1, new AddSoundPacket());
  }
}
