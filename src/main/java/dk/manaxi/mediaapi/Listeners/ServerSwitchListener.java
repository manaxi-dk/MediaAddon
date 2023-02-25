package dk.manaxi.mediaapi.Listeners;

import com.google.common.base.Charsets;
import com.google.common.primitives.Bytes;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dk.manaxi.mediaapi.Classes.Speaker;
import dk.manaxi.mediaapi.Main;
import dk.manaxi.mediaapi.OggShit.OggUtils;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import net.labymod.api.events.PluginMessageEvent;
//import net.labymod.opus.OpusCodec;
import net.minecraft.network.PacketBuffer;
import org.apache.commons.codec.binary.Base64;
import org.lwjgl.openal.AL10;

import java.applet.AudioClip;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.UUID;

public class ServerSwitchListener implements PluginMessageEvent {
    private static final JsonParser jsonParser = new JsonParser();
    private static byte[] bytes;
    @Override
    public void receiveMessage(String channelName, PacketBuffer packetBuffer) {
        if (channelName.equals("MC|Brand")) {
            Main.getInstance().getSpeakerManager().cleanup();
            Main.getInstance().getPlayerSpeaker().setOggInputStreamQueue(new LinkedList<>());
        }
        if(channelName.equals("labymod3:media")) {
            String messageKey = readStringFromBuffer(32767, packetBuffer);
            String messageContent = readStringFromBuffer(3000000, packetBuffer);
            JsonElement parsedServerMessage = jsonParser.parse(messageContent);
            if(messageKey.equals("sound")) {
                if(!parsedServerMessage.isJsonObject()) return;
                JsonObject jsonObject = parsedServerMessage.getAsJsonObject();
                switch (jsonObject.get("type").getAsString()) {
                    case "play":
                        if(jsonObject.get("data") != null) {
                            System.out.println("spil 1");
                            byte[] backToBytes = Base64.decodeBase64(jsonObject.get("data").getAsString());
                            Main.getInstance().getPlayerSpeaker().addSound(backToBytes, jsonObject.get("id").getAsString());
                        } else {
                            if(bytes == null) return;
                            System.out.println("spil 2");
                            Main.getInstance().getPlayerSpeaker().addSound(bytes, jsonObject.get("id").getAsString());
                            bytes = null;
                        }
                        Main.getInstance().getPlayerSpeaker().play();
                        break;
                    case "addsound":
                        if(jsonObject.get("data") == null) return;
                        byte[] backToBytes = Base64.decodeBase64(jsonObject.get("data").getAsString());
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

    public String readStringFromBuffer(int maxLength, PacketBuffer packetBuffer) {
        int i = this.readVarIntFromBuffer(packetBuffer);
        if (i > maxLength * 4) {
            throw new DecoderException("The received encoded string buffer length is longer than maximum allowed (" + i + " > " + maxLength * 4 + ")");
        } else if (i < 0) {
            throw new DecoderException("The received encoded string buffer length is less than zero! Weird string!");
        } else {
            ByteBuf byteBuf = packetBuffer.readBytes(i);
            byte[] bytes = null;
            if (byteBuf.hasArray()) {
                bytes = byteBuf.array();
            } else {
                bytes = new byte[byteBuf.readableBytes()];
                byteBuf.getBytes(byteBuf.readerIndex(), bytes);
            }

            String s = new String(bytes, Charsets.UTF_8);
            return s;
        }
    }

    public int readVarIntFromBuffer(PacketBuffer packetBuffer) {
        int i = 0;
        int j = 0;

        byte b0;
        do {
            b0 = packetBuffer.readByte();
            i |= (b0 & 127) << j++ * 7;
            if (j > 5) {
                throw new RuntimeException("VarInt too big");
            }
        } while((b0 & 128) == 128);

        return i;
    }


}
