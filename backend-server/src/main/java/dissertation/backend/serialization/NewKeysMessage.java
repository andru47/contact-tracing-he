package dissertation.backend.serialization;

public class NewKeysMessage {
    private String pubKey, relinKey, userId;

    public String getRelinKey() {
        return relinKey;
    }

    public String getPubKey() {
        return pubKey;
    }

    public String getUserId() {
        return userId;
    }
}
