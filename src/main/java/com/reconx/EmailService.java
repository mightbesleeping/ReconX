package com.reconx;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EmailService {
    // New reliable free API: Disify
    private static final String API_URL = "https://www.disify.com/api/email/";

    public String getEmailReport(String email) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(API_URL + email)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonData = response.body().string();
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();

                // Safe extraction using helper method
                boolean format = getSafeBoolean(json, "format");
                boolean disposable = getSafeBoolean(json, "disposable");
                boolean dns = getSafeBoolean(json, "dns");

                String domain = (json.has("domain") && !json.get("domain").isJsonNull())
                        ? json.get("domain").getAsString()
                        : "Unknown";

                return String.format(
                        "--- EMAIL INTELLIGENCE ---\n" +
                                "[+] Target: %s\n" +
                                "[+] Format Valid: %s\n" +
                                "[+] Domain: %s\n" +
                                "[+] DNS Active: %s\n" +
                                "[+] Is Disposable (Burner): %s\n",
                        email,
                        format ? "YES" : "NO",
                        domain,
                        dns ? "YES" : "NO",
                        disposable ? "YES (RISK)" : "NO"
                );
            } else {
                return "[!] Error: API returned status " + response.code();
            }
        } catch (Exception e) {
            return "[!] Error fetching email data: " + e.getMessage();
        }
    }

    // Helper method to safely get boolean values
    private boolean getSafeBoolean(JsonObject json, String key) {
        if (json.has(key) && !json.get(key).isJsonNull()) {
            return json.get(key).getAsBoolean();
        }
        return false; // Default to false if missing
    }
}