package org.betterx.bclib.config;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.networking.VersionChecker;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

public class CachedConfig extends NamedPathConfig {

    @ConfigUI(hide = true)
    public static final ConfigToken<String> LAST_CHECK_DATE = ConfigToken.String(
            "never",
            "last",
            "version"
    );

    @ConfigUI(hide = true)
    public static final ConfigToken<String> LAST_JSON = ConfigToken.String(
            "",
            "cached",
            "version"
    );

    public CachedConfig() {
        super(BCLib.MOD_ID, "cache", false, false);
    }

    public String lastVersionJson() {
        byte[] decodedBytes = Base64.getUrlDecoder().decode(get(LAST_JSON));
        return new String(decodedBytes, StandardCharsets.UTF_8);
    }

    public void setLastVersionJson(String json) {
        set(LAST_JSON, Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8)));
    }

    public Instant lastCheckDate() {
        String d = get(LAST_CHECK_DATE);
        if (d.trim().toLowerCase().equals("never")) {
            return Instant.now().minus(VersionChecker.WAIT_FOR_DAYS + 1, ChronoUnit.DAYS);
        }
        return Instant.parse(d);
    }

    public void setLastCheckDate() {
        set(LAST_CHECK_DATE, Instant.now().toString());
    }

    @Override
    public void saveChanges() {
        synchronized (this) {
            super.saveChanges();
        }
    }
}
