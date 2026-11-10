package ui;

import model.Alert;
import model.CanAlert;
import model.V2xAlert;
import service.Team6_AlertManagement;
import service.Team6_RiskRanking;
import util.ValidationException;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collections;
import java.util.Locale;

/**
 * Java AWT Modal Dialog for creating and editing security alerts.
 */
public class AwtAlertFormDialog extends Dialog {

    private final Team6_AlertManagement alertService;
    private final Team6_RiskRanking riskRankingService;
    private final Alert existingAlert;
    private boolean saved = false;

    private final Choice cmbDomain = new Choice();
    private final TextField txtAttackType = new TextField(20);
    private final TextField txtConfidence = new TextField("0.90", 10);
    private final Choice cmbTechnique = new Choice();
    private final Choice cmbSeverity = new Choice();
    private final TextField txtTargetAssets = new TextField("['Braking_ECU']", 25);
    private final Choice cmbStatus = new Choice();
    private final Label lblError = new Label("");

    public AwtAlertFormDialog(Frame parent, String title, Alert existingAlert,
                              Team6_AlertManagement alertService,
                              Team6_RiskRanking riskRankingService) {
        super(parent, title, true);
        this.alertService = alertService;
        this.riskRankingService = riskRankingService;
        this.existingAlert = existingAlert;

        setLayout(new BorderLayout(10, 10));
        setBackground(new Color(241, 245, 249));
        setSize(540, 430);
        setLocationRelativeTo(parent);

        Label lblTitle = new Label(title);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblTitle.setAlignment(Label.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        Panel formPanel = new Panel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 10, 6, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        cmbDomain.add("CAN");
        cmbDomain.add("V2X");

        cmbTechnique.add("T0814 - Denial of Service");
        cmbTechnique.add("T0855 - Unauthorized Command Message");
        cmbTechnique.add("T0856 - Spoof Reporting Message");
        cmbTechnique.add("T0831 - Manipulation of Control");
        cmbTechnique.add("T1498 - Network Denial of Service");
        cmbTechnique.add("T1565.002 - Transmitted Data Manipulation");

        cmbSeverity.add("CRITICAL");
        cmbSeverity.add("HIGH");
        cmbSeverity.add("MEDIUM");
        cmbSeverity.add("LOW");

        cmbStatus.add("NEW");
        cmbStatus.add("ACKNOWLEDGED");
        cmbStatus.add("RESOLVED");
        cmbStatus.add("DISMISSED");

        int r = 0;
        addField(formPanel, gbc, r++, "Vehicular Domain:", cmbDomain);
        addField(formPanel, gbc, r++, "Predicted Attack:", txtAttackType);
        addField(formPanel, gbc, r++, "Model Confidence (0.0 - 1.0):", txtConfidence);
        addField(formPanel, gbc, r++, "MITRE ATT&CK Technique:", cmbTechnique);
        addField(formPanel, gbc, r++, "Severity Tier:", cmbSeverity);
        addField(formPanel, gbc, r++, "Affected Target Assets:", txtTargetAssets);
        addField(formPanel, gbc, r++, "Alert Lifecycle Status:", cmbStatus);

        add(formPanel, BorderLayout.CENTER);

        Panel bottomPanel = new Panel(new BorderLayout());
        bottomPanel.setBackground(new Color(241, 245, 249));

        lblError.setForeground(new Color(220, 38, 38));
        lblError.setFont(new Font("SansSerif", Font.BOLD, 11));
        bottomPanel.add(lblError, BorderLayout.NORTH);

        Panel btnPanel = new Panel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        Button btnSave = new Button(existingAlert == null ? "Create Alert" : "Save Changes");
        btnSave.setFont(new Font("SansSerif", Font.BOLD, 12));
        btnSave.setBackground(new Color(37, 99, 235));
        btnSave.setForeground(Color.WHITE);
        btnSave.addActionListener(e -> onSave());

        Button btnCancel = new Button("Cancel");
        btnCancel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());

        btnPanel.add(btnCancel);
        btnPanel.add(btnSave);
        bottomPanel.add(btnPanel, BorderLayout.SOUTH);
        add(bottomPanel, BorderLayout.SOUTH);

        if (existingAlert != null) {
            cmbDomain.select(existingAlert.getDomain());
            txtAttackType.setText(existingAlert.getAttackType());
            txtConfidence.setText(String.format(Locale.US, "%.4f", existingAlert.getConfidence()));
            selectTechnique(existingAlert.getMitreTechnique());
            cmbSeverity.select(existingAlert.getSeverity());
            txtTargetAssets.setText(existingAlert.getTargetAssets());
            cmbStatus.select(existingAlert.getStatus());
        } else {
            txtAttackType.setText("DoS");
        }

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });
    }

    private void selectTechnique(String mitreId) {
        if (mitreId == null) return;
        for (int i = 0; i < cmbTechnique.getItemCount(); i++) {
            if (cmbTechnique.getItem(i).startsWith(mitreId.trim())) {
                cmbTechnique.select(i);
                return;
            }
        }
    }

    private void addField(Panel p, GridBagConstraints gbc, int row, String label, Component comp) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.3;
        Label l = new Label(label);
        l.setFont(new Font("SansSerif", Font.BOLD, 11));
        p.add(l, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        p.add(comp, gbc);
    }

    private void onSave() {
        try {
            String domain = cmbDomain.getSelectedItem();
            String attack = txtAttackType.getText().trim();
            if (attack.isEmpty()) throw new ValidationException("attackType", "Attack type cannot be empty.");

            double conf;
            try {
                conf = Double.parseDouble(txtConfidence.getText().trim());
            } catch (NumberFormatException e) {
                throw new ValidationException("confidence", "Confidence must be a valid number.");
            }
            if (conf < 0.0 || conf > 1.0) {
                throw new ValidationException("confidence", "Confidence must be between 0.0 and 1.0.");
            }

            String[] techParts = cmbTechnique.getSelectedItem().split(" - ", 2);
            String tech = techParts[0];
            String techName = techParts.length > 1 ? techParts[1] : "Unknown Technique";
            String sev = cmbSeverity.getSelectedItem();
            String assets = txtTargetAssets.getText().trim();
            String status = cmbStatus.getSelectedItem();

            String cwe = "CWE-345";
            if (attack.toUpperCase().contains("DOS")) cwe = "CWE-400";
            else if (attack.toUpperCase().contains("GAS") || attack.toUpperCase().contains("STEERING")) cwe = "CWE-306";
            else if (attack.toUpperCase().contains("RANDOM_POS")) cwe = "CWE-399";

            if (existingAlert == null) {
                Alert newAlert;
                if ("CAN".equalsIgnoreCase(domain)) {
                    int canId = attack.toUpperCase().contains("DOS") ? 291 : 513;
                    newAlert = new CanAlert(0, 0, attack, conf, 0.0, tech, techName, sev, assets, cwe,
                            java.time.LocalDateTime.now().toString(), status, canId, "", 8);
                } else {
                    newAlert = new V2xAlert(0, 0, attack, conf, 0.0, tech, techName, sev, assets, cwe,
                            java.time.LocalDateTime.now().toString(), status, 0, 0, 0.0, 0.0, 0.0);
                }
                if (riskRankingService != null) {
                    riskRankingService.recalculateHeuristicRiskScores(Collections.singletonList(newAlert));
                } else {
                    newAlert.calculateHeuristicRisk(3.5, 3.5);
                }
                newAlert.setSeverity(sev);
                alertService.ap25110090004_AlertManagement_create(newAlert);
            } else {
                existingAlert.setDomain(domain);
                existingAlert.setAttackType(attack);
                existingAlert.setConfidence(conf);
                existingAlert.setMitreTechnique(tech);
                existingAlert.setTechniqueName(techName);
                existingAlert.setSeverity(sev);
                existingAlert.setTargetAssets(assets);
                existingAlert.setStatus(status);
                if (riskRankingService != null) {
                    riskRankingService.recalculateHeuristicRiskScores(Collections.singletonList(existingAlert));
                } else {
                    existingAlert.calculateHeuristicRisk(3.5, 3.5);
                }
                existingAlert.setSeverity(sev);
                alertService.ap25110090004_AlertManagement_update(existingAlert);
            }

            saved = true;
            dispose();
        } catch (Exception ex) {
            lblError.setText("Validation Error: " + ex.getMessage());
        }
    }

    public boolean isSaved() {
        return saved;
    }
}
