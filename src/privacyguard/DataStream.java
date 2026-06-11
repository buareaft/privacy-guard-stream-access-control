package privacyguard;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataStream {
    private final String id;
    private final String device;
    private final String name;
    private final String description;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final List<DataRecord> records;

    public DataStream(String device, String name, String description, LocalDateTime startTime, LocalDateTime endTime) {
        this.id = UUID.randomUUID().toString().substring(0, 8);
        this.device = device;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.records = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getDevice() {
        return device;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public List<DataRecord> getRecords() {
        return records;
    }

    @Override
    public String toString() {
        return name + " [" + id + "]";
    }
}
