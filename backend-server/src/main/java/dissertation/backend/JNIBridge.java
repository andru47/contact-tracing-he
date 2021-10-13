package dissertation.backend;

public class JNIBridge {
  static {
    try {
      System.loadLibrary("bridge");
    } catch (Error | Exception e) {
      e.printStackTrace();
    }
  }

  public String getGreetingMessage(String name) {
    return getNativeGreetingMessage(name);
  }

  private native String getNativeGreetingMessage(String name);
}
