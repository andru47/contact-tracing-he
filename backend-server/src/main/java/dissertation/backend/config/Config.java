package dissertation.backend.config;

public class Config {
  private static final EncryptionType encryptionType = EncryptionType.SINGLE;
  private static final boolean timingEnabled = true;

  public static EncryptionType getEncryptionType() {
    return encryptionType;
  }

  public static boolean isTimingEnabled() {
    return timingEnabled;
  }
}
