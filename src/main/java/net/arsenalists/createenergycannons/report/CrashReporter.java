package net.arsenalists.createenergycannons.report;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.architectury.platform.Platform;
import net.arsenalists.createenergycannons.CECMod;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * Opt-in crash sharing: find a recent crash report naming this mod and, with the player's consent,
 * upload it to mclo.gs and relay the link. Touches no client classes so it stays server-safe.
 */
public final class CrashReporter {

    private static final String MCLOGS = "https://api.mclo.gs/1/log";
    // A Cloudflare Worker that holds the real Discord webhook secret and rate-limits. The mod only
    // ever ships this Worker URL, never the webhook. Source in tools/crash-relay-worker.js.
    private static final String RELAY = "https://cec-crash-relay.arsenalists-mc.workers.dev";

    private static final String MARKER = "net.arsenalists.createenergycannons";
    private static final HttpClient HTTP = HttpClient.newHttpClient();

    private static Path pending;

    private CrashReporter() {}

    public static void detectOnStartup() {
        try {
            Path dir = Platform.getGameFolder().resolve("crash-reports");
            if (!Files.isDirectory(dir)) return;
            Optional<Path> newest;
            try (Stream<Path> files = Files.list(dir)) {
                newest = files.filter(p -> p.getFileName().toString().endsWith(".txt"))
                        .max(Comparator.comparingLong(p -> p.toFile().lastModified()));
            }
            if (newest.isEmpty()) return;
            Path crash = newest.get();
            if (handled().contains(crash.getFileName().toString())) return;
            if (!Files.readString(crash).contains(MARKER)) return;
            pending = crash;
            CECMod.getLogger().info("Recent crash report {} looks related to this mod; will offer to share it",
                    crash.getFileName());
        } catch (Exception e) {
            CECMod.getLogger().debug("Crash-report scan skipped", e);
        }
    }

    public static boolean hasPending() {
        return pending != null;
    }

    /** Upload the pending report to mclo.gs off-thread; onLink receives the URL, or null on failure. */
    public static void submitPending(Consumer<String> onLink) {
        Path crash = pending;
        pending = null;
        if (crash == null) return;
        Thread worker = new Thread(() -> {
            String link = null;
            try {
                link = uploadToMclogs(Files.readString(crash));
                if (link != null) relay(link);
            } catch (Exception e) {
                CECMod.getLogger().warn("Crash report upload failed", e);
            } finally {
                markHandled(crash.getFileName().toString());
            }
            if (onLink != null) onLink.accept(link);
        }, "cec-crash-report");
        worker.setDaemon(true);
        worker.start();
    }

    /** Marks it handled so it won't be offered again, without sending. */
    public static void dismissPending() {
        if (pending != null) {
            markHandled(pending.getFileName().toString());
            pending = null;
        }
    }

    private static String uploadToMclogs(String log) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(MCLOGS))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("content=" + URLEncoder.encode(log, StandardCharsets.UTF_8)))
                .build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        return json.get("success").getAsBoolean() ? json.get("url").getAsString() : null;
    }

    private static void relay(String link) throws IOException, InterruptedException {
        if (RELAY.contains("REPLACE")) return; // relay not configured yet
        String payload = "{\"url\":\"" + link + "\",\"version\":\"" + Platform.getMod(CECMod.MODID).getVersion() + "\"}";
        HttpRequest req = HttpRequest.newBuilder(URI.create(RELAY))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();
        HTTP.send(req, HttpResponse.BodyHandlers.discarding());
    }

    // Preference + dedup live in a small text file, so this needs no config plumbing.

    private static Path stateFile() {
        return Platform.getConfigFolder().resolve("cec_crash_reports.txt");
    }

    public static boolean isAutoEnabled() {
        try {
            return Files.exists(stateFile()) && Files.readString(stateFile()).contains("auto=true");
        } catch (IOException e) {
            return false;
        }
    }

    public static void setAutoEnabled(boolean on) {
        write(on, handled());
    }

    private static Set<String> handled() {
        Set<String> set = new HashSet<>();
        try {
            if (Files.exists(stateFile())) {
                for (String line : Files.readAllLines(stateFile())) {
                    if (line.startsWith("handled=")) set.add(line.substring("handled=".length()));
                }
            }
        } catch (IOException ignored) {
        }
        return set;
    }

    private static void markHandled(String name) {
        Set<String> set = handled();
        set.add(name);
        write(isAutoEnabled(), set);
    }

    private static void write(boolean auto, Set<String> handled) {
        StringBuilder sb = new StringBuilder("auto=").append(auto).append('\n');
        for (String h : handled) sb.append("handled=").append(h).append('\n');
        try {
            Files.writeString(stateFile(), sb.toString());
        } catch (IOException e) {
            CECMod.getLogger().warn("Could not save crash-report state", e);
        }
    }
}
