package ui;

import model.User;
import service.*;
import util.AuthenticationException;
import util.DataAccessException;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Java AWT Operator Login Window with Salted SHA-256 Authentication.
 */
public class AwtLoginFrame extends Frame {

    private final Team6_UserReports userReportsService;
    private final Team6_AlertManagement alertService;
    private final Team6_ThreatGraph threatGraphService;
    private final Team6_TelemetryIngest telemetryService;
    private final Team6_RiskRanking riskRankingService;

    private final TextField txtUsername = new TextField("admin", 18);
    private final TextField txtPassword = new TextField("admin123", 18);
    private final Label lblMessage = new Label("");

    public AwtLoginFrame(Team6_UserReports userReportsService,
                         Team6_AlertManagement alertService,
                         Team6_ThreatGraph threatGraphService,
                         Team6_TelemetryIngest telemetryService,
                         Team6_RiskRanking riskRankingService) {
        super("Connected Vehicle CTI Console - Operator Authentication");
        this.userReportsService = userReportsService;
        this.alertService = alertService;
        this.threatGraphService = threatGraphService;
        this.telemetryService = telemetryService;
        this.riskRankingService = riskRankingService;

        setSize(460, 360);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(241, 245, 249));

        txtPassword.setEchoChar('*');

        // Banner
        Panel banner = new Panel(new GridLayout(2, 1, 4, 4));
        banner.setBackground(new Color(30, 41, 59));
        Label l1 = new Label("AUTOMOTIVE CTI OPERATIONS CONSOLE", Label.CENTER);
        l1.setFont(new Font("SansSerif", Font.BOLD, 13));
        l1.setForeground(Color.WHITE);

        Label l2 = new Label("Operator Access Control (Salted SHA-256)", Label.CENTER);
        l2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        l2.setForeground(new Color(203, 213, 225));

        banner.add(l1);
        banner.add(l2);
        add(banner, BorderLayout.NORTH);

        // Center Form
        Panel formPanel = new Panel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 12, 8, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        Label lu = new Label("Username:");
        lu.setFont(new Font("SansSerif", Font.BOLD, 12));
        formPanel.add(lu, gbc);

        gbc.gridx = 1;
        formPanel.add(txtUsername, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        Label lp = new Label("Password:");
        lp.setFont(new Font("SansSerif", Font.BOLD, 12));
        formPanel.add(lp, gbc);

        gbc.gridx = 1;
        formPanel.add(txtPassword, gbc);

        // Credentials Hint
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 2;
        Label hint = new Label("Default Accounts: admin / admin123 | analyst / analyst123", Label.CENTER);
        hint.setFont(new Font("SansSerif", Font.ITALIC, 10));
        hint.setForeground(new Color(100, 116, 139));
        formPanel.add(hint, gbc);

        add(formPanel, BorderLayout.CENTER);

        // Bottom Controls
        Panel bottom = new Panel(new BorderLayout(5, 5));
        bottom.setBackground(new Color(241, 245, 249));

        lblMessage.setAlignment(Label.CENTER);
        lblMessage.setForeground(new Color(220, 38, 38));
        lblMessage.setFont(new Font("SansSerif", Font.BOLD, 11));
        bottom.add(lblMessage, BorderLayout.NORTH);

        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        Button btnLogin = new Button("Secure Login");
        btnLogin.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnLogin.setBackground(new Color(37, 99, 235));
        btnLogin.setForeground(Color.WHITE);
        btnLogin.addActionListener(e -> attemptLogin());

        Button btnExit = new Button("Exit");
        btnExit.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnExit.addActionListener(e -> System.exit(0));

        btnPanel.add(btnLogin);
        btnPanel.add(btnExit);
        bottom.add(btnPanel, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        // Enter key submission
        txtPassword.addActionListener(e -> attemptLogin());
        txtUsername.addActionListener(e -> attemptLogin());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }

    private void attemptLogin() {
        String u = txtUsername.getText().trim();
        String p = txtPassword.getText();
        try {
            User user = userReportsService.authenticate(u, p);
            dispose();
            AwtDashboardFrame dashboard = new AwtDashboardFrame(user, alertService, threatGraphService,
                    telemetryService, riskRankingService, userReportsService);
            dashboard.setVisible(true);
        } catch (AuthenticationException ex) {
            lblMessage.setText("Authentication Failed: " + ex.getMessage());
        } catch (DataAccessException ex) {
            lblMessage.setText("Database Connection Error: " + ex.getMessage());
        }
    }
}

