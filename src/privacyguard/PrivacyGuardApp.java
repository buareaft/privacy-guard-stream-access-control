package privacyguard;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
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

public class PrivacyGuardApp {
    private static final String SYSTEM_TITLE = "端到端的大模型安全推理系统";
    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Color PAGE = new Color(238, 238, 238);
    private static final Color BUTTON = new Color(198, 220, 239);
    private static final Color BORDER = new Color(142, 142, 142);
    private static final Color FIELD = new Color(244, 248, 252);

    private final JFrame frame = new JFrame(SYSTEM_TITLE);
    private final CardLayout rootCards = new CardLayout();
    private final JPanel root = new JPanel(rootCards);
    private final CardLayout ownerCards = new CardLayout();
    private final JPanel ownerContent = new JPanel(ownerCards);

    private final DefaultTableModel storageModel = new DefaultTableModel(
            new String[]{"输入编号", "输入名称", "密文状态", "目标服务器", "上传时间"}, 0);
    private final DefaultTableModel inferenceModel = new DefaultTableModel(
            new String[]{"任务编号", "结果编号", "推理状态", "处理时间", "结果内容"}, 0);
    private final JTextArea resultText = area();

    public void show() {
        setLookAndFeel();
        seedTables();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setContentPane(root);
        root.add(loginPanel("数据拥有者登录", "owner", "loginOwner"), "loginOwner");
        root.add(loginPanel("数据消费者登录", "consumer", "loginConsumer"), "loginConsumer");
        root.add(ownerShell(), "owner");
        root.add(consumerShell(), "consumer");
        buildOwnerPages();
        frame.setSize(1280, 790);
        frame.setLocationRelativeTo(null);
        rootCards.show(root, "loginOwner");
        frame.setVisible(true);
    }

