package com.reconx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    // Declare services globally so they are created once
    private final GoogleSearchService googleService = new GoogleSearchService();
    private final EmailService emailService = new EmailService();
    private final DomainService domainService = new DomainService();
    private final IPService ipService = new IPService();

    @Override
    public void start(Stage stage) {
        // --- UI COMPONENTS ---
        Label titleLabel = new Label("> ReconX_Scanner_v1.0");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #00ff00;");

        TextField inputField = new TextField();
        inputField.setPromptText("Enter Target (IP / Domain / Email / Username)...");

        Button searchBtn = new Button("EXECUTE INTELLIGENCE SCAN");
        searchBtn.setMaxWidth(Double.MAX_VALUE); // Button stretches to fill width

        Button saveBtn = new Button("SAVE REPORT TO FILE");
        saveBtn.setStyle("-fx-background-color: #0000FF; -fx-text-fill: white;");
        saveBtn.setMaxWidth(Double.MAX_VALUE);

        Button clearBtn = new Button("CLEAR CONSOLE");
        clearBtn.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");
        clearBtn.setMaxWidth(Double.MAX_VALUE);

        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(400);
        resultsArea.setStyle("-fx-control-inner-background: #000000; -fx-text-fill: #00ff00; -fx-font-family: 'Consolas';");

        // --- LAYOUT ---
        VBox layout = new VBox(15, titleLabel, inputField, searchBtn, saveBtn, clearBtn, resultsArea);
        layout.setPadding(new Insets(20));
        VBox.setVgrow(resultsArea, Priority.ALWAYS); // Area grows with window

        // Initialize DB
        DatabaseManager.initialize();

        // --- ACTION HANDLERS ---

        // 1. CLEAR BUTTON
        clearBtn.setOnAction(e -> resultsArea.clear());

        // 2. SAVE BUTTON
        saveBtn.setOnAction(e -> {
            String target = inputField.getText().trim();
            String data = resultsArea.getText();
            if (!target.isEmpty() && !data.isEmpty()) {
                ReportManager.saveReport(target, data);
            } else {
                resultsArea.setText("[!] No data to save. Run a scan first.");
            }
        });

        // 3. SEARCH BUTTON (The Core Logic)
        searchBtn.setOnAction(e -> {
            String input = inputField.getText().trim();
            if (input.isEmpty()) return;

            resultsArea.clear();
            String type = detectInputType(input);

            // Initial Status Update
            resultsArea.setText("[*] Target: " + input + "\n[*] Type Detected: " + type + "\n[*] Status: Initializing Search Algorithms...\n");

            // Save to History DB
            DatabaseManager.saveSearch(input, type);

            // Launch Search in Background Thread
            new Thread(() -> {
                String result = "";

                try {
                    switch (type) {
                        case "IP_ADDRESS":
                            updateStatus(resultsArea, "Querying Geo-Location Databases...");
                            result = ipService.getIPInfo(input);
                            break;

                        case "DOMAIN_NAME":
                            updateStatus(resultsArea, "Fetching DNS & WHOIS Records...");
                            result = domainService.getDNSRecords(input);
                            break;

                        case "EMAIL_ADDRESS":
                            updateStatus(resultsArea, "Checking Reputation & Breaches...");
                            result = emailService.getEmailReport(input);
                            break;

                        case "USERNAME":
                            updateStatus(resultsArea, "Running Google Intelligence Algorithm...");
                            // This uses your new Google API Service
                            result = googleService.searchSocialMedia(input);
                            break;

                        default:
                            result = "[!] Error: Unknown format. Try an IP, Domain, or Email.";
                    }
                } catch (Exception ex) {
                    result = "[!] Critical Error during scan: " + ex.getMessage();
                }

                // Final Update to UI
                String finalResult = result;
                Platform.runLater(() -> resultsArea.appendText("\n" + finalResult + "\n\n[*] SCAN COMPLETE."));

            }).start();
        });

        // --- SCENE SETUP ---
        Scene scene = new Scene(layout, 600, 550);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
            System.out.println("Warning: style.css not found.");
        }

        stage.setTitle("ReconX - Advanced OSINT Tool");
        stage.setScene(scene);
        stage.show();
    }

    // Helper to safely update text area from background thread
    private void updateStatus(TextArea area, String status) {
        Platform.runLater(() -> area.appendText("[*] " + status + "\n"));
    }

    // Smart Detection Regex
    private String detectInputType(String input) {
        if (input.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) return "IP_ADDRESS";
        if (input.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return "EMAIL_ADDRESS";
        // Check for domain (must have dot, not start with http, and no spaces)
        if (input.contains(".") && !input.startsWith("http") && !input.contains(" ")) return "DOMAIN_NAME";
        // Fallback to Username
        return "USERNAME";
    }

    public static void main(String[] args) {
        launch();
    }
}