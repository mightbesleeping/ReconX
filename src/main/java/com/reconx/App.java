package com.reconx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class App extends Application {

    private final GoogleSearchService googleService = new GoogleSearchService();
    private final SocialMediaService socialMediaService = new SocialMediaService();
    private final EmailService emailService = new EmailService();
    private final DomainService domainService = new DomainService();
    private final IPService ipService = new IPService();

    private Label statusLabel;
    private ProgressBar progressBar;

    @Override
    public void start(Stage stage) {
        // --- HEADER ---
        VBox header = new VBox(5);
        header.getStyleClass().add("header-section");
        Label titleLabel = new Label("RECONX INTELLIGENCE SYSTEM");
        titleLabel.getStyleClass().add("title-label");
        Label subtitleLabel = new Label("Advanced OSINT Analysis Framework v1.0");
        subtitleLabel.getStyleClass().add("subtitle-label");
        header.getChildren().addAll(titleLabel, subtitleLabel);

        // --- SEARCH SECTION ---
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER);
        TextField inputField = new TextField();
        inputField.setPromptText("Enter Target (IP / Domain / Email / Username)...");
        HBox.setHgrow(inputField, Priority.ALWAYS);
        Button searchBtn = new Button("EXECUTE SCAN");
        searchBtn.getStyleClass().add("button-primary");
        searchBox.getChildren().addAll(inputField, searchBtn);

        // --- ACTION TOOLBAR ---
        FlowPane toolbar = new FlowPane(10, 10);
        toolbar.setAlignment(Pos.CENTER);

        Button saveBtn = new Button("SAVE REPORT");
        saveBtn.getStyleClass().add("button-secondary");

        Button openFolderBtn = new Button("REPORTS FOLDER");
        openFolderBtn.getStyleClass().add("button-secondary");

        Button historyBtn = new Button("HISTORY");
        historyBtn.getStyleClass().add("button-secondary");

        Button clearBtn = new Button("CLEAR CONSOLE");
        clearBtn.getStyleClass().add("button-danger");

        toolbar.getChildren().addAll(saveBtn, openFolderBtn, historyBtn, clearBtn);

        // --- RESULTS AREA ---
        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPromptText("Analysis results will appear here...");
        VBox.setVgrow(resultsArea, Priority.ALWAYS);

        // --- FOOTER / STATUS ---
        HBox footer = new HBox(15);
        footer.getStyleClass().add("footer-section");
        footer.setAlignment(Pos.CENTER_LEFT);
        statusLabel = new Label("Ready");
        progressBar = new ProgressBar(0);
        progressBar.setVisible(false);
        progressBar.setPrefWidth(150);
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label versionLabel = new Label("OSINT CORE v2.5");
        footer.getChildren().addAll(statusLabel, progressBar, spacer, versionLabel);

        // --- MAIN LAYOUT ---
        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));
        mainLayout.getStyleClass().add("main-container");
        mainLayout.getChildren().addAll(header, searchBox, toolbar, resultsArea, footer);

        // Initialize DB
        DatabaseManager.initialize();

        // --- ACTIONS ---
        clearBtn.setOnAction(e -> resultsArea.clear());

        saveBtn.setOnAction(e -> {
            String target = inputField.getText().trim();
            String data = resultsArea.getText();
            if (!target.isEmpty() && !data.isEmpty()) {
                ReportManager.saveReport(stage, target, data);
            } else {
                showToast("No data to save. Run a scan first.");
            }
        });

        openFolderBtn.setOnAction(e -> ReportManager.openReportsFolder());

        historyBtn.setOnAction(e -> {
            resultsArea.clear();
            resultsArea.setText(DatabaseManager.getHistory());
        });

        searchBtn.setOnAction(e -> {
            String input = inputField.getText().trim();
            if (input.isEmpty()) return;

            resultsArea.clear();
            String type = detectInputType(input);

            updateStatus("Scanning " + input + "...", true);
            DatabaseManager.saveSearch(input, type);

            new Thread(() -> {
                StringBuilder resultBuilder = new StringBuilder();
                try {
                    switch (type) {
                        case "IP_ADDRESS":
                            updateStatus("Querying Geo-IP...", true);
                            resultBuilder.append(ipService.getIPInfo(input));
                            break;
                        case "DOMAIN_NAME":
                            updateStatus("Fetching WHOIS/DNS...", true);
                            resultBuilder.append(domainService.getDNSRecords(input));
                            break;
                        case "EMAIL_ADDRESS":
                            updateStatus("Checking Breaches...", true);
                            resultBuilder.append(emailService.getEmailReport(input));
                            break;
                        case "USERNAME":
                            updateStatus("Checking Social Media...", true);
                            resultBuilder.append(socialMediaService.checkProfiles(input));
                            resultBuilder.append("\n").append(googleService.searchSocialMedia(input));
                            break;
                    }
                } catch (Exception ex) {
                    resultBuilder.append("[!] Error: ").append(ex.getMessage());
                }

                Platform.runLater(() -> {
                    resultsArea.setText(resultBuilder.toString());
                    updateStatus("Scan Complete", false);
                });
            }).start();
        });

        // --- SCENE ---
        Scene scene = new Scene(mainLayout, 850, 650);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
            System.err.println("Style could not be loaded: " + ex.getMessage());
        }

        stage.setTitle("ReconX Intelligence System");
        stage.setScene(scene);
        stage.show();
    }

    private void updateStatus(String message, boolean active) {
        Platform.runLater(() -> {
            statusLabel.setText(message);
            progressBar.setVisible(active);
            progressBar.setProgress(active ? -1 : 0);
        });
    }

    private void showToast(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.show();
    }

    private String detectInputType(String input) {
        if (input.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) return "IP_ADDRESS";
        if (input.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return "EMAIL_ADDRESS";
        if (input.contains(".") && !input.startsWith("http")) return "DOMAIN_NAME";
        return "USERNAME";
    }

    public static void main(String[] args) {
        launch();
    }
}