package privacyguard;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class PrivacyGuardService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final List<DataStream> streams = new ArrayList<>();
    private final List<PrivacyPolicy> policies = new ArrayList<>();
    private final List<String> controllerLogs = new ArrayList<>();
    private final List<String> serverLogs = new ArrayList<>();
    private final Random random = new Random(7);

    public PrivacyGuardService() {
        seed();
    }

    public boolean login(String role, String username, String password) {
        boolean owner = "数据拥有者".equals(role) && "owner".equals(username) && "123456".equals(password);
        boolean consumer = "数据消费者".equals(role) && "consumer".equals(username) && "123456".equals(password);
        boolean controller = "隐私控制器".equals(role) && "admin".equals(username) && "123456".equals(password);
        if (owner || consumer || controller) {
            controllerLogs.add(stamp("用户 " + username + " 以 " + role + " 身份登录成功"));
            return true;
        }
        return false;
    }

    public DataStream createStream(String device, String name, String description,
                                   LocalDateTime startTime, LocalDateTime endTime) {
        DataStream stream = new DataStream(device, name, description, startTime, endTime);
        streams.add(stream);
        controllerLogs.add(stamp("创建数据流 " + stream.getName() + "，绑定设备 " + device));
        return stream;
    }

    public void generateEncryptedRecords(DataStream stream, int count) {
        stream.getRecords().clear();
        long minutes = Math.max(1, java.time.Duration.between(stream.getStartTime(), stream.getEndTime()).toMinutes());
        long step = Math.max(1, minutes / Math.max(1, count - 1));
        for (int i = 0; i < count; i++) {
            LocalDateTime time = stream.getStartTime().plusMinutes(step * i);
            if (time.isAfter(stream.getEndTime())) {
                time = stream.getEndTime();
            }
            double value = 18 + random.nextDouble() * 12 + Math.sin(i / 3.0) * 8;
            String plain = stream.getId() + "|" + FORMATTER.format(time) + "|" + String.format(Locale.US, "%.2f", value);
            stream.getRecords().add(new DataRecord(time, value, CryptoUtil.encrypt(plain)));
        }
        serverLogs.add(stamp("Netty 接收密文流 " + stream.getName() + "，写入 Kafka Topic secure_stream_" + stream.getId()));
        serverLogs.add(stamp("Kafka 已保存 " + count + " 条密文数据块"));
    }

    public PrivacyPolicy addPolicy(DataStream stream, String consumer, LocalDateTime start,
                                   LocalDateTime end, int granularity, boolean federated) {
        PrivacyPolicy policy = new PrivacyPolicy(stream, consumer, start, end, granularity, federated);
        policies.add(policy);
        controllerLogs.add(stamp("生成访问令牌 " + policy.getId() + "，授权 " + consumer + " 访问 " + stream.getName()));
        return policy;
    }

    public void deleteStream(DataStream stream) {
        streams.remove(stream);
        policies.removeIf(policy -> policy.getStream() == stream);
        controllerLogs.add(stamp("删除数据流 " + stream.getName() + "，关联隐私策略已撤销"));
    }

    public void deletePolicy(PrivacyPolicy policy) {
        policies.remove(policy);
        controllerLogs.add(stamp("撤销隐私策略 " + policy.getId() + "，消费者 " + policy.getConsumer() + " 访问权限失效"));
    }

    public List<DataStream> getStreams() {
        return streams;
    }

    public List<PrivacyPolicy> getPolicies() {
        return policies;
    }

    public List<String> getControllerLogs() {
        return controllerLogs;
    }

    public List<String> getServerLogs() {
        return serverLogs;
    }

    public List<DataRecord> queryOwnerRecords(DataStream stream, LocalDateTime start, LocalDateTime end) {
        return stream.getRecords().stream()
                .filter(record -> !record.getTime().isBefore(start) && !record.getTime().isAfter(end))
                .sorted(Comparator.comparing(DataRecord::getTime))
                .collect(Collectors.toList());
    }

    public List<DataRecord> queryConsumerRecords(String consumer, DataStream stream,
                                                 LocalDateTime start, LocalDateTime end) {
        Optional<PrivacyPolicy> match = policies.stream()
                .filter(policy -> policy.getStream() == stream && policy.getConsumer().equals(consumer))
                .filter(policy -> !start.isBefore(policy.getAllowStart()) && !end.isAfter(policy.getAllowEnd()))
                .findFirst();
        if (match.isEmpty()) {
            controllerLogs.add(stamp("拒绝 " + consumer + " 的越权访问请求：" + stream.getName()));
            return List.of();
        }
        controllerLogs.add(stamp("校验访问令牌 " + match.get().getId() + " 通过，返回解密结果"));
        return queryOwnerRecords(stream, start, end);
    }

    public String buildStats(List<DataRecord> records, int granularity) {
        if (records.isEmpty()) {
            return "没有查询到符合条件的数据，或访问请求超出隐私策略范围。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("分组统计结果（粒度倍数：").append(granularity).append("）\n");
        for (int i = 0; i < records.size(); i += Math.max(1, granularity)) {
            List<DataRecord> group = records.subList(i, Math.min(records.size(), i + Math.max(1, granularity)));
            double avg = group.stream().mapToDouble(DataRecord::getValue).average().orElse(0);
            double min = group.stream().mapToDouble(DataRecord::getValue).min().orElse(0);
            double max = group.stream().mapToDouble(DataRecord::getValue).max().orElse(0);
            builder.append(FORMATTER.format(group.get(0).getTime()))
                    .append(" ~ ")
                    .append(FORMATTER.format(group.get(group.size() - 1).getTime()))
                    .append("  平均值=")
                    .append(String.format(Locale.US, "%.2f", avg))
                    .append("  最小值=")
                    .append(String.format(Locale.US, "%.2f", min))
                    .append("  最大值=")
                    .append(String.format(Locale.US, "%.2f", max))
                    .append('\n');
        }
        return builder.toString();
    }

    public String buildRecordTable(List<DataRecord> records) {
        if (records.isEmpty()) {
            return "没有查询到符合条件的数据，或访问请求超出隐私策略范围。";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("时间                     解密值        密文摘要\n");
        builder.append("--------------------------------------------------------------\n");
        for (DataRecord record : records) {
            builder.append(String.format(Locale.US, "%-20s  %-10.2f  %s...%n",
                    FORMATTER.format(record.getTime()),
                    record.getValue(),
                    record.getCipherText().substring(0, Math.min(22, record.getCipherText().length()))));
        }
        return builder.toString();
    }

    public List<PrivacyPolicy> policiesFor(String consumer, boolean federatedOnly) {
        return policies.stream()
                .filter(policy -> policy.getConsumer().equals(consumer))
                .filter(policy -> !federatedOnly || policy.isFederated())
                .collect(Collectors.toList());
    }

    public String federatedStats(String consumer) {
        List<PrivacyPolicy> allowed = policiesFor(consumer, true);
        if (allowed.size() < 2) {
            return "可参与联邦查询的数据流不足 2 条，请先由数据拥有者设置联邦隐私策略。";
        }
        LocalDateTime start = allowed.stream().map(PrivacyPolicy::getAllowStart).max(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        LocalDateTime end = allowed.stream().map(PrivacyPolicy::getAllowEnd).min(LocalDateTime::compareTo).orElse(LocalDateTime.now());
        if (start.isAfter(end)) {
            return "联邦策略之间不存在共同时间范围。";
        }
        StringBuilder builder = new StringBuilder("联邦数据统计（共同时间范围 ")
                .append(FORMATTER.format(start)).append(" ~ ").append(FORMATTER.format(end)).append("）\n");
        double total = 0;
        int count = 0;
        for (PrivacyPolicy policy : allowed) {
            List<DataRecord> records = queryOwnerRecords(policy.getStream(), start, end);
            double avg = records.stream().mapToDouble(DataRecord::getValue).average().orElse(0);
            total += avg;
            count++;
            builder.append(policy.getStream().getName()).append("：局部平均值 ")
                    .append(String.format(Locale.US, "%.2f", avg)).append('\n');
        }
        builder.append("联邦聚合平均值：").append(String.format(Locale.US, "%.2f", total / Math.max(1, count))).append('\n');
        controllerLogs.add(stamp("完成 " + consumer + " 的联邦统计请求，参与流数量 " + count));
        return builder.toString();
    }

    private void seed() {
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        DataStream heart = createStream("Sensor-A01", "心率监测流", "可穿戴设备实时采集心率数据", now.minusHours(6), now);
        DataStream temp = createStream("Sensor-B12", "环境温度流", "园区传感器采集环境温度", now.minusHours(6), now);
        generateEncryptedRecords(heart, 48);
        generateEncryptedRecords(temp, 48);
        addPolicy(heart, "consumer", now.minusHours(4), now.minusHours(1), 4, true);
        addPolicy(temp, "consumer", now.minusHours(4), now.minusHours(1), 6, true);
    }

    private String stamp(String text) {
        return "[" + FORMATTER.format(LocalDateTime.now()) + "] " + text;
    }
}