    private JPanel loginPanel(String titleText, String role, String cardName) {
        JPanel page = new JPanel(new GridBagLayout());
        page.setBackground(PAGE);
        JPanel box = new JPanel(new GridBagLayout());
        box.setPreferredSize(new Dimension(560, 330));
        box.setBackground(PAGE);

        JLabel brand = new JLabel(SYSTEM_TITLE, SwingConstants.LEFT);
        brand.setFont(font(Font.PLAIN, 13));
        JLabel title = new JLabel(spaced(titleText), SwingConstants.CENTER);
        title.setFont(font(Font.PLAIN, 20));
        JTextField account = new JTextField("0001", 16);
        JPasswordField password = new JPasswordField("123456", 16);
        JButton login = actionButton("登录");
        JButton exit = actionButton("退出");
        JButton register = actionButton("注册");

        login.addActionListener(event -> {
            boolean ok = "0001".equals(account.getText().trim()) && "123456".equals(new String(password.getPassword()));
            if (!ok) {
                JOptionPane.showMessageDialog(frame, "账号或密码错误！", "消息", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            frame.setTitle("owner".equals(role) ? "数据拥有者系统" : "推理结果查询界面");
            rootCards.show(root, "owner".equals(role) ? "owner" : "consumer");
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
        box.add(label("账号："), c);
        c.gridx = 2;
        box.add(account, c);
        c.gridy++;
        c.gridx = 1;
        box.add(label("密码："), c);
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

    private JPanel ownerShell() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(PAGE);
        page.add(sidebar("0001", "ABC", "数据拥有者", ownerMenu(), "loginOwner"), BorderLayout.WEST);
        ownerContent.setBackground(PAGE);
        page.add(ownerContent, BorderLayout.CENTER);
        return page;
    }

    private JPanel consumerShell() {
        JPanel page = new JPanel(new BorderLayout());
        page.setBackground(PAGE);
        page.add(consumerSide(), BorderLayout.WEST);
        page.add(consumerQueryPage(), BorderLayout.CENTER);
        return page;
    }

    private JPanel sidebar(String account, String username, String role, JPanel menu, String loginCard) {
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
        top.add(userLabel("用户名："), c);
        c.gridx = 1;
        top.add(userValue(username), c);
        c.gridy++;
        c.gridx = 0;
        top.add(userLabel("角色："), c);
        c.gridx = 1;
        top.add(userValue(role), c);
        c.gridy++;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(14, 0, 18, 0);
        top.add(separator(), c);
        c.gridy++;
        c.insets = new Insets(4, 0, 22, 0);
        top.add(timeLabel(), c);
        c.gridy++;
        if (menu != null) {
            JLabel menuTitle = new JLabel("菜单选择：");
            menuTitle.setFont(font(Font.BOLD, 22));
            top.add(menuTitle, c);
            c.gridy++;
            top.add(menu, c);
        }
        side.add(top, BorderLayout.NORTH);

        JButton logout = actionButton("退出登录");
        logout.setPreferredSize(new Dimension(132, 36));
        logout.addActionListener(event -> {
            frame.setTitle(SYSTEM_TITLE);
            rootCards.show(root, loginCard);
        });
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        bottom.setOpaque(false);
        bottom.add(logout);
        side.add(bottom, BorderLayout.SOUTH);
        return side;
    }

    private JPanel consumerSide() {
        return sidebar("0001", "Consumer1", "数据消费者1", null, "loginConsumer");
    }

    private JPanel ownerMenu() {
        JPanel grid = new JPanel(new GridLayout(3, 2, 8, 12));
        grid.setOpaque(false);
        grid.add(nav("导航主页", () -> ownerCards.show(ownerContent, "home")));
        grid.add(nav("生产者管理", () -> ownerCards.show(ownerContent, "producer")));
        grid.add(nav("安全加密", () -> ownerCards.show(ownerContent, "encrypt")));
        grid.add(nav("密文上传", () -> ownerCards.show(ownerContent, "upload")));
        grid.add(nav("存储内容", () -> ownerCards.show(ownerContent, "storage")));
        return grid;
    }

    private void buildOwnerPages() {
        ownerContent.add(navigationPage(), "home");
        ownerContent.add(producerPage(), "producer");
        ownerContent.add(encryptPage(), "encrypt");
        ownerContent.add(uploadPage(), "upload");
        ownerContent.add(storagePage(), "storage");
    }

    private JPanel navigationPage() {
        JPanel page = titledPage("导航界面");
        JPanel content = group("导航界面");
        JTextArea text = area();
        text.setFont(font(Font.PLAIN, 18));
        text.setText("""
                该界面为数据拥有者功能入口界面。
                数据拥有者可在本界面完成生产者管理、安全加密、密文上传、以及查询存储内容等操作。

                如有疑问请联系管理员：XXXXXXXX

                功能导航
                ① 如需要管理输入端，请到 生产者管理 界面
                ③ 如需对数据进行加密，请到 安全加密 界面
                ④ 如需完成密文数据提交，请到 密文上传 界面
                ⑥ 如需查看已经加密完成的数据，请到 存储内容 界面
                """);
        addTextResult(content, 0, text, 520);
        addSection(page, content);
        return page;
    }

    private JPanel producerPage() {
        JPanel page = titledPage("生产者管理");
        JPanel source = group("输入来源信息");
        JComboBox<String> type = new JComboBox<>(new String[]{"终端输入", "文件导入"});
        addLine(source, 0, label("生产者编号："), field("1"), label("生产者名称："), field("Terminal_01"));
        addLine(source, 1, label("输入来源类型："), type, label("所属拥有者："), field("1001"));

        JPanel submit = group("输入提交信息");
        addLine(submit, 0, label("输入编号："), field("IN-001"), label("输入名称："), field("Prompt_01"));
        addLine(submit, 1, label("数据类型："), field("Text"), label("数据长度："), field("2048 tokens"));
        addLine(submit, 2, label("提交时间："), field(now()), label("输入说明："), field("大模型安全推理输入样本"));
        JButton save = actionButton("保存输入");
        JButton process = actionButton("提交处理");
        JButton reset = actionButton("重置");
        addButtonLine(submit, 3, save, process, reset);
        save.addActionListener(event -> JOptionPane.showMessageDialog(frame, "输入信息已保存！", "消息", JOptionPane.INFORMATION_MESSAGE));
        process.addActionListener(event -> ownerCards.show(ownerContent, "encrypt"));
        reset.addActionListener(event -> JOptionPane.showMessageDialog(frame, "输入项已重置为示例值。", "消息", JOptionPane.INFORMATION_MESSAGE));

        addSection(page, source);
        addSection(page, submit);
        return page;
    }

    private JPanel encryptPage() {
        JPanel page = titledPage("安全加密");
        JPanel input = group("待处理输入区");
        addLine(input, 0, label("输入编号："), field("IN-001"), label("输入名称："), field("Prompt_01"));
        addLine(input, 1, label("数据类型："), field("Text"), label("输入状态："), field("待处理"));
        JButton uploadData = actionButton("上传数据");
        addLine(input, 2, label("上传数据："), field("dataset/input_sample.txt"), uploadData);

        JPanel process = group("处理信息区");
        JTextField status = field("待处理");
        JTextField processTime = field("-");
        addLine(process, 0, label("处理方式："), field("本地安全处理"), label("处理状态："), status);
        addLine(process, 1, label("处理时间："), processTime, label("输出形式："), field("密文输入"));
        JButton start = actionButton("开始处理");
        JButton check = actionButton("查看处理状态");
        JButton next = actionButton("进入上传");
        addButtonLine(process, 2, start, check, next);
        uploadData.addActionListener(event -> JOptionPane.showMessageDialog(frame, "数据集已选择：dataset/input_sample.txt", "消息", JOptionPane.INFORMATION_MESSAGE));
        start.addActionListener(event -> {
            status.setText("处理中");
            processTime.setText(now());
            Timer timer = new Timer(700, e -> {
                status.setText("已完成");
                JOptionPane.showMessageDialog(frame, "本地安全处理完成，已生成密文输入。", "消息", JOptionPane.INFORMATION_MESSAGE);
            });
            timer.setRepeats(false);
            timer.start();
        });
        check.addActionListener(event -> JOptionPane.showMessageDialog(frame, "当前处理状态：" + status.getText(), "消息", JOptionPane.INFORMATION_MESSAGE));
        next.addActionListener(event -> ownerCards.show(ownerContent, "upload"));

        addSection(page, input);
        addSection(page, process);
        return page;
    }

    private JPanel uploadPage() {
        JPanel page = titledPage("密文上传");
        JPanel object = group("上传对象信息");
        addLine(object, 0, label("输入编号："), field("IN-001"), label("输入名称："), field("Prompt_01"));
        addLine(object, 1, label("密文状态："), field("已完成"), label("目标服务器："), field("CipherStore-Server-01"));
        addLine(object, 2, label("上传时间："), field(now()));

        JPanel state = group("上传状态区");
        JTextField pending = field("1 项任务");
        JTextField running = field("0 项任务");
        JTextField completed = field("2 项任务");
        JTextField failed = field("0 项任务");
        JProgressBar progress = new JProgressBar(0, 100);
        progress.setStringPainted(true);
        addLine(state, 0, label("待上传："), pending, label("上传中："), running);
        addLine(state, 1, label("上传完成："), completed, label("上传失败："), failed);
        addLine(state, 2, label("上传进度："), progress);
        JButton start = actionButton("开始上传");
        JButton refresh = actionButton("刷新状态");
        addButtonLine(state, 3, start, refresh);
        start.addActionListener(event -> runUpload(progress, pending, running, completed));
        refresh.addActionListener(event -> JOptionPane.showMessageDialog(frame, "上传状态已刷新。", "消息", JOptionPane.INFORMATION_MESSAGE));

        addSection(page, object);
        addSection(page, state);
        return page;
    }

    private JPanel storagePage() {
        JPanel page = titledPage("存储内容");
        JPanel storage = group("已存储密文内容");
        JTable table = new JTable(storageModel);
        table.setRowHeight(26);
        GridBagConstraints c = gbc();
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 8;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane pane = new JScrollPane(table);
        pane.setPreferredSize(new Dimension(880, 430));
        storage.add(pane, c);
        addSection(page, storage);
        return page;
    }

    private JPanel consumerQueryPage() {
        JPanel page = titledPage("推理结果查询界面");
        JPanel intro = group("界面说明");
        JTextArea text = area();
        text.setFont(font(Font.PLAIN, 15));
        text.setText("""
                该界面面向数据消费者，用于查看由数据拥有者共享的密文推理结果。
                数据消费者不直接参与数据加密与推理发起过程，而是在获得共享结果后，
                通过界面查询相关任务的推理状态与输出结果。

                推理任务：对应发起密文推理请求。
                结果查询：对应查看推理状态、推理结果。
                """);
        addTextResult(intro, 0, text, 145);

        JPanel task = group("任务创建区");
        JTextField taskId = field("INF-001");
        JTextField object = field("Prompt_01 / CipherInput");
        JTextField submitTime = field(now());
        JButton query = actionButton("查询");
        JButton reset = actionButton("重置");
        addLine(task, 0, label("任务编号："), taskId, label("提交对象："), object);
        addLine(task, 1, label("提交时间："), submitTime, query, reset);

        JPanel result = group("结果展示区");
        JTable table = new JTable(inferenceModel);
        table.setRowHeight(26);
        addTextResult(result, 0, resultText, 150);
        GridBagConstraints c = gbc();
        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 8;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane tablePane = new JScrollPane(table);
        tablePane.setPreferredSize(new Dimension(880, 210));
        result.add(tablePane, c);

        query.addActionListener(event -> {
            resultText.setText(inferenceText());
            JOptionPane.showMessageDialog(frame, "查询完成！", "消息", JOptionPane.INFORMATION_MESSAGE);
        });
        reset.addActionListener(event -> {
            taskId.setText("INF-001");
            object.setText("Prompt_01 / CipherInput");
            submitTime.setText(now());
            resultText.setText("");
        });

        addSection(page, intro);
        addSection(page, task);
        addSection(page, result);
        return page;
    }

    private void runUpload(JProgressBar progress, JTextField pending, JTextField running, JTextField completed) {
        SwingWorker<Void, Integer> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                pending.setText("0 项任务");
                running.setText("1 项任务");
                for (int i = 0; i <= 100; i += 5) {
                    publish(i);
                    Thread.sleep(40);
                }
                return null;
            }

            @Override
            protected void process(java.util.List<Integer> chunks) {
                int value = chunks.get(chunks.size() - 1);
                progress.setValue(value);
                progress.setString(value < 100 ? "密文上传中..." : "上传完成");
            }

            @Override
            protected void done() {
                running.setText("0 项任务");
                completed.setText("3 项任务");
                storageModel.addRow(new Object[]{"IN-004", "Prompt_04", "已上传", "CipherStore-Server-01", now()});
                JOptionPane.showMessageDialog(frame, "密文数据已提交至密文存储服务器。", "消息", JOptionPane.INFORMATION_MESSAGE);
            }
        };
        worker.execute();
    }

    private void seedTables() {
        storageModel.addRow(new Object[]{"IN-001", "Prompt_01", "已上传", "CipherStore-Server-01", "2026-06-13 18:20:12"});
        storageModel.addRow(new Object[]{"IN-002", "Prompt_02", "已上传", "CipherStore-Server-01", "2026-06-13 18:22:45"});
        storageModel.addRow(new Object[]{"IN-003", "Prompt_03", "待上传", "CipherStore-Server-01", "2026-06-13 18:24:09"});

        inferenceModel.addRow(new Object[]{"INF-001", "RES-001", "已完成", "2.41s", "***"});
        inferenceModel.addRow(new Object[]{"INF-002", "RES-002", "处理中", "0.86s", "待返回"});
        inferenceModel.addRow(new Object[]{"INF-003", "RES-003", "已完成", "3.12s", "***"});
    }

    private String inferenceText() {
        return """
                任务编号：INF-001，结果编号：RES-001，推理状态：已完成，处理时间：2.41s，结果内容：***
                任务编号：INF-002，结果编号：RES-002，推理状态：处理中，处理时间：0.86s，结果内容：待返回
                任务编号：INF-003，结果编号：RES-003，推理状态：已完成，处理时间：3.12s，结果内容：***
                """;
    }

    private JPanel titledPage(String title) {
        JPanel page = new JPanel(new GridBagLayout());
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

    private JPanel group(String title) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(PAGE);
        panel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER), new EmptyBorder(10, 10, 10, 10)));
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
        panel.putClientProperty("baseRows", 2);
        return panel;
    }

    private void addSection(JPanel page, Component section) {
        GridBagConstraints c = gbc();
        c.gridx = 0;
        c.gridy = componentCountRows(page);
        c.weightx = 1;
        c.fill = GridBagConstraints.BOTH;
        page.add(section, c);
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

    private void addTextResult(JPanel panel, int row, JTextArea area, int height) {
        GridBagConstraints c = gbc();
        c.gridy = (Integer) panel.getClientProperty("baseRows") + row;
        c.gridx = 0;
        c.gridwidth = 8;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
        JScrollPane pane = new JScrollPane(area);
        pane.setPreferredSize(new Dimension(880, height));
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
        button.setBackground(BUTTON);
        button.setFocusPainted(false);
        button.setFont(font(Font.BOLD, 13));
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(120, 150, 178)),
                new EmptyBorder(4, 14, 4, 14)));
        return button;
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.PLAIN, 14));
        return label;
    }

    private JLabel userLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.RIGHT);
        label.setFont(font(Font.BOLD, 21));
        return label;
    }

    private JLabel userValue(String text) {
        JLabel label = new JLabel(text);
        label.setFont(font(Font.BOLD, 21));
        return label;
    }

    private JTextField field(String text) {
        JTextField field = new JTextField(text, 16);
        field.setBackground(FIELD);
        return field;
    }

    private JTextArea area() {
        JTextArea area = new JTextArea();
        area.setFont(new Font("SimSun", Font.PLAIN, 13));
        area.setEditable(false);
        return area;
    }

    private JLabel timeLabel() {
        JLabel label = new JLabel();
        label.setFont(font(Font.BOLD, 13));
        Timer timer = new Timer(1000, event -> label.setText("时间： " + now()));
        timer.start();
        label.setText("时间： " + now());
        return label;
    }

    private JPanel separator() {
        JPanel line = new JPanel();
        line.setPreferredSize(new Dimension(1, 1));
        line.setBackground(BORDER);
        return line;
    }

    private GridBagConstraints gbc() {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(7, 8, 7, 8);
        c.fill = GridBagConstraints.HORIZONTAL;
        return c;
    }

    private Font font(int style, int size) {
        return new Font("Microsoft YaHei UI", style, size);
    }

    private String spaced(String text) {
        return String.join(" ", text.split(""));
    }

    private String now() {
        return LocalDateTime.now().format(TIME);
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
    }
}
