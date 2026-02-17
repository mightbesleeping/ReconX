package com.reconx;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class IPService {
    private static final String BASE_URL = "https://ipinfo.io/";
    // TOKKEN
    private final String apiKey = "45e250b8097a51";

    public String getIPInfo(String ip) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(BASE_URL + ip + "?token=" + apiKey)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String rawJson = response.body().string();
                JsonObject json = JsonParser.parseString(rawJson).getAsJsonObject();

                // Build a clean report string
                return String.format(
                        "[+] IP: %s\n[+] City: %s\n[+] Region: %s\n[+] Org: %s\n[+] Location: %s",
                        json.has("ip") ? json.get("ip").getAsString() : "N/A",
                        json.has("city") ? json.get("city").getAsString() : "N/A",
                        json.has("region") ? json.get("region").getAsString() : "N/A",
                        json.has("org") ? json.get("org").getAsString() : "N/A",
                        json.has("loc") ? json.get("loc").getAsString() : "N/A"
                );
            } else {
                return "[!] Error: Received status code " + response.code();
            }
        } catch (Exception e) {
            return "[!] Network Error: " + e.getMessage();
        }
    }
}