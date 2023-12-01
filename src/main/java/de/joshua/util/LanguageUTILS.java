package de.joshua.util;

import de.joshua.ShopPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LanguageUTILS {
    private final Map<String, YamlConfiguration> languageFiles = new HashMap<>();
    ShopPlugin plugin;

    public LanguageUTILS(ShopPlugin plugin) {
        this.plugin = plugin;

        List<String> languages = plugin.getConfig().getStringList("shop.languages");
        for (String language : languages) {
            loadLanguageFiles(language);
        }
    }

    private void loadLanguageFiles(String languageKey) {
        try {
            System.out.println("Loading language file: " + languageKey);
            InputStream stream = plugin.getResource("languages/" + languageKey + ".yml");
            if (stream == null)
                throw new NullPointerException("Language file not found: " + languageKey);
            Reader reader = new InputStreamReader(stream);
            this.languageFiles.put(languageKey, YamlConfiguration.loadConfiguration(reader));
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLanguageString(String languageKey, String key) {
        String str = languageFiles.get(languageKey).getString(key);
        return str == null ? "Err" : str;
    }
}
