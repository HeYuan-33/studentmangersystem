import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.io.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.*;
import java.util.Map;
import java.util.HashMap;
import javax.swing.border.TitledBorder;
import java.util.function.BiConsumer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.lang.management.ManagementFactory;
import java.util.Properties;

// 主面板
class AdminPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Color primaryColor = new Color(64, 158, 255);
    private Color backgroundColor = new Color(245, 247, 250);
    private Color sidebarColor = new Color(255, 255, 255);
    private Color textColor = new Color(51, 51, 51);
    private Connection conn;

    public AdminPanel() {
        super();
        initializeDatabase();
        initializeUI();
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student?useSSL=false&serverTimezone=UTC",
                    "root", "1405269390a");
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接失败: " + e.getMessage());
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(backgroundColor);

        // 创建侧边栏
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 10, 10));
        sidebar.setBackground(sidebarColor);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建内容面板
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(backgroundColor);

        // 添加功能按钮
        JButton queryButton = createStyledButton("查询学生", true);
        JButton modifyButton = createStyledButton("修改学生", false);
        JButton addButton = createStyledButton("添加学生", false);
        JButton dbConfigButton = createStyledButton("数据库配置", false);
        JButton backupButton = createStyledButton("系统备份", false);

        queryButton.addActionListener(e -> cardLayout.show(contentPanel, "query"));
        modifyButton.addActionListener(e -> cardLayout.show(contentPanel, "modify"));
        addButton.addActionListener(e -> cardLayout.show(contentPanel, "add"));
        dbConfigButton.addActionListener(e -> cardLayout.show(contentPanel, "dbconfig"));
        backupButton.addActionListener(e -> cardLayout.show(contentPanel, "backup"));

        sidebar.add(queryButton);
        sidebar.add(modifyButton);
        sidebar.add(addButton);
        sidebar.add(dbConfigButton);
        sidebar.add(backupButton);

        // 添加功能面板
        contentPanel.add(createQueryPanel(), "query");
        contentPanel.add(createModifyPanel(), "modify");
        contentPanel.add(createAddPanel(), "add");
        contentPanel.add(createDBConfigPanel(), "dbconfig");
        contentPanel.add(createBackupPanel(), "backup");

        // 添加到主面板
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // 默认显示查询面板
        cardLayout.show(contentPanel, "query");
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 35));

        if (isPrimary) {
            button.setBackground(new Color(64, 158, 255));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(245, 247, 250));
            button.setForeground(new Color(96, 98, 102));
        }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(new Color(102, 177, 255));
                } else {
                    button.setBackground(new Color(236, 239, 241));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(new Color(64, 158, 255));
                } else {
                    button.setBackground(new Color(245, 247, 250));
                }
            }
        });

        return button;
    }

    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(15);
        JButton searchButton = createStyledButton("搜索学生", true);
        searchPanel.add(new JLabel("请输入要查询的学生学号："));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 创建表格
        String[] columnNames = {"学号", "姓名", "班级", "电话", "邮箱", "操作"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 设置操作列
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JButton modifyBtn = new JButton("查询");
                modifyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                modifyBtn.setForeground(new Color(64, 158, 255));
                return modifyBtn;
            }
        });

        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private final JButton button;
            {
                button = new JButton();
                button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                button.setForeground(new Color(64, 158, 255));
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                button.setText("查询");
                button.addActionListener(e -> {
                    String studentId = table.getValueAt(row, 0).toString();
                    String name = table.getValueAt(row, 1).toString();
                    String classNumber = table.getValueAt(row, 2).toString();
                    String phone = table.getValueAt(row, 3).toString();
                    String email = table.getValueAt(row, 4).toString();

                    JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JTextField nameField = new JTextField(name);
                    JTextField classField = new JTextField(classNumber);
                    JTextField phoneField = new JTextField(phone);
                    JTextField emailField = new JTextField(email);

                    inputPanel.add(new JLabel("姓名:"));
                    inputPanel.add(nameField);
                    inputPanel.add(new JLabel("班级:"));
                    inputPanel.add(classField);
                    inputPanel.add(new JLabel("电话:"));
                    inputPanel.add(phoneField);
                    inputPanel.add(new JLabel("邮箱:"));
                    inputPanel.add(emailField);

                    int result = JOptionPane.showConfirmDialog(null, inputPanel,
                            "查询学生信息", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            String sql = "SELECT * FROM student WHERE stuid1 = ?";
                            PreparedStatement pstmt = conn.prepareStatement(sql);
                            pstmt.setString(1, studentId);
                            ResultSet rs = pstmt.executeQuery();

                            model.setRowCount(0);
                            while (rs.next()) {
                                model.addRow(new Object[]{
                                        rs.getString("stuid1"),
                                        rs.getString("name"),
                                        rs.getString("classnumber"),
                                        rs.getString("telenumber"),
                                        rs.getString("qqmail"),
                                        "查询"
                                });
                            }

                            if (model.getRowCount() == 0) {
                                JOptionPane.showMessageDialog(null, "未找到该学生信息");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "查询失败: " + ex.getMessage());
                        }
                    }
                    fireEditingStopped();
                });
                return button;
            }
        });

        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String studentId = searchField.getText().trim();
            if (!studentId.isEmpty()) {
                try {
                    String sql = "SELECT * FROM student WHERE stuid1 = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, studentId);
                    ResultSet rs = pstmt.executeQuery();

                    model.setRowCount(0);
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getString("stuid1"),
                                rs.getString("name"),
                                rs.getString("classnumber"),
                                rs.getString("telenumber"),
                                rs.getString("qqmail"),
                                "查询"
                        });
                    }

                    if (model.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(null, "未找到该学生信息");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "查询失败: " + ex.getMessage());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createModifyPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(15);
        JButton searchButton = createStyledButton("搜索学生", true);
        searchPanel.add(new JLabel("请输入要修改的学生学号："));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 创建表格
        String[] columnNames = {"学号", "姓名", "班级", "电话", "邮箱", "操作"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 设置操作列
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JButton modifyBtn = new JButton("修改");
                modifyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                modifyBtn.setForeground(new Color(64, 158, 255));
                return modifyBtn;
            }
        });

        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private final JButton button;
            {
                button = new JButton();
                button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                button.setForeground(new Color(64, 158, 255));
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                button.setText("修改");
                button.addActionListener(e -> {
                    String studentId = table.getValueAt(row, 0).toString();
                    String name = table.getValueAt(row, 1).toString();
                    String classNumber = table.getValueAt(row, 2).toString();
                    String phone = table.getValueAt(row, 3).toString();
                    String email = table.getValueAt(row, 4).toString();

                    JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JTextField nameField = new JTextField(name);
                    JTextField classField = new JTextField(classNumber);
                    JTextField phoneField = new JTextField(phone);
                    JTextField emailField = new JTextField(email);

                    inputPanel.add(new JLabel("姓名:"));
                    inputPanel.add(nameField);
                    inputPanel.add(new JLabel("班级:"));
                    inputPanel.add(classField);
                    inputPanel.add(new JLabel("电话:"));
                    inputPanel.add(phoneField);
                    inputPanel.add(new JLabel("邮箱:"));
                    inputPanel.add(emailField);

                    int result = JOptionPane.showConfirmDialog(null, inputPanel,
                            "修改学生信息", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            String sql = "UPDATE student SET name=?, classnumber=?, telenumber=?, qqmail=? WHERE stuid1=?";
                            PreparedStatement pstmt = conn.prepareStatement(sql);

                            pstmt.setString(1, nameField.getText().trim());
                            pstmt.setString(2, classField.getText().trim());
                            pstmt.setString(3, phoneField.getText().trim());
                            pstmt.setString(4, emailField.getText().trim());
                            pstmt.setString(5, studentId);

                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows > 0) {
                                JOptionPane.showMessageDialog(null, "学生信息更新成功！");
                                // 更新表格显示
                                table.setValueAt(nameField.getText().trim(), row, 1);
                                table.setValueAt(classField.getText().trim(), row, 2);
                                table.setValueAt(phoneField.getText().trim(), row, 3);
                                table.setValueAt(emailField.getText().trim(), row, 4);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "更新失败: " + ex.getMessage());
                        }
                    }
                    fireEditingStopped();
                });
                return button;
            }
        });

        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String studentId = searchField.getText().trim();
            if (!studentId.isEmpty()) {
                try {
                    String sql = "SELECT * FROM student WHERE stuid1 = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, studentId);
                    ResultSet rs = pstmt.executeQuery();

                    model.setRowCount(0);
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getString("stuid1"),
                                rs.getString("name"),
                                rs.getString("classnumber"),
                                rs.getString("telenumber"),
                                rs.getString("qqmail"),
                                "修改"
                        });
                    }

                    if (model.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(null, "未找到该学生信息");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "查询失败: " + ex.getMessage());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建输入面板
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField classField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField emailField = new JTextField();

        inputPanel.add(new JLabel("学号:"));
        inputPanel.add(idField);
        inputPanel.add(new JLabel("姓名:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("班级:"));
        inputPanel.add(classField);
        inputPanel.add(new JLabel("电话:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("邮箱:"));
        inputPanel.add(emailField);

        // 创建表格
        String[] columnNames = {"学号", "姓名", "班级", "电话", "邮箱"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        JButton addButton = createStyledButton("添加学生", false);
        addButton.addActionListener(evt -> {
            try {
                String sql = "INSERT INTO student (stuid1, name, classnumber, telenumber, qqmail) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, idField.getText().trim());
                pstmt.setString(2, nameField.getText().trim());
                pstmt.setString(3, classField.getText().trim());
                pstmt.setString(4, phoneField.getText().trim());
                pstmt.setString(5, emailField.getText().trim());

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(null, "添加学生成功！");
                    // 更新表格显示
                    model.addRow(new Object[]{
                            idField.getText().trim(),
                            nameField.getText().trim(),
                            classField.getText().trim(),
                            phoneField.getText().trim(),
                            emailField.getText().trim()
                    });
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "添加失败: " + ex.getMessage());
            }
        });

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);
        panel.add(addButton, BorderLayout.SOUTH);

        return panel;
    }

    public void modifyStudent(JTable table, int row) {
        String studentId = table.getValueAt(row, 0).toString();
        String name = table.getValueAt(row, 1).toString();
        String classNumber = table.getValueAt(row, 2).toString();
        String phone = table.getValueAt(row, 3).toString();
        String email = table.getValueAt(row, 4).toString();

        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField nameField = new JTextField(name);
        JTextField classField = new JTextField(classNumber);
        JTextField phoneField = new JTextField(phone);
        JTextField emailField = new JTextField(email);

        inputPanel.add(new JLabel("姓名:"));
        inputPanel.add(nameField);
        inputPanel.add(new JLabel("班级:"));
        inputPanel.add(classField);
        inputPanel.add(new JLabel("电话:"));
        inputPanel.add(phoneField);
        inputPanel.add(new JLabel("邮箱:"));
        inputPanel.add(emailField);

        int result = JOptionPane.showConfirmDialog(null, inputPanel,
                "修改学生信息", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String sql = "UPDATE student SET name=?, classnumber=?, telenumber=?, qqmail=? WHERE stuid1=?";
                PreparedStatement pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, nameField.getText().trim());
                pstmt.setString(2, classField.getText().trim());
                pstmt.setString(3, phoneField.getText().trim());
                pstmt.setString(4, emailField.getText().trim());
                pstmt.setString(5, studentId);

                int affectedRows = pstmt.executeUpdate();
                if (affectedRows > 0) {
                    JOptionPane.showMessageDialog(null, "学生信息更新成功！");
                    // 更新表格显示
                    table.setValueAt(nameField.getText().trim(), row, 1);
                    table.setValueAt(classField.getText().trim(), row, 2);
                    table.setValueAt(phoneField.getText().trim(), row, 3);
                    table.setValueAt(emailField.getText().trim(), row, 4);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "更新失败: " + ex.getMessage());
            }
        }
    }

    private void searchStudent(DefaultTableModel model, String studentId) {
        try {
            String sql = "SELECT * FROM student WHERE stuid1 = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("stuid1"),
                        rs.getString("name"),
                        rs.getString("classnumber"),
                        rs.getString("telenumber"),
                        rs.getString("qqmail")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "未找到该学生信息");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
        }
    }

    private void deleteStudent(String studentId) {
        try {
            String sql = "DELETE FROM student WHERE stuid1 = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "学生删除成功！");
            } else {
                JOptionPane.showMessageDialog(null, "未找到该学生信息！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
        }
    }

    private JPanel createImportPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        controlPanel.setBackground(Color.WHITE);

        JButton importButton = createStyledButton("导入成绩", true);
        controlPanel.add(importButton);

        // 创建表格
        String[] columnNames = {"学号", "姓名", "高数", "马原", "英语", "线代", "总分", "平均分"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 添加导入功能
        importButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV 文件", "csv"));

            if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                    String line;
                    boolean isFirstLine = true;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        String[] data = line.split(",");
                        if (data.length >= 6) {
                            String studentId = data[0].trim();
                            Map<String, Integer> grades = new HashMap<>();

                            try {
                                if (!data[2].trim().isEmpty()) {
                                    grades.put("math", Integer.parseInt(data[2].trim()));
                                }
                                if (!data[3].trim().isEmpty()) {
                                    grades.put("MaYuan", Integer.parseInt(data[3].trim()));
                                }
                                if (!data[4].trim().isEmpty()) {
                                    grades.put("English", Integer.parseInt(data[4].trim()));
                                }
                                if (!data[5].trim().isEmpty()) {
                                    grades.put("linemath", Integer.parseInt(data[5].trim()));
                                }

                                updateGrades(studentId, grades);
                            } catch (NumberFormatException ex) {
                                JOptionPane.showMessageDialog(null,
                                        "成绩格式错误，请确保所有成绩都是有效的数字！\n" +
                                                "错误行: " + line);
                                return;
                            }
                        }
                    }

                    JOptionPane.showMessageDialog(null, "成绩导入成功！");
                    loadAllGrades(model);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "导入失败: " + ex.getMessage());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        controlPanel.setBackground(Color.WHITE);

        JButton exportButton = createStyledButton("导出成绩", true);
        controlPanel.add(exportButton);

        String[] columnNames = {"学号", "姓名", "高数", "马原", "英语", "线代", "总分", "平均分"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        loadAllGrades(model);

        exportButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("CSV 文件", "csv"));

            if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }

                try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                    writer.println("学号,姓名,高数,马原,英语,线代,总分,平均分");

                    for (int i = 0; i < model.getRowCount(); i++) {
                        StringBuilder line = new StringBuilder();
                        for (int j = 0; j < model.getColumnCount(); j++) {
                            if (j > 0) line.append(",");
                            Object value = model.getValueAt(i, j);
                            line.append(value != null ? value.toString() : "");
                        }
                        writer.println(line.toString());
                    }

                    JOptionPane.showMessageDialog(null, "成绩导出成功！");
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "导出失败: " + ex.getMessage());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建顶部操作面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        String[] subjects = {"高数", "马原", "英语", "线代"};
        JComboBox<String> subjectCombo = new JComboBox<>(subjects);
        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        String[] chartTypes = {"柱状图", "饼图", "折线图"};
        JComboBox<String> chartTypeCombo = new JComboBox<>(chartTypes);
        chartTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JButton analyzeButton = createStyledButton("分析成绩", true);

        topPanel.add(new JLabel("选择科目："));
        topPanel.add(subjectCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("图表类型："));
        topPanel.add(chartTypeCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(analyzeButton);

        // 创建图表和统计信息面板
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBackground(Color.WHITE);

        // 创建图表面板（初始为空）
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);

        // 创建统计信息面板
        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "统计信息",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14)
        ));

        contentPanel.add(chartPanel);
        contentPanel.add(statsPanel);

        // 添加分析按钮事件
        analyzeButton.addActionListener(e -> {
            String subject = (String) subjectCombo.getSelectedItem();
            String chartType = (String) chartTypeCombo.getSelectedItem();

            try {
                // 获取成绩数据
                Map<String, Integer> gradeDistribution = new HashMap<>();
                int totalStudents = 0;
                double totalScore = 0;
                int maxScore = 0;
                int minScore = 100;

                String columnName = getColumnNameForSubject(subject);
                String sql = "SELECT " + columnName + " FROM studentgrade";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        int score = rs.getInt(1);
                        if (!rs.wasNull()) {
                            totalStudents++;
                            totalScore += score;
                            maxScore = Math.max(maxScore, score);
                            minScore = Math.min(minScore, score);

                            // 统计分数段
                            String range = getScoreRange(score);
                            gradeDistribution.merge(range, 1, Integer::sum);
                        }
                    }
                }

                // 更新统计信息
                statsPanel.removeAll();
                if (totalStudents > 0) {
                    addStatItem(statsPanel, "总人数", String.valueOf(totalStudents));
                    addStatItem(statsPanel, "平均分", String.format("%.2f", totalScore / totalStudents));
                    addStatItem(statsPanel, "最高分", String.valueOf(maxScore));
                    addStatItem(statsPanel, "最低分", String.valueOf(minScore));

                    // 创建并显示图表
                    JFreeChart chart = createChart(gradeDistribution, subject, chartType);
                    ChartPanel newChartPanel = new ChartPanel(chart);
                    newChartPanel.setPreferredSize(new Dimension(400, 300));

                    chartPanel.removeAll();
                    chartPanel.add(newChartPanel, BorderLayout.CENTER);
                    chartPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(new Color(200, 200, 200)),
                            subject + "成绩分布",
                            TitledBorder.LEFT,
                            TitledBorder.TOP,
                            new Font("微软雅黑", Font.BOLD, 14)
                    ));
                }

                // 刷新面板
                contentPanel.revalidate();
                contentPanel.repaint();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "分析成绩失败: " + ex.getMessage());
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private String getColumnNameForSubject(String subject) {
        switch (subject) {
            case "高数": return "math";
            case "马原": return "mayuan";
            case "英语": return "english";
            case "线代": return "linemath";
            default: return "math";
        }
    }

    private String getScoreRange(int score) {
        if (score >= 90) return "90-100";
        if (score >= 80) return "80-89";
        if (score >= 70) return "70-79";
        if (score >= 60) return "60-69";
        return "0-59";
    }

    private void addStatItem(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label + "：", JLabel.RIGHT);
        JLabel valueComp = new JLabel(value, JLabel.LEFT);
        labelComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        valueComp.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelComp);
        panel.add(valueComp);
    }

    private JFreeChart createChart(Map<String, Integer> data, String subject, String chartType) {
        switch (chartType) {
            case "柱状图":
                return createBarChart(data, subject);
            case "饼图":
                return createPieChart(data, subject);
            case "折线图":
                return createLineChart(data, subject);
            default:
                return createBarChart(data, subject);
        }
    }

    private JFreeChart createBarChart(Map<String, Integer> data, String subject) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        for (String range : ranges) {
            dataset.addValue(data.getOrDefault(range, 0), "人数", range);
        }

        return ChartFactory.createBarChart(
                subject + "成绩分布",
                "分数段",
                "人数",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
    }

    private JFreeChart createPieChart(Map<String, Integer> data, String subject) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        for (String range : ranges) {
            dataset.setValue(range, data.getOrDefault(range, 0));
        }

        return ChartFactory.createPieChart(
                subject + "成绩分布",
                dataset,
                true,
                true,
                false
        );
    }

    private JFreeChart createLineChart(Map<String, Integer> data, String subject) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        for (String range : ranges) {
            dataset.addValue(data.getOrDefault(range, 0), "人数", range);
        }

        return ChartFactory.createLineChart(
                subject + "成绩分布",
                "分数段",
                "人数",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
    }

    private void updateGrades(String studentId, Map<String, Integer> grades) {
        try {
            String checkSql = "SELECT * FROM studentgrade WHERE stuid2 = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, studentId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // 更新现有记录
                StringBuilder updateSql = new StringBuilder("UPDATE studentgrade SET ");
                List<String> updates = new ArrayList<>();
                List<Object> values = new ArrayList<>();

                for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                    updates.add(entry.getKey() + " = ?");
                    values.add(entry.getValue());
                }

                updateSql.append(String.join(", ", updates));
                updateSql.append(" WHERE stuid2 = ?");

                PreparedStatement updateStmt = conn.prepareStatement(updateSql.toString());
                int paramIndex = 1;
                for (Object value : values) {
                    updateStmt.setObject(paramIndex++, value);
                }
                updateStmt.setString(paramIndex, studentId);
                updateStmt.executeUpdate();
            } else {
                // 插入新记录
                StringBuilder insertSql = new StringBuilder("INSERT INTO studentgrade (stuid2");
                StringBuilder valuesSql = new StringBuilder(") VALUES (?");
                List<Object> values = new ArrayList<>();
                values.add(studentId);

                for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                    insertSql.append(", ").append(entry.getKey());
                    valuesSql.append(", ?");
                    values.add(entry.getValue());
                }
                insertSql.append(valuesSql).append(")");

                PreparedStatement insertStmt = conn.prepareStatement(insertSql.toString());
                for (int i = 0; i < values.size(); i++) {
                    insertStmt.setObject(i + 1, values.get(i));
                }
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "更新成绩失败: " + e.getMessage());
        }
    }

    private void loadAllGrades(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT s.stuid1, s.name, g.math, g.MaYuan, g.English, g.linemath " +
                    "FROM student s LEFT JOIN studentgrade g ON s.stuid1 = g.stuid2";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String studentId = rs.getString("stuid1");
                String name = rs.getString("name");
                Integer math = rs.getObject("math") != null ? rs.getInt("math") : null;
                Integer mayuan = rs.getObject("MaYuan") != null ? rs.getInt("MaYuan") : null;
                Integer english = rs.getObject("English") != null ? rs.getInt("English") : null;
                Integer linemath = rs.getObject("linemath") != null ? rs.getInt("linemath") : null;

                int validCount = 0;
                int total = 0;

                if (math != null) { total += math; validCount++; }
                if (mayuan != null) { total += mayuan; validCount++; }
                if (english != null) { total += english; validCount++; }
                if (linemath != null) { total += linemath; validCount++; }

                model.addRow(new Object[]{
                        studentId,
                        name,
                        math != null ? math : "-",
                        mayuan != null ? mayuan : "-",
                        english != null ? english : "-",
                        linemath != null ? linemath : "-",
                        validCount > 0 ? total : "-",
                        validCount > 0 ? String.format("%.2f", total * 1.0 / validCount) : "-"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载成绩失败: " + e.getMessage());
        }
    }

    private JPanel createDBConfigPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建配置面板
        JPanel configPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        configPanel.setBackground(Color.WHITE);
        configPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "数据库配置",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));

        // 添加配置字段
        JTextField hostField = addConfigField(configPanel, "主机地址", "localhost");
        JTextField portField = addConfigField(configPanel, "端口", "3306");
        JTextField dbNameField = addConfigField(configPanel, "数据库名", "student");
        JTextField userField = addConfigField(configPanel, "用户名", "root");
        JPasswordField passwordField = addPasswordField(configPanel, "密码");

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton testButton = createStyledButton("测试连接", true);
        JButton saveButton = createStyledButton("保存配置", true);

        // 测试连接按钮事件
        testButton.addActionListener(e -> {
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            String dbName = dbNameField.getText().trim();
            String user = userField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (testConnection(host, port, dbName, user, password)) {
                JOptionPane.showMessageDialog(panel, "连接测试成功！");
            } else {
                JOptionPane.showMessageDialog(panel, "连接测试失败！请检查配置信息。");
            }
        });

        // 保存配置按钮事件
        saveButton.addActionListener(e -> {
            String host = hostField.getText().trim();
            String port = portField.getText().trim();
            String dbName = dbNameField.getText().trim();
            String user = userField.getText().trim();
            String password = new String(passwordField.getPassword());

            if (saveConfiguration(host, port, dbName, user, password)) {
                JOptionPane.showMessageDialog(panel, "配置保存成功！");
            } else {
                JOptionPane.showMessageDialog(panel, "配置保存失败！");
            }
        });

        buttonPanel.add(testButton);
        buttonPanel.add(saveButton);

        // 添加所有面板到主面板
        panel.add(configPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JTextField addConfigField(JPanel panel, String label, String defaultValue) {
        JLabel labelComp = new JLabel(label + ":", JLabel.RIGHT);
        labelComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JTextField field = new JTextField(defaultValue);
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(labelComp);
        panel.add(field);
        return field;
    }

    private JPasswordField addPasswordField(JPanel panel, String label) {
        JLabel labelComp = new JLabel(label + ":", JLabel.RIGHT);
        labelComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(labelComp);
        panel.add(field);
        return field;
    }

    private boolean testConnection(String host, String port, String dbName, String user, String password) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + dbName;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection testConn = DriverManager.getConnection(url, user, password)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean saveConfiguration(String host, String port, String dbName, String user, String password) {
        Properties props = new Properties();
        props.setProperty("db.host", host);
        props.setProperty("db.port", port);
        props.setProperty("db.name", dbName);
        props.setProperty("db.user", user);
        props.setProperty("db.password", password);

        try (FileOutputStream out = new FileOutputStream("db.properties")) {
            props.store(out, "Database Configuration");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private JPanel createBackupPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建备份面板
        JPanel backupPanel = new JPanel(new GridLayout(0, 1, 10, 10));
        backupPanel.setBackground(Color.WHITE);
        backupPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "数据库备份与恢复",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));

        // 备份路径选择
        JPanel pathPanel = new JPanel(new BorderLayout(10, 0));
        pathPanel.setBackground(Color.WHITE);
        JTextField backupPathField = new JTextField();
        JButton choosePathButton = createStyledButton("选择路径", false);
        pathPanel.add(new JLabel("备份路径: "), BorderLayout.WEST);
        pathPanel.add(backupPathField, BorderLayout.CENTER);
        pathPanel.add(choosePathButton, BorderLayout.EAST);

        // 备份和恢复按钮
        JButton backupButton = createStyledButton("备份数据库", true);
        JButton restoreButton = createStyledButton("恢复数据库", true);

        // 选择路径按钮事件
        choosePathButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                backupPathField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // 备份按钮事件
        backupButton.addActionListener(e -> {
            String backupPath = backupPathField.getText().trim();
            if (backupPath.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "请选择备份路径");
                return;
            }

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String backupFile = backupPath + File.separator + "backup_" + timestamp + ".sql";

            try {
                String[] command = {
                    "mysqldump",
                    "-h", "localhost",
                    "-P", "3306",
                    "-u", "root",
                    "-p1405269390a",
                    "student"
                };

                ProcessBuilder pb = new ProcessBuilder(command);
                pb.redirectOutput(new File(backupFile));
                Process process = pb.start();

                if (process.waitFor() == 0) {
                    JOptionPane.showMessageDialog(panel, "数据库备份成功！\n备份文件: " + backupFile);
                } else {
                    JOptionPane.showMessageDialog(panel, "备份失败！");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "备份失败: " + ex.getMessage());
            }
        });

        // 恢复按钮事件
        restoreButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileFilter(new FileNameExtensionFilter("SQL文件", "sql"));
            
            if (fileChooser.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                File sqlFile = fileChooser.getSelectedFile();
                
                try {
                    String[] command = {
                        "mysql",
                        "-h", "localhost",
                        "-P", "3306",
                        "-u", "root",
                        "-p1405269390a",
                        "student"
                    };

                    ProcessBuilder pb = new ProcessBuilder(command);
                    Process process = pb.start();

                    // 将SQL文件内容写入mysql进程的输入流
                    try (BufferedReader reader = new BufferedReader(new FileReader(sqlFile));
                         PrintWriter writer = new PrintWriter(process.getOutputStream())) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            writer.println(line);
                        }
                    }

                    if (process.waitFor() == 0) {
                        JOptionPane.showMessageDialog(panel, "数据库恢复成功！");
                    } else {
                        JOptionPane.showMessageDialog(panel, "恢复失败！");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "恢复失败: " + ex.getMessage());
                }
            }
        });

        // 添加组件到备份面板
        backupPanel.add(pathPanel);
        backupPanel.add(backupButton);
        backupPanel.add(restoreButton);

        // 创建系统信息面板
        JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        infoPanel.setBackground(Color.WHITE);
        infoPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)),
            "系统信息",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("微软雅黑", Font.BOLD, 14)
        ));

        // 添加系统信息
        addInfoField(infoPanel, "操作系统", System.getProperty("os.name"));
        addInfoField(infoPanel, "系统版本", System.getProperty("os.version"));
        addInfoField(infoPanel, "Java版本", System.getProperty("java.version"));
        addInfoField(infoPanel, "数据库版本", getMySQLVersion());
        addInfoField(infoPanel, "系统内存", getSystemMemory());
        addInfoField(infoPanel, "CPU核心数", Runtime.getRuntime().availableProcessors() + "核");

        // 创建主面板的上下布局
        JPanel mainPanel = new JPanel(new GridLayout(2, 1, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.add(backupPanel);
        mainPanel.add(infoPanel);

        panel.add(mainPanel, BorderLayout.CENTER);

        return panel;
    }

    private void addInfoField(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label + ":", JLabel.RIGHT);
        JLabel valueComp = new JLabel(value, JLabel.LEFT);
        labelComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        valueComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(labelComp);
        panel.add(valueComp);
    }

    private String getMySQLVersion() {
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT VERSION()");
            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "未知";
    }

    private String getSystemMemory() {
        long totalMemory = ((com.sun.management.OperatingSystemMXBean) 
            ManagementFactory.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
        return String.format("%.1f GB", totalMemory / (1024.0 * 1024.0 * 1024.0));
    }
}

class MainPanel extends JPanel {
    public MainPanel() {
        setLayout(new BorderLayout());

        // 设置渐变背景
        setBackground(new Color(240, 248, 255)); // 淡蓝色背景
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建标题面板
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        JLabel titleLabel = new JLabel("学生成绩管理系统", JLabel.CENTER);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 32));
        titleLabel.setForeground(new Color(51, 51, 51));
        titlePanel.add(titleLabel);

        // 创建按钮面板
        JPanel buttonPanel = new JPanel(new GridLayout(3, 1, 20, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 40, 100));

        // 创建现代化按钮
        JButton btnStudentLogin = createModernButton("学生登录");
        JButton btnTeacherLogin = createModernButton("教师登录");
        JButton btnAdminLogin = createModernButton("管理员登录");

        // 添加按钮事件
        btnStudentLogin.addActionListener(e -> studentLogin());
        btnTeacherLogin.addActionListener(e -> teacherLogin());
        btnAdminLogin.addActionListener(e -> adminLogin());

        // 添加按钮到面板
        buttonPanel.add(btnStudentLogin);
        buttonPanel.add(btnTeacherLogin);
        buttonPanel.add(btnAdminLogin);

        // 添加版权信息
        JPanel footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        JLabel footerLabel = new JLabel("© 2024 学生成绩管理系统 版权所有", JLabel.CENTER);
        footerLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        footerLabel.setForeground(new Color(128, 128, 128));
        footerPanel.add(footerLabel);

        // 将所有面板添加到主面板
        add(titlePanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(footerPanel, BorderLayout.SOUTH);
    }

    private JButton createModernButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 18));
        button.setForeground(new Color(51, 51, 51));  // 修改为黑色
        button.setBackground(new Color(70, 130, 180));
        button.setPreferredSize(new Dimension(250, 60));
        button.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // 添加悬停效果
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(100, 160, 210));
                button.setForeground(new Color(0, 0, 0));  // 悬停时更深的黑色
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(70, 130, 180));
                button.setForeground(new Color(51, 51, 51));  // 恢复原来的黑色
            }
        });

        return button;
    }

    private void studentLogin() {
        login("学生", this::showStudentFeatures);
    }

    private void teacherLogin() {
        login("教师", this::showTeacherFeatures);
    }

    private void adminLogin() {
        login("管理员", this::showAdminFeatures);
    }

    private void login(String role, LoginCallback callback) {
        JTextField idField = new JTextField(15);
        JPasswordField passwordField = new JPasswordField(15);
        JPanel loginPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 设置字体和颜色
        JLabel idLabel = new JLabel("ID:");
        JLabel passwordLabel = new JLabel("密码:");
        Font labelFont = new Font("微软雅黑", Font.BOLD, 14);
        Font fieldFont = new Font("微软雅黑", Font.PLAIN, 14);

        idLabel.setFont(labelFont);
        passwordLabel.setFont(labelFont);
        idField.setFont(fieldFont);
        passwordField.setFont(fieldFont);

        loginPanel.add(idLabel);
        loginPanel.add(idField);
        loginPanel.add(passwordLabel);
        loginPanel.add(passwordField);

        // 设置对话框标题字体
        UIManager.put("OptionPane.messageFont", new Font("微软雅黑", Font.PLAIN, 14));
        UIManager.put("OptionPane.buttonFont", new Font("微软雅黑", Font.PLAIN, 14));

        int option = JOptionPane.showConfirmDialog(this, loginPanel, role + "登录", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String id = idField.getText();
            String password = new String(passwordField.getPassword());
            callback.showFeatures(id, password);
        }
    }

    private void showStudentFeatures(String studentId, String password) {
        // 创建学生功能窗口
        JFrame studentFrame = new JFrame("学生信息管理系统");
        studentFrame.setSize(1200, 800);  // 增加窗口大小
        studentFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        studentFrame.setMinimumSize(new Dimension(1000, 700)); // 设置最小尺寸

        // 设置窗口图标
        ImageIcon icon = new ImageIcon("C:\\Users\\ASUS\\Desktop\\微信图片_20241031111338.jpg");
        studentFrame.setIconImage(icon.getImage());

        // 添加学生面板
        studentFrame.add(new StudentPanel(studentId));

        // 居中显示
        studentFrame.setLocationRelativeTo(null);
        studentFrame.setVisible(true);
    }

    private void showTeacherFeatures(String teacherId, String password) {
        JFrame teacherFrame = new JFrame("教师功能");
        teacherFrame.setSize(1200, 800);  // 增加窗口大小
        teacherFrame.setMinimumSize(new Dimension(1000, 700)); // 设置最小尺寸
        teacherFrame.add(new TeacherPanel());
        teacherFrame.setLocationRelativeTo(null);
        teacherFrame.setVisible(true);
    }

    private void showAdminFeatures(String adminId, String password) {
        JFrame adminFrame = new JFrame("管理员系统");
        adminFrame.setSize(1200, 800);  // 增加窗口大小
        adminFrame.setMinimumSize(new Dimension(1000, 700)); // 设置最小尺寸
        adminFrame.add(new AdminPanel());
        adminFrame.setLocationRelativeTo(null);
        adminFrame.setVisible(true);
    }

    interface LoginCallback {
        void showFeatures(String id, String password);
    }
}

