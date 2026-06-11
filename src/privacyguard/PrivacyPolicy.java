package privacyguard;

import java.time.LocalDateTime;
import java.util.UUID;

public class PrivacyPolicy {
    private final String id;
    private final DataStream stream;
    private final String consumer;
    private final LocalDateTime allowStart;
    private final LocalDateTime allowEnd;
    private final int granularity;
    private final boolean federated;

    public PrivacyPolicy(DataStream stream, String consumer, LocalDateTime allowStart,
                         LocalDateTime allowEnd, int granularity, boolean federated) {
        this.id = "POL-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        this.stream = stream;
        this.consumer = consumer;
        this.allowStart = allowStart;
        this.allowEnd = allowEnd;
        this.granularity = granularity;
        this.federated = federated;
    }

    public String getId() {
        return id;
    }

    public DataStream getStream() {
        return stream;
    }

    public String getConsumer() {
        return consumer;
    }

    public LocalDateTime getAllowStart() {
        return allowStart;
    }

    public LocalDateTime getAllowEnd() {
        return allowEnd;
    }

    public int getGranularity() {
        return granularity;
    }

    public boolean isFederated() {
        return federated;
    }

    @Override
    public String toString() {
        return id + " | " + stream.getName() + " -> " + consumer
                + " | 粒度 " + granularity
                + (federated ? " | 联邦" : "");
    }
}
