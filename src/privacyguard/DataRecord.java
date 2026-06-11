package privacyguard;

import java.time.LocalDateTime;

public class DataRecord {
    private final LocalDateTime time;
    private final double value;
    private final String cipherText;

    public DataRecord(LocalDateTime time, double value, String cipherText) {
        this.time = time;
        this.value = value;
        this.cipherText = cipherText;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public double getValue() {
        return value;
    }

    public String getCipherText() {
        return cipherText;
    }
}
