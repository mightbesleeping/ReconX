package com.reconx;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class EmailService {
    // This API is free for limited daily use
    private static final String API_URL = "https://emailvalidation.abstractapi.com/v1/?api_key=YOUR_FREE_KEY&email=";

    // Note: You will need a free key from AbstractAPI or use a different free endpoint like:
    // https://api.eva.pingutil.com/email?email= (Totally free, no key needed)

    private static final String FREE_API = "https://api.eva.pingutil.com/email?email=";

    public String getEmailReport(String email) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(FREE_API + email).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.body() != null) {
                String jsonData = response.body().string();
                JsonObject json = JsonParser.parseString(jsonData).getAsJsonObject();

                // Extract useful data
                boolean isDisposable = json.get("data").getAsJsonObject().get("disposable").getAsBoolean();
                boolean isWebmail = json.get("data").getAsJsonObject().get("webmail").getAsBoolean();
                boolean deliverable = json.get("data").getAsJsonObject().get("deliverable").getAsBoolean();

                return String.format(
                        "--- EMAIL INTELLIGENCE ---\n" +
                                "[+] Target: %s\n" +
                                "[+] Deliverable: %s\n" +
                                "[+] Is Disposable (Burner): %s\n" +
                                "[+] Is Webmail (Gmail/Yahoo): %s\n",
                        email,
                        deliverable ? "YES" : "NO",
                        isDisposable ? "YES (Suspicious)" : "NO",
                        isWebmail ? "YES" : "NO"
                );
            }
        } catch (Exception e) {
            return "[!] Error checking email: " + e.getMessage();
        }
        return "[!] No data found.";
    }
}
