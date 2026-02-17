package com.reconx;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class DomainService {
    private static final String DNS_API = "https://dns.google/resolve";

    public String getDNSRecords(String domain) {
        StringBuilder report = new StringBuilder();
        report.append("--- DOMAIN INTELLIGENCE: ").append(domain).append(" ---\n");

        // Fetch A Records (IP Addresses) - Type 1
        report.append(fetchRecord(domain, "A"));

        // Fetch MX Records (Mail Servers) - Type 15
        report.append(fetchRecord(domain, "MX"));

        // Fetch NS Records (Name Servers) - Type 2
        report.append(fetchRecord(domain, "NS"));

        return report.toString();
    }

    private String fetchRecord(String domain, String type) {
        OkHttpClient client = new OkHttpClient();
        String url = DNS_API + "?name=" + domain + "&type=" + type;

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonData = response.body().string();
                JsonObject json = new Gson().fromJson(jsonData, JsonObject.class);

                if (json.has("Answer")) {
                    StringBuilder result = new StringBuilder("\n[+] " + type + " Records:\n");
                    JsonArray answers = json.getAsJsonArray("Answer");

                    answers.forEach(element -> {
                        String data = element.getAsJsonObject().get("data").getAsString();
                        result.append("    -> ").append(data).append("\n");
                    });
                    return result.toString();
                }
            }
        } catch (Exception e) {
            return "\n[!] Error fetching " + type + ": " + e.getMessage() + "\n";
        }
        return "\n[-] No " + type + " records found.\n";
    }
}
