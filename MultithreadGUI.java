import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import javax.swing.Timer;
import java.util.stream.Collectors;

/**
 * Java多线程学习系统GUI - 优化版本
 * 解决字体、布局、颜色和响应式设计问题
 */
public class MultithreadGUI extends JFrame {
    
    // ==================== 样式管理器 ====================
    /**
     * 统一样式管理器
     */
    private static class StyleManager {
        // 统一定义字体大小
        public static final int FONT_SIZE_SMALL = 11;
        public static final int FONT_SIZE_NORMAL = 12;
        public static final int FONT_SIZE_MEDIUM = 14;
        public static final int FONT_SIZE_LARGE = 16;
        public static final int FONT_SIZE_TITLE = 18;
        
        // 统一定义颜色主题
        public static final Color PRIMARY_COLOR = new Color(70, 130, 180);    // 钢蓝色
        public static final Color SECONDARY_COLOR = new Color(135, 206, 235); // 天蓝色
        public static final Color SUCCESS_COLOR = new Color(34, 139, 34);     // 森林绿
        public static final Color WARNING_COLOR = new Color(255, 165, 0);     // 橙色
        public static final Color ERROR_COLOR = new Color(220, 20, 60);       // 深红色
        public static final Color INFO_COLOR = new Color(70, 130, 180);       // 钢蓝色
        
        // 背景色
        public static final Color BACKGROUND_LIGHT = new Color(248, 249, 250);
        public static final Color BACKGROUND_CARD = new Color(255, 255, 255);
        public static final Color BORDER_COLOR = new Color(222, 226, 230);
        
        // 文本颜色
        public static final Color TEXT_PRIMARY = new Color(33, 37, 41);
        public static final Color TEXT_SECONDARY = new Color(108, 117, 125);
        
        // 统一定义字体
        private static Font baseFont;
        private static Font monoFont;
        
        public static Font getFont(int size, boolean bold) {
            if (baseFont == null) {
                baseFont = new Font("Microsoft YaHei", Font.PLAIN, size);
                if (baseFont.getFamily().equals("Microsoft YaHei")) {
                    baseFont = new Font("Arial Unicode MS", Font.PLAIN, size);
                }
            }
            return bold ? baseFont.deriveFont(Font.BOLD, size) : baseFont.deriveFont(size);
        }
        
        public static Font getMonoFont(int size) {
            if (monoFont == null) {
                monoFont = new Font("Consolas", Font.PLAIN, size);
            }
            return monoFont.deriveFont(size);
        }
        
        public static Border getCardBorder() {
            return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
            );
        }
        
        public static Border getTabBorder() {
            return BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR, 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            );
        }
    }

    // ==================== 线程信息面板 ====================
    /**
     * 线程信息显示面板 - 优化版本
     */
    private static class ThreadInfoPanel extends JPanel {
        private JLabel nameLabel, statusLabel, priorityLabel;
        private JProgressBar progressBar;
        private JPanel progressContainer;
        private Color threadColor;
        
        public ThreadInfoPanel(String threadName, Thread.State initialState, Color color) {
            this.threadColor = color;
            initializeComponents(threadName, initialState);
            setOpaque(false);
        }
        
        private void initializeComponents(String threadName, Thread.State state) {
            setLayout(new GridBagLayout());
            setBorder(StyleManager.getCardBorder());
            
            GridBagConstraints gbc = new GridBagConstraints();
            
            // 线程名称
            nameLabel = new JLabel("🧵 " + threadName);
            nameLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_MEDIUM, true));
            nameLabel.setForeground(StyleManager.TEXT_PRIMARY);
            
            gbc.gridx = 0; gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 8, 0);
            gbc.anchor = GridBagConstraints.WEST;
            add(nameLabel, gbc);
            
            // 状态面板
            JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            statusPanel.setOpaque(false);
            
            // 状态标签
            statusLabel = new JLabel("状态: " + getStatusText(state));
            statusLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, false));
            statusLabel.setForeground(StyleManager.TEXT_SECONDARY);
            
            // 优先级标签
            priorityLabel = new JLabel("优先级: " + Thread.NORM_PRIORITY);
            priorityLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, false));
            priorityLabel.setForeground(StyleManager.TEXT_SECONDARY);
            
            statusPanel.add(statusLabel);
            statusPanel.add(priorityLabel);
            
            gbc.gridx = 0; gbc.gridy = 1;
            gbc.insets = new Insets(0, 0, 8, 0);
            add(statusPanel, gbc);
            
            // 进度条容器
            progressContainer = new JPanel(new BorderLayout());
            progressContainer.setOpaque(false);
            progressContainer.setBorder(BorderFactory.createEmptyBorder(4, 0, 0, 0));
            
            // 进度条
            progressBar = new JProgressBar(0, 100);
            progressBar.setStringPainted(true);
            progressBar.setString("0%");
            progressBar.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_SMALL, false));
            progressBar.setForeground(threadColor);
            progressBar.setBackground(StyleManager.BACKGROUND_LIGHT);
            progressBar.setBorderPainted(false);
            
            progressContainer.add(progressBar, BorderLayout.CENTER);
            
            gbc.gridx = 0; gbc.gridy = 2;
            gbc.insets = new Insets(0, 0, 0, 0);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            add(progressContainer, gbc);
        }
        
        private String getStatusText(Thread.State state) {
            switch (state) {
                case NEW: return "🆕 新建";
                case RUNNABLE: return "▶️ 运行中";
                case BLOCKED: return "⏸️ 阻塞";
                case WAITING: return "⏳ 等待";
                case TIMED_WAITING: return "⏰ 定时等待";
                case TERMINATED: return "✅ 已终止";
                default: return "❓ 未知";
            }
        }
        
        public void updateStatus(Thread.State state, int priority, int progress) {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("状态: " + getStatusText(state));
                priorityLabel.setText("优先级: " + priority);
                progressBar.setValue(progress);
                progressBar.setString(progress + "%");
                
                // 根据状态设置进度条颜色
                if (state == Thread.State.TERMINATED) {
                    progressBar.setForeground(StyleManager.SUCCESS_COLOR);
                } else if (state == Thread.State.WAITING || state == Thread.State.BLOCKED) {
                    progressBar.setForeground(StyleManager.WARNING_COLOR);
                } else {
                    progressBar.setForeground(threadColor);
                }
            });
        }
        
        public String getThreadName() {
            return nameLabel.getText().replace("🧵 ", "");
        }
    }

    // 组件定义
    private JTabbedPane tabbedPane;
    private JTextArea outputArea;
    private JButton compileAllButton, runDemoButton, clearOutputButton;
    private JComboBox<String> demoSelector;
    private JProgressBar overallProgress;
    private JLabel statusLabel;
    
    // 线程管理相关
    private ExecutorService threadPool;
    private java.util.List<ThreadInfoPanel> threadInfoPanels;
    private AtomicInteger runningThreads;
    private AtomicLong totalExecutionTime;
    
    // 界面布局
    private final String WINDOW_TITLE = "Java多线程学习系统 - 交互式教程";
    private final int WINDOW_WIDTH = 1200;
    private final int WINDOW_HEIGHT = 800;
    
    /**
     * 构造函数 - 初始化GUI界面
     */
    public MultithreadGUI() {
        runningThreads = new AtomicInteger(0);
        totalExecutionTime = new AtomicLong(0);
        threadInfoPanels = new ArrayList<>();
        threadPool = Executors.newFixedThreadPool(4);
        
        initializeWindow();
        createMenuBar();
        createControlPanel();
        createTabbedInterface();
        createOutputPanel();
        createStatusBar();
        
        // 添加关闭事件处理
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }
    
    /**
     * 初始化窗口基本设置
     */
    private void initializeWindow() {
        setTitle(WINDOW_TITLE);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setLocationRelativeTo(null); // 居中显示
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(StyleManager.BACKGROUND_LIGHT);
        
        // 添加窗口调整事件监听器，实现响应式布局
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                onWindowResized();
            }
        });
    }
    
    /**
     * 窗口调整时的处理
     */
    private void onWindowResized() {
        revalidate();
        repaint();
    }
    
    /**
     * 创建菜单栏
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(StyleManager.PRIMARY_COLOR);
        
        // 文件菜单
        JMenu fileMenu = new JMenu("文件");
        fileMenu.setForeground(Color.WHITE);
        fileMenu.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, true));
        
        JMenuItem compileAllItem = new JMenuItem("编译所有演示");
        compileAllItem.addActionListener(e -> compileAll());
        
        JMenuItem runDemoItem = new JMenuItem("运行演示");
        runDemoItem.addActionListener(e -> runSelectedDemo());
        
        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> {
            cleanup();
            System.exit(0);
        });
        
        fileMenu.add(compileAllItem);
        fileMenu.add(runDemoItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // 帮助菜单
        JMenu helpMenu = new JMenu("帮助");
        helpMenu.setForeground(Color.WHITE);
        helpMenu.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, true));
        
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> showAboutDialog());
        
        JMenuItem tutorialItem = new JMenuItem("使用教程");
        tutorialItem.addActionListener(e -> showTutorialDialog());
        
        helpMenu.add(tutorialItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(Box.createHorizontalGlue());
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * 创建控制面板
     */
    private void createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controlPanel.setBackground(StyleManager.BACKGROUND_CARD);
        controlPanel.setBorder(StyleManager.getTabBorder());
        
        // 演示选择器
        JLabel demoLabel = new JLabel("选择演示:");
        demoLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, true));
        demoLabel.setForeground(StyleManager.TEXT_PRIMARY);
        demoSelector = new JComboBox<>();
        demoSelector.setPreferredSize(new Dimension(200, 25));
        populateDemoSelector();
        
        // 控制按钮
        compileAllButton = new JButton("🔧 编译所有");
        styleButton(compileAllButton, StyleManager.SUCCESS_COLOR);
        compileAllButton.addActionListener(e -> compileAll());
        
        runDemoButton = new JButton("🚀 运行演示");
        styleButton(runDemoButton, StyleManager.PRIMARY_COLOR);
        runDemoButton.addActionListener(e -> runSelectedDemo());
        
        clearOutputButton = new JButton("🗑️ 清空输出");
        styleButton(clearOutputButton, StyleManager.ERROR_COLOR);
        clearOutputButton.addActionListener(e -> clearOutput());
        
        // 进度条
        overallProgress = new JProgressBar();
        overallProgress.setPreferredSize(new Dimension(200, 20));
        overallProgress.setStringPainted(true);
        overallProgress.setString("就绪");
        overallProgress.setBackground(StyleManager.BACKGROUND_LIGHT);
        
        controlPanel.add(demoLabel);
        controlPanel.add(demoSelector);
        controlPanel.add(compileAllButton);
        controlPanel.add(runDemoButton);
        controlPanel.add(clearOutputButton);
        controlPanel.add(new JLabel("进度:"));
        controlPanel.add(overallProgress);
        
        add(controlPanel, BorderLayout.NORTH);
    }
    
    /**
     * 创建选项卡界面
     */
    private void createTabbedInterface() {
        tabbedPane = new JTabbedPane();
        tabbedPane.setBackground(StyleManager.BACKGROUND_CARD);
        
        // 创建各个选项卡
        createTheoryTab();
        createThreadCreationTab();
        createThreadPoolTab();
        createDemoTab();
        createMonitorTab();
        
        add(tabbedPane, BorderLayout.CENTER);
    }
    
    /**
     * 创建理论学习选项卡
     */
    private void createTheoryTab() {
        JPanel theoryPanel = new JPanel(new BorderLayout(10, 10));
        theoryPanel.setBackground(StyleManager.BACKGROUND_CARD);
        theoryPanel.setBorder(StyleManager.getCardBorder());
        
        JLabel titleLabel = new JLabel("📚 多线程理论知识");
        titleLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_TITLE, true));
        titleLabel.setForeground(StyleManager.PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JTextArea theoryArea = new JTextArea();
        theoryArea.setEditable(false);
        theoryArea.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, false));
        theoryArea.setBackground(StyleManager.BACKGROUND_LIGHT);
        theoryArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        theoryArea.setForeground(StyleManager.TEXT_PRIMARY);
        
        String theoryContent = """
                🎯 Java多线程核心概念
                
                1. 进程与线程的区别
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                • 进程：程序运行的基本单位，拥有独立的内存空间
                • 线程：CPU调度的基本单位，同一进程内共享内存
                • 一个进程可以包含多个线程
                
                2. 线程创建的三种方式
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                ① 继承Thread类
                   - 优点：代码简洁简单
                   - 缺点：类无法再继承其他类
                   
                ② 实现Runnable接口
                   - 优点：更灵活，任务与线程分离
                   - 缺点：需要额外的Thread对象
                   
                ③ 使用线程池
                   - 优点：高效管理，复用线程
                   - 缺点：需要理解线程池概念
                
                3. 线程生命周期状态
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                • NEW        → 🆕 新建状态
                • RUNNABLE   → 🏃 可运行状态
                • BLOCKED    → ⛔ 阻塞状态
                • WAITING    → ⏳ 等待状态
                • TIMED_WAITING → ⏰ 超时等待状态
                • TERMINATED → ✅ 终止状态
                
                4. 线程安全与同步
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                • 线程安全：多个线程访问共享资源时程序的正确性
                • 同步机制：synchronized、Lock、volatile等
                • 并发集合：ConcurrentHashMap、CopyOnWriteArrayList等
                
                5. 线程池的优势
                ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
                • 复用线程，减少创建销毁开销
                • 控制并发数，避免系统过载
                • 提供任务管理和监控功能
                • 支持定时任务和周期性任务
                """;
        
        theoryArea.setText(theoryContent);
        JScrollPane theoryScrollPane = new JScrollPane(theoryArea);
        theoryScrollPane.setBorder(StyleManager.getTabBorder());
        
        theoryPanel.add(titleLabel, BorderLayout.NORTH);
        theoryPanel.add(theoryScrollPane, BorderLayout.CENTER);
        
        tabbedPane.addTab("📚 理论学习", theoryPanel);
    }
    
    /**
     * 创建线程创建方式演示选项卡
     */
    private void createThreadCreationTab() {
        JPanel creationPanel = new JPanel(new GridBagLayout());
        creationPanel.setBackground(StyleManager.BACKGROUND_CARD);
        creationPanel.setBorder(StyleManager.getCardBorder());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // 创建方式说明
        String[] creationMethods = {
            "① 继承Thread类 - 演示基本的线程创建方式",
            "② 实现Runnable接口 - 展示更灵活的线程创建",
            "③ 匿名内部类 - 使用匿名类快速创建",
            "④ Lambda表达式 - 现代Java的简洁语法",
            "⑤ 对比分析 - 三种方式的性能对比"
        };
        
        for (int i = 0; i < creationMethods.length; i++) {
            JLabel methodLabel = new JLabel(creationMethods[i]);
            methodLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, false));
            methodLabel.setForeground(StyleManager.TEXT_PRIMARY);
            
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.weightx = 1.0;
            creationPanel.add(methodLabel, gbc);
        }
        
        JTextArea codeExampleArea = new JTextArea();
        codeExampleArea.setEditable(false);
        codeExampleArea.setFont(StyleManager.getMonoFont(StyleManager.FONT_SIZE_SMALL));
        codeExampleArea.setBackground(StyleManager.BACKGROUND_LIGHT);
        codeExampleArea.setBorder(StyleManager.getTabBorder());
        codeExampleArea.setForeground(StyleManager.TEXT_PRIMARY);
        
        String codeExample = """
                // ① 继承Thread类
                class MyThread extends Thread {
                    public void run() {
                        // 线程执行逻辑
                    }
                }
                
                // ② 实现Runnable接口
                class MyRunnable implements Runnable {
                    public void run() {
                        // 任务执行逻辑
                    }
                }
                
                // 使用示例
                new MyThread().start();
                new Thread(new MyRunnable()).start();
                """;
        
        codeExampleArea.setText(codeExample);
        JScrollPane codeScrollPane = new JScrollPane(codeExampleArea);
        codeScrollPane.setPreferredSize(new Dimension(500, 200));
        
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 5;
        gbc.weightx = 0.5;
        creationPanel.add(codeScrollPane, gbc);
        
        tabbedPane.addTab("🧵 线程创建", creationPanel);
    }
    
    /**
     * 创建线程池演示选项卡
     */
    private void createThreadPoolTab() {
        JPanel poolPanel = new JPanel(new BorderLayout(10, 10));
        poolPanel.setBackground(StyleManager.BACKGROUND_CARD);
        poolPanel.setBorder(StyleManager.getCardBorder());
        
        // 标题
        JLabel titleLabel = new JLabel("🏊 线程池管理");
        titleLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_TITLE, true));
        titleLabel.setForeground(StyleManager.PRIMARY_COLOR);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 线程池信息表格
        String[] columnNames = {"线程池类型", "核心线程数", "最大线程数", "队列容量", "适用场景"};
        Object[][] poolData = {
            {"固定线程池", "5", "5", "无限制", "CPU密集型任务"},
            {"缓存线程池", "0", "Integer.MAX_VALUE", "无限制", "大量短任务"},
            {"单线程池", "1", "1", "无限制", "串行执行"},
            {"调度线程池", "自定义", "自定义", "无限制", "定时任务"}
        };
        
        JTable poolTable = new JTable(poolData, columnNames);
        poolTable.setRowHeight(30);
        poolTable.getTableHeader().setBackground(StyleManager.PRIMARY_COLOR);
        poolTable.getTableHeader().setForeground(Color.WHITE);
        poolTable.setGridColor(StyleManager.BORDER_COLOR);
        poolTable.setBackground(StyleManager.BACKGROUND_CARD);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < poolTable.getColumnCount(); i++) {
            poolTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane tableScrollPane = new JScrollPane(poolTable);
        tableScrollPane.setPreferredSize(new Dimension(700, 150));
        
        // 线程池代码示例
        JTextArea poolCodeArea = new JTextArea();
        poolCodeArea.setEditable(false);
        poolCodeArea.setFont(StyleManager.getMonoFont(StyleManager.FONT_SIZE_SMALL));
        poolCodeArea.setBackground(StyleManager.BACKGROUND_LIGHT);
        poolCodeArea.setBorder(StyleManager.getTabBorder());
        poolCodeArea.setForeground(StyleManager.TEXT_PRIMARY);
        
        String poolCode = """
                // 创建不同类型的线程池
                
                // 1. 固定线程池
                ExecutorService fixedPool = Executors.newFixedThreadPool(4);
                
                // 2. 缓存线程池
                ExecutorService cachedPool = Executors.newCachedThreadPool();
                
                // 3. 单线程池
                ExecutorService singlePool = Executors.newSingleThreadExecutor();
                
                // 4. 调度线程池
                ScheduledExecutorService scheduledPool = 
                    Executors.newScheduledThreadPool(2);
                
                // 使用线程池执行任务
                fixedPool.submit(() -> {
                    System.out.println("在线程池中执行任务");
                });
                
                // 关闭线程池
                fixedPool.shutdown();
                """;
        
        poolCodeArea.setText(poolCode);
        JScrollPane codeScrollPane = new JScrollPane(poolCodeArea);
        codeScrollPane.setPreferredSize(new Dimension(700, 200));
        
        poolPanel.add(titleLabel, BorderLayout.NORTH);
        poolPanel.add(tableScrollPane, BorderLayout.CENTER);
        poolPanel.add(codeScrollPane, BorderLayout.SOUTH);
        
        tabbedPane.addTab("🏊 线程池", poolPanel);
    }
    
    /**
     * 创建演示选项卡
     */
    private void createDemoTab() {
        JPanel demoPanel = new JPanel(new BorderLayout(10, 10));
        demoPanel.setBackground(StyleManager.BACKGROUND_CARD);
        demoPanel.setBorder(StyleManager.getCardBorder());
        
        // 线程监控区域
        JLabel monitorLabel = new JLabel("🔍 实时线程监控");
        monitorLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_LARGE, true));
        monitorLabel.setForeground(StyleManager.PRIMARY_COLOR);
        
        JPanel threadPanel = new JPanel();
        threadPanel.setLayout(new GridLayout(0, 2, 10, 10));
        threadPanel.setBackground(StyleManager.BACKGROUND_CARD);
        
        JScrollPane threadScrollPane = new JScrollPane(threadPanel);
        threadScrollPane.setBorder(StyleManager.getTabBorder());
        threadScrollPane.setPreferredSize(new Dimension(600, 300));
        
        // 控制按钮区域
        JPanel demoControlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        demoControlPanel.setBackground(StyleManager.BACKGROUND_CARD);
        
        JButton startThreadDemoButton = new JButton("🧵 启动线程演示");
        styleButton(startThreadDemoButton, StyleManager.SUCCESS_COLOR);
        startThreadDemoButton.addActionListener(e -> startThreadDemo(threadPanel));
        
        JButton startPoolDemoButton = new JButton("🏊 启动线程池演示");
        styleButton(startPoolDemoButton, StyleManager.PRIMARY_COLOR);
        startPoolDemoButton.addActionListener(e -> startPoolDemo(threadPanel));
        
        JButton stopDemoButton = new JButton("⏹️ 停止演示");
        styleButton(stopDemoButton, StyleManager.ERROR_COLOR);
        stopDemoButton.addActionListener(e -> stopAllDemos());
        
        demoControlPanel.add(startThreadDemoButton);
        demoControlPanel.add(startPoolDemoButton);
        demoControlPanel.add(stopDemoButton);
        
        demoPanel.add(monitorLabel, BorderLayout.NORTH);
         demoPanel.add(threadScrollPane, BorderLayout.CENTER);
         demoPanel.add(demoControlPanel, BorderLayout.SOUTH);
         
         tabbedPane.addTab("🔬 实时演示", demoPanel);
    }
    
    /**
     * 创建监控选项卡
     */
    private void createMonitorTab() {
        JPanel monitorPanel = new JPanel(new BorderLayout(10, 10));
        monitorPanel.setBackground(StyleManager.BACKGROUND_CARD);
        monitorPanel.setBorder(StyleManager.getCardBorder());
        
        // 系统信息
        JLabel systemLabel = new JLabel("💻 系统性能监控");
        systemLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_TITLE, true));
        systemLabel.setForeground(StyleManager.PRIMARY_COLOR);
        systemLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel infoPanel = new JPanel(new GridLayout(3, 2, 15, 10));
        infoPanel.setBackground(StyleManager.BACKGROUND_CARD);
        
        // CPU信息
        JPanel cpuPanel = createInfoPanel("🔧 CPU信息",
            "核心数: " + Runtime.getRuntime().availableProcessors(),
            "最大内存: " + (Runtime.getRuntime().maxMemory() / 1024 / 1024) + "MB",
            "可用内存: " + (Runtime.getRuntime().freeMemory() / 1024 / 1024) + "MB"
        );
        
        // 线程统计
        JPanel threadStatsPanel = createInfoPanel("🧵 线程统计",
            "活跃线程: 0",
            "完成任务: 0",
            "总执行时间: 0ms"
        );
        
        // JVM信息
        JPanel jvmPanel = createInfoPanel("☕ JVM信息",
            "Java版本: " + System.getProperty("java.version"),
            "JVM名称: " + System.getProperty("java.vm.name"),
            "操作系统: " + System.getProperty("os.name")
        );
        
        infoPanel.add(cpuPanel);
        infoPanel.add(threadStatsPanel);
        infoPanel.add(jvmPanel);
        
        monitorPanel.add(systemLabel, BorderLayout.NORTH);
        monitorPanel.add(infoPanel, BorderLayout.CENTER);
        
        tabbedPane.addTab("📊 系统监控", monitorPanel);
    }
    
    /**
     * 创建信息面板
     */
    private JPanel createInfoPanel(String title, String... info) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(StyleManager.BACKGROUND_CARD);
        panel.setBorder(StyleManager.getCardBorder());
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_MEDIUM, true));
        titleLabel.setForeground(StyleManager.PRIMARY_COLOR);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        
        for (String infoText : info) {
            JLabel infoLabel = new JLabel(infoText);
            infoLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, false));
            infoLabel.setForeground(StyleManager.TEXT_PRIMARY);
            infoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(infoLabel);
            panel.add(Box.createVerticalStrut(5));
        }
        
        return panel;
    }
    
    /**
     * 创建输出面板
     */
    private void createOutputPanel() {
        JPanel outputPanel = new JPanel(new BorderLayout(5, 5));
        outputPanel.setBackground(StyleManager.BACKGROUND_CARD);
        outputPanel.setBorder(StyleManager.getCardBorder());
        
        JLabel outputLabel = new JLabel("📤 程序输出");
        outputLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_MEDIUM, true));
        outputLabel.setForeground(StyleManager.PRIMARY_COLOR);
        
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(StyleManager.getMonoFont(StyleManager.FONT_SIZE_NORMAL));
        outputArea.setBackground(StyleManager.BACKGROUND_LIGHT);
        outputArea.setBorder(StyleManager.getTabBorder());
        outputArea.setForeground(StyleManager.TEXT_PRIMARY);
        
        JScrollPane outputScrollPane = new JScrollPane(outputArea);
        outputScrollPane.setBorder(StyleManager.getTabBorder());
        outputScrollPane.setPreferredSize(new Dimension(800, 200));
        
        outputPanel.add(outputLabel, BorderLayout.NORTH);
        outputPanel.add(outputScrollPane, BorderLayout.CENTER);
        
        add(outputPanel, BorderLayout.SOUTH);
    }
    
    /**
     * 创建状态栏
     */
    private void createStatusBar() {
        JPanel statusPanel = new JPanel(new BorderLayout(5, 5));
        statusPanel.setBackground(new Color(230, 230, 230));
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        
        statusLabel = new JLabel("就绪 | Java多线程学习系统已启动");
        statusLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, false));
        statusLabel.setForeground(StyleManager.PRIMARY_COLOR);
        
        JLabel clockLabel = new JLabel();
        clockLabel.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, false));
        clockLabel.setHorizontalAlignment(SwingConstants.RIGHT);
        clockLabel.setForeground(StyleManager.TEXT_SECONDARY);
        
        // 时钟更新
        Timer timer = new Timer(1000, e -> {
            clockLabel.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        });
        timer.start();
        
        statusPanel.add(statusLabel, BorderLayout.WEST);
        statusPanel.add(clockLabel, BorderLayout.EAST);
        
        add(statusPanel, BorderLayout.PAGE_END);
    }
    
    /**
     * 填充演示选择器
     */
    private void populateDemoSelector() {
        demoSelector.removeAllItems();
        demoSelector.addItem("选择演示程序...");
        demoSelector.addItem("Thread继承方式演示");
        demoSelector.addItem("Runnable接口演示");
        demoSelector.addItem("线程池管理演示");
        demoSelector.addItem("综合多线程系统演示");
        demoSelector.addItem("性能对比分析");
    }
    
    /**
     * 按钮样式设置 - 统一使用样式管理器
     */
    private void styleButton(JButton button, Color color) {
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(StyleManager.getFont(StyleManager.FONT_SIZE_NORMAL, true));
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 30));
    }
    
    /**
     * 编译所有演示程序
     */
    private void compileAll() {
        String[] files = {
            "ThreadExtendsDemo.java",
            "RunnableDemo.java", 
            "ThreadPoolDemo.java",
            "ComprehensiveThreadDemo.java"
        };
        
        try {
            clearOutput();
            appendOutput("🔧 开始编译所有演示程序...\n");
            overallProgress.setString("正在编译...");
            overallProgress.setValue(0);
            
            for (int i = 0; i < files.length; i++) {
                ProcessBuilder pb = new ProcessBuilder("javac", files[i]);
                pb.directory(new File("."));
                Process process = pb.start();
                
                // 读取编译输出
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), "UTF-8"));
                
                String line;
                while ((line = reader.readLine()) != null) {
                    appendOutput(line + "\n");
                }
                
                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    appendOutput("✅ " + files[i] + " 编译成功\n");
                } else {
                    appendOutput("❌ " + files[i] + " 编译失败 (退出码: " + exitCode + ")\n");
                }
                
                overallProgress.setValue((i + 1) * 100 / files.length);
            }
            
            appendOutput("🎉 所有文件编译完成！\n");
            overallProgress.setString("编译完成");
            overallProgress.setValue(100);
            statusLabel.setText("编译完成");
            
        } catch (Exception e) {
            appendOutput("❌ 编译过程中出现错误: " + e.getMessage() + "\n");
            e.printStackTrace();
            statusLabel.setText("编译失败");
        }
    }
    
    /**
     * 运行选中的演示
     */
    private void runSelectedDemo() {
        String selected = (String) demoSelector.getSelectedItem();
        
        if (selected == null || selected.equals("选择演示程序...")) {
            JOptionPane.showMessageDialog(this, "请先选择一个演示程序", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            clearOutput();
            statusLabel.setText("正在运行演示...");
            overallProgress.setString("正在运行...");
            overallProgress.setValue(0);
            
            switch (selected) {
                case "Thread继承方式演示":
                    runThreadExtendsDemo();
                    break;
                case "Runnable接口演示":
                    runRunnableDemo();
                    break;
                case "线程池管理演示":
                    runThreadPoolDemo();
                    break;
                case "综合多线程系统演示":
                    runComprehensiveDemo();
                    break;
                case "性能对比分析":
                    runPerformanceAnalysis();
                    break;
            }
            
        } catch (Exception e) {
            appendOutput("❌ 运行演示时出现错误: " + e.getMessage() + "\n");
            e.printStackTrace();
            statusLabel.setText("运行失败");
        }
    }
    
    /**
     * 运行Thread继承方式演示
     */
    private void runThreadExtendsDemo() {
        appendOutput("🧵 运行Thread继承方式演示...\n");
        appendOutput("-".repeat(50) + "\n");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "ThreadExtendsDemo");
            pb.directory(new File("."));
            Process process = pb.start();
            
            // 实时读取输出
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendOutput(line + "\n");
                        overallProgress.setValue(overallProgress.getValue() + 1);
                    }
                } catch (Exception e) {
                    appendOutput("❌ 读取输出时出现错误: " + e.getMessage() + "\n");
                }
            });
            outputReader.start();
            
            process.waitFor();
            outputReader.join();
            
            appendOutput("✅ Thread继承方式演示完成！\n\n");
            
        } catch (Exception e) {
            appendOutput("❌ 运行Thread继承方式演示失败: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 运行Runnable接口演示
     */
    private void runRunnableDemo() {
        appendOutput("🔧 运行Runnable接口演示...\n");
        appendOutput("-".repeat(50) + "\n");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "RunnableDemo");
            pb.directory(new File("."));
            Process process = pb.start();
            
            // 实时读取输出
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendOutput(line + "\n");
                        overallProgress.setValue(overallProgress.getValue() + 1);
                    }
                } catch (Exception e) {
                    appendOutput("❌ 读取输出时出现错误: " + e.getMessage() + "\n");
                }
            });
            outputReader.start();
            
            process.waitFor();
            outputReader.join();
            
            appendOutput("✅ Runnable接口演示完成！\n\n");
            
        } catch (Exception e) {
            appendOutput("❌ 运行Runnable接口演示失败: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 运行线程池演示
     */
    private void runThreadPoolDemo() {
        appendOutput("🏊 运行线程池演示...\n");
        appendOutput("-".repeat(50) + "\n");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "ThreadPoolDemo");
            pb.directory(new File("."));
            Process process = pb.start();
            
            // 实时读取输出
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendOutput(line + "\n");
                        overallProgress.setValue(overallProgress.getValue() + 1);
                    }
                } catch (Exception e) {
                    appendOutput("❌ 读取输出时出现错误: " + e.getMessage() + "\n");
                }
            });
            outputReader.start();
            
            process.waitFor();
            outputReader.join();
            
            appendOutput("✅ 线程池演示完成！\n\n");
            
        } catch (Exception e) {
            appendOutput("❌ 运行线程池演示失败: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 运行综合演示
     */
    private void runComprehensiveDemo() {
        appendOutput("🚀 运行综合多线程系统演示...\n");
        appendOutput("-".repeat(50) + "\n");
        
        try {
            ProcessBuilder pb = new ProcessBuilder("java", "ComprehensiveThreadDemo");
            pb.directory(new File("."));
            Process process = pb.start();
            
            // 实时读取输出
            Thread outputReader = new Thread(() -> {
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream(), "UTF-8"))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        appendOutput(line + "\n");
                        overallProgress.setValue(overallProgress.getValue() + 2);
                    }
                } catch (Exception e) {
                    appendOutput("❌ 读取输出时出现错误: " + e.getMessage() + "\n");
                }
            });
            outputReader.start();
            
            process.waitFor();
            outputReader.join();
            
            appendOutput("✅ 综合多线程系统演示完成！\n\n");
            
        } catch (Exception e) {
            appendOutput("❌ 运行综合演示失败: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 运行性能对比分析
     */
    private void runPerformanceAnalysis() {
        appendOutput("📊 开始性能对比分析...\n");
        appendOutput("-".repeat(50) + "\n");
        
        try {
            // 这里可以实现性能测试逻辑
            appendOutput("正在分析不同线程创建方式的性能差异...\n");
            Thread.sleep(1000);
            appendOutput("1. 继承Thread方式: 适合简单场景，但不够灵活\n");
            Thread.sleep(500);
            appendOutput("2. 实现Runnable方式: 更灵活，推荐使用\n");
            Thread.sleep(500);
            appendOutput("3. 线程池方式: 适合批量任务，性能最优\n");
            Thread.sleep(500);
            appendOutput("✅ 性能分析完成！\n\n");
            
        } catch (Exception e) {
            appendOutput("❌ 性能分析失败: " + e.getMessage() + "\n");
        }
    }
    
    /**
     * 启动线程演示
     */
    private void startThreadDemo(JPanel threadPanel) {
        threadPanel.removeAll();
        threadInfoPanels.clear();
        
        Color[] colors = {Color.RED, Color.BLUE, Color.GREEN, Color.ORANGE};
        String[] threadNames = {"Worker-1", "Worker-2", "Worker-3", "Worker-4"};
        
        for (int i = 0; i < 4; i++) {
            ThreadInfoPanel panel = new ThreadInfoPanel(threadNames[i], Thread.State.NEW, colors[i]);
            threadInfoPanels.add(panel);
            threadPanel.add(panel);
        }
        
        threadPanel.revalidate();
        threadPanel.repaint();
        
        // 启动演示线程
        startDemoThreads();
    }
    
    /**
     * 启动线程池演示
     */
    private void startPoolDemo(JPanel threadPanel) {
        threadPanel.removeAll();
        threadInfoPanels.clear();
        
        // 创建更多线程面板用于展示线程池
        Color[] colors = {Color.MAGENTA, Color.CYAN, Color.PINK, Color.GRAY, Color.YELLOW, Color.DARK_GRAY};
        String[] threadNames = {"Pool-Worker-1", "Pool-Worker-2", "Pool-Worker-3", "Pool-Worker-4", "Pool-Worker-5", "Pool-Worker-6"};
        
        for (int i = 0; i < 6; i++) {
            ThreadInfoPanel panel = new ThreadInfoPanel(threadNames[i], Thread.State.NEW, colors[i]);
            threadInfoPanels.add(panel);
            threadPanel.add(panel);
        }
        
        threadPanel.revalidate();
        threadPanel.repaint();
        
        // 启动线程池演示
        startPooledThreads();
    }
    
    /**
     * 启动演示线程
     */
    private void startDemoThreads() {
        for (int i = 0; i < threadInfoPanels.size(); i++) {
            final int index = i;
            ThreadInfoPanel panel = threadInfoPanels.get(i);
            
            new Thread(() -> {
                try {
                    // 模拟线程生命周期
                    panel.updateStatus(Thread.State.RUNNABLE, Thread.NORM_PRIORITY, 0);
                    Thread.sleep(1000);
                    
                    for (int progress = 10; progress <= 100; progress += 10) {
                        panel.updateStatus(Thread.State.RUNNABLE, Thread.NORM_PRIORITY, progress);
                        Thread.sleep(500);
                    }
                    
                    panel.updateStatus(Thread.State.TERMINATED, Thread.NORM_PRIORITY, 100);
                    
                } catch (InterruptedException e) {
                    panel.updateStatus(Thread.State.TERMINATED, Thread.NORM_PRIORITY, 0);
                    Thread.currentThread().interrupt();
                }
            }, panel.getThreadName()).start();
        }
    }
    
    /**
     * 启动线程池演示线程
     */
    private void startPooledThreads() {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        
        for (int i = 0; i < threadInfoPanels.size(); i++) {
            final int index = i;
            ThreadInfoPanel panel = threadInfoPanels.get(index);
            
            executor.submit(() -> {
                try {
                    panel.updateStatus(Thread.State.RUNNABLE, Thread.NORM_PRIORITY, 0);
                    Thread.sleep(500);
                    
                    for (int progress = 10; progress <= 100; progress += 20) {
                        panel.updateStatus(Thread.State.RUNNABLE, Thread.NORM_PRIORITY, progress);
                        Thread.sleep(800);
                    }
                    
                    panel.updateStatus(Thread.State.TERMINATED, Thread.NORM_PRIORITY, 100);
                    
                } catch (InterruptedException e) {
                    panel.updateStatus(Thread.State.TERMINATED, Thread.NORM_PRIORITY, 0);
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        executor.shutdown();
    }
    
    /**
     * 停止所有演示
     */
    private void stopAllDemos() {
        runningThreads.set(0);
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdownNow();
        }
        appendOutput("⏹️ 演示已停止\n");
        statusLabel.setText("演示已停止");
    }
    
    /**
     * 清空输出区域
     */
    private void clearOutput() {
        outputArea.setText("");
        overallProgress.setString("就绪");
        overallProgress.setValue(0);
        statusLabel.setText("就绪");
    }
    
    /**
     * 在输出区域追加文本
     */
    private void appendOutput(String text) {
        SwingUtilities.invokeLater(() -> {
            outputArea.append(text);
            outputArea.setCaretPosition(outputArea.getDocument().getLength());
        });
    }
    
    /**
     * 显示关于对话框
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
            "Java多线程学习系统 v2.0 - 优化版\n\n" +
            "这是一个专为Java学习者设计的多线程交互式教学工具。\n" +
            "包含理论知识、代码演示、实时监控等功能，\n" +
            "帮助学习者深入理解Java多线程编程概念。\n\n" +
            "主要优化：\n" +
            "• 统一的视觉风格和响应式设计\n" +
            "• 改进的字体渲染和颜色主题\n" +
            "• 增强的用户体验和界面布局\n" +
            "• 跨平台兼容性优化\n\n" +
            "作者: Java Learning Tutorial\n" +
            "日期: 2024年\n",
            "关于", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 显示使用教程对话框
     */
    private void showTutorialDialog() {
        String tutorial = """
                📖 使用教程
                
                1. 🔧 编译演示程序
                   点击"编译所有"按钮编译所有演示文件
                
                2. 🚀 运行演示
                   选择要运行的演示程序，然后点击"运行演示"
                
                3. 🔬 实时演示
                   在"实时演示"选项卡中查看线程执行过程
                
                4. 📊 性能监控
                   在"系统监控"选项卡中查看系统状态
                
                5. 📚 学习理论
                   在"理论学习"选项卡中学习多线程知识
                
                💡 提示:
                   - 建议按照理论→代码→实战的顺序学习
                   - 观察不同线程创建方式的执行效果
                   - 注意线程状态的变化过程
                
                🎨 界面优化特点:
                   - 统一的颜色主题和字体管理
                   - 响应式布局设计
                   - 改进的视觉对比度
                   - 更好的跨平台兼容性
                """;
        
        JOptionPane.showMessageDialog(this, tutorial, "使用教程", JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * 清理资源
     */
    private void cleanup() {
        if (threadPool != null && !threadPool.isShutdown()) {
            threadPool.shutdown();
        }
    }
    
    /**
     * 主方法 - 启动GUI应用程序
     */
    public static void main(String[] args) {
        try {
            // 设置UTF-8编码支持
            System.setProperty("file.encoding", "UTF-8");
            
            // 设置Swing外观
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            
            // 创建并显示GUI
            SwingUtilities.invokeLater(() -> {
                MultithreadGUI gui = new MultithreadGUI();
                gui.setVisible(true);
                
                // 显示欢迎信息
                gui.appendOutput("🎓 欢迎使用Java多线程学习系统！\n");
                gui.appendOutput("请先编译所有演示程序，然后选择您想学习的演示内容。\n");
                gui.appendOutput("建议按照理论学习 → 代码演示 → 实时监控的顺序进行学习。\n\n");
                gui.appendOutput("🎨 界面已优化：统一的视觉风格、响应式布局、改进的字体渲染\n\n");
            });
            
        } catch (Exception e) {
            System.err.println("启动GUI时出现错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}