package com.reconx;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        EmailService emailService = new EmailService();
        DomainService domainService = new DomainService();
        // 1. Initialize all UI components first
        Label titleLabel = new Label("> ReconX_Scanner_v1.0");
        TextField inputField = new TextField();
        inputField.setPromptText("Enter target (IP/Domain/Email)...");

        Button searchBtn = new Button("EXECUTE SCAN");
        Button clearBtn = new Button("CLEAR CONSOLE");
        clearBtn.setStyle("-fx-background-color: #ff0000; -fx-text-fill: white;");

        TextArea resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setPrefHeight(300);

        // 2. Setup the Layout
        VBox layout = new VBox(15, titleLabel, inputField, searchBtn, clearBtn, resultsArea);
        layout.setPadding(new Insets(20));

        // 3. Services
        IPService ipService = new IPService();
        DatabaseManager.initialize(); // Ensure DB is ready

        // 4. Button Actions
        clearBtn.setOnAction(e -> resultsArea.clear());

        searchBtn.setOnAction(e -> {
            String input = inputField.getText().trim();
            if (input.isEmpty()) return;

            String type = detectInputType(input);
            resultsArea.setText("[*] Target: " + input + " (" + type + ")\n[*] Status: Querying APIs...");

            // Save to local history database
            DatabaseManager.saveSearch(input, type);

            if (type.equals("IP_ADDRESS")) {
                new Thread(() -> {
                    String result = ipService.getIPInfo(input);
                    javafx.application.Platform.runLater(() -> resultsArea.setText(result));
                }).start();
            }
            else if (type.equals("DOMAIN_NAME")) {
                new Thread(() -> {
                    String result = domainService.getDNSRecords(input);
                    javafx.application.Platform.runLater(() -> resultsArea.setText(result));
                }).start();
            }
            else if (type.equals("EMAIL_ADDRESS")) {
                new Thread(() -> {
                    String result = emailService.getEmailReport(input);
                    javafx.application.Platform.runLater(() -> resultsArea.setText(result));
                }).start();
            }
// Final Catch-All for typos or empty detections
            else {
                resultsArea.setText("[!] Error: Could not detect a valid Target Type.\n[!] Please try an IP (8.8.8.8), Domain (google.com), or Email.");
            }
        });

        // 5. Scene & Styling
        Scene scene = new Scene(layout, 550, 500);
        try {
            scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        } catch (Exception ex) {
            System.out.println("Style sheet not found.");
        }

        stage.setTitle("ReconX - OSINT Aggregator");
        stage.setScene(scene);
        stage.show();
    }

    private String detectInputType(String input) {
        if (input.matches("^(\\d{1,3}\\.){3}\\d{1,3}$")) return "IP_ADDRESS";
        if (input.matches("^[A-Za-z0-9+_.-]+@(.+)$")) return "EMAIL_ADDRESS";
        if (input.contains(".") && !input.startsWith("http")) return "DOMAIN_NAME";
        return "UNKNOWN_FORMAT";
    }

    public static void main(String[] args) {
        launch();
    }
}