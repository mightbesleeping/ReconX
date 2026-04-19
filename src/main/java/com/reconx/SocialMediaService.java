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
        // Major Social Platforms
        sites.put("GitHub", "https://github.com/%s");
        sites.put("Reddit", "https://www.reddit.com/user/%s");
        sites.put("Instagram", "https://www.instagram.com/%s/");
        sites.put("Twitter/X", "https://twitter.com/%s");
        sites.put("TikTok", "https://www.tiktok.com/@%s");
        sites.put("YouTube", "https://www.youtube.com/@%s");
        
        // Professional & Dev
        sites.put("LinkedIn", "https://www.linkedin.com/in/%s");
        sites.put("StackOverflow", "https://stackoverflow.com/users/search?q=%s");
        sites.put("Dev.to", "https://dev.to/%s");
        
        // Creative & Others
        sites.put("Pinterest", "https://www.pinterest.com/%s/");
        sites.put("Behance", "https://www.behance.net/%s");
        sites.put("Dribbble", "https://dribbble.com/%s");
        sites.put("Twitch", "https://www.twitch.tv/%s");
        sites.put("Spotify", "https://open.spotify.com/user/%s");
        sites.put("Steam", "https://steamcommunity.com/id/%s");
        
        // Knowledge
        sites.put("Wikipedia", "https://en.wikipedia.org/wiki/User:%s");
        sites.put("Medium", "https://medium.com/@%s");
    }

    public String checkProfiles(String originalUsername) {
        StringBuilder report = new StringBuilder();
        report.append("--- SOCIAL MEDIA SCAN: ").append(originalUsername).append(" ---\n");

        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(true)
                .build();
        
        report.append("[*] Scanning ").append(sites.size()).append(" platforms...\n\n");

        scanUsername(client, originalUsername, report);

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