package com.reconx;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class GoogleSearchService {

    // 🔴 PASTE YOUR API KEY HERE!
    private static final String API_KEY = "AIzaSyCUf3JeabnJRRAkALC9stKKB0n9FAZtiQ8";

    // ✅ Your Search Engine ID (from the snippet you showed me)
    private static final String CX_ID = "c6443d4967c81404c";

    private static final String BASE_URL = "https://www.googleapis.com/customsearch/v1";

    public String searchSocialMedia(String query) {
        StringBuilder report = new StringBuilder();
        report.append("--- GOOGLE SOCIAL INTELLIGENCE ---\n");
        report.append("[*] Target: '").append(query).append("'\n");

        OkHttpClient client = new OkHttpClient();

        try {
            // Encode the query so spaces don't break the URL
            String safeQuery = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = BASE_URL + "?key=" + API_KEY + "&cx=" + CX_ID + "&q=" + safeQuery;

            Request request = new Request.Builder().url(url).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {
                    String jsonData = response.body().string();
                    JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();

                    if (json.has("items")) {
                        JsonArray items = json.getAsJsonArray("items");

                        // Loop through the results
                        items.forEach(item -> {
                            JsonObject result = item.getAsJsonObject();
                            String title = result.get("title").getAsString();
                            String link = result.get("link").getAsString();
                            String snippet = result.has("snippet")
                                    ? result.get("snippet").getAsString().replace("\n", " ")
                                    : "No description.";

                            report.append("\n[+] FOUND: ").append(title).append("\n");
                            report.append("    URL: ").append(link).append("\n");
                            report.append("    INFO: ").append(snippet).append("\n");
                        });
                    } else {
                        report.append("\n[-] No matches found.\n");
                        report.append("    (Tip: Did you add sites like instagram.com to your Search Engine config?)\n");
                    }
                } else {
                    report.append("[!] API Error: ").append(response.code());
                    report.append(" - Check if 'Custom Search API' is enabled in Google Cloud Console.\n");
                }
            }
        } catch (Exception e) {
            report.append("[!] Network Error: ").append(e.getMessage());
        }

        return report.toString();
    }
}