package privacyguard;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class PrivacyGuardApp {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Color BACKGROUND = new Color(238, 243, 248);
    private static final Color PRIMARY = new Color(35, 92, 150);
    private static final Color ACCENT = new Color(33, 150, 136);
    private static final Color PANEL = Color.WHITE;

    private final PrivacyGuardService service = new PrivacyGuardService();
    private final JFrame frame = new JFrame("隐私卫士 - 端到端的可定制化流数据密文访问控制系统");
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel root = new JPanel(cardLayout);
    private final JComboBox<DataStream> streamCombo = new JComboBox<>();
    private final JComboBox<DataStream> policyStreamCombo = new JComboBox<>();
    private final JComboBox<DataStream> ownerQueryStreamCombo = new JComboBox<>();
    private final JComboBox<DataStream> consumerStreamCombo = new JComboBox<>();
    private final JList<DataStream> manageStreamList = new JList<>();
    private final JList<PrivacyPolicy> managePolicyList = new JList<>();
    private final JList<PrivacyPolicy> consumerPolicyList = new JList<>();
    private final JTextArea controllerLogArea = textArea();
    private final JTextArea serverLogArea = textArea();
    private final JTextArea ownerQueryResult = textArea();
    private final JTextArea consumerQueryResult = textArea();
    private final JTextArea federalResult = textArea();

    public void show() {
        setLookAndFeel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(1100, 720));
        frame.setContentPane(root);
        root.add(loginPanel(), "login");
        root.add(mainPanel("数据拥有者"), "owner");
        root.add(mainPanel("数据消费者"), "consumer");
        root.add(mainPanel("隐私控制器"), "controller");
        refreshAll();
        cardLayout.show(root, "login");
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private JPanel loginPanel() {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(BACKGROUND);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(205, 216, 228)),
                new EmptyBorder(32, 40, 32, 40)));

        JLabel title = new JLabel("隐私卫士", SwingConstants.CENTER);
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 34));
        title.setForeground(PRIMARY);
        JLabel subtitle = new JLabel("端到端的可定制化流数据密文访问控制系统", SwingConstants.CENTER);
        subtitle.setForeground(new Color(83, 98, 115));

        JComboBox<String> role = new JComboBox<>(new String[]{"数据拥有者", "数据消费者", "隐私控制器"});
        JTextField username = new JTextField("owner", 18);
        JPasswordField password = new JPasswordField("123456", 18);
        JButton login = primaryButton("登录系统");
        JLabel hint = new JLabel("测试账号：owner / consumer / admin，密码均为 123456");
        hint.setForeground(new Color(96, 112, 128));

        role.addActionListener(event -> {
            String selected = String.valueOf(role.getSelectedItem());
            if ("数据消费者".equals(selected)) {
                username.setText("consumer");
            } else if ("隐私控制器".equals(selected)) {
                username.setText("admin");
            } else {
                username.setText("owner");
            }
        });
        login.addActionListener(event -> {
            String selectedRole = String.valueOf(role.getSelectedItem());
            if (service.login(selectedRole, username.getText(), new String(password.getPassword()))) {
                refreshAll();
                cardLayout.show(root, switch (selectedRole) {
                    case "数据消费者" -> "consumer";
                    case "隐私控制器" -> "controller";
                    default -> "owner";
                });
            } else {
                JOptionPane.showMessageDialog(frame, "账号、密码或角色不匹配。", "登录失败", JOptionPane.WARNING_MESSAGE);
            }
        });

        GridBagConstraints c = gbc();
        c.gridwidth = 2;
        c.insets = new Insets(4, 6, 8, 6);
        panel.add(title, c);
        c.gridy++;
        panel.add(subtitle, c);
        c.gridwidth = 1;
        c.gridy++;
        panel.add(label("角色"), c);
        c.gridx = 1;
        panel.add(role, c);
        c.gridx = 0;
        c.gridy++;
        panel.add(label("账号"), c);
        c.gridx = 1;
        panel.add(username, c);
        c.gridx = 0;
        c.gridy++;
        panel.add(label("密码"), c);
        c.gridx = 1;
        panel.add(password, c);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        panel.add(login, c);
        c.gridy++;
        panel.add(hint, c);

        page.add(panel);
        return page;
    }

    private JPanel mainPanel(String role) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(BACKGROUND);
        page.add(header(role), BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 15));
        if ("数据拥有者".equals(role)) {
            tabs.addTab("基本设置", ownerSettingsPanel());
            tabs.addTab("加密上传", uploadPanel());
            tabs.addTab("隐私策略", policyPanel());
            tabs.addTab("信息管理", managementPanel());
            tabs.addTab("数据查询", ownerQueryPanel());
        } else if ("数据消费者".equals(role)) {
            tabs.addTab("单流数据查询", consumerQueryPanel());
            tabs.addTab("联邦数据查询", federatedPanel());
        } else {
            tabs.addTab("隐私控制器日志", logPanel(controllerLogArea));
            tabs.addTab("数据服务器日志", logPanel(serverLogArea));
        }
        tabs.setBorder(new EmptyBorder(12, 16, 16, 16));
        page.add(tabs, BorderLayout.CENTER);
        return page;
    }

    private JPanel header(String role) {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(PRIMARY);
        header.setBorder(new EmptyBorder(16, 22, 16, 22));
        JLabel title = new JLabel("隐私卫士");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 24));
        JLabel right = new JLabel(role + " 工作台");
        right.setForeground(new Color(224, 238, 248));
        right.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 16));
        JButton back = new JButton("退出登录");
        back.addActionListener(event -> cardLayout.show(root, "login"));
        header.add(title, BorderLayout.WEST);
        JPanel side = new JPanel();
        side.setOpaque(false);
        side.add(right);
        side.add(back);
        header.add(side, BorderLayout.EAST);
        return header;
    }

    private JPanel ownerSettingsPanel() {
        JPanel panel = formPanel();
        JTextField device = new JTextField("Sensor-C09");
        JTextField name = new JTextField("血氧监测流");
        JTextField desc = new JTextField("医疗设备实时采集血氧变化");
        JTextField start = new JTextField(FORMATTER.format(LocalDateTime.now().minusHours(3).withSecond(0).withNano(0)));
        JTextField end = new JTextField(FORMATTER.format(LocalDateTime.now().withSecond(0).withNano(0)));
        JButton create = primaryButton("创建数据流");

        addRow(panel, 0, "绑定数据生产者", device);
        addRow(panel, 1, "流名称", name);
        addRow(panel, 2, "描述", desc);
        addRow(panel, 3, "开始时间", start);
        addRow(panel, 4, "结束时间", end);
        addWide(panel, 5, create);

        create.addActionListener(event -> {
            try {
                DataStream stream = service.createStream(device.getText(), name.getText(), desc.getText(),
                        parse(start.getText()), parse(end.getText()));
                refreshAll();
                JOptionPane.showMessageDialog(frame, "数据流创建成功：" + stream.getName());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(frame, "请输入正确时间格式：yyyy-MM-dd HH:mm", "创建失败", JOptionPane.WARNING_MESSAGE);
            }
        });
        return wrap(panel);
    }

    private JPanel uploadPanel() {
        JPanel panel = formPanel();
        JProgressBar progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        JTextArea status = textArea();
        JSpinner count = new JSpinner(new SpinnerNumberModel(36, 6, 200, 6));
        JButton upload = primaryButton("加密并上传");

        addRow(panel, 0, "选择数据流", streamCombo);
        addRow(panel, 1, "生成数据块数量", count);
        addWide(panel, 2, progressBar);
        addWide(panel, 3, upload);
        addWide(panel, 4, new JScrollPane(status));

        upload.addActionListener(event -> {
            DataStream stream = (DataStream) streamCombo.getSelectedItem();
            if (stream == null) {
                return;
            }
            upload.setEnabled(false);
            status.setText("正在为 " + stream.getName() + " 生成明文数据、执行轻量密文封装并上传至数据服务器...\n");
            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (int i = 0; i <= 100; i += 5) {
                        publish(i);
                        Thread.sleep(45);
                    }
                    service.generateEncryptedRecords(stream, (Integer) count.getValue());
                    return null;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    int value = chunks.get(chunks.size() - 1);
                    progressBar.setValue(value);
                }

                @Override
                protected void done() {
                    upload.setEnabled(true);
                    refreshAll();
                    status.append("上传完成：密文数据已写入模拟 Kafka，服务器仅保存密文摘要。\n");
                }
            };
            worker.execute();
        });
        return wrap(panel);
    }

    private JPanel policyPanel() {
        JPanel panel = formPanel();
        JTextField consumer = new JTextField("consumer");
        JTextField start = new JTextField(FORMATTER.format(LocalDateTime.now().minusHours(3).withSecond(0).withNano(0)));
        JTextField end = new JTextField(FORMATTER.format(LocalDateTime.now().minusHours(1).withSecond(0).withNano(0)));
        JSpinner granularity = new JSpinner(new SpinnerNumberModel(4, 1, 24, 1));
        JCheckBox federated = new JCheckBox("允许参与联邦计算", true);
        JButton save = primaryButton("制定隐私策略");

        addRow(panel, 0, "选择数据流", policyStreamCombo);
        addRow(panel, 1, "允许访问消费者", consumer);
        addRow(panel, 2, "允许开始时间", start);
        addRow(panel, 3, "允许结束时间", end);
        addRow(panel, 4, "粒度倍数", granularity);
        addRow(panel, 5, "联邦策略", federated);
        addWide(panel, 6, save);

        save.addActionListener(event -> {
            DataStream stream = (DataStream) policyStreamCombo.getSelectedItem();
            if (stream == null) {
                return;
            }
            try {
                PrivacyPolicy policy = service.addPolicy(stream, consumer.getText(), parse(start.getText()), parse(end.getText()),
                        (Integer) granularity.getValue(), federated.isSelected());
                refreshAll();
                JOptionPane.showMessageDialog(frame, "隐私策略设置完成，访问令牌：" + policy.getId());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(frame, "请输入正确时间格式：yyyy-MM-dd HH:mm", "保存失败", JOptionPane.WARNING_MESSAGE);
            }
        });
        return wrap(panel);
    }

    private JPanel managementPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 14, 14));
        panel.setBackground(BACKGROUND);
        manageStreamList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        managePolicyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JButton delStream = dangerButton("删除选中数据流");
        JButton delPolicy = dangerButton("撤销选中隐私策略");
        delStream.addActionListener(event -> {
            DataStream stream = manageStreamList.getSelectedValue();
            if (stream != null && confirm("删除数据流会同时删除关联隐私策略，是否继续？")) {
                service.deleteStream(stream);
                refreshAll();
            }
        });
        delPolicy.addActionListener(event -> {
            PrivacyPolicy policy = managePolicyList.getSelectedValue();
            if (policy != null && confirm("确认撤销该消费者访问权限？")) {
                service.deletePolicy(policy);
                refreshAll();
            }
        });
        panel.add(listBlock("数据流信息", manageStreamList, delStream));
        panel.add(listBlock("隐私策略信息", managePolicyList, delPolicy));
        return panel;
    }

    private JPanel ownerQueryPanel() {
        return queryPanel(ownerQueryStreamCombo, ownerQueryResult, true);
    }

    private JPanel consumerQueryPanel() {
        JPanel page = new JPanel(new BorderLayout(12, 12));
        page.setBackground(BACKGROUND);
        page.add(listBlock("当前可访问隐私策略", consumerPolicyList, null), BorderLayout.WEST);
        page.add(queryPanel(consumerStreamCombo, consumerQueryResult, false), BorderLayout.CENTER);
        return page;
    }

    private JPanel queryPanel(JComboBox<DataStream> streams, JTextArea output, boolean owner) {
        JPanel panel = formPanel();
        JTextField start = new JTextField(FORMATTER.format(LocalDateTime.now().minusHours(3).withSecond(0).withNano(0)));
        JTextField end = new JTextField(FORMATTER.format(LocalDateTime.now().minusHours(1).withSecond(0).withNano(0)));
        JSpinner granularity = new JSpinner(new SpinnerNumberModel(4, 1, 24, 1));
        JButton blocks = primaryButton("查询数据块");
        JButton stats = secondaryButton("查询统计信息");

        addRow(panel, 0, "选择数据流", streams);
        addRow(panel, 1, "开始时间", start);
        addRow(panel, 2, "结束时间", end);
        addRow(panel, 3, "粒度倍数", granularity);
        JPanel buttons = new JPanel();
        buttons.setOpaque(false);
        buttons.add(blocks);
        buttons.add(stats);
        addWide(panel, 4, buttons);
        addWide(panel, 5, new JScrollPane(output));

        blocks.addActionListener(event -> runQuery(streams, output, owner, start.getText(), end.getText(), (Integer) granularity.getValue(), false));
        stats.addActionListener(event -> runQuery(streams, output, owner, start.getText(), end.getText(), (Integer) granularity.getValue(), true));
        return wrap(panel);
    }

    private JPanel federatedPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(BACKGROUND);
        JButton query = primaryButton("执行联邦统计");
        query.addActionListener(event -> {
            federalResult.setText(service.federatedStats("consumer"));
            refreshLogs();
        });
        panel.add(query, BorderLayout.NORTH);
        panel.add(new JScrollPane(federalResult), BorderLayout.CENTER);
        return panel;
    }

    private JPanel logPanel(JTextArea area) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        JButton refresh = secondaryButton("刷新日志");
        refresh.addActionListener(event -> refreshLogs());
        panel.add(refresh, BorderLayout.NORTH);
        panel.add(new JScrollPane(area), BorderLayout.CENTER);
        return panel;
    }

    private void runQuery(JComboBox<DataStream> streams, JTextArea output, boolean owner,
                          String startText, String endText, int granularity, boolean stats) {
        DataStream stream = (DataStream) streams.getSelectedItem();
        if (stream == null) {
            return;
        }
        try {
            List<DataRecord> records = owner
                    ? service.queryOwnerRecords(stream, parse(startText), parse(endText))
                    : service.queryConsumerRecords("consumer", stream, parse(startText), parse(endText));
            output.setText(stats ? service.buildStats(records, granularity) : service.buildRecordTable(records));
            refreshLogs();
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(frame, "请输入正确时间格式：yyyy-MM-dd HH:mm", "查询失败", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void refreshAll() {
        refreshCombo(streamCombo);
        refreshCombo(policyStreamCombo);
        refreshCombo(ownerQueryStreamCombo);
        refreshCombo(consumerStreamCombo);
        manageStreamList.setListData(service.getStreams().toArray(new DataStream[0]));
        managePolicyList.setListData(service.getPolicies().toArray(new PrivacyPolicy[0]));
        consumerPolicyList.setListData(service.policiesFor("consumer", false).toArray(new PrivacyPolicy[0]));
        refreshLogs();
    }

    private void refreshCombo(JComboBox<DataStream> combo) {
        combo.setModel(new DefaultComboBoxModel<>(service.getStreams().toArray(new DataStream[0])));
    }

    private void refreshLogs() {
        controllerLogArea.setText(String.join("\n", service.getControllerLogs()));
        serverLogArea.setText(String.join("\n", service.getServerLogs()));
    }

    private JPanel listBlock(String title, JList<?> list, JButton action) {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 230)),
                new EmptyBorder(12, 12, 12, 12)));
        JLabel label = new JLabel(title);
        label.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 16));
        panel.add(label, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        if (action != null) {
            panel.add(action, BorderLayout.SOUTH);
        }
        return panel;
    }

    private JPanel formPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 220, 230)),
                new EmptyBorder(22, 28, 22, 28)));
        return panel;
    }

    private JPanel wrap(Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BACKGROUND);
        panel.add(component, BorderLayout.NORTH);
        return panel;
    }

    private void addRow(JPanel panel, int row, String name, Component field) {
        GridBagConstraints c = gbc();
        c.gridy = row;
        c.anchor = GridBagConstraints.WEST;
        panel.add(label(name), c);
        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    private void addWide(JPanel panel, int row, Component component) {
        GridBagConstraints c = gbc();
        c.gridy = row;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(component, c);
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Microsoft YaHei UI", Font.PLAIN, 14));
        return label;
    }

    private JButton primaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(PRIMARY);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        button.setFont(new Font("Microsoft YaHei UI", Font.BOLD, 14));
        return button;
    }

    private JButton secondaryButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(ACCENT);
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        return button;
    }

    private JButton dangerButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(new Color(190, 70, 70));
        button.setForeground(Color.BLACK);
        button.setFocusPainted(false);
        return button;
    }

    private JTextArea textArea() {
        JTextArea area = new JTextArea(13, 72);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(7, 7, 7, 7);
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private LocalDateTime parse(String text) {
        return LocalDateTime.parse(text.trim(), FORMATTER);
    }

    private boolean confirm(String text) {
        return JOptionPane.showConfirmDialog(frame, text, "确认操作", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        Timer timer = new Timer(1, event -> frame.repaint());
        timer.setRepeats(false);
        timer.start();
    }
}
