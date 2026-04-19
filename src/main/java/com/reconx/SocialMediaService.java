package com.reconx;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SocialMediaService {

    private final Map<String, String> sites = new HashMap<>();

    public SocialMediaService() {
        sites.put("GitHub", "https://github.com/%s");
        sites.put("Reddit", "https://www.reddit.com/user/%s");
        sites.put("Wikipedia", "https://en.wikipedia.org/wiki/User:%s");
        // Add more if you like!
    }

    public String checkProfiles(String originalUsername) {
        StringBuilder report = new StringBuilder();
        report.append("--- SOCIAL MEDIA SCAN: ").append(originalUsername).append(" ---\n");

        OkHttpClient client = new OkHttpClient();
        List<String> variations = generateVariations(originalUsername);

        report.append("[*] Checking variations: ").append(variations.toString()).append("\n\n");

        // Scan the original username first
        scanUsername(client, originalUsername, report);

        // Scan variations (Limit to 3 variations to keep it fast)
        int count = 0;
        for (String variant : variations) {
            if (count >= 3) break; // Don't scan too many or it gets slow
            if (!variant.equals(originalUsername)) {
                scanUsername(client, variant, report);
            }
            count++;
        }

        return report.toString();
    }

    private void scanUsername(OkHttpClient client, String username, StringBuilder report) {
        for (Map.Entry<String, String> entry : sites.entrySet()) {
            String url = String.format(entry.getValue(), username);
            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.code() == 200) {
                    report.append("[+] FOUND: ").append(username).append(" on ").append(entry.getKey()).append("\n");
                    report.append("    -> ").append(url).append("\n");
                }
            } catch (Exception e) {
                // Ignore errors to keep report clean
            }
        }
    }

    // This creates the "Similar Usernames"
    private List<String> generateVariations(String input) {
        List<String> list = new ArrayList<>();
        list.add(input); // Original
        list.add(input + "123"); // Common suffix
        list.add(input + "_official");
        list.add(input.replace("a", "4").replace("e", "3")); // Leet speak
        list.add(input + "."); // Dot separation
        return list;
    }
}