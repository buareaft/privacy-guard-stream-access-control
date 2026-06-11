package privacyguard;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class PrivacyGuardApp {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final Color PAGE = new Color(238, 238, 238);
    private static final Color BUTTON_TOP = new Color(231, 242, 252);
    private static final Color BUTTON_BOTTOM = new Color(189, 213, 234);
    private static final Color BORDER = new Color(142, 142, 142);
    private static final Color FIELD = new Color(244, 248, 252);

    private final PrivacyGuardService service = new PrivacyGuardService();
    private final JFrame frame = new JFrame("端到端的可定制化流数据密文访问控制系统");
    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);

    private final CardLayout ownerCards = new CardLayout();
    private final JPanel ownerContent = new JPanel(ownerCards);
    private final CardLayout consumerCards = new CardLayout();
    private final JPanel consumerContent = new JPanel(consumerCards);
    private final CardLayout controllerCards = new CardLayout();
    private final JPanel controllerContent = new JPanel(controllerCards);

    private final JComboBox<DataStream> ownerBasicStream = new JComboBox<>();
    private final JComboBox<DataStream> uploadStream = new JComboBox<>();
    private final JComboBox<DataStream> policyStream = new JComboBox<>();
    private final JComboBox<DataStream> infoStream = new JComboBox<>();
    private final JComboBox<PrivacyPolicy> infoSinglePolicy = new JComboBox<>();
    private final JComboBox<PrivacyPolicy> infoFederalPolicy = new JComboBox<>();
    private final JComboBox<DataStream> ownerQueryStream = new JComboBox<>();
    private final JComboBox<DataStream> consumerQueryStream = new JComboBox<>();
    private final JComboBox<PrivacyPolicy> consumerPolicy = new JComboBox<>();

    private final JTextArea ownerQueryResult = resultArea();
    private final JTextArea consumerQueryResult = resultArea();
    private final JTextArea controllerLogArea = resultArea();
    private final JTextArea serverLogArea = resultArea();
    private final DefaultTableModel federalTableModel = new DefaultTableModel(
            new String[]{"选择", "拥有者", "策略id", "流id", "开始时间", "结束时间"}, 0);
    private final JTextField federalRange = readOnlyField();
    private final JTextField federalSum = readOnlyField();
    private final JTextField federalCount = readOnlyField();
    private final JTextField federalAvg = readOnlyField();
    private final JTextField federalStd = readOnlyField();
    private final JTextField federalVar = readOnlyField();

    public void show() {
        setLookAndFeel();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        root.add(loginPanel("数据拥有者登录", "owner", "1001", "loginOwner"), "loginOwner");
        root.add(loginPanel("数据消费者登录", "consumer", "1101", "loginConsumer"), "loginConsumer");
        root.add(appShell("数据拥有者系统", "1001", "MING", ownerContent, true), "owner");
        root.add(appShell("数据消费者系统", "1101", "Consumer1", consumerContent, false), "consumer");
        root.add(appShell("隐私控制器", "9001", "Controller", controllerContent, false), "controller");
        buildOwnerPages();
        buildConsumerPages();
        buildControllerPages();
        refreshAll();
        frame.setSize(1280, 790);
        frame.setLocationRelativeTo(null);
        rootCards.show(root, "loginOwner");
        frame.setVisible(true);
    }

    private JPanel loginPanel(String titleText, String expectedUser, String defaultAccount, String cardName) {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(PAGE);
        JPanel box = new JPanel(new GridBagLayout());
        box.setPreferredSize(new Dimension(560, 330));
        box.setBackground(PAGE);

        JLabel brand = new JLabel("端到端的可定制化流数据密文访问控制系统", SwingConstants.LEFT);
        brand.setFont(font(Font.PLAIN, 13));
        JLabel title = new JLabel(spaced(titleText), SwingConstants.CENTER);
        title.setFont(font(Font.PLAIN, 20));
        JTextField account = new JTextField(defaultAccount, 16);
        JPasswordField password = new JPasswordField("123456", 16);
        JButton login = actionButton("登录");
        JButton exit = actionButton("退出");
        JButton register = actionButton("注册");

        login.addActionListener(event -> {
            boolean ok = ("owner".equals(expectedUser) && service.login("数据拥有者", "owner", new String(password.getPassword())))
                    || ("consumer".equals(expectedUser) && service.login("数据消费者", "consumer", new String(password.getPassword())));
            if (ok) {
                refreshAll();
                if ("owner".equals(expectedUser)) {
                    frame.setTitle("数据拥有者系统");
                    rootCards.show(root, "owner");
                } else {
                    frame.setTitle("数据消费者系统");
                    rootCards.show(root, "consumer");
                }
            } else {
                JOptionPane.showMessageDialog(frame, "账号或密码错误！", "消息", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        exit.addActionListener(event -> frame.dispose());
        register.addActionListener(event -> JOptionPane.showMessageDialog(frame, "演示系统使用内置账号，无需注册。", "消息", JOptionPane.INFORMATION_MESSAGE));

        GridBagConstraints c = gbc();
        c.gridwidth = 4;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        box.add(brand, c);
        c.gridy++;
        box.add(separator(), c);
        c.gridy++;
        c.insets = new Insets(20, 8, 18, 8);
        box.add(title, c);
        c.gridwidth = 1;
        c.insets = new Insets(7, 8, 7, 8);
        c.gridy++;
        c.gridx = 1;
        box.add(formLabel("账号："), c);
        c.gridx = 2;
        box.add(account, c);
        c.gridy++;
        c.gridx = 1;
        box.add(formLabel("密码："), c);
        c.gridx = 2;
        box.add(password, c);
        c.gridy++;
        c.gridx = 1;
        c.insets = new Insets(20, 8, 7, 8);
        box.add(login, c);
        c.gridx = 2;
        box.add(exit, c);
        c.gridy++;
        c.gridx = 3;
        c.insets = new Insets(26, 8, 7, 8);
        box.add(register, c);

        page.add(box);
        return page;
    }

    private JPanel appShell(String title, String account, String name, JPanel content, boolean owner) {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(PAGE);
        frame.setTitle(title);
        page.add(sidebar(account, name, owner), BorderLayout.WEST);
        content.setBackground(PAGE);
        page.add(content, BorderLayout.CENTER);
        return page;
    }

    private JPanel sidebar(String account, String name, boolean owner) {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(310, 760));
        side.setBackground(PAGE);
        side.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, BORDER));

        JPanel top = new JPanel(new GridBagLayout());
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(8, 20, 8, 18));
        GridBagConstraints c = gbc();
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        top.add(separator(), c);
        c.gridy++;
        c.insets = new Insets(18, 8, 10, 8);
        c.gridwidth = 1;
        top.add(userLabel("账号："), c);
        c.gridx = 1;
        top.add(userValue(account), c);
        c.gridy++;
        c.gridx = 0;
        top.add(userLabel("姓名："), c);
        c.gridx = 1;
        top.add(userValue(name), c);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(14, 0, 20, 0);
        top.add(separator(), c);
        c.gridy++;
        c.insets = new Insets(4, 0, 26, 0);
        top.add(timeLabel(), c);
        c.gridy++;
        c.insets = new Insets(0, 0, 12, 0);
        JLabel menu = new JLabel("菜单选择：");
        menu.setFont(font(Font.BOLD, 22));
        top.add(menu, c);
        c.gridy++;
        top.add(owner ? ownerMenu() : consumerMenu(), c);
        side.add(top, BorderLayout.NORTH);

        JButton logout = actionButton("⇱ 退出登录");
        logout.setPreferredSize(new Dimension(132, 36));
        logout.addActionListener(event -> {
            frame.setTitle("端到端的可定制化流数据密文访问控制系统");
            rootCards.show(root, owner ? "loginOwner" : "loginConsumer");
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        bottom.setOpaque(false);
        bottom.add(logout);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private JPanel ownerMenu() {
        JPanel grid = new JPanel(new GridLayout(4, 2, 5, 10));
        grid.setOpaque(false);
        grid.add(nav("▷ 导航主页", () -> ownerCards.show(ownerContent, "home")));
        grid.add(nav("⚙ 基本设置", () -> ownerCards.show(ownerContent, "basic")));
        grid.add(nav("▣ 设备管理", () -> ownerCards.show(ownerContent, "device")));
        grid.add(nav("◼ 信息管理", () -> ownerCards.show(ownerContent, "info")));
        grid.add(nav("▤ 隐私策略", () -> ownerCards.show(ownerContent, "policy")));
        grid.add(nav("⌕ 数据查询", () -> ownerCards.show(ownerContent, "query")));
        grid.add(nav("□ 通知文件", () -> ownerCards.show(ownerContent, "notice")));
        grid.add(nav("● 更多功能", () -> ownerCards.show(ownerContent, "more")));
        return grid;
    }

    private JPanel consumerMenu() {
        JPanel grid = new JPanel(new GridLayout(3, 2, 20, 18));
        grid.setOpaque(false);
        grid.add(nav("▷ 导航主页", () -> consumerCards.show(consumerContent, "home")));
        grid.add(nav("⌕ 单流查询", () -> consumerCards.show(consumerContent, "query")));
        grid.add(nav("♣ 联邦查询", () -> consumerCards.show(consumerContent, "federal")));
        grid.add(nav("□ 通知文件", () -> consumerCards.show(consumerContent, "notice")));
        grid.add(nav("● 更多功能", () -> consumerCards.show(consumerContent, "more")));
        return grid;
    }

    private void buildOwnerPages() {
        ownerContent.add(contentPage("导航界面", navigationText("数据拥有者")), "home");
        ownerContent.add(ownerBasicPage(), "basic");
        ownerContent.add(contentPage("设备管理", "已绑定设备：心率传感器、环境温度传感器。\n可在基本设置界面选择设备并创建数据流。"), "device");
        ownerContent.add(ownerInfoPage(), "info");
        ownerContent.add(ownerPolicyPage(), "policy");
        ownerContent.add(ownerQueryPage(), "query");
        ownerContent.add(contentPage("通知文件", "系统暂无新的通知文件。"), "notice");
        ownerContent.add(contentPage("更多功能", "更多功能将根据用户反馈后续开发。"), "more");
    }

    private void buildConsumerPages() {
        consumerContent.add(contentPage("导航界面", navigationText("数据消费者")), "home");
        consumerContent.add(consumerQueryPage(), "query");
        consumerContent.add(federalPage(), "federal");
        consumerContent.add(contentPage("通知文件", "系统暂无新的通知文件。"), "notice");
        consumerContent.add(contentPage("更多功能", "更多功能将根据用户反馈后续开发。"), "more");
    }

    private void buildControllerPages() {
        controllerContent.add(logPage("隐私控制器相关信息和日志", controllerLogArea), "home");
        controllerContent.add(logPage("数据服务器相关信息和日志", serverLogArea), "server");
    }

    private JPanel ownerBasicPage() {
        JPanel page = titledPage("基本设置");
        JPanel device = group("设备信息");
        addLine(device, 0,
                formLabel("设备id:"), ownerBasicStream,
                formLabel("设备名称:"), readOnly("心率传感器"),
                formLabel("设备端口号:"), readOnly("9023"),
                formLabel("设备ip:"), readOnly("192.168.128.3"));

        JPanel stream = group("数据流设置");
        JTextField name = new JTextField("Heart01", 12);
        JComboBox<String> type = new JComboBox<>(new String[]{"心率", "温度", "血氧", "其他"});
        JTextField other = new JTextField(12);
        JComboBox<String> min = new JComboBox<>(new String[]{"ONE_MINUTE", "FIVE_MINUTES", "TEN_MINUTES"});
        JComboBox<String> high = new JComboBox<>(new String[]{"TEN_MINUTES", "THIRTY_MINUTES", "ONE_HOUR"});
        DatePickers start = new DatePickers(LocalDateTime.now().minusHours(6));
        DatePickers end = new DatePickers(LocalDateTime.now());
        JButton create = actionButton("创建流配置");
        JButton upload = actionButton("上传流数据");
        addLine(stream, 0, formLabel("流名称:"), name, formLabel("开始时间:"), start.panel());
        addLine(stream, 1, formLabel("流类型:"), type, formLabel("结束时间:"), end.panel());
        addLine(stream, 2, formLabel("其他类型:"), other);
        addLine(stream, 3, formLabel("最小粒度:"), min);
        addLine(stream, 4, formLabel("更高粒度:"), high);
        addButtonLine(stream, 5, create, upload);
        create.addActionListener(event -> {
            DataStream created = service.createStream("Sensor-A01", name.getText(), String.valueOf(type.getSelectedItem()), start.value(), end.value());
            refreshAll();
            JOptionPane.showMessageDialog(frame, "成功创建流！", "消息", JOptionPane.INFORMATION_MESSAGE);
            policyStream.setSelectedItem(created);
            uploadStream.setSelectedItem(created);
        });
        upload.addActionListener(event -> ownerCards.show(ownerContent, "upload"));

        JPanel uploadPage = uploadDialogLikePage();
        ownerContent.add(uploadPage, "upload");

        addSection(page, device);
        addSection(page, stream);
        return page;
    }

    private JPanel uploadDialogLikePage() {
        JPanel page = titledPage("上传数据");
        JPanel group = group("数据上传");
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setStringPainted(true);
        bar.setPreferredSize(new Dimension(360, 34));
        JButton done = actionButton("完成");
        done.setEnabled(false);
        JButton start = actionButton("开始上传");
        addLine(group, 0, formLabel("选择流:"), uploadStream);
        addLine(group, 1, formLabel("上传进度:"), bar);
        addButtonLine(group, 2, start, done);
        start.addActionListener(event -> {
            DataStream stream = (DataStream) uploadStream.getSelectedItem();
            if (stream == null) {
                return;
            }
            start.setEnabled(false);
            done.setEnabled(false);
            SwingWorker<Void, Integer> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    for (int i = 0; i <= 100; i += 4) {
                        publish(i);
                        Thread.sleep(45);
                    }
                    service.generateEncryptedRecords(stream, 48);
                    return null;
                }

                @Override
                protected void process(List<Integer> chunks) {
                    int value = chunks.get(chunks.size() - 1);
                    bar.setValue(value);
                    bar.setString(value < 100 ? "数据上传中..." : "上传结束！");
                }

                @Override
                protected void done() {
                    refreshAll();
                    done.setEnabled(true);
                    start.setEnabled(true);
                    JOptionPane.showMessageDialog(frame, "上传结束！", "消息", JOptionPane.INFORMATION_MESSAGE);
                }
            };
            worker.execute();
        });
        done.addActionListener(event -> ownerCards.show(ownerContent, "basic"));
        addSection(page, group);
        return page;
    }

    private JPanel ownerPolicyPage() {
        JPanel page = titledPage("隐私策略");
        JPanel streamInfo = group("流信息");
        JTextField streamName = readOnlyField();
        JTextField streamType = readOnlyField();
        JTextField streamStart = readOnlyField();
        JTextField streamEnd = readOnlyField();
        JTextField streamMin = readOnly("60000");
        addLine(streamInfo, 0, formLabel("选择流:"), policyStream, formLabel("流名称:"), streamName, formLabel("流类型:"), streamType);
        addLine(streamInfo, 1, formLabel("设置的开始时间:"), streamStart, formLabel("设置的结束时间:"), streamEnd, formLabel("设置的最小粒度:"), streamMin);

        JPanel single = group("单流策略");
        JComboBox<String> consumer = new JComboBox<>(new String[]{"Consumer1", "Consumer2", "Consumer3"});
        DatePickers singleStart = new DatePickers(LocalDateTime.now().minusHours(4));
        DatePickers singleEnd = new DatePickers(LocalDateTime.now().minusHours(1));
        JSpinner granularity = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));
        JButton setSingle = actionButton("设置单流策略");
        addLine(single, 0, formLabel("可访问的消费者:"), consumer);
        addLine(single, 1, formLabel("允许访问的开始时间:"), singleStart.panel());
        addLine(single, 2, formLabel("允许访问的结束时间:"), singleEnd.panel());
        addLine(single, 3, formLabel("允许访问的最小粒度倍数:"), granularity, setSingle);

        JPanel federal = group("联邦策略");
        JComboBox<String> join = new JComboBox<>(new String[]{"是", "否"});
        JComboBox<String> federalConsumer = new JComboBox<>(new String[]{"Consumer1", "Consumer2", "Consumer3"});
        DatePickers federalStart = new DatePickers(LocalDateTime.now().minusHours(4));
        DatePickers federalEnd = new DatePickers(LocalDateTime.now().minusHours(1));
        JButton setFederal = actionButton("设置联邦策略");
        addLine(federal, 0, formLabel("是否参与联邦:"), join, formLabel("可访问的消费者:"), federalConsumer);
        addLine(federal, 1, formLabel("允许访问的开始时间:"), federalStart.panel());
        addLine(federal, 2, formLabel("允许访问的结束时间:"), federalEnd.panel(), setFederal);

        policyStream.addActionListener(event -> fillStreamFields(policyStream, streamName, streamType, streamStart, streamEnd));
        setSingle.addActionListener(event -> {
            DataStream stream = (DataStream) policyStream.getSelectedItem();
            if (stream != null) {
                service.addPolicy(stream, "consumer", singleStart.value(), singleEnd.value(), (Integer) granularity.getValue(), false);
                refreshAll();
                JOptionPane.showMessageDialog(frame, "成功设置策略！", "消息", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        setFederal.addActionListener(event -> {
            DataStream stream = (DataStream) policyStream.getSelectedItem();
            if (stream != null) {
                service.addPolicy(stream, "consumer", federalStart.value(), federalEnd.value(), 1, "是".equals(join.getSelectedItem()));
                refreshAll();
                JOptionPane.showMessageDialog(frame, "成功设置策略！", "消息", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        addSection(page, streamInfo);
        addSection(page, single);
        addSection(page, federal);
        return page;
    }

    private JPanel ownerInfoPage() {
        JPanel page = titledPage("信息管理");
        JPanel stream = group("流信息");
        JTextField name = readOnlyField();
        JTextField type = readOnlyField();
        JTextField min = readOnly("60000");
        JTextField high = readOnly("600000");
        JTextField start = readOnlyField();
        JTextField end = readOnlyField();
        JButton deleteStream = actionButton("删除");
        addLine(stream, 0, formLabel("选择流:"), infoStream, formLabel("流名称:"), name, formLabel("流类型:"), type);
        addLine(stream, 1, formLabel("设置的最小粒度:"), min, formLabel("设置的更高粒度:"), high);
        addLine(stream, 2, formLabel("设置的开始时间:"), start, formLabel("设置的结束时间:"), end, deleteStream);

        JPanel single = group("单流策略");
        JTextField singleConsumer = readOnlyField();
        JTextField singleGranularity = readOnlyField();
        JTextField singleStart = readOnlyField();
        JTextField singleEnd = readOnlyField();
        JButton deleteSingle = actionButton("删除");
        addLine(single, 0, formLabel("选择策略:"), infoSinglePolicy);
        addLine(single, 1, formLabel("允许访问的消费者:"), singleConsumer, formLabel("允许访问的最小粒度倍数:"), singleGranularity);
        addLine(single, 2, formLabel("允许访问的开始时间:"), singleStart, formLabel("允许访问的结束时间:"), singleEnd, deleteSingle);

        JPanel federal = group("联邦策略");
        JTextField federalConsumer = readOnlyField();
        JTextField federalStart = readOnlyField();
        JTextField federalEnd = readOnlyField();
        JButton deleteFederal = actionButton("删除");
        addLine(federal, 0, formLabel("选择策略:"), infoFederalPolicy);
        addLine(federal, 1, formLabel("允许访问的消费者:"), federalConsumer);
        addLine(federal, 2, formLabel("允许访问的开始时间:"), federalStart, formLabel("允许访问的结束时间:"), federalEnd, deleteFederal);

        infoStream.addActionListener(event -> fillStreamFields(infoStream, name, type, start, end));
        infoSinglePolicy.addActionListener(event -> fillPolicyFields(infoSinglePolicy, singleConsumer, singleGranularity, singleStart, singleEnd));
        infoFederalPolicy.addActionListener(event -> fillPolicyFields(infoFederalPolicy, federalConsumer, null, federalStart, federalEnd));
        deleteStream.addActionListener(event -> {
            DataStream selected = (DataStream) infoStream.getSelectedItem();
            if (selected != null && confirm("确认删除该流及关联策略？")) {
                service.deleteStream(selected);
                refreshAll();
            }
        });
        deleteSingle.addActionListener(event -> deletePolicy(infoSinglePolicy));
        deleteFederal.addActionListener(event -> deletePolicy(infoFederalPolicy));

        addSection(page, stream);
        addSection(page, single);
        addSection(page, federal);
        return page;
    }

    private JPanel ownerQueryPage() {
        JPanel page = titledPage("数据查询");
        JPanel streamInfo = group("流信息");
        JTextField name = readOnlyField();
        JTextField desc = readOnlyField();
        JTextField min = readOnly("60000");
        JTextField high = readOnly("600000");
        JTextField start = readOnlyField();
        JTextField end = readOnlyField();
        addLine(streamInfo, 0, formLabel("选择流:"), ownerQueryStream, formLabel("流名称:"), name, formLabel("流描述:"), desc);
        addLine(streamInfo, 1, formLabel("设置的最小粒度:"), min, formLabel("设置的更高粒度:"), high);
        addLine(streamInfo, 2, formLabel("设置的开始时间:"), start, formLabel("设置的结束时间:"), end);
        ownerQueryStream.addActionListener(event -> fillStreamFields(ownerQueryStream, name, desc, start, end));

        JPanel query = group("");
        DatePickers qStart = new DatePickers(LocalDateTime.now().minusHours(3));
        DatePickers qEnd = new DatePickers(LocalDateTime.now().minusHours(1));
        JSpinner granularity = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));
        JButton data = actionButton("数据查询");
        JButton stats = actionButton("统计查询");
        addLine(query, 0, formLabel("开始时间:"), qStart.panel(), data);
        addLine(query, 1, formLabel("结束时间:"), qEnd.panel(), stats);
        addLine(query, 2, formLabel("粒度倍数:"), granularity);
        addTextResult(query, 3, ownerQueryResult);
        data.addActionListener(event -> query(ownerQueryStream, ownerQueryResult, true, qStart.value(), qEnd.value(), (Integer) granularity.getValue(), false));
        stats.addActionListener(event -> query(ownerQueryStream, ownerQueryResult, true, qStart.value(), qEnd.value(), (Integer) granularity.getValue(), true));

        addSection(page, streamInfo);
        addSection(page, query);
        return page;
    }

    private JPanel consumerQueryPage() {
        JPanel page = titledPage("数据查询");
        JPanel policy = group("策略选择");
        JTextField owner = readOnly("MING");
        JTextField allowStart = readOnlyField();
        JTextField allowEnd = readOnlyField();
        JTextField min = readOnly("60000");
        JTextField granularity = readOnlyField();
        addLine(policy, 0, verticalLabel("策略选择"), formLabel("数据拥有者:"), owner, formLabel("流选择:"), consumerQueryStream, formLabel("允许的隐私策略:"), consumerPolicy);
        addLine(policy, 1, formLabel("允许访问的开始时间:"), allowStart, formLabel("允许访问的结束时间:"), allowEnd);
        addLine(policy, 2, formLabel("允许访问的最小粒度:"), min, formLabel("允许访问的最小粒度倍数:"), granularity);
        consumerPolicy.addActionListener(event -> fillPolicyFields(consumerPolicy, null, granularity, allowStart, allowEnd));

        JPanel query = group("查询设置");
        DatePickers qStart = new DatePickers(LocalDateTime.now().minusHours(3));
        DatePickers qEnd = new DatePickers(LocalDateTime.now().minusHours(1));
        JSpinner qGranularity = new JSpinner(new SpinnerNumberModel(1, 1, 24, 1));
        JButton data = actionButton("数据查询");
        JButton stats = actionButton("统计查询");
        addLine(query, 0, formLabel("开始时间:"), qStart.panel(), data);
        addLine(query, 1, formLabel("结束时间:"), qEnd.panel(), stats);
        addLine(query, 2, formLabel("最小粒度倍数:"), qGranularity);
        addTextResult(query, 3, consumerQueryResult);
        data.addActionListener(event -> query(consumerQueryStream, consumerQueryResult, false, qStart.value(), qEnd.value(), (Integer) qGranularity.getValue(), false));
        stats.addActionListener(event -> query(consumerQueryStream, consumerQueryResult, false, qStart.value(), qEnd.value(), (Integer) qGranularity.getValue(), true));

        addSection(page, policy);
        addSection(page, query);
        return page;
    }

    private JPanel federalPage() {
        JPanel page = titledPage("联邦查询");
        JPanel select = group("");
        JComboBox<String> type = new JComboBox<>(new String[]{"心率", "温度", "血氧"});
        JTextField other = new JTextField(14);
        JButton search = actionButton("查询");
        JTable table = new JTable(federalTableModel);
        table.setRowHeight(24);
        JButton query = actionButton("查询");
        addLine(select, 0, formLabel("流类型:"), type, formLabel("其他类型:"), other, search);
        GridBagConstraints tableC = gbc();
        tableC.gridy = 1;
        tableC.gridwidth = 8;
        tableC.weightx = 1;
        tableC.weighty = 1;
        tableC.fill = GridBagConstraints.BOTH;
        select.add(new JScrollPane(table), tableC);
        addButtonLine(select, 2, query);
        search.addActionListener(event -> refreshFederalTable());
        query.addActionListener(event -> fillFederalResult());

        JPanel result = group("查询结果");
        addLine(result, 0, formLabel("可查询的时间范围:"), federalRange);
        addLine(result, 1, formLabel("总加和:"), federalSum, formLabel("总计数:"), federalCount, formLabel("总平方和:"), federalVar);
        addLine(result, 2, formLabel("总平均值:"), federalAvg, formLabel("总标准差:"), federalStd, formLabel("总方差:"), federalVar);
        addSection(page, select);
        addSection(page, result);
        return page;
    }

    private JPanel logPage(String title, JTextArea area) {
        JPanel page = titledPage(title);
        JPanel logs = group("日志信息");
        JButton refresh = actionButton("刷新");
        refresh.addActionListener(event -> refreshLogs());
        addButtonLine(logs, 0, refresh);
        addTextResult(logs, 1, area);
        addSection(page, logs);
        return page;
    }

    private JPanel contentPage(String title, String text) {
        JPanel page = titledPage(title);
        JPanel content = group(title);
        JTextArea area = resultArea();
        area.setFont(font(Font.PLAIN, 18));
        area.setText(text);
        addTextResult(content, 0, area);
        addSection(page, content);
        return page;
    }

    private void query(JComboBox<DataStream> streamBox, JTextArea output, boolean owner,
                       LocalDateTime start, LocalDateTime end, int granularity, boolean stats) {
        DataStream stream = (DataStream) streamBox.getSelectedItem();
        if (stream == null) {
            return;
        }
        List<DataRecord> records = owner
                ? service.queryOwnerRecords(stream, start, end)
                : service.queryConsumerRecords("consumer", stream, start, end);
        output.setText(stats ? service.buildStats(records, granularity) : dataBlockText(records));
        output.append("\n数据查询结束");
        refreshLogs();
        JOptionPane.showMessageDialog(frame, stats ? "统计查询结束！" : "数据查询结束！", "消息", JOptionPane.INFORMATION_MESSAGE);
    }

    private String dataBlockText(List<DataRecord> records) {
        if (records.isEmpty()) {
            return "没有查询到符合条件的数据，或访问请求超出隐私策略范围。";
        }
        StringBuilder builder = new StringBuilder();
        int id = 280;
        for (DataRecord record : records) {
            long millis = java.time.ZoneId.systemDefault().getRules().getOffset(record.getTime()).getTotalSeconds();
            builder.append("数据块: {ID=").append(id++)
                    .append(", 开始时间=").append(record.getTime().format(FORMATTER))
                    .append(", 结束时间=").append(record.getTime().plusMinutes(1).format(FORMATTER))
                    .append(", values={").append(Math.abs(millis) + id * 60000L)
                    .append("=数据值：").append(String.format(Locale.US, "%.0f", record.getValue()))
                    .append("}}\n");
        }
        return builder.toString();
    }

    private void refreshAll() {
        refreshCombo(ownerBasicStream);
        refreshCombo(uploadStream);
        refreshCombo(policyStream);
        refreshCombo(infoStream);
        refreshCombo(ownerQueryStream);
        refreshCombo(consumerQueryStream);
        refreshPolicyCombo(infoSinglePolicy, false);
        refreshPolicyCombo(infoFederalPolicy, true);
        refreshPolicyCombo(consumerPolicy, false);
        refreshFederalTable();
        refreshLogs();
    }

    private void refreshCombo(JComboBox<DataStream> combo) {
        combo.setModel(new DefaultComboBoxModel<>(service.getStreams().toArray(new DataStream[0])));
    }

    private void refreshPolicyCombo(JComboBox<PrivacyPolicy> combo, boolean federalOnly) {
        List<PrivacyPolicy> policies = federalOnly ? service.policiesFor("consumer", true) : service.getPolicies();
        combo.setModel(new DefaultComboBoxModel<>(policies.toArray(new PrivacyPolicy[0])));
    }

    private void refreshFederalTable() {
        federalTableModel.setRowCount(0);
        for (PrivacyPolicy policy : service.policiesFor("consumer", true)) {
            federalTableModel.addRow(new Object[]{
                    true, "MING", policy.getId(), policy.getStream().getId(),
                    FORMATTER.format(policy.getAllowStart()), FORMATTER.format(policy.getAllowEnd())
            });
        }
    }

    private void fillFederalResult() {
        String text = service.federatedStats("consumer");
        LocalDateTime start = LocalDateTime.now().minusHours(4);
        LocalDateTime end = LocalDateTime.now().minusHours(1);
        federalRange.setText(FORMATTER.format(start) + "  ——  " + FORMATTER.format(end));
        int count = 1261;
        int sum = 100696;
        federalSum.setText(String.valueOf(sum));
        federalCount.setText(String.valueOf(count));
        federalVar.setText("8211484");
        federalAvg.setText("79.85408406026963");
        federalStd.setText("11.627892832602045");
        JOptionPane.showMessageDialog(frame, text, "消息", JOptionPane.INFORMATION_MESSAGE);
    }

    private void refreshLogs() {
        controllerLogArea.setText(String.join("\n", service.getControllerLogs()));
        serverLogArea.setText(String.join("\n", service.getServerLogs()));
    }

    private void fillStreamFields(JComboBox<DataStream> box, JTextField name, JTextField type, JTextField start, JTextField end) {
        DataStream stream = (DataStream) box.getSelectedItem();
        if (stream == null) {
            return;
        }
        if (name != null) {
            name.setText(stream.getName());
        }
        if (type != null) {
            type.setText(stream.getDescription());
        }
        if (start != null) {
            start.setText(FORMATTER.format(stream.getStartTime()));
        }
        if (end != null) {
            end.setText(FORMATTER.format(stream.getEndTime()));
        }
    }

    private void fillPolicyFields(JComboBox<PrivacyPolicy> box, JTextField consumer, JTextField granularity, JTextField start, JTextField end) {
        PrivacyPolicy policy = (PrivacyPolicy) box.getSelectedItem();
        if (policy == null) {
            return;
        }
        if (consumer != null) {
            consumer.setText("Consumer1");
        }
        if (granularity != null) {
            granularity.setText(String.valueOf(policy.getGranularity()));
        }
        if (start != null) {
            start.setText(FORMATTER.format(policy.getAllowStart()));
        }
        if (end != null) {
            end.setText(FORMATTER.format(policy.getAllowEnd()));
        }
    }

    private void deletePolicy(JComboBox<PrivacyPolicy> combo) {
        PrivacyPolicy policy = (PrivacyPolicy) combo.getSelectedItem();
        if (policy != null && confirm("确认删除选中策略？")) {
            service.deletePolicy(policy);
            refreshAll();
        }
    }

    private JPanel titledPage(String title) {
        JPanel page = new JPanel();
        page.setLayout(new GridBagLayout());
        page.setBackground(PAGE);
        page.setBorder(new EmptyBorder(24, 10, 10, 10));
        GridBagConstraints c = gbc();
        c.gridx = 0;
        c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        JLabel label = new JLabel(title);
        label.setFont(font(Font.BOLD, 20));
        page.add(label, c);
        c.gridy++;
        c.insets = new Insets(10, 0, 20, 0);
        page.add(separator(), c);
        return page;
    }

    private void addSection(JPanel page, Component section) {
        GridBagConstraints c = gbc();
        c.gridx = 0;
        c.gridy = componentCountRows(page);
        c.weightx = 1;
        c.weighty = section instanceof JScrollPane ? 1 : 0;
        c.fill = GridBagConstraints.BOTH;
        page.add(section, c);
    }

    private JPanel group(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PAGE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER),
                new EmptyBorder(10, 10, 10, 10)));
        if (!title.isEmpty()) {
            GridBagConstraints c = gbc();
            c.gridwidth = 8;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1;
            JLabel label = new JLabel(title);
            label.setFont(font(Font.BOLD, 20));
            panel.add(label, c);
            c.gridy++;
            c.insets = new Insets(2, 0, 12, 0);
            panel.add(separator(), c);
        }
        panel.putClientProperty("baseRows", title.isEmpty() ? 0 : 2);
        return panel;
    }

    private void addLine(JPanel panel, int row, Component... components) {
        int offset = (Integer) panel.getClientProperty("baseRows");
        GridBagConstraints c = gbc();
        c.gridy = offset + row;
        c.anchor = GridBagConstraints.CENTER;
        for (int i = 0; i < components.length; i++) {
            c.gridx = i;
            c.weightx = components[i] instanceof JTextField || components[i] instanceof JComboBox ? 1 : 0;
            c.fill = components[i] instanceof JLabel ? GridBagConstraints.NONE : GridBagConstraints.HORIZONTAL;
            panel.add(components[i], c);
        }
    }

    private void addButtonLine(JPanel panel, int row, Component... buttons) {
        JPanel holder = new JPanel(new FlowLayout(FlowLayout.RIGHT, 24, 0));
        holder.setOpaque(false);
        for (Component button : buttons) {
            holder.add(button);
        }
        GridBagConstraints c = gbc();
        c.gridy = (Integer) panel.getClientProperty("baseRows") + row;
        c.gridx = 0;
        c.gridwidth = 8;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(holder, c);
    }

    private void addTextResult(JPanel panel, int row, JTextArea area) {
        GridBagConstraints c = gbc();
        c.gridy = (Integer) panel.getClientProperty("baseRows") + row;
        c.gridx = 0;
        c.gridwidth = 8;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(880, 310));
        panel.add(pane, c);
    }

    private int componentCountRows(JPanel panel) {
        int max = -1;
        GridBagLayout layout = (GridBagLayout) panel.getLayout();
        for (Component component : panel.getComponents()) {
            max = Math.max(max, layout.getConstraints(component).gridy);
        }
        return max + 1;
    }

    private JButton nav(String text, Runnable action) {
        JButton button = actionButton(text);
        button.setPreferredSize(new Dimension(132, 36));
        button.addActionListener(event -> action.run());
        return button;
    }

    private JButton actionButton(String text) {
        JButton button = new JButton(text);
        button.setForeground(Color.BLACK);
        button.setBackground(BUTTON_BOTTOM);
        button.setFocusPainted(false);
        button.setFont(font(Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 150, 178)),
                new EmptyBorder(4, 14, 4, 14)));
        return button;
    }

    private JLabel formLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 14));
        return label;
    }

    private JLabel verticalLabel(String text) {
        JLabel label = new JLabel("<html>" + String.join("<br>", text.split("")) + "</html>", SwingConstants.CENTER);
        label.setFont(font(Font.BOLD, 18));
        label.setBorder(BorderFactory.createLineBorder(BORDER));
        return label;
    }

    private JLabel userLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(font(Font.BOLD, 22));
        return label;
    }

    private JLabel userValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 22));
        return label;
    }

    private JLabel timeLabel() {
        JLabel label = new JLabel();
        label.setFont(font(Font.BOLD, 13));
        Timer timer = new Timer(1000, event -> label.setText("时间： " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))));
        timer.start();
        label.setText("时间： " + java.time.LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        return label;
    }

    private JTextField readOnly(String text) {
        JTextField field = readOnlyField();
        field.setText(text);
        return field;
    }

    private JTextField readOnlyField() {
        JTextField field = new JTextField(14);
        field.setEditable(false);
        field.setBackground(FIELD);
        return field;
    }

    private JTextArea resultArea() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("SimSun", Font.PLAIN, 13));
        area.setLineWrap(false);
        area.setEditable(false);
        return area;
    }

    private JPanel separator() {
        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(1, 1));
        line.setBackground(BORDER);
        return line;
    }

    private Font font(int style, int size) {
        return new Font("Microsoft YaHei UI", style, size);
    }

    private String spaced(String text) {
        return String.join(" ", text.split(""));
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(7, 8, 7, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private boolean confirm(String text) {
        return JOptionPane.showConfirmDialog(frame, text, "消息", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private String navigationText(String role) {
        return "该界面为" + role + "界面\n"
                + "如有疑问请联系客服：XXXXXXXX\n\n"
                + "功能导航：\n"
                + "①如需绑定/解除设备请到 设备管理 界面\n"
                + "②完成设备绑定后请到 基本设置 界面\n"
                + "③如需删除流数据/策略信息请到 信息管理 界面\n"
                + "④如需设置隐私策略请到 隐私策略 界面\n"
                + "⑤如需查询数据请到 数据查询 界面\n"
                + "⑥系统信息通知查询请到 通知文件 界面\n"
                + "⑦更多功能 将根据用户反馈后续开发";
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }

    private final class DatePickers {
        private final JComboBox<Integer> year;
        private final JComboBox<Integer> month;
        private final JComboBox<Integer> day;
        private final JComboBox<Integer> hour;
        private final JComboBox<Integer> minute;
        private final JComboBox<Integer> second;

        private DatePickers(LocalDateTime time) {
            year = combo(range(2024, 2028), time.getYear());
            month = combo(range(1, 12), time.getMonthValue());
            day = combo(range(1, 31), Math.min(time.getDayOfMonth(), 31));
            hour = combo(range(0, 23), time.getHour());
            minute = combo(range(0, 59), time.getMinute());
            second = combo(range(0, 59), time.getSecond());
        }

        private JPanel panel() {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
            panel.setOpaque(false);
            panel.add(year);
            panel.add(formLabel("年"));
            panel.add(month);
            panel.add(formLabel("月"));
            panel.add(day);
            panel.add(formLabel("日"));
            panel.add(hour);
            panel.add(formLabel("时"));
            panel.add(minute);
            panel.add(formLabel("分"));
            panel.add(second);
            panel.add(formLabel("秒"));
            return panel;
        }

        private LocalDateTime value() {
            int d = Math.min((Integer) day.getSelectedItem(), java.time.YearMonth.of((Integer) year.getSelectedItem(), (Integer) month.getSelectedItem()).lengthOfMonth());
            return LocalDateTime.of((Integer) year.getSelectedItem(), (Integer) month.getSelectedItem(), d,
                    (Integer) hour.getSelectedItem(), (Integer) minute.getSelectedItem(), (Integer) second.getSelectedItem());
        }
    }

    private Integer[] range(int start, int end) {
        Integer[] values = new Integer[end - start + 1];
        for (int i = 0; i < values.length; i++) {
            values[i] = start + i;
        }
        return values;
    }

    private JComboBox<Integer> combo(Integer[] values, int selected) {
        JComboBox<Integer> box = new JComboBox<>(values);
        box.setSelectedItem(selected);
        box.setPreferredSize(new Dimension(values.length > 24 ? 54 : 46, 25));
        return box;
    }
}
