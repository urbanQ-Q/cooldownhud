package pl.cooldownhud;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

public final class CooldownHudConfig {
    public enum Corner {
        BOTTOM_LEFT, BOTTOM_RIGHT, TOP_LEFT, TOP_RIGHT;

        public Corner next() {
            Corner[] all = values();
            return all[(ordinal() + 1) % all.length];
        }

        public boolean isBottom() {
            return this == BOTTOM_LEFT || this == BOTTOM_RIGHT;
        }

        public boolean isRight() {
            return this == BOTTOM_RIGHT || this == TOP_RIGHT;
        }
    }

    public Corner corner = Corner.BOTTOM_LEFT;
    public int offsetX = 4;
    public int offsetY = 4;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static CooldownHudConfig instance = new CooldownHudConfig();

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("cooldownhud.json");
    }

    public static CooldownHudConfig get() {
        return instance;
    }

    public static void load() {
        Path p = path();
        if (Files.exists(p)) {
            try {
                CooldownHudConfig loaded = GSON.fromJson(Files.readString(p), CooldownHudConfig.class);
                if (loaded != null) {
                    if (loaded.corner == null) loaded.corner = Corner.BOTTOM_LEFT;
                    instance = loaded;
                }
            } catch (Exception ignored) {
                // uszkodzony plik -> zostają domyślne ustawienia
            }
        }
        save();
    }

    public static void save() {
        try {
            Files.writeString(path(), GSON.toJson(instance));
        } catch (IOException ignored) {
        }
    }
}
