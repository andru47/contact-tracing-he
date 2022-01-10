package dissertation.backend;

import java.io.File;

public class JNIBridge {
  static {
    try {
      File libraryFile = new File(System.getProperty("user.dir") + "/../he-component/bridge/jni/" + System.mapLibraryName("HELib"));
      System.load(libraryFile.getAbsolutePath());
    } catch (Error | Exception e) {
      e.printStackTrace();
    }
  }

  public native char[] getDistance(char[][] location1, char[][] location2);

  public native CiphertextWrapper getMultiKeyDistance(char[][] location1, char[][] location2, char[] pubKey1, char[] relinKey1,
                                            char[] pubKey2, char[] relinKey2);

  public native char[] getAltitudeDifference(char[] altitude1, char[] altitude2);

  public native CiphertextWrapper getMultiKeyAltitudeDifference(char[] altitude1, char[] altitude2, char[] pubKey1, char[] relinKey1,
                                                     char[] pubKey2, char[] relinKey2);
}