/// 学生功能面板
class StudentPanel extends JPanel {
    private String studentId;
    private JPanel contentPanel;
    private CardLayout cardLayout;
    private Color primaryColor = new Color(64, 158, 255);    // 主题蓝色
    private Color backgroundColor = new Color(236, 245, 255); // 浅蓝色背景
    private Color sidebarColor = new Color(255, 255, 255);   // 白色侧边栏
    private Color textColor = new Color(51, 51, 51);         // 文字颜色
    private final String DB_URL = "jdbc:mysql://localhost:3306/student?useSSL=false&serverTimezone=UTC";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "1405269390a";
    private Connection conn;  // 添加 Connection 变量

    public StudentPanel(String studentId) {
        super();
        this.studentId = studentId;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接失败: " + e.getMessage());
        }
        setLayout(new BorderLayout(0, 0));
        setBackground(backgroundColor);

        // 创建左侧导航面板
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(280, 0));
        sidePanel.setBackground(sidebarColor);
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(225, 235, 245))); // 淡蓝色边框

        // 创建头像和用户信息面板
        JPanel profilePanel = new JPanel(new BorderLayout(0, 15));
        profilePanel.setBackground(sidebarColor);
        profilePanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 30, 20));

        // 头像面板
        JPanel avatarPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        avatarPanel.setOpaque(false);

        // 创建圆形头像
        ImageIcon originalIcon = new ImageIcon("C:\\Users\\ASUS\\Desktop\\微信图片_20241031111338.jpg");
        Image scaledImage = originalIcon.getImage().getScaledInstance(140, 140, Image.SCALE_SMOOTH);
        JLabel avatarLabel = new JLabel(new ImageIcon(scaledImage)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Dimension arcs = new Dimension(140, 140);
                int width = getWidth();
                int height = getHeight();

                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, width-1, height-1, arcs.width, arcs.height);

                super.paintComponent(g);
            }
        };
        avatarLabel.setBorder(BorderFactory.createLineBorder(primaryColor, 3));
        avatarPanel.add(avatarLabel);

        // 用户信息面板
        JPanel userInfoPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        userInfoPanel.setOpaque(false);

        // 从数据库获取用户名
        String userName = "同学";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student?useSSL=false&serverTimezone=UTC",
                    "root", "1405269390a");
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT name FROM student WHERE stuid1 = ?")) {
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    userName = rs.getString("name");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JLabel nameLabel = new JLabel(userName, JLabel.CENTER);
        nameLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        nameLabel.setForeground(textColor);

        JLabel idLabel = new JLabel("学号: " + studentId, JLabel.CENTER);
        idLabel.setFont(new Font("微软雅黑", Font.PLAIN, 16));
        idLabel.setForeground(new Color(102, 102, 102));

        userInfoPanel.add(nameLabel);
        userInfoPanel.add(idLabel);

        profilePanel.add(avatarPanel, BorderLayout.CENTER);
        profilePanel.add(userInfoPanel, BorderLayout.SOUTH);

        // 创建导航按钮面板
        JPanel navPanel = new JPanel(new GridLayout(4, 1, 0, 1));
        navPanel.setOpaque(false);
        navPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        // 创建导航按钮（移除了系统设置）
        addNavButton(navPanel, "📊  成绩查询", "grades", true);
        addNavButton(navPanel, "👤  个人资料", "profile", false);
        addNavButton(navPanel, "📚  课程信息", "courses", false);
        addNavButton(navPanel, "📅  课程表", "schedule", false);

        sidePanel.add(profilePanel, BorderLayout.NORTH);
        sidePanel.add(navPanel, BorderLayout.CENTER);

        // 创建主内容面板
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(backgroundColor);
        mainContent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建内容切换面板
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setOpaque(false);

        // 添加各个功能面板（移除了设置面板）
        contentPanel.add(createGradesPanel(), "grades");
        contentPanel.add(createProfilePanel(), "profile");
        contentPanel.add(createCoursesPanel(), "courses");
        contentPanel.add(createSchedulePanel(), "schedule");

        mainContent.add(contentPanel, BorderLayout.CENTER);

        // 添加到主面板
        add(sidePanel, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
    }

    private void addNavButton(JPanel panel, String text, String cardName, boolean isSelected) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.BOLD, 16));
        button.setForeground(isSelected ? primaryColor : textColor);
        button.setBackground(isSelected ? backgroundColor : sidebarColor);
        button.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent evt) {
                if (!button.getForeground().equals(primaryColor)) {
                    button.setBackground(backgroundColor);
                }
            }

            public void mouseExited(MouseEvent evt) {
                if (!button.getForeground().equals(primaryColor)) {
                    button.setBackground(sidebarColor);
                }
            }

            public void mousePressed(MouseEvent evt) {
                for (Component c : panel.getComponents()) {
                    if (c instanceof JButton) {
                        JButton b = (JButton) c;
                        b.setForeground(textColor);
                        b.setBackground(sidebarColor);
                    }
                }
                button.setForeground(primaryColor);
                button.setBackground(backgroundColor);
                cardLayout.show(contentPanel, cardName);
            }
        });

        panel.add(button);
    }

    // 创建成绩查询面板
    private JPanel createGradesPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建表格
        String[] columnNames = {"学号", "姓名", "高数", "马原", "英语", "线代", "总分", "平均分"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));
        table.setEnabled(false); // 设置表格不可编辑

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 从数据库加载学生成绩
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student?useSSL=false&serverTimezone=UTC",
                    "root", "1405269390a");
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT s.stuid1, s.name, sg.math, sg.MaYuan, sg.English, sg.linemath " +
                                 "FROM student s " +
                                 "LEFT JOIN studentgrade sg ON s.stuid1 = sg.stuid2 " +
                                 "WHERE s.stuid1 = ?")) {

                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    // 获取成绩
                    int math = rs.getObject("math") != null ? rs.getInt("math") : 0;
                    int mayuan = rs.getObject("MaYuan") != null ? rs.getInt("MaYuan") : 0;
                    int english = rs.getObject("English") != null ? rs.getInt("English") : 0;
                    int linemath = rs.getObject("linemath") != null ? rs.getInt("linemath") : 0;

                    // 计算总分和平均分
                    int total = math + mayuan + english + linemath;
                    double average = total / 4.0;

                    // 添加到表格
                    model.addRow(new Object[]{
                            rs.getString("stuid1"),
                            rs.getString("name"),
                            math > 0 ? math : "-",
                            mayuan > 0 ? mayuan : "-",
                            english > 0 ? english : "-",
                            linemath > 0 ? linemath : "-",
                            total > 0 ? total : "-",
                            total > 0 ? String.format("%.1f", average) : "-"
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(panel, "加载成绩失败: " + e.getMessage());
        }

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 创建个人资料面板
    private JPanel createProfilePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 从数据库获取个人信息
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/student?useSSL=false&serverTimezone=UTC",
                    "root", "1405269390a");
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT * FROM student WHERE stuid1 = ?")) {

                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    JPanel infoPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    infoPanel.setBackground(Color.WHITE);

                    addInfoField(infoPanel, "学号:", rs.getString("stuid1"));
                    addInfoField(infoPanel, "姓名:", rs.getString("name"));
                    addInfoField(infoPanel, "电话:", rs.getString("telenumber"));
                    addInfoField(infoPanel, "邮箱:", rs.getString("qqmail"));
                    addInfoField(infoPanel, "班级:", rs.getString("classnumber"));

                    panel.add(infoPanel, BorderLayout.NORTH);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return panel;
    }

    private void addInfoField(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label);
        JLabel valueComp = new JLabel(value);

        labelComp.setFont(new Font("微软雅黑", Font.BOLD, 14));
        valueComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        panel.add(labelComp);
        panel.add(valueComp);
    }

    // 创建课程信息面板
    private JPanel createCoursesPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        String[] columnNames = {"课程名称", "学分", "上课时间", "教师"};
        Object[][] data = {
                {"高等数学", "4.0", "周一 1-2节", "张教授"},
                {"马原", "3.0", "周二 3-4节", "李教授"},
                {"英语", "4.0", "周三 1-2节", "王教授"},
                {"线性代数", "3.0", "周四 5-6节", "刘教授"}
        };

        JTable table = new JTable(data, columnNames);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(30);

        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    // 创建设置面板
    private JPanel createSettingsPanel() {
        JPanel panel = new JPanel(new GridLayout(4, 1, 10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton btnTheme = new JButton("主题设置");
        JButton btnPassword = new JButton("修改密码");
        JButton btnNotification = new JButton("通知设置");
        JButton btnAbout = new JButton("关于系统");

        // 统一按钮样式
        for (JButton btn : new JButton[]{btnTheme, btnPassword, btnNotification, btnAbout}) {
            btn.setFont(new Font("微软雅黑", Font.PLAIN, 14));
            btn.setFocusPainted(false);
        }

        panel.add(btnTheme);
        panel.add(btnPassword);
        panel.add(btnNotification);
        panel.add(btnAbout);

        return panel;
    }

    private JPanel createSchedulePanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 添加标题和周次选择
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setBackground(Color.WHITE);

        JLabel titleLabel = new JLabel("课程表", JLabel.LEFT);
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 24));
        titleLabel.setForeground(new Color(51, 51, 51));

        String[] weeks = new String[20];
        for (int i = 0; i < 20; i++) {
            weeks[i] = "第" + (i + 1) + "周";
        }
        JComboBox<String> weekSelector = new JComboBox<>(weeks);
        weekSelector.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        weekSelector.setPreferredSize(new Dimension(120, 35));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(weekSelector, BorderLayout.EAST);

        // 创建课程表面板
        JPanel schedulePanel = new JPanel(new GridLayout(6, 6, 1, 1));
        schedulePanel.setBackground(new Color(220, 220, 220));
        schedulePanel.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        // 添加时间列标题
        schedulePanel.add(createHeaderCell("时间/星期"));
        schedulePanel.add(createHeaderCell("周一"));
        schedulePanel.add(createHeaderCell("周二"));
        schedulePanel.add(createHeaderCell("周三"));
        schedulePanel.add(createHeaderCell("周四"));
        schedulePanel.add(createHeaderCell("周五"));

        // 课程时间
        String[] times = {
                "1-2节\n08:00\n09:35",
                "3-4节\n10:05\n11:40",
                "5-6节\n14:00\n15:35",
                "7-8节\n15:55\n17:30",
                "9-10节\n18:30\n20:05"
        };

        // 课程数据
        String[][] schedule = {
                {"", "", "", "", ""},  // 第1-2节
                {"", "", "", "", ""},  // 第3-4节
                {"", "", "", "", ""},  // 第5-6节
                {"", "", "", "", ""},  // 第7-8节
                {"", "", "", "", ""}   // 第9-10节
        };

        // 添加一些示例课程
        schedule[0][0] = "高等数学@A101";  // 周一1-2节
        schedule[1][1] = "马原@C305";      // 周二3-4节
        schedule[0][2] = "英语@B203";      // 周三1-2节
        schedule[2][3] = "线性代数@A102";  // 周四5-6节

        // 填充课程表
        for (int i = 0; i < 5; i++) {
            schedulePanel.add(createTimeCell(times[i]));
            for (int j = 0; j < 5; j++) {
                if (schedule[i][j].isEmpty()) {
                    schedulePanel.add(createEmptyCell());
                } else {
                    schedulePanel.add(createCourseCell(schedule[i][j]));
                }
            }
        }

        // 创建带圆角和阴影的面板
        JPanel contentPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.add(schedulePanel);

        // 将所有组件添加到主面板
        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createHeaderCell(String text) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(new Color(245, 245, 245));

        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.BOLD, 14));
        label.setForeground(new Color(51, 51, 51));
        label.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

        cell.add(label, BorderLayout.CENTER);
        return cell;
    }

    private JPanel createTimeCell(String text) {
        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(new Color(250, 250, 250));

        JLabel label = new JLabel("<html>" + text.replace("\n", "<br>") + "</html>", JLabel.CENTER);
        label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        label.setForeground(new Color(102, 102, 102));
        label.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        cell.add(label, BorderLayout.CENTER);
        return cell;
    }

    private JPanel createCourseCell(String courseInfo) {
        String[] parts = courseInfo.split("@");
        String courseName = parts[0];
        String location = parts.length > 1 ? parts[1] : "";

        JPanel cell = new JPanel(new BorderLayout());
        cell.setBackground(new Color(230, 247, 255));
        cell.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel courseLabel = new JLabel("<html><center>" + courseName + "<br><small>" + location + "</small></center></html>", JLabel.CENTER);
        courseLabel.setFont(new Font("微软雅黑", Font.BOLD, 13));
        courseLabel.setForeground(new Color(24, 144, 255));

        cell.add(courseLabel, BorderLayout.CENTER);
        return cell;
    }

    private JPanel createEmptyCell() {
        JPanel cell = new JPanel();
        cell.setBackground(Color.WHITE);
        return cell;
    }

    private JPanel createCourseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建控制面板
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        controlPanel.setBackground(Color.WHITE);

        // 创建课程表格
        String[] columnNames = {"课程编号", "课程名称", "学分", "授课教师", "上课时间", "上课地点"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 设置表格不可编辑
            }
        };

        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 从数据库加载课程数据
        loadCourseData(model);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(controlPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void loadCourseData(DefaultTableModel model) {
        try {
            String sql = "SELECT * FROM course ORDER BY course_id";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            model.setRowCount(0); // 清空表格

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("course_id"),
                        rs.getString("course_name"),
                        rs.getDouble("credit"),
                        rs.getString("teacher"),
                        rs.getString("class_time"),
                        rs.getString("classroom")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载课程数据失败: " + e.getMessage());
        }
    }

    public void showResultsInNewWindow(String title, DefaultTableModel model) {
        JFrame frame = new JFrame(title);
        frame.setSize(800, 400);
        frame.setLocationRelativeTo(null);

        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane scrollPane = new JScrollPane(table);
        frame.add(scrollPane);
        frame.setVisible(true);
    }

    public void editGrades(String studentId) {
        try {
            // 先查询学生信息
            String sql = "SELECT s.stuid1, s.name, sg.math, sg.MaYuan, sg.English, sg.linemath " +
                    "FROM student s " +
                    "LEFT JOIN studentgrade sg ON s.stuid1 = sg.stuid2 " +
                    "WHERE s.stuid1 = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String studentName = rs.getString("name");
                Integer math = rs.getObject("math") != null ? rs.getInt("math") : null;
                Integer mayuan = rs.getObject("MaYuan") != null ? rs.getInt("MaYuan") : null;
                Integer english = rs.getObject("English") != null ? rs.getInt("English") : null;
                Integer linemath = rs.getObject("linemath") != null ? rs.getInt("linemath") : null;

                // 创建输入面板
                JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                JTextField mathField = new JTextField(math != null ? math.toString() : "");
                JTextField mayuanField = new JTextField(mayuan != null ? mayuan.toString() : "");
                JTextField englishField = new JTextField(english != null ? english.toString() : "");
                JTextField linemathField = new JTextField(linemath != null ? linemath.toString() : "");

                inputPanel.add(new JLabel("高数:"));
                inputPanel.add(mathField);
                inputPanel.add(new JLabel("马原:"));
                inputPanel.add(mayuanField);
                inputPanel.add(new JLabel("英语:"));
                inputPanel.add(englishField);
                inputPanel.add(new JLabel("线代:"));
                inputPanel.add(linemathField);

                int result = JOptionPane.showConfirmDialog(null, inputPanel,
                        "编辑 " + studentName + " 的成绩", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    try {
                        // 验证输入
                        Map<String, Integer> grades = new HashMap<>();

                        // 验证并解析成绩
                        if (!mathField.getText().trim().isEmpty()) {
                            int mathScore = Integer.parseInt(mathField.getText().trim());
                            if (mathScore < 0 || mathScore > 100) {
                                JOptionPane.showMessageDialog(null, "成绩必须在0-100之间！");
                                return;
                            }
                            grades.put("math", mathScore);
                        }

                        if (!mayuanField.getText().trim().isEmpty()) {
                            int mayuanScore = Integer.parseInt(mayuanField.getText().trim());
                            if (mayuanScore < 0 || mayuanScore > 100) {
                                JOptionPane.showMessageDialog(null, "成绩必须在0-100之间！");
                                return;
                            }
                            grades.put("MaYuan", mayuanScore);
                        }

                        if (!englishField.getText().trim().isEmpty()) {
                            int englishScore = Integer.parseInt(englishField.getText().trim());
                            if (englishScore < 0 || englishScore > 100) {
                                JOptionPane.showMessageDialog(null, "成绩必须在0-100之间！");
                                return;
                            }
                            grades.put("English", englishScore);
                        }

                        if (!linemathField.getText().trim().isEmpty()) {
                            int linemathScore = Integer.parseInt(linemathField.getText().trim());
                            if (linemathScore < 0 || linemathScore > 100) {
                                JOptionPane.showMessageDialog(null, "成绩必须在0-100之间！");
                                return;
                            }
                            grades.put("linemath", linemathScore);
                        }

                        // 更新数据库
                        updateGrades(studentId, grades);
                    } catch (NumberFormatException e) {
                        JOptionPane.showMessageDialog(null, "请输入有效的成绩！");
                    }
                }
            } else {
                JOptionPane.showMessageDialog(null, "未找到该学生信息！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "查询学生信息失败: " + e.getMessage());
        }
    }

    private void updateGrades(String studentId, Map<String, Integer> grades) {
        try {
            String checkSql = "SELECT * FROM studentgrade WHERE stuid2 = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, studentId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                // 更新现有记录
                StringBuilder updateSql = new StringBuilder("UPDATE studentgrade SET ");
                List<String> updates = new ArrayList<>();
                List<Object> values = new ArrayList<>();

                for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                    updates.add(entry.getKey() + " = ?");
                    values.add(entry.getValue());
                }

                updateSql.append(String.join(", ", updates));
                updateSql.append(" WHERE stuid2 = ?");

                PreparedStatement updateStmt = conn.prepareStatement(updateSql.toString());
                int paramIndex = 1;
                for (Object value : values) {
                    updateStmt.setObject(paramIndex++, value);
                }
                updateStmt.setString(paramIndex, studentId);
                updateStmt.executeUpdate();
            } else {
                // 插入新记录
                StringBuilder insertSql = new StringBuilder("INSERT INTO studentgrade (stuid2");
                StringBuilder valuesSql = new StringBuilder(") VALUES (?");
                List<Object> values = new ArrayList<>();
                values.add(studentId);

                for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                    insertSql.append(", ").append(entry.getKey());
                    valuesSql.append(", ?");
                    values.add(entry.getValue());
                }
                insertSql.append(valuesSql).append(")");

                PreparedStatement insertStmt = conn.prepareStatement(insertSql.toString());
                for (int i = 0; i < values.size(); i++) {
                    insertStmt.setObject(i + 1, values.get(i));
                }
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "更新成绩失败: " + e.getMessage());
        }
    }

    private void loadAllGrades(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT s.stuid1, s.name, g.math, g.MaYuan, g.English, g.linemath " +
                    "FROM student s LEFT JOIN studentgrade g ON s.stuid1 = g.stuid2";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String studentId = rs.getString("stuid1");
                String name = rs.getString("name");
                Integer math = rs.getObject("math") != null ? rs.getInt("math") : null;
                Integer mayuan = rs.getObject("MaYuan") != null ? rs.getInt("MaYuan") : null;
                Integer english = rs.getObject("English") != null ? rs.getInt("English") : null;
                Integer linemath = rs.getObject("linemath") != null ? rs.getInt("linemath") : null;

                int validCount = 0;
                int total = 0;

                if (math != null) { total += math; validCount++; }
                if (mayuan != null) { total += mayuan; validCount++; }
                if (english != null) { total += english; validCount++; }
                if (linemath != null) { total += linemath; validCount++; }

                model.addRow(new Object[]{
                        studentId,
                        name,
                        math != null ? math : "-",
                        mayuan != null ? mayuan : "-",
                        english != null ? english : "-",
                        linemath != null ? linemath : "-",
                        validCount > 0 ? total : "-",
                        validCount > 0 ? String.format("%.2f", total * 1.0 / validCount) : "-"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载成绩失败: " + e.getMessage());
        }
    }
}

class TeacherPanel extends JPanel {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private Color primaryColor = new Color(64, 158, 255);
    private Color backgroundColor = new Color(245, 247, 250);
    private Color sidebarColor = new Color(255, 255, 255);
    private Color textColor = new Color(51, 51, 51);
    private Connection conn = null;
    private final String DB_URL = "jdbc:mysql://localhost:3306/student?useSSL=false&serverTimezone=UTC";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = "1405269390a";
    private JTable table;

    public TeacherPanel() {
        initializeDatabase();
        initializeUI();
    }

    private void initializeDatabase() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "数据库连接失败: " + e.getMessage());
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout(0, 0));
        setBackground(backgroundColor);

        // 创建侧边栏
        JPanel sidebar = new JPanel(new GridLayout(0, 1, 10, 10));
        sidebar.setBackground(sidebarColor);
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建内容面板
        contentPanel = new JPanel();
        cardLayout = new CardLayout();
        contentPanel.setLayout(cardLayout);
        contentPanel.setBackground(backgroundColor);

        // 添加功能按钮
        JButton queryButton = createStyledButton("查询成绩", true);
        JButton modifyButton = createStyledButton("修改成绩", false);
        JButton addButton = createStyledButton("添加成绩", false);
        JButton importButton = createStyledButton("导入成绩", false);
        JButton exportButton = createStyledButton("导出成绩", false);
        JButton analysisButton = createStyledButton("成绩分析", false);

        queryButton.addActionListener(e -> cardLayout.show(contentPanel, "query"));
        modifyButton.addActionListener(e -> cardLayout.show(contentPanel, "modify"));
        addButton.addActionListener(e -> cardLayout.show(contentPanel, "add"));
        importButton.addActionListener(e -> cardLayout.show(contentPanel, "import"));
        exportButton.addActionListener(e -> cardLayout.show(contentPanel, "export"));
        analysisButton.addActionListener(e -> cardLayout.show(contentPanel, "analysis"));

        sidebar.add(queryButton);
        sidebar.add(modifyButton);
        sidebar.add(addButton);
        sidebar.add(importButton);
        sidebar.add(exportButton);
        sidebar.add(analysisButton);

        // 添加功能面板
        contentPanel.add(createQueryPanel(), "query");
        contentPanel.add(createModifyPanel(), "modify");
        contentPanel.add(createAddPanel(), "add");
        contentPanel.add(createImportPanel(), "import");
        contentPanel.add(createExportPanel(), "export");
        contentPanel.add(createAnalysisPanel(), "analysis");

        // 添加到主面板
        add(sidebar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        // 默认显示查询面板
        cardLayout.show(contentPanel, "query");
    }

    private JButton createStyledButton(String text, boolean isPrimary) {
        JButton button = new JButton(text);
        button.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setPreferredSize(new Dimension(100, 35));

        if (isPrimary) {
            button.setBackground(new Color(64, 158, 255));
            button.setForeground(Color.WHITE);
        } else {
            button.setBackground(new Color(245, 247, 250));
            button.setForeground(new Color(96, 98, 102));
        }

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(new Color(102, 177, 255));
                } else {
                    button.setBackground(new Color(236, 239, 241));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (isPrimary) {
                    button.setBackground(new Color(64, 158, 255));
                } else {
                    button.setBackground(new Color(245, 247, 250));
                }
            }
        });

        return button;
    }

    private static class ModifyButtonEditor extends DefaultCellEditor {
        private final JButton button;
        private final BiConsumer<JTable, Integer> modifyAction;

        public ModifyButtonEditor(BiConsumer<JTable, Integer> modifyAction) {
            super(new JTextField());
            this.modifyAction = modifyAction;
            button = new JButton();
            button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
            button.setForeground(new Color(64, 158, 255));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            button.setText("修改");
            button.addActionListener(e -> modifyAction.accept(table, row));
            return button;
        }
    }

    private JPanel createQueryPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(15);
        JButton searchButton = createStyledButton("搜索学生", true);
        searchPanel.add(new JLabel("请输入要查询的学生学号："));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 创建表格
        String[] columnNames = {"学号", "姓名", "班级", "电话", "邮箱", "操作"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 设置操作列
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JButton modifyBtn = new JButton("查询");
                modifyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                modifyBtn.setForeground(new Color(64, 158, 255));
                return modifyBtn;
            }
        });

        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private final JButton button;
            {
                button = new JButton();
                button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                button.setForeground(new Color(64, 158, 255));
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                button.setText("查询");
                button.addActionListener(e -> {
                    String studentId = table.getValueAt(row, 0).toString();
                    String name = table.getValueAt(row, 1).toString();
                    String classNumber = table.getValueAt(row, 2).toString();
                    String phone = table.getValueAt(row, 3).toString();
                    String email = table.getValueAt(row, 4).toString();

                    JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JTextField nameField = new JTextField(name);
                    JTextField classField = new JTextField(classNumber);
                    JTextField phoneField = new JTextField(phone);
                    JTextField emailField = new JTextField(email);

                    inputPanel.add(new JLabel("姓名:"));
                    inputPanel.add(nameField);
                    inputPanel.add(new JLabel("班级:"));
                    inputPanel.add(classField);
                    inputPanel.add(new JLabel("电话:"));
                    inputPanel.add(phoneField);
                    inputPanel.add(new JLabel("邮箱:"));
                    inputPanel.add(emailField);

                    int result = JOptionPane.showConfirmDialog(null, inputPanel,
                            "查询学生信息", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            String sql = "SELECT * FROM student WHERE stuid1 = ?";
                            PreparedStatement pstmt = conn.prepareStatement(sql);
                            pstmt.setString(1, studentId);
                            ResultSet rs = pstmt.executeQuery();

                            model.setRowCount(0);
                            while (rs.next()) {
                                model.addRow(new Object[]{
                                        rs.getString("stuid1"),
                                        rs.getString("name"),
                                        rs.getString("classnumber"),
                                        rs.getString("telenumber"),
                                        rs.getString("qqmail"),
                                        "查询"
                                });
                            }

                            if (model.getRowCount() == 0) {
                                JOptionPane.showMessageDialog(null, "未找到该学生信息");
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "查询失败: " + ex.getMessage());
                        }
                    }
                    fireEditingStopped();
                });
                return button;
            }
        });

        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String studentId = searchField.getText().trim();
            if (!studentId.isEmpty()) {
                try {
                    String sql = "SELECT * FROM student WHERE stuid1 = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, studentId);
                    ResultSet rs = pstmt.executeQuery();

                    model.setRowCount(0);
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getString("stuid1"),
                                rs.getString("name"),
                                rs.getString("classnumber"),
                                rs.getString("telenumber"),
                                rs.getString("qqmail"),
                                "查询"
                        });
                    }

                    if (model.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(null, "未找到该学生信息");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "查询失败: " + ex.getMessage());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createModifyPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        searchPanel.setBackground(Color.WHITE);

        JTextField searchField = new JTextField(15);
        JButton searchButton = createStyledButton("搜索学生", true);
        searchPanel.add(new JLabel("请输入要修改的学生学号："));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 创建表格
        String[] columnNames = {"学号", "高数", "马原", "线代", "英语", "操作"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(40);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(245, 247, 250));

        // 设置表格内容居中
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < table.getColumnCount() - 1; i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // 设置操作列
        table.getColumnModel().getColumn(5).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JButton modifyBtn = new JButton("修改");
                modifyBtn.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                modifyBtn.setForeground(new Color(64, 158, 255));
                return modifyBtn;
            }
        });

        table.getColumnModel().getColumn(5).setCellEditor(new DefaultCellEditor(new JTextField()) {
            private final JButton button;
            {
                button = new JButton();
                button.setFont(new Font("微软雅黑", Font.PLAIN, 12));
                button.setForeground(new Color(64, 158, 255));
            }

            @Override
            public Component getTableCellEditorComponent(JTable table, Object value,
                                                         boolean isSelected, int row, int column) {
                button.setText("修改");
                button.addActionListener(e -> {
                    String studentId = table.getValueAt(row, 0).toString();
                    String name = table.getValueAt(row, 1).toString();
                    String classNumber = table.getValueAt(row, 2).toString();
                    String phone = table.getValueAt(row, 3).toString();
                    String email = table.getValueAt(row, 4).toString();

                    JPanel inputPanel = new JPanel(new GridLayout(0, 2, 10, 10));
                    inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    JTextField nameField = new JTextField(name);
                    JTextField classField = new JTextField(classNumber);
                    JTextField phoneField = new JTextField(phone);
                    JTextField emailField = new JTextField(email);

                    inputPanel.add(new JLabel("姓名:"));
                    inputPanel.add(nameField);
                    inputPanel.add(new JLabel("班级:"));
                    inputPanel.add(classField);
                    inputPanel.add(new JLabel("电话:"));
                    inputPanel.add(phoneField);
                    inputPanel.add(new JLabel("邮箱:"));
                    inputPanel.add(emailField);

                    int result = JOptionPane.showConfirmDialog(null, inputPanel,
                            "修改学生信息", JOptionPane.OK_CANCEL_OPTION);

                    if (result == JOptionPane.OK_OPTION) {
                        try {
                            String sql = "UPDATE student SET name=?, classnumber=?, telenumber=?, qqmail=? WHERE stuid1=?";
                            PreparedStatement pstmt = conn.prepareStatement(sql);

                            pstmt.setString(1, nameField.getText().trim());
                            pstmt.setString(2, classField.getText().trim());
                            pstmt.setString(3, phoneField.getText().trim());
                            pstmt.setString(4, emailField.getText().trim());
                            pstmt.setString(5, studentId);

                            int affectedRows = pstmt.executeUpdate();
                            if (affectedRows > 0) {
                                JOptionPane.showMessageDialog(null, "学生信息更新成功！");
                                // 更新表格显示
                                table.setValueAt(nameField.getText().trim(), row, 1);
                                table.setValueAt(classField.getText().trim(), row, 2);
                                table.setValueAt(phoneField.getText().trim(), row, 3);
                                table.setValueAt(emailField.getText().trim(), row, 4);
                            }
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(null, "更新失败: " + ex.getMessage());
                        }
                    }
                    fireEditingStopped();
                });
                return button;
            }
        });

        // 添加搜索功能
        searchButton.addActionListener(e -> {
            String studentId = searchField.getText().trim();
            if (!studentId.isEmpty()) {
                try {
                    String sql = "SELECT * FROM student WHERE stuid1 = ?";
                    PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setString(1, studentId);
                    ResultSet rs = pstmt.executeQuery();

                    model.setRowCount(0);
                    while (rs.next()) {
                        model.addRow(new Object[]{
                                rs.getString("stuid1"),
                                rs.getString("name"),
                                rs.getString("classnumber"),
                                rs.getString("telenumber"),
                                rs.getString("qqmail"),
                                "修改"
                        });
                    }

                    if (model.getRowCount() == 0) {
                        JOptionPane.showMessageDialog(null, "未找到该学生信息");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, "查询失败: " + ex.getMessage());
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAddPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建搜索和输入面板
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(Color.WHITE);

        // 搜索面板
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        JTextField searchField = new JTextField(15);
        JButton searchButton = createStyledButton("搜索学生", true);
        searchPanel.add(new JLabel("学号："));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // 成绩输入面板
        JPanel gradePanel = new JPanel(new GridLayout(5, 2, 10, 10));
        gradePanel.setBackground(Color.WHITE);
        gradePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField mathField = new JTextField();
        JTextField mayuanField = new JTextField();
        JTextField englishField = new JTextField();
        JTextField linemathField = new JTextField();

        gradePanel.add(new JLabel("高数成绩："));
        gradePanel.add(mathField);
        gradePanel.add(new JLabel("马原成绩："));
        gradePanel.add(mayuanField);
        gradePanel.add(new JLabel("英语成绩："));
        gradePanel.add(englishField);
        gradePanel.add(new JLabel("线代成绩："));
        gradePanel.add(linemathField);

        JButton submitButton = createStyledButton("提交成绩", true);
        gradePanel.add(new JLabel(""));
        gradePanel.add(submitButton);

        // 表格面板
        String[] columns = {"学号", "姓名", "高数", "马原", "英语", "线代"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));

        // 搜索功能
        searchButton.addActionListener(e -> {
            String studentId = searchField.getText().trim();
            if (studentId.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "请输入学号");
                return;
            }

            try {
                String sql = "SELECT s.stuid1, s.name, g.math, g.MaYuan, g.English, g.linemath " +
                        "FROM student s LEFT JOIN studentgrade g ON s.stuid1 = g.stuid2 " +
                        "WHERE s.stuid1 = ?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, studentId);
                ResultSet rs = stmt.executeQuery();

                model.setRowCount(0);
                if (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getString("stuid1"),
                            rs.getString("name"),
                            rs.getObject("math"),
                            rs.getObject("MaYuan"),
                            rs.getObject("English"),
                            rs.getObject("linemath")
                    });
                } else {
                    JOptionPane.showMessageDialog(panel, "未找到该学生");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "查询失败: " + ex.getMessage());
            }
        });

        // 提交成绩功能
        submitButton.addActionListener(e -> {
            String studentId = searchField.getText().trim();
            if (studentId.isEmpty() || model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(panel, "请先搜索学生");
                return;
            }

            Map<String, Integer> grades = new HashMap<>();
            try {
                // 验证并收集成绩
                if (!mathField.getText().trim().isEmpty()) {
                    int score = Integer.parseInt(mathField.getText().trim());
                    if (score < 0 || score > 100) throw new NumberFormatException();
                    grades.put("math", score);
                }
                if (!mayuanField.getText().trim().isEmpty()) {
                    int score = Integer.parseInt(mayuanField.getText().trim());
                    if (score < 0 || score > 100) throw new NumberFormatException();
                    grades.put("MaYuan", score);
                }
                if (!englishField.getText().trim().isEmpty()) {
                    int score = Integer.parseInt(englishField.getText().trim());
                    if (score < 0 || score > 100) throw new NumberFormatException();
                    grades.put("English", score);
                }
                if (!linemathField.getText().trim().isEmpty()) {
                    int score = Integer.parseInt(linemathField.getText().trim());
                    if (score < 0 || score > 100) throw new NumberFormatException();
                    grades.put("linemath", score);
                }

                if (grades.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "请至少输入一门课程的成绩");
                    return;
                }

                updateGrades(studentId, grades);
                JOptionPane.showMessageDialog(panel, "成绩提交成功");
                searchButton.doClick(); // 刷新显示
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(panel, "请输入0-100之间的有效成绩");
            }
        });

        topPanel.add(searchPanel, BorderLayout.NORTH);
        topPanel.add(gradePanel, BorderLayout.CENTER);

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private void searchStudentGrades(DefaultTableModel model, String studentId) {
        // 删除此方法，因为管理员面板不需要处理成绩
    }

    private void deleteGrades(String studentId) {
        // 删除此方法，因为管理员面板不需要处理成绩
    }

    private void searchStudent(DefaultTableModel model, String studentId) {
        try {
            String sql = "SELECT * FROM student WHERE stuid1 = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);
            ResultSet rs = pstmt.executeQuery();

            model.setRowCount(0);
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("stuid1"),
                        rs.getString("name"),
                        rs.getString("classnumber"),
                        rs.getString("telenumber"),
                        rs.getString("qqmail")
                });
            }

            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(null, "未找到该学生信息");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "查询失败: " + e.getMessage());
        }
    }

    private void deleteStudent(String studentId) {
        try {
            String sql = "DELETE FROM student WHERE stuid1 = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, studentId);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(null, "学生删除成功！");
            } else {
                JOptionPane.showMessageDialog(null, "未找到该学生信息！");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "删除失败: " + e.getMessage());
        }
    }

    private JPanel createImportPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建顶部操作面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        JButton chooseFileButton = createStyledButton("选择文件", true);
        JButton importButton = createStyledButton("导入成绩", true);
        JLabel fileLabel = new JLabel("未选择文件");
        fileLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        topPanel.add(chooseFileButton);
        topPanel.add(importButton);
        topPanel.add(fileLabel);

        // 创建表格
        String[] columns = {"学号", "姓名", "高数", "马原", "英语", "线代", "状态"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));

        // 选择文件功能
        chooseFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV文件", "csv");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showOpenDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                fileLabel.setText(selectedFile.getName());

                // 清空表格
                model.setRowCount(0);

                // 读取CSV文件
                try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
                    String line;
                    boolean isFirstLine = true;

                    while ((line = br.readLine()) != null) {
                        if (isFirstLine) {
                            isFirstLine = false;
                            continue;
                        }

                        String[] data = line.split(",");
                        if (data.length >= 6) {
                            model.addRow(new Object[]{
                                    data[0].trim(), // 学号
                                    data[1].trim(), // 姓名
                                    data[2].trim(), // 高数
                                    data[3].trim(), // 马原
                                    data[4].trim(), // 英语
                                    data[5].trim(), // 线代
                                    "待导入"
                            });
                        }
                    }
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(panel, "读取文件失败: " + ex.getMessage());
                }
            }
        });

        // 导入功能
        importButton.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(panel, "请先选择要导入的文件");
                return;
            }

            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < model.getRowCount(); i++) {
                String studentId = model.getValueAt(i, 0).toString();
                Map<String, Integer> grades = new HashMap<>();

                try {
                    // 收集成绩
                    String mathGrade = model.getValueAt(i, 2).toString();
                    String mayuanGrade = model.getValueAt(i, 3).toString();
                    String englishGrade = model.getValueAt(i, 4).toString();
                    String linemathGrade = model.getValueAt(i, 5).toString();

                    if (!mathGrade.isEmpty()) {
                        int score = Integer.parseInt(mathGrade);
                        if (score >= 0 && score <= 100) grades.put("math", score);
                    }
                    if (!mayuanGrade.isEmpty()) {
                        int score = Integer.parseInt(mayuanGrade);
                        if (score >= 0 && score <= 100) grades.put("MaYuan", score);
                    }
                    if (!englishGrade.isEmpty()) {
                        int score = Integer.parseInt(englishGrade);
                        if (score >= 0 && score <= 100) grades.put("English", score);
                    }
                    if (!linemathGrade.isEmpty()) {
                        int score = Integer.parseInt(linemathGrade);
                        if (score >= 0 && score <= 100) grades.put("linemath", score);
                    }

                    if (!grades.isEmpty()) {
                        updateGrades(studentId, grades);
                        model.setValueAt("导入成功", i, 6);
                        successCount++;
                    } else {
                        model.setValueAt("无有效成绩", i, 6);
                        failCount++;
                    }
                } catch (NumberFormatException ex) {
                    model.setValueAt("成绩格式错误", i, 6);
                    failCount++;
                } catch (Exception ex) {
                    model.setValueAt("导入失败", i, 6);
                    failCount++;
                }
            }

            JOptionPane.showMessageDialog(panel,
                    String.format("导入完成\n成功：%d条\n失败：%d条", successCount, failCount));
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createExportPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建顶部操作面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        JButton exportButton = createStyledButton("导出成绩", true);
        JButton refreshButton = createStyledButton("刷新数据", true);
        topPanel.add(refreshButton);
        topPanel.add(exportButton);

        // 创建表格
        String[] columns = {"学号", "姓名", "高数", "马原", "英语", "线代", "总分", "平均分"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        table.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        table.setRowHeight(35);
        table.getTableHeader().setFont(new Font("微软雅黑", Font.BOLD, 14));

        // 加载所有成绩
        loadAllGrades(model);

        // 刷新功能
        refreshButton.addActionListener(e -> {
            loadAllGrades(model);
        });

        // 导出功能
        exportButton.addActionListener(e -> {
            if (model.getRowCount() == 0) {
                JOptionPane.showMessageDialog(panel, "没有可导出的数据");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setSelectedFile(new File("学生成绩表.csv"));
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV文件", "csv");
            fileChooser.setFileFilter(filter);

            int result = fileChooser.showSaveDialog(panel);
            if (result == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (!file.getName().toLowerCase().endsWith(".csv")) {
                    file = new File(file.getAbsolutePath() + ".csv");
                }

                try (FileWriter fw = new FileWriter(file);
                     BufferedWriter bw = new BufferedWriter(fw)) {

                    // 写入表头
                    StringBuilder header = new StringBuilder();
                    for (int i = 0; i < model.getColumnCount(); i++) {
                        if (i > 0) header.append(",");
                        header.append(model.getColumnName(i));
                    }
                    bw.write(header.toString());
                    bw.newLine();

                    // 写入数据
                    for (int row = 0; row < model.getRowCount(); row++) {
                        StringBuilder line = new StringBuilder();
                        for (int col = 0; col < model.getColumnCount(); col++) {
                            if (col > 0) line.append(",");
                            Object value = model.getValueAt(row, col);
                            line.append(value != null ? value.toString() : "");
                        }
                        bw.write(line.toString());
                        bw.newLine();
                    }

                    JOptionPane.showMessageDialog(panel, "成绩导出成功");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(panel, "导出失败: " + ex.getMessage());
                }
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(new JScrollPane(table), BorderLayout.CENTER);

        return panel;
    }

    private JPanel createAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // 创建顶部操作面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Color.WHITE);

        String[] subjects = {"高数", "马原", "英语", "线代"};
        JComboBox<String> subjectCombo = new JComboBox<>(subjects);
        subjectCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        String[] chartTypes = {"柱状图", "饼图", "折线图"};
        JComboBox<String> chartTypeCombo = new JComboBox<>(chartTypes);
        chartTypeCombo.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JButton analyzeButton = createStyledButton("分析成绩", true);

        topPanel.add(new JLabel("选择科目："));
        topPanel.add(subjectCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(new JLabel("图表类型："));
        topPanel.add(chartTypeCombo);
        topPanel.add(Box.createHorizontalStrut(20));
        topPanel.add(analyzeButton);

        // 创建图表和统计信息面板
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setBackground(Color.WHITE);

        // 创建图表面板（初始为空）
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBackground(Color.WHITE);

        // 创建统计信息面板
        JPanel statsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        statsPanel.setBackground(Color.WHITE);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                "统计信息",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("微软雅黑", Font.BOLD, 14)
        ));

        contentPanel.add(chartPanel);
        contentPanel.add(statsPanel);

        // 添加分析按钮事件
        analyzeButton.addActionListener(e -> {
            String subject = (String) subjectCombo.getSelectedItem();
            String chartType = (String) chartTypeCombo.getSelectedItem();

            try {
                // 获取成绩数据
                Map<String, Integer> gradeDistribution = new HashMap<>();
                int totalStudents = 0;
                double totalScore = 0;
                int maxScore = 0;
                int minScore = 100;

                String columnName = getColumnNameForSubject(subject);
                String sql = "SELECT " + columnName + " FROM studentgrade";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        int score = rs.getInt(1);
                        if (!rs.wasNull()) {
                            totalStudents++;
                            totalScore += score;
                            maxScore = Math.max(maxScore, score);
                            minScore = Math.min(minScore, score);

                            // 统计分数段
                            String range = getScoreRange(score);
                            gradeDistribution.merge(range, 1, Integer::sum);
                        }
                    }
                }

                // 更新统计信息
                statsPanel.removeAll();
                if (totalStudents > 0) {
                    addStatItem(statsPanel, "总人数", String.valueOf(totalStudents));
                    addStatItem(statsPanel, "平均分", String.format("%.2f", totalScore / totalStudents));
                    addStatItem(statsPanel, "最高分", String.valueOf(maxScore));
                    addStatItem(statsPanel, "最低分", String.valueOf(minScore));

                    // 创建并显示图表
                    JFreeChart chart = createChart(gradeDistribution, subject, chartType);
                    ChartPanel newChartPanel = new ChartPanel(chart);
                    newChartPanel.setPreferredSize(new Dimension(400, 300));

                    chartPanel.removeAll();
                    chartPanel.add(newChartPanel, BorderLayout.CENTER);
                    chartPanel.setBorder(BorderFactory.createTitledBorder(
                            BorderFactory.createLineBorder(new Color(200, 200, 200)),
                            subject + "成绩分布",
                            TitledBorder.LEFT,
                            TitledBorder.TOP,
                            new Font("微软雅黑", Font.BOLD, 14)
                    ));
                }

                // 刷新面板
                contentPanel.revalidate();
                contentPanel.repaint();

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(panel, "分析成绩失败: " + ex.getMessage());
            }
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(contentPanel, BorderLayout.CENTER);

        return panel;
    }

    private String getColumnNameForSubject(String subject) {
        switch (subject) {
            case "高数": return "math";
            case "马原": return "mayuan";
            case "英语": return "english";
            case "线代": return "linemath";
            default: return "math";
        }
    }

    private String getScoreRange(int score) {
        if (score >= 90) return "90-100";
        if (score >= 80) return "80-89";
        if (score >= 70) return "70-79";
        if (score >= 60) return "60-69";
        return "0-59";
    }

    private void addStatItem(JPanel panel, String label, String value) {
        JLabel labelComp = new JLabel(label + "：", JLabel.RIGHT);
        JLabel valueComp = new JLabel(value, JLabel.LEFT);
        labelComp.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        valueComp.setFont(new Font("微软雅黑", Font.BOLD, 14));
        panel.add(labelComp);
        panel.add(valueComp);
    }

    private JFreeChart createChart(Map<String, Integer> data, String subject, String chartType) {
        switch (chartType) {
            case "柱状图":
                return createBarChart(data, subject);
            case "饼图":
                return createPieChart(data, subject);
            case "折线图":
                return createLineChart(data, subject);
            default:
                return createBarChart(data, subject);
        }
    }

    private JFreeChart createBarChart(Map<String, Integer> data, String subject) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        for (String range : ranges) {
            dataset.addValue(data.getOrDefault(range, 0), "人数", range);
        }

        return ChartFactory.createBarChart(
                subject + "成绩分布",
                "分数段",
                "人数",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
    }

    private JFreeChart createPieChart(Map<String, Integer> data, String subject) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        for (String range : ranges) {
            dataset.setValue(range, data.getOrDefault(range, 0));
        }

        return ChartFactory.createPieChart(
                subject + "成绩分布",
                dataset,
                true,
                true,
                false
        );
    }

    private JFreeChart createLineChart(Map<String, Integer> data, String subject) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        String[] ranges = {"90-100", "80-89", "70-79", "60-69", "0-59"};
        for (String range : ranges) {
            dataset.addValue(data.getOrDefault(range, 0), "人数", range);
        }

        return ChartFactory.createLineChart(
                subject + "成绩分布",
                "分数段",
                "人数",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );
    }

    private void updateGrades(String studentId, Map<String, Integer> grades) {
        try {
            String checkSql = "SELECT * FROM studentgrade WHERE stuid2 = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, studentId);
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                StringBuilder updateSql = new StringBuilder("UPDATE studentgrade SET ");
                List<String> updates = new ArrayList<>();
                List<Object> values = new ArrayList<>();

                for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                    updates.add(entry.getKey() + " = ?");
                    values.add(entry.getValue());
                }

                updateSql.append(String.join(", ", updates));
                updateSql.append(" WHERE stuid2 = ?");

                PreparedStatement updateStmt = conn.prepareStatement(updateSql.toString());
                int paramIndex = 1;
                for (Object value : values) {
                    updateStmt.setObject(paramIndex++, value);
                }
                updateStmt.setString(paramIndex, studentId);
                updateStmt.executeUpdate();
            } else {
                StringBuilder insertSql = new StringBuilder("INSERT INTO studentgrade (stuid2");
                StringBuilder valuesSql = new StringBuilder(") VALUES (?");
                List<Object> values = new ArrayList<>();
                values.add(studentId);

                for (Map.Entry<String, Integer> entry : grades.entrySet()) {
                    insertSql.append(", ").append(entry.getKey());
                    valuesSql.append(", ?");
                    values.add(entry.getValue());
                }
                insertSql.append(valuesSql).append(")");

                PreparedStatement insertStmt = conn.prepareStatement(insertSql.toString());
                for (int i = 0; i < values.size(); i++) {
                    insertStmt.setObject(i + 1, values.get(i));
                }
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "更新成绩失败: " + e.getMessage());
        }
    }

    private void loadAllGrades(DefaultTableModel model) {
        try {
            model.setRowCount(0);
            String sql = "SELECT s.stuid1, s.name, g.math, g.MaYuan, g.English, g.linemath " +
                    "FROM student s LEFT JOIN studentgrade g ON s.stuid1 = g.stuid2";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                String studentId = rs.getString("stuid1");
                String name = rs.getString("name");
                Integer math = rs.getObject("math") != null ? rs.getInt("math") : null;
                Integer mayuan = rs.getObject("MaYuan") != null ? rs.getInt("MaYuan") : null;
                Integer english = rs.getObject("English") != null ? rs.getInt("English") : null;
                Integer linemath = rs.getObject("linemath") != null ? rs.getInt("linemath") : null;

                int validCount = 0;
                int total = 0;

                if (math != null) { total += math; validCount++; }
                if (mayuan != null) { total += mayuan; validCount++; }
                if (english != null) { total += english; validCount++; }
                if (linemath != null) { total += linemath; validCount++; }

                model.addRow(new Object[]{
                        studentId,
                        name,
                        math != null ? math : "-",
                        mayuan != null ? mayuan : "-",
                        english != null ? english : "-",
                        linemath != null ? linemath : "-",
                        validCount > 0 ? total : "-",
                        validCount > 0 ? String.format("%.2f", total * 1.0 / validCount) : "-"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载成绩失败: " + e.getMessage());
        }
    }
}

public class StudentGradeManagementSystem {
    public static void main(String[] args) {
        try {
            // 设置界面风格为系统默认外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 创建并显示主窗口
        JFrame frame = new JFrame("学生成绩管理系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 800);
        frame.setMinimumSize(new Dimension(1000, 700));

        // 添加主面板
        frame.add(new MainPanel());

        // 居中显示
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
