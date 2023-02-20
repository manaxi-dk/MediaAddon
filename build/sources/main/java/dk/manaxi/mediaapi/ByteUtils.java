package dk.manaxi.mediaapi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class ByteUtils {
    public static byte[] compressBytes(byte[] uncompressedData) {
        // Create the compressor with highest level of compression
        Deflater compressor = new Deflater();
        compressor.setLevel(Deflater.BEST_COMPRESSION);

        // Give the compressor the data to compress
        compressor.setInput(uncompressedData);
        compressor.finish();

        // Create an expandable byte array to hold the compressed data.
        // You cannot use an array that's the same size as the orginal because
        // there is no guarantee that the compressed data will be smaller than
        // the uncompressed data.
        ByteArrayOutputStream bos = new ByteArrayOutputStream(
                uncompressedData.length);

        // Compress the data
        byte[] buf = new byte[1024];
        while (!compressor.finished()) {
            int count = compressor.deflate(buf);
            bos.write(buf, 0, count);
        }
        try {
            bos.close();
        } catch (IOException e) {
        }

        // Get the compressed data
        byte[] compressedData = bos.toByteArray();
        return compressedData;
    }

    public static byte[] decompressBytes(byte[] compressedData) {
        Inflater decompressor = new Inflater();
        decompressor.setInput(compressedData);

        ByteArrayOutputStream bos = new ByteArrayOutputStream(compressedData.length);

        byte[] buf = new byte[1024];
        while (!decompressor.finished()) {
            try {
                int count = decompressor.inflate(buf);
                bos.write(buf, 0, count);
            } catch (DataFormatException e) {
                // Handle the exception
            }
        }
        try {
            bos.close();
        } catch (IOException e) {
            // Handle the exception
        }

        byte[] decompressedData = bos.toByteArray();
        return decompressedData;
    }
}