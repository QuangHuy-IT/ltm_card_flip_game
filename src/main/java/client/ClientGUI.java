package client;

import common.Protocol;
import org.json.JSONArray;
import org.json.JSONObject;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class ClientGUI extends JFrame {
    private NetworkClient network;
    private CardLayout cardLayout;
    private JPanel mainPanel;
    private ImageManager imageManager;
    
    // Modern Color Scheme
    private static final Color PRIMARY_DARK = new Color(17, 24, 39);
    private static final Color PRIMARY_COLOR = new Color(31, 41, 55);
    private static final Color ACCENT_BLUE = new Color(59, 130, 246);
    private static final Color ACCENT_PURPLE = new Color(139, 92, 246);
    private static final Color SUCCESS_COLOR = new Color(34, 197, 94);
    private static final Color DANGER_COLOR = new Color(239, 68, 68);
    private static final Color WARNING_COLOR = new Color(249, 115, 22);
    private static final Color BACKGROUND_COLOR = new Color(249, 250, 251);
    private static final Color TEXT_DARK = new Color(17, 24, 39);
    private static final Color TEXT_MUTED = new Color(107, 114, 128);
    private static final Color BORDER_COLOR = new Color(229, 231, 235);
    
    // Components
    private JPanel loginPanel, lobbyPanel, gamePanel;
    private JTextField loginUsername;
    private JPasswordField loginPassword;
    private JPanel loginCard;
    
    private JLabel welcomeLabel, statsLabel;
    private JTable playerTable, leaderboardTable;
    private DefaultTableModel playerTableModel, leaderboardTableModel;
    private String currentUsername;
    private int currentPlayerId;
    
    private JPanel cardGridPanel;
    private JLabel myScoreLabel, opponentScoreLabel, timerLabel;
    private JButton quitButton;
    private List<CardPanel> cards;
    private CardPanel firstCard, secondCard;
    private boolean canClick;
    private String opponent;
    private Timer gameTimer;
    private int remainingTime;
    private JProgressBar gameProgress;
    private JDialog rematchWaitingDialog;
    private JDialog rematchRequestDialog;
    
    // Match History
    private JDialog historyDialog;
    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    
    public ClientGUI() {
        setTitle("GAME LẬT THẺ BÀI - TRẬN ĐẤU TRÍ NHỚ");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        imageManager = ImageManager.getInstance();
        if (!imageManager.hasImages()) {
            imageManager.createDefaultCardSet();
        }
        imageManager.printImageInstructions();
        
        network = new NetworkClient(this);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        initComponents();
        
        if (network.connect()) {
            System.out.println("✓ Connected to server");
        } else {
            showModernError("Cannot connect to server!", "Connection Error");
            System.exit(1);
        }
        
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                network.disconnect();
            }
        });
    }
    
    private void initComponents() {
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        createLoginPanel();
        createLobbyPanel();
        createGamePanel();
        
        mainPanel.add(loginPanel, "LOGIN");
        mainPanel.add(lobbyPanel, "LOBBY");
        mainPanel.add(gamePanel, "GAME");
        
        add(mainPanel);
        cardLayout.show(mainPanel, "LOGIN");
    }
    
    private void createLoginPanel() {
        loginPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                GradientPaint gp = new GradientPaint(0, 0, new Color(79, 70, 229),
                    getWidth(), getHeight(), new Color(124, 58, 237));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                g2d.setColor(new Color(255, 255, 255, 40));
                g2d.fillOval(-150, -150, 500, 500);
                g2d.fillOval(getWidth() - 350, getHeight() - 350, 500, 500);
            }
        };
        loginPanel.setLayout(new GridBagLayout());
        
        loginCard = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 80));
                g2d.fillRoundRect(8, 8, getWidth() - 8, getHeight() - 8, 24, 24);
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 16, getHeight() - 16, 24, 24);
            }
        };
        loginCard.setOpaque(false);
        loginCard.setLayout(new BoxLayout(loginCard, BoxLayout.Y_AXIS));
        loginCard.setBorder(new EmptyBorder(60, 70, 60, 70));
        
        JPanel logoPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, ACCENT_BLUE, getWidth(), getHeight(), ACCENT_PURPLE);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
            }
        };
        logoPanel.setOpaque(false);
        logoPanel.setPreferredSize(new Dimension(100, 100));
        logoPanel.setMaximumSize(new Dimension(100, 100));
        logoPanel.setLayout(new GridBagLayout());
        
        JLabel logoLabel = new JLabel("🎮");
        logoLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 56));
        logoPanel.add(logoLabel);
        logoPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(logoPanel);
        
        loginCard.add(Box.createVerticalStrut(25));
        
        JLabel titleLabel = new JLabel("GAME LẬT THẺ BÀI");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 38));
        titleLabel.setForeground(TEXT_DARK);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(titleLabel);
        
        loginCard.add(Box.createVerticalStrut(8));
        
        JLabel subtitleLabel = new JLabel("TRẬN ĐẤU TRÍ NHỚ");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        subtitleLabel.setForeground(TEXT_MUTED);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginCard.add(subtitleLabel);
        
        loginCard.add(Box.createVerticalStrut(50));
        
        JPanel usernameContainer = new JPanel(new BorderLayout(0, 8));
        usernameContainer.setOpaque(false);
        usernameContainer.setMaximumSize(new Dimension(380, 80));
        
        JLabel usernameLabel = new JLabel("Username");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(TEXT_DARK);
        usernameContainer.add(usernameLabel, BorderLayout.NORTH);
        
        loginUsername = new JTextField(20);
        loginUsername.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loginUsername.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(12, 16, 12, 16)));
        usernameContainer.add(loginUsername, BorderLayout.CENTER);
        
        loginCard.add(usernameContainer);
        loginCard.add(Box.createVerticalStrut(20));
        
        JPanel passwordContainer = new JPanel(new BorderLayout(0, 8));
        passwordContainer.setOpaque(false);
        passwordContainer.setMaximumSize(new Dimension(380, 80));
        
        JLabel passwordLabel = new JLabel("Password");
        passwordLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passwordLabel.setForeground(TEXT_DARK);
        passwordContainer.add(passwordLabel, BorderLayout.NORTH);
        
        loginPassword = new JPasswordField(20);
        loginPassword.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loginPassword.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(BORDER_COLOR, 2, true),
            new EmptyBorder(12, 16, 12, 16)));
        loginPassword.addActionListener(e -> handleLogin());
        passwordContainer.add(loginPassword, BorderLayout.CENTER);
        
        loginCard.add(passwordContainer);
        loginCard.add(Box.createVerticalStrut(35));
        
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        buttonPanel.setMaximumSize(new Dimension(380, 55));
        buttonPanel.setOpaque(false);
        
        JButton loginButton = createModernButton("Đăng nhập", ACCENT_BLUE, true);
        loginButton.setPreferredSize(new Dimension(180, 55));
        loginButton.addActionListener(e -> handleLogin());
        buttonPanel.add(loginButton);
        
        JButton registerButton = createModernButton("Đăng ký", SUCCESS_COLOR, false);
        registerButton.setPreferredSize(new Dimension(180, 55));
        registerButton.addActionListener(e -> handleRegister());
        buttonPanel.add(registerButton);
        
        loginCard.add(buttonPanel);
        loginPanel.add(loginCard);
    }
    
    private void createLobbyPanel() {
        lobbyPanel = new JPanel(new BorderLayout(0, 0));
        lobbyPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel topBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;  
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_DARK, getWidth(), 0, PRIMARY_COLOR);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        topBar.setPreferredSize(new Dimension(0, 90));
        topBar.setBorder(new EmptyBorder(20, 0, 10, 0));
        
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        welcomePanel.setOpaque(false);
        
        JLabel avatarLabel = new JLabel("👤");
        avatarLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        welcomePanel.add(avatarLabel);
        
        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        
        welcomeLabel = new JLabel("Xin chào, Player!");
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        welcomeLabel.setForeground(Color.WHITE);
        textPanel.add(welcomeLabel);
        
        statsLabel = new JLabel("Loading stats...");
        statsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statsLabel.setForeground(new Color(203, 213, 224));
        textPanel.add(statsLabel);
        
        welcomePanel.add(textPanel);
        topBar.add(welcomePanel, BorderLayout.WEST);
        
        // Right panel with Match History and Logout buttons
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        rightPanel.setOpaque(false);
        
        JButton historyButton = createModernButton("Lịch sử đấu", ACCENT_PURPLE, false);
        historyButton.setPreferredSize(new Dimension(140, 45));
        historyButton.addActionListener(e -> showMatchHistoryDialog());
        rightPanel.add(historyButton);
        
        JButton logoutButton = createModernButton("Đăng xuất", DANGER_COLOR, false);
        logoutButton.setPreferredSize(new Dimension(110, 45));
        logoutButton.addActionListener(e -> handleLogout());
        rightPanel.add(logoutButton);
        
        topBar.add(rightPanel, BorderLayout.EAST);
        
        lobbyPanel.add(topBar, BorderLayout.NORTH);
        
        JPanel centerPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        centerPanel.setBackground(BACKGROUND_COLOR);
        centerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JPanel playersCard = createModernCard("Người chơi online", ACCENT_BLUE);
        
        String[] playerColumns = {"Người chơi", "Trạng thái"};
        playerTableModel = new DefaultTableModel(playerColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        playerTable = new JTable(playerTableModel);
        styleModernTable(playerTable);
        
        playerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = playerTable.getSelectedRow();
                    if (row != -1) {
                        handleQuickChallenge(row);
                    }
                }
            }
        });
        
        JScrollPane playerScroll = new JScrollPane(playerTable);
        playerScroll.setBorder(null);
        playerScroll.getViewport().setBackground(Color.WHITE);
        
        JPanel playersContent = new JPanel(new BorderLayout(0, 15));
        playersContent.setBackground(Color.WHITE);
        playersContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        playersContent.add(playerScroll, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 12));
        infoPanel.setBackground(new Color(249, 250, 251));
        infoPanel.setBorder(BorderFactory.createCompoundBorder(
            new MatteBorder(1, 0, 0, 0, BORDER_COLOR),
            new EmptyBorder(8, 15, 8, 15)));
        
        JLabel tipIcon = new JLabel("💡");
        tipIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        
        JLabel infoLabel = new JLabel("Nháy đúp để thách đấu với họ!");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(TEXT_MUTED);
        
        infoPanel.add(tipIcon);
        infoPanel.add(infoLabel);
        
        playersContent.add(infoPanel, BorderLayout.SOUTH);
        playersCard.add(playersContent, BorderLayout.CENTER);
        
        centerPanel.add(playersCard);
        
        JPanel leaderboardCard = createModernCard("Bảng xếp hạng", new Color(245, 158, 11));
        
        String[] leaderColumns = {"#", "Người chơi", "Điểm", "W-L", "Win %"};
        leaderboardTableModel = new DefaultTableModel(leaderColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        leaderboardTable = new JTable(leaderboardTableModel);
        styleModernTable(leaderboardTable);
        JScrollPane leaderScroll = new JScrollPane(leaderboardTable);
        leaderScroll.setBorder(null);
        leaderScroll.getViewport().setBackground(Color.WHITE);
        
        JPanel leaderContent = new JPanel(new BorderLayout(0, 15));
        leaderContent.setBackground(Color.WHITE);
        leaderContent.setBorder(new EmptyBorder(15, 15, 15, 15));
        leaderContent.add(leaderScroll, BorderLayout.CENTER);
        
        JButton refreshButton = createModernButton("Làm mới", SUCCESS_COLOR, false);
        refreshButton.setPreferredSize(new Dimension(120, 35));
        refreshButton.addActionListener(e -> {
            network.getPlayers();
            network.getLeaderboard();
        });
        
        JPanel refreshPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        refreshPanel.setBackground(Color.WHITE);
        refreshPanel.add(refreshButton);
        leaderContent.add(refreshPanel, BorderLayout.SOUTH);
        
        leaderboardCard.add(leaderContent, BorderLayout.CENTER);
        centerPanel.add(leaderboardCard);
        
        lobbyPanel.add(centerPanel, BorderLayout.CENTER);
        
        Timer refreshTimer = new Timer(3000, e -> {
            if (lobbyPanel.isVisible()) {
                network.getPlayers();
                network.getLeaderboard();
            }
        });
        refreshTimer.start();
    }
    
    // MATCH HISTORY DIALOG
    private void showMatchHistoryDialog() {
        if (historyDialog != null && historyDialog.isVisible()) {
            historyDialog.toFront();
            return;
        }
        
        historyDialog = new JDialog(this, "Lịch sử đấu - " + currentUsername, false);
        historyDialog.setSize(1000, 600);
        historyDialog.setLocationRelativeTo(this);
        
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ACCENT_PURPLE);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel titleLabel = new JLabel("Lịch sử đấu của bạn");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        JButton refreshBtn = createModernButton("Làm mới", SUCCESS_COLOR, false);
        refreshBtn.setPreferredSize(new Dimension(110, 40));
        refreshBtn.addActionListener(e -> network.getMatchHistory());
        headerPanel.add(refreshBtn, BorderLayout.EAST);
        
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        String[] columns = {"Ngày", "Đối thủ", "Kết quả", "Độ khó", "Điểm của bạn", "Điểm của đối thủ", "Thời gian"};
        historyTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        historyTable = new JTable(historyTableModel);
        styleModernTable(historyTable);
        historyTable.setRowHeight(50);
        
        historyTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String result = value.toString();
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.BOLD, 15));
                
                if (!isSelected) {
                    if (result.contains("WIN")) {
                        setForeground(SUCCESS_COLOR);
                        c.setBackground(new Color(220, 252, 231));
                    } else if (result.contains("LOSS")) {
                        setForeground(DANGER_COLOR);
                        c.setBackground(new Color(254, 226, 226));
                    } else {
                        setForeground(WARNING_COLOR);
                        c.setBackground(new Color(254, 243, 199));
                    }
                }
                
                return c;
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(historyTable);
        scrollPane.setBorder(new EmptyBorder(15, 15, 15, 15));
        scrollPane.getViewport().setBackground(Color.WHITE);
        
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footerPanel.setBackground(new Color(249, 250, 251));
        footerPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));
        
        JLabel infoLabel = new JLabel("20 trận đấu gần nhất");
        infoLabel.setFont(new Font("Segoe UI", Font.ITALIC, 13));
        infoLabel.setForeground(TEXT_MUTED);
        footerPanel.add(infoLabel);
        
        mainPanel.add(footerPanel, BorderLayout.SOUTH);
        
        historyDialog.add(mainPanel);
        historyDialog.setVisible(true);
        
        network.getMatchHistory();
    }
    
    public void updateMatchHistory(JSONArray data) {
        SwingUtilities.invokeLater(() -> {
            if (historyTableModel == null) return;
            
            historyTableModel.setRowCount(0);
            
            for (int i = 0; i < data.length(); i++) {
                JSONObject match = data.getJSONObject(i);
                
                String opponent = match.getString("opponent");
                String result = match.getString("result");
                String difficulty = match.getString("difficulty");
                int myScore = match.getInt("my_score");
                int oppScore = match.getInt("opponent_score");
                int duration = match.getInt("duration");
                String createdAt = match.getString("created_at");
                
                String date = createdAt.substring(0, 16).replace("T", " ");
                
                int minutes = duration / 60;
                int seconds = duration % 60;
                String durationStr = String.format("%d:%02d", minutes, seconds);
                
                String resultIcon = "";
                switch(result) {
                    case "WIN": resultIcon = ""; break;
                    case "LOSS": resultIcon = ""; break;
                    case "DRAW": resultIcon = ""; break;
                }
                
                historyTableModel.addRow(new Object[]{
                    date,
                    opponent,
                    resultIcon + result,
                    difficulty,
                    myScore,
                    oppScore,
                    durationStr
                });
            }
            
            System.out.println("Match history updated: " + data.length() + " matches");
        });
    }
    
    private void createGamePanel() {
        gamePanel = new JPanel(new BorderLayout(0, 0));
        gamePanel.setBackground(BACKGROUND_COLOR);
        
        JPanel infoBar = new JPanel(new BorderLayout());
        infoBar.setBackground(PRIMARY_DARK);
        infoBar.setPreferredSize(new Dimension(0, 120));
        infoBar.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        JPanel scoresPanel = new JPanel(new GridLayout(1, 3, 30, 0));
        scoresPanel.setOpaque(false);
        
        JPanel myScorePanel = createModernScoreCard("ĐIỂM CỦA BẠN", "0", new Color(16, 185, 129), "");
        Component[] myComponents = myScorePanel.getComponents();
        for (Component comp : myComponents) {
            if (comp instanceof JLabel) {
                JLabel lbl = (JLabel) comp;
                if (lbl.getFont().getSize() == 42) {
                    myScoreLabel = lbl;
                    break;
                }
            }
        }
        scoresPanel.add(myScorePanel);
        
        JPanel timerPanel = new JPanel(new GridBagLayout());
        timerPanel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 8, 0);
        
        timerLabel = new JLabel("5:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 52));
        timerLabel.setForeground(Color.WHITE);
        timerPanel.add(timerLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        
        gameProgress = new JProgressBar(0, 100);
        gameProgress.setValue(100);
        gameProgress.setStringPainted(false);
        gameProgress.setPreferredSize(new Dimension(220, 6));
        gameProgress.setBackground(new Color(255, 255, 255, 30));
        gameProgress.setForeground(new Color(251, 191, 36));
        gameProgress.setBorder(null);
        timerPanel.add(gameProgress, gbc);
        
        scoresPanel.add(timerPanel);
        
        JPanel oppScorePanel = createModernScoreCard("ĐIỂM ĐỐI THỦ", "0", new Color(239, 68, 68), "");
        Component[] oppComponents = oppScorePanel.getComponents();
        for (Component comp : oppComponents) {
            if (comp instanceof JLabel) {
                JLabel lbl = (JLabel) comp;
                if (lbl.getFont().getSize() == 42) {
                    opponentScoreLabel = lbl;
                    break;
                }
            }
        }
        scoresPanel.add(oppScorePanel);
        
        infoBar.add(scoresPanel, BorderLayout.CENTER);
        gamePanel.add(infoBar, BorderLayout.NORTH);
        
        JPanel gridContainer = new JPanel(new BorderLayout(0, 15));
        gridContainer.setBackground(BACKGROUND_COLOR);
        gridContainer.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        JLabel boardTitleLabel = new JLabel("Hãy tìm tất cả những cặp bài!", JLabel.CENTER);
        boardTitleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        boardTitleLabel.setForeground(TEXT_DARK);
        gridContainer.add(boardTitleLabel, BorderLayout.NORTH);
        
        cardGridPanel = new JPanel();
        cardGridPanel.setBackground(BACKGROUND_COLOR);
        
        JScrollPane scrollPane = new JScrollPane(cardGridPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        gridContainer.add(scrollPane, BorderLayout.CENTER);
        
        gamePanel.add(gridContainer, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        controlPanel.setBackground(BACKGROUND_COLOR);
        
        quitButton = createModernButton("Thoát Game", DANGER_COLOR, false);
        quitButton.setFont(new Font("Segoe UI", Font.BOLD, 15));
        quitButton.setPreferredSize(new Dimension(150, 45));
        quitButton.addActionListener(e -> handleQuitGame());
        controlPanel.add(quitButton);
        
        gamePanel.add(controlPanel, BorderLayout.SOUTH);
        
        cards = new ArrayList<>();
        canClick = true;
    }
    
    private JPanel createModernCard(String title, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                g2d.setColor(new Color(0, 0, 0, 40));
                g2d.fillRoundRect(4, 4, getWidth() - 4, getHeight() - 4, 20, 20);
                
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 20, 20);
                
                g2d.setColor(BORDER_COLOR);
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth() - 8, getHeight() - 8, 20, 20);
            }
        };
        card.setOpaque(false);
        
        JPanel titleBar = new JPanel(new BorderLayout());
        titleBar.setBackground(accentColor);
        titleBar.setBorder(new EmptyBorder(18, 24, 18, 24));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 17));
        titleLabel.setForeground(Color.WHITE);
        titleBar.add(titleLabel, BorderLayout.WEST);
        
        card.add(titleBar, BorderLayout.NORTH);
        
        return card;
    }
    
    private JPanel createModernScoreCard(String label, String score, Color color, String icon) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 5, 0);
        
        JLabel iconLabel = new JLabel(icon + " " + label);
        iconLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        iconLabel.setForeground(new Color(203, 213, 224));
        panel.add(iconLabel, gbc);
        
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 0, 0);
        
        JLabel scoreLabel = new JLabel(score);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 42));
        scoreLabel.setForeground(color);
        panel.add(scoreLabel, gbc);
        
        return panel;
    }
    
    private JButton createModernButton(String text, Color bgColor, boolean isPrimary) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (getModel().isPressed()) {
                    g2d.setColor(bgColor.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(bgColor.brighter());
                } else {
                    g2d.setColor(bgColor);
                }
                
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", isPrimary ? Font.BOLD : Font.PLAIN, 15));
        button.setForeground(Color.WHITE);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(140, 50));
        
        return button;
    }
    
    private void styleModernTable(JTable table) {
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        table.setRowHeight(45);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(219, 234, 254));
        table.setSelectionForeground(TEXT_DARK);
        table.setBackground(Color.WHITE);
        
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                         boolean isSelected, boolean hasFocus,
                                                         int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(249, 250, 251));
                }
                
                setHorizontalAlignment(JLabel.CENTER);
                setFont(new Font("Segoe UI", Font.PLAIN, 14));
                setForeground(TEXT_DARK);
                
                return c;
            }
        });
        
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        table.getTableHeader().setBackground(new Color(243, 244, 246));
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, BORDER_COLOR));
        table.getTableHeader().setPreferredSize(new Dimension(0, 48));
    }
    
    private void handleLogin() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showModernError("Please enter username and password", "Validation Error");
            return;
        }
        
        network.login(username, password);
    }
    
    private void handleRegister() {
        String username = loginUsername.getText().trim();
        String password = new String(loginPassword.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showModernError("Please enter username and password", "Validation Error");
            return;
        }
        
        if (password.length() < 4) {
            showModernError("Password must be at least 4 characters", "Validation Error");
            return;
        }
        
        network.register(username, password);
    }
    
    private void handleLogout() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Bạn có chắc muốn đăng xuất chứ?",
            "Xác nhận đăng xuất!",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (network != null && network.isConnected()) {
                network.disconnect();
            }
            
            currentUsername = null;
            currentPlayerId = 0;
            loginUsername.setText("");
            loginPassword.setText("");
            cardLayout.show(mainPanel, "LOGIN");
            System.out.println("✓ Logged out successfully");
        }
    }
    
    private void handleQuickChallenge(int selectedRow) {
        String targetPlayer = (String) playerTableModel.getValueAt(selectedRow, 0);
        String status = (String) playerTableModel.getValueAt(selectedRow, 1);
        
        if (targetPlayer.equals(currentUsername)) {
            showModernError("You cannot challenge yourself", "Invalid Action");
            return;
        }
        
        if (status.equals("In Game")) {
            showModernError("Player is currently in a game", "Player Unavailable");
            return;
        }
        
        showDifficultySelectionDialog(targetPlayer);
    }
    
    private void showDifficultySelectionDialog(String targetPlayer) {
        JDialog diffDialog = new JDialog(this, "Thách đấu " + targetPlayer, true);
        diffDialog.setSize(450, 480);
        diffDialog.setLocationRelativeTo(this);
        diffDialog.setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 0));
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ACCENT_BLUE);
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));

        JLabel headerLabel = new JLabel("Thách đấu " + targetPlayer);
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        JLabel subLabel = new JLabel("Chọn độ khó cho game");
        subLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subLabel.setForeground(new Color(255, 255, 255, 200));
        subLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(subLabel, BorderLayout.SOUTH);

        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.Y_AXIS));
        optionsPanel.setBackground(Color.WHITE);
        optionsPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        final String[] selectedDifficulty = {"MEDIUM"};

        String[][] optionData = {
            {"Dễ", "12 thẻ bài - 3 phút", "EASY"},
            {"Trung bình", "20 thẻ bài - 4 phút", "MEDIUM"},
            {"Khó", "30 thẻ bài - 5 phút", "HARD"}
        };

        JPanel[] optionPanels = new JPanel[3];

        for (int i = 0; i < 3; i++) {
            final int index = i;
            String title = optionData[i][0];
            String desc = optionData[i][1];
            String difficulty = optionData[i][2];
            boolean isSelected = difficulty.equals("MEDIUM");

            JPanel optionPanel = new JPanel(new BorderLayout(15, 0));
            optionPanel.setPreferredSize(new Dimension(380, 70));
            optionPanel.setMaximumSize(new Dimension(400, 70));
            optionPanel.setMinimumSize(new Dimension(380, 70));
            optionPanel.setOpaque(true);
            optionPanel.setBackground(Color.WHITE);
            optionPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            if (isSelected) {
                optionPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(ACCENT_BLUE, 2, true),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            } else {
                optionPanel.setBorder(BorderFactory.createCompoundBorder(
                    new LineBorder(BORDER_COLOR, 2, true),
                    new EmptyBorder(10, 15, 10, 15)
                ));
            }

            JPanel radioPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2d = (Graphics2D) g;
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int size = 20;
                    int x = (getWidth() - size) / 2;
                    int y = (getHeight() - size) / 2;

                    if (selectedDifficulty[0].equals(difficulty)) {
                        g2d.setColor(ACCENT_BLUE);
                        g2d.setStroke(new java.awt.BasicStroke(2.5f));
                    } else {
                        g2d.setColor(BORDER_COLOR);
                        g2d.setStroke(new java.awt.BasicStroke(2f));
                    }
                    g2d.drawOval(x, y, size, size);

                    if (selectedDifficulty[0].equals(difficulty)) {
                        g2d.setColor(ACCENT_BLUE);
                        int innerSize = 10;
                        int innerX = x + (size - innerSize) / 2;
                        int innerY = y + (size - innerSize) / 2;
                        g2d.fillOval(innerX, innerY, innerSize, innerSize);
                    }
                }
            };
            radioPanel.setPreferredSize(new Dimension(30, 30));
            radioPanel.setOpaque(false);

            optionPanel.add(radioPanel, BorderLayout.WEST);

            JPanel textPanel = new JPanel();
            textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
            textPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
            titleLabel.setForeground(TEXT_DARK);
            titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel descLabel = new JLabel(desc);
            descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            descLabel.setForeground(TEXT_MUTED);
            descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

            textPanel.add(titleLabel);
            textPanel.add(Box.createVerticalStrut(3));
            textPanel.add(descLabel);

            optionPanel.add(textPanel, BorderLayout.CENTER);

            MouseAdapter clickHandler = new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedDifficulty[0] = difficulty;

                    for (int j = 0; j < optionPanels.length; j++) {
                        if (optionPanels[j] != null) {
                            if (j == index) {
                                optionPanels[j].setBorder(BorderFactory.createCompoundBorder(
                                    new LineBorder(ACCENT_BLUE, 2, true),
                                    new EmptyBorder(10, 15, 10, 15)
                                ));
                            } else {
                                optionPanels[j].setBorder(BorderFactory.createCompoundBorder(
                                    new LineBorder(BORDER_COLOR, 2, true),
                                    new EmptyBorder(10, 15, 10, 15)
                                ));
                            }
                            optionPanels[j].repaint();
                        }
                    }
                    radioPanel.repaint();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!selectedDifficulty[0].equals(difficulty)) {
                        optionPanel.setBackground(new Color(249, 250, 251));
                    }
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    optionPanel.setBackground(Color.WHITE);
                }
            };

            optionPanel.addMouseListener(clickHandler);
            radioPanel.addMouseListener(clickHandler);
            textPanel.addMouseListener(clickHandler);
            titleLabel.addMouseListener(clickHandler);
            descLabel.addMouseListener(clickHandler);

            optionPanels[i] = optionPanel;
            optionsPanel.add(optionPanel);

            if (i < 2) {
                optionsPanel.add(Box.createVerticalStrut(15));
            }
        }

        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton sendButton = createModernButton("Thách đấu", SUCCESS_COLOR, true);
        sendButton.setPreferredSize(new Dimension(150, 45));
        sendButton.addActionListener(e -> {
            network.sendChallenge(targetPlayer, selectedDifficulty[0]);
            diffDialog.dispose();
            showModernSuccess(
                "Đã gửi lời thách đấu đến " + targetPlayer + "!\nXin chờ phản hồi...",
                "Đã gửi thách dấu"
            );
        });

        JButton cancelButton = createModernButton("Hủy", new Color(107, 114, 128), false);
        cancelButton.setPreferredSize(new Dimension(120, 45));
        cancelButton.addActionListener(e -> diffDialog.dispose());

        buttonPanel.add(sendButton);
        buttonPanel.add(cancelButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        diffDialog.add(mainPanel);
        diffDialog.setVisible(true);
    }
    
    private void handleQuitGame() {
        int confirm = JOptionPane.showConfirmDialog(this,
            "Cảnh báo: Hệ thống sẽ ghi lại hành động của bạn.\n" +
            "3 lần thoát đấu = Tài khoản của bạn sẽ bị BAN!\n\nBạn chắc chứ?",
            "Xác nhận thoát",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (gameTimer != null) {
                gameTimer.stop();
            }
            network.quitGame();
            cardLayout.show(mainPanel, "LOBBY");
            network.getPlayers();
        }
    }
    
    public void handleLoginSuccess(JSONObject data) {
        SwingUtilities.invokeLater(() -> {
            currentUsername = data.getString("username");
            currentPlayerId = data.getInt("id");
            welcomeLabel.setText("Welcome, " + currentUsername + "!");
            
            int score = data.getInt("total_score");
            int wins = data.getInt("wins");
            int losses = data.getInt("losses");
            int totalGames = wins + losses;
            
            statsLabel.setText(String.format("Score: %d | Wins: %d | Losses: %d | Games: %d", 
                                            score, wins, losses, totalGames));
            
            cardLayout.show(mainPanel, "LOBBY");
            network.getPlayers();
            network.getLeaderboard();
        });
    }
    
    public void handleLoginFailed(String message) {
        SwingUtilities.invokeLater(() -> showModernError(message, "Login Failed"));
    }
    
    public void handleRegisterSuccess(String message) {
        SwingUtilities.invokeLater(() -> showModernSuccess(message + "\n\nPlease login now", "Registration Successful"));
    }
    
    public void handleRegisterFailed(String message) {
        SwingUtilities.invokeLater(() -> showModernError(message, "Registration Failed"));
    }
    
    public void updatePlayerList(JSONArray players) {
        SwingUtilities.invokeLater(() -> {
            playerTableModel.setRowCount(0);
            for (int i = 0; i < players.length(); i++) {
                JSONObject player = players.getJSONObject(i);
                String username = player.getString("username");
                if (!username.equals(currentUsername)) {
                    String status = player.getBoolean("inGame") ? "In Game" : "Available";
                    playerTableModel.addRow(new Object[]{username, status});
                }
            }
        });
    }
    
    public void updateLeaderboard(JSONArray data) {
        SwingUtilities.invokeLater(() -> {
            leaderboardTableModel.setRowCount(0);
            for (int i = 0; i < data.length(); i++) {
                JSONObject player = data.getJSONObject(i);
                int rank = i + 1;
                String username = player.getString("username");
                int score = player.getInt("total_score");
                int wins = player.getInt("wins");
                int losses = player.getInt("losses");
                String wl = wins + "-" + losses;
                double winRate = player.getDouble("win_rate");
                
                String rankIcon = getRankIcon(rank);
                leaderboardTableModel.addRow(new Object[]{
                    rankIcon + rank, 
                    username, 
                    score, 
                    wl, 
                    String.format("%.1f%%", winRate)
                });
            }
        });
    }
    
    private String getRankIcon(int rank) {
        switch(rank) {
            case 1: return "### ";
            case 2: return "## ";
            case 3: return "# ";
            default: return "";
        }
    }
    
    public void handleChallengeReceived(String from, String difficulty) {
        SwingUtilities.invokeLater(() -> {
            int response = JOptionPane.showConfirmDialog(this,
                "🎮 " + from + " đã thách đấu với chế độ " + difficulty + "!\n\nBạn có chấp nhận lời thách đấu không?",
                "Đã nhận được lời thách đấu",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            
            if (response == JOptionPane.YES_OPTION) {
                network.acceptChallenge(from, difficulty);
            } else {
                network.declineChallenge(from);
            }
        });
    }
    
    public void handleChallengeDeclined(String decliner) {
        SwingUtilities.invokeLater(() -> showModernError(decliner + " từ chối lời thách đấu", "Từ chối thách đấu"));
    }
    
    public void handleGameStart(JSONObject data) {
        SwingUtilities.invokeLater(() -> {
            opponent = data.getString("opponent");
            int cardCount = data.getInt("cardCount");
            remainingTime = data.getInt("timeLimit");
            int rows = data.getInt("rows");
            int cols = data.getInt("cols");
            
            JSONArray cardValuesArray = data.getJSONArray("cardValues");
            int[] cardValues = new int[cardValuesArray.length()];
            for (int i = 0; i < cardValuesArray.length(); i++) {
                cardValues[i] = cardValuesArray.getInt(i);
            }
            
            setupGameBoard(cardCount, cardValues, rows, cols);
            
            myScoreLabel.setText("0");
            opponentScoreLabel.setText("0");
            updateTimer();
            
            gameTimer = new Timer(1000, e -> {
                remainingTime--;
                updateTimer();
                if (remainingTime <= 0) {
                    gameTimer.stop();
                }
            });
            gameTimer.start();
            
            cardLayout.show(mainPanel, "GAME");
            canClick = true;
        });
    }
    
    private void setupGameBoard(int cardCount, int[] cardValues, int rows, int cols) {
        cardGridPanel.removeAll();
        cards.clear();
        firstCard = null;
        secondCard = null;
        
        cardGridPanel.setLayout(new GridLayout(rows, cols, 15, 15));
        
        for (int i = 0; i < cardCount; i++) {
            CardPanel card = new CardPanel(i);
            int value = cardValues[i];
            card.setValue(value);
            
            String imagePath = imageManager.getImagePath(value);
            if (imagePath != null) {
                card.setCardImage(imagePath);
            }
            
            card.setCardClickListener(this::onCardClick);
            cards.add(card);
            cardGridPanel.add(card);
        }
        
        cardGridPanel.revalidate();
        cardGridPanel.repaint();
        
        System.out.println("Game board setup: " + cardCount + " cards");
    }
    
    private void onCardClick(CardPanel card) {
        if (!canClick) return;
        
        if (firstCard == null) {
            firstCard = card;
            firstCard.flip();
        } else if (secondCard == null && card != firstCard) {
            secondCard = card;
            secondCard.flip();
            canClick = false;
            
            network.flipCards(firstCard.getIndex(), secondCard.getIndex());
        }
    }
    
    public void handleGameUpdate(JSONObject data) {
        SwingUtilities.invokeLater(() -> {
            int card1 = data.getInt("card1");
            int card2 = data.getInt("card2");
            int value1 = data.getInt("value1");
            int value2 = data.getInt("value2");
            boolean matched = data.getBoolean("matched");
            
            CardPanel c1 = cards.get(card1);
            CardPanel c2 = cards.get(card2);
            
            c1.setValue(value1);
            c2.setValue(value2);
            
            String img1 = imageManager.getImagePath(value1);
            String img2 = imageManager.getImagePath(value2);
            if (img1 != null) c1.setCardImage(img1);
            if (img2 != null) c2.setCardImage(img2);
            
            c1.flip();
            c2.flip();
            
            if (matched) {
                Timer matchTimer = new Timer(500, e -> {
                    c1.setMatched(true);
                    c2.setMatched(true);
                    firstCard = null;
                    secondCard = null;
                    canClick = true;
                });
                matchTimer.setRepeats(false);
                matchTimer.start();
            } else {
                Timer unflipTimer = new Timer(1200, e -> {
                    c1.unflip();
                    c2.unflip();
                    firstCard = null;
                    secondCard = null;
                    canClick = true;
                });
                unflipTimer.setRepeats(false);
                unflipTimer.start();
            }
        });
    }
    
    public void handleScoreUpdate(JSONObject data) {
        SwingUtilities.invokeLater(() -> {
            if (data.has(currentUsername)) {
                int myScore = data.getInt(currentUsername);
                myScoreLabel.setText(String.valueOf(myScore));
            }
            if (data.has(opponent)) {
                int oppScore = data.getInt(opponent);
                opponentScoreLabel.setText(String.valueOf(oppScore));
            }
        });
    }
    
    public void handleGameEnd(JSONObject data) {
        SwingUtilities.invokeLater(() -> {
            if (gameTimer != null) {
                gameTimer.stop();
            }

            String winner = data.getString("winner");
            String player1 = data.getString("player1");
            String player2 = data.getString("player2");
            int p1Score = data.getInt("player1Score");
            int p2Score = data.getInt("player2Score");

            int myScore = player1.equals(currentUsername) ? p1Score : p2Score;
            int oppScore = player1.equals(currentUsername) ? p2Score : p1Score;

            String message, title;

            if (winner.equals("DRAW")) {
                title = "Hòa!";
                message = String.format(
                    "<html><div style='text-align: center; padding: 10px;'>" +
                    "<h2 style='color: #F97316; margin: 10px 0;'>Kết thúc game ván này hòa!</h2>" +
                    "<p style='font-size: 14px; margin: 15px 0;'>" +
                    "<b>Điểm của bạn:</b> %d<br>" +
                    "<b>Điểm của %s's:</b> %d" +
                    "</p></div></html>",
                    myScore, opponent, oppScore
                );
            } else if (winner.equals(currentUsername)) {
                title = "Chiến thắng!";
                message = String.format(
                    "<html><div style='text-align: center; padding: 10px;'>" +
                    "<h2 style='color: #22C55E; margin: 10px 0;'>🏆 Xin chúc mừng! Bạn đã thắng! 🏆</h2>" +
                    "<p style='font-size: 14px; margin: 15px 0;'>" +
                    "<b>Điểm của bạn:</b> %d<br>" +
                    "<b>Điểm của %s's:</b> %d<br>" +
                    "<br><span style='color: #22C55E;'>nhận được +%d điểm!</span>" +
                    "</p></div></html>",
                    myScore, opponent, oppScore, myScore
                );
            } else {
                title = "Thua cuộc";
                message = String.format(
                    "<html><div style='text-align: center; padding: 10px;'>" +
                    "<h2 style='color: #EF4444; margin: 10px 0;'>Chúc bạn may mắn lần sau!</h2>" +
                    "<p style='font-size: 14px; margin: 15px 0;'>" +
                    "<b>Điểm của bạn:</b> %d<br>" +
                    "<b>Điểm của %s's:</b> %d" +
                    "</p></div></html>",
                    myScore, opponent, oppScore
                );
            }

            JOptionPane.showMessageDialog(this, message, title, JOptionPane.INFORMATION_MESSAGE);
            showRematchChoiceDialog(opponent);
        });
    }
    
    private void showRematchChoiceDialog(String opponentName) {
        closeRematchWaitingDialog();
        closeRematchRequestDialog();

        JDialog choiceDialog = new JDialog(this, "Game Finished", false);
        choiceDialog.setSize(450, 250);
        choiceDialog.setLocationRelativeTo(this);
        choiceDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        choiceDialog.setName("rematchChoiceDialog");

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(ACCENT_BLUE);
        headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel headerLabel = new JLabel("Play Again?");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(Color.WHITE);
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(0, 0, 0, 0));

        JLabel messageLabel = new JLabel(
            "<html><div style='text-align: center;'>" +
            "<p style='font-size: 16px; margin: 10px 0;'>Want to challenge <b>" + opponentName + "</b> again?</p>" +
            "</div></html>",
            SwingConstants.CENTER
        );
        contentPanel.add(messageLabel, BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

        JButton rematchButton = createModernButton("Rematch", SUCCESS_COLOR, true);
        rematchButton.setPreferredSize(new Dimension(140, 45));
        rematchButton.addActionListener(e -> {
            choiceDialog.dispose();
            showDifficultySelectionForRematch(opponentName);
        });

        JButton lobbyButton = createModernButton("Return to Lobby", new Color(107, 114, 128), false);
        lobbyButton.setPreferredSize(new Dimension(160, 45));
        lobbyButton.addActionListener(e -> {
            choiceDialog.dispose();
            network.notifyLeftLobby(opponentName);
            returnToLobby();
        });

        buttonPanel.add(rematchButton);
        buttonPanel.add(lobbyButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        choiceDialog.add(mainPanel);
        choiceDialog.setVisible(true);
    }
    
    private void showDifficultySelectionForRematch(String targetPlayer) {
        String[] difficulties = {"EASY", "MEDIUM", "HARD"};

        JDialog diffDialog = new JDialog(this, "Select Difficulty", true);
        diffDialog.setSize(380, 300);
        diffDialog.setLocationRelativeTo(this);

        JPanel mainPanel = new JPanel(new BorderLayout(0, 15));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel("Select Difficulty");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel optionsPanel = new JPanel(new GridLayout(3, 1, 0, 10));
        optionsPanel.setBackground(Color.WHITE);

        for (String diff : difficulties) {
            JButton diffButton = createModernButton(
                diff + " - " + Protocol.getCardCount(diff) + " cards",
                ACCENT_BLUE,
                false
            );
            diffButton.setPreferredSize(new Dimension(300, 50));
            diffButton.addActionListener(e -> {
                diffDialog.dispose();
                network.sendRematchRequest(targetPlayer, diff);
                showWaitingForRematchDialog(targetPlayer);
            });
            optionsPanel.add(diffButton);
        }

        mainPanel.add(optionsPanel, BorderLayout.CENTER);

        JButton cancelButton = createModernButton("Cancel", DANGER_COLOR, false);
        cancelButton.setPreferredSize(new Dimension(100, 40));
        cancelButton.addActionListener(e -> {
            diffDialog.dispose();
            returnToLobby();
        });

        JPanel cancelPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        cancelPanel.setBackground(Color.WHITE);
        cancelPanel.add(cancelButton);
        mainPanel.add(cancelPanel, BorderLayout.SOUTH);

        diffDialog.add(mainPanel);
        diffDialog.setVisible(true);
    }

    public void handleOpponentLeftLobby(String opponentName) {
        SwingUtilities.invokeLater(() -> {
            closeRematchChoiceDialog();
            closeRematchRequestDialog();
            closeRematchWaitingDialog();
            
            JOptionPane.showMessageDialog(this,
                "<html><div style='text-align: center; padding: 10px;'>" +
                "<h3 style='color: #F97316;'>⚠️ Opponent Left</h3>" +
                "<p style='color: #6B7280; margin-top: 10px;'>" +
                opponentName + " returned to lobby</p>" +
                "</div></html>",
                "Opponent Left",
                JOptionPane.INFORMATION_MESSAGE);

            returnToLobby();
        });
    }
    
    private void closeRematchChoiceDialog() {
        for (Window window : Window.getWindows()) {
            if (window instanceof JDialog) {
                JDialog dialog = (JDialog) window;
                if ("rematchChoiceDialog".equals(dialog.getName())) {
                    dialog.dispose();
                    break;
                }
            }
        }
    }
    
    private void showWaitingForRematchDialog(String opponentName) {
        SwingUtilities.invokeLater(() -> {
            closeRematchWaitingDialog();
            closeRematchRequestDialog();

            rematchWaitingDialog = new JDialog(this, "Waiting for Response", false);
            rematchWaitingDialog.setSize(400, 200);
            rematchWaitingDialog.setLocationRelativeTo(this);
            rematchWaitingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel panel = new JPanel(new BorderLayout(0, 20));
            panel.setBackground(Color.WHITE);
            panel.setBorder(new EmptyBorder(30, 30, 30, 30));

            JLabel waitingLabel = new JLabel(
                "<html><div style='text-align: center;'>" +
                "<h2 style='color: #3B82F6;'>Waiting for " + opponentName + "</h2>" +
                "<p style='color: #6B7280; margin-top: 10px;'>Waiting for response to your rematch request...</p>" +
                "</div></html>",
                SwingConstants.CENTER
            );
            panel.add(waitingLabel, BorderLayout.CENTER);

            JButton cancelButton = createModernButton("Cancel", DANGER_COLOR, false);
            cancelButton.addActionListener(e -> {
                closeRematchWaitingDialog();
                returnToLobby();
            });

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.add(cancelButton);
            panel.add(buttonPanel, BorderLayout.SOUTH);

            rematchWaitingDialog.add(panel);
            rematchWaitingDialog.setVisible(true);
        });
    }
    
    public void handleOpponentQuit(String quitter) {
        SwingUtilities.invokeLater(() -> {
            if (gameTimer != null) {
                gameTimer.stop();
            }
            showModernSuccess(quitter + " quit the game.\n\nYou win by default! +100 points", "Victory!");
            returnToLobby();
        });
    }
    
    public void handleRematchRequest(String from, String difficulty) {
        SwingUtilities.invokeLater(() -> {
            closeRematchChoiceDialog();
            closeRematchWaitingDialog();
            closeRematchRequestDialog();

            rematchRequestDialog = new JDialog(this, "Đấu lại", true);
            rematchRequestDialog.setSize(450, 280);
            rematchRequestDialog.setLocationRelativeTo(this);
            rematchRequestDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

            JPanel mainPanel = new JPanel(new BorderLayout());
            mainPanel.setBackground(Color.WHITE);

            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(ACCENT_BLUE);
            headerPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            JLabel headerLabel = new JLabel("Đấu lại!");
            headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
            headerLabel.setForeground(Color.WHITE);
            headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
            headerPanel.add(headerLabel, BorderLayout.CENTER);
            mainPanel.add(headerPanel, BorderLayout.NORTH);

            JPanel contentPanel = new JPanel(new BorderLayout(0, 20));
            contentPanel.setBackground(Color.WHITE);
            contentPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

            JLabel messageLabel = new JLabel(
                "<html><div style='text-align: center;'>" +
                "<p style='font-size: 16px; margin: 10px 0;'>" +
                "<b>" + from + "</b> Bạn muốn đấu lại chứ!</p>" +
                "<p style='font-size: 14px; color: #6B7280; margin: 15px 0;'>" +
                "Độ khó: <b style='color: #3B82F6;'>" + difficulty + "</b></p>" +
                "<p style='font-size: 14px; color: #6B7280;'>Bạn có muốn chấp nhận?</p>" +
                "</div></html>",
                SwingConstants.CENTER
            );
            contentPanel.add(messageLabel, BorderLayout.CENTER);
            mainPanel.add(contentPanel, BorderLayout.CENTER);

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 20));
            buttonPanel.setBackground(Color.WHITE);
            buttonPanel.setBorder(new MatteBorder(1, 0, 0, 0, BORDER_COLOR));

            JButton acceptButton = createModernButton("Đồng ý", SUCCESS_COLOR, true);
            acceptButton.setPreferredSize(new Dimension(130, 45));
            acceptButton.addActionListener(e -> {
                closeRematchRequestDialog();
                network.sendRematch(from, difficulty, true);
            });

            JButton declineButton = createModernButton("Từ chối", DANGER_COLOR, false);
            declineButton.setPreferredSize(new Dimension(130, 45));
            declineButton.addActionListener(e -> {
                closeRematchRequestDialog();
                closeRematchWaitingDialog();
                closeRematchChoiceDialog();
                network.sendRematch(from, difficulty, false);
                returnToLobby();
            });

            buttonPanel.add(acceptButton);
            buttonPanel.add(declineButton);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);

            rematchRequestDialog.add(mainPanel);
            rematchRequestDialog.setVisible(true);
        });
    }
    
    public void handleRematchAccepted(String from) {
        SwingUtilities.invokeLater(() -> {
            closeRematchWaitingDialog();
            closeRematchRequestDialog();
        });
    }
    
    public void handleRematchDeclined(String from) {
        SwingUtilities.invokeLater(() -> {
            closeRematchWaitingDialog();
            closeRematchRequestDialog();

            JOptionPane.showMessageDialog(this,
                "<html><div style='text-align: center; padding: 10px;'>" +
                "<h3 style='color: #EF4444;'>Yêu cầu bị từ chối</h3>" +
                "<p style='color: #6B7280; margin-top: 10px;'>" +
                from + " Đã từ chối yêu cầu của bạn</p>" +
                "</div></html>",
                "Từ chối đấu lại",
                JOptionPane.INFORMATION_MESSAGE);

            returnToLobby();
        });
    }
    
    private void closeRematchWaitingDialog() {
        if (rematchWaitingDialog != null && rematchWaitingDialog.isVisible()) {
            rematchWaitingDialog.dispose();
            rematchWaitingDialog = null;
        }
    }
    
    private void closeRematchRequestDialog() {
        if (rematchRequestDialog != null && rematchRequestDialog.isVisible()) {
            rematchRequestDialog.dispose();
            rematchRequestDialog = null;
        }
    }
    
    private void returnToLobby() {
        cardLayout.show(mainPanel, "LOBBY");
        network.getPlayers();
        network.getLeaderboard();
    }
    
    public void showError(String message) {
        SwingUtilities.invokeLater(() -> showModernError(message, "Error"));
    }
    
    private void showModernError(String message, String title) {
        JOptionPane.showMessageDialog(this, "❌ " + message, title, JOptionPane.ERROR_MESSAGE);
    }
    
    private void showModernSuccess(String message, String title) {
        JOptionPane.showMessageDialog(this, "✓ " + message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void updateTimer() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;
        timerLabel.setText(String.format("%d:%02d", minutes, seconds));
        
        int totalTime = Protocol.getTimeLimit(Protocol.MEDIUM);
        int progress = (remainingTime * 100) / totalTime;
        gameProgress.setValue(progress);
        
        if (remainingTime <= 30) {
            timerLabel.setForeground(DANGER_COLOR);
            gameProgress.setForeground(DANGER_COLOR);
        } else if (remainingTime <= 60) {
            timerLabel.setForeground(WARNING_COLOR);
            gameProgress.setForeground(WARNING_COLOR);
        }
    }
    
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ClientGUI gui = new ClientGUI();
            gui.setVisible(true);
        });
    }
}
