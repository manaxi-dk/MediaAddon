package dk.manaxi.core.ogghelper;

import java.util.Arrays;

public class OggUtils {
  public static byte[] joinOggData(byte[] oggData1, byte[] oggData2) {
    // find the page boundary of the first Ogg data
    int boundaryIndex = findPageBoundary(oggData1);

    // if the first Ogg data ends in a page boundary, remove the boundary
    if (boundaryIndex == oggData1.length) {
      oggData1 = Arrays.copyOfRange(oggData1, 0, boundaryIndex - 1);
    }

    // find the page boundary of the second Ogg data
    boundaryIndex = findPageBoundary(oggData2);

    // if the second Ogg data starts with a page boundary, remove the boundary
    if (boundaryIndex == 0) {
      oggData2 = Arrays.copyOfRange(oggData2, 1, oggData2.length);
    }

    // combine the two Ogg data arrays into a single array
    byte[] combinedOggData = new byte[oggData1.length + oggData2.length];
    System.arraycopy(oggData1, 0, combinedOggData, 0, oggData1.length);
    System.arraycopy(oggData2, 0, combinedOggData, oggData1.length, oggData2.length);

    // return the combined Ogg data
    return combinedOggData;
  }

  /**
   * Finds the index of the page boundary in the Ogg data.
   *
   * @param oggData the Ogg data
   * @return the index of the page boundary
   */
  private static int findPageBoundary(byte[] oggData) {
    for (int i = 0; i < oggData.length - 3; i++) {
      if (oggData[i] == 'O' && oggData[i + 1] == 'g' && oggData[i + 2] == 'g' && oggData[i + 3] == 'S') {
        return i;
      }
    }
    return oggData.length;
  }
}