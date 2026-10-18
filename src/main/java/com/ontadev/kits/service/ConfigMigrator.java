// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.ontadev.kits.config.Config;
import com.ontadev.kits.config.MenuConfig;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.libs.config.YamlConfig;
import com.ontadev.libs.ioc.annotation.stereotype.Service;
import com.ontadev.libs.message.Message;
import com.ontadev.libs.serialization.adapters.MessageAdapter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Дописывает в существующие конфиги недостающие ключи на любой глубине (загрузчик Libs добавляет только
 * верхний уровень). Уже заданные значения не трогает.
 */
@Service
public class ConfigMigrator {

    private final Plugin plugin;
    private final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Message.class, new MessageAdapter())
            .excludeFieldsWithModifiers(Modifier.TRANSIENT, Modifier.STATIC)
            .create();

    public ConfigMigrator(Plugin plugin) {
        this.plugin = plugin;
    }

    public int migrate() {
        return migrate(new Config()) + migrate(new MenuConfig()) + migrate(new MessageConfig());
    }

    private int migrate(YamlConfig defaults) {
        File file = new File(plugin.getDataFolder(), defaults.getFileName() + ".yml");
        if (!file.exists()) return 0;

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        int added = merge(yaml, "", gson.toJsonTree(defaults));

        if (added > 0) {
            try {
                yaml.save(file);
                plugin.getLogger().info(file.getName() + ": добавлено новых ключей - " + added);
            } catch (IOException e) {
                plugin.getLogger().severe("Не удалось обновить " + file.getName() + ": " + e.getMessage());
                return 0;
            }
        }
        return added;
    }

    private int merge(YamlConfiguration yaml, String path, JsonElement element) {
        if (!element.isJsonObject()) {
            return 0;
        }

        int added = 0;
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            String child = path.isEmpty() ? entry.getKey() : path + "." + entry.getKey();
            JsonElement value = entry.getValue();

            if (value.isJsonObject()) {
                JsonObject object = value.getAsJsonObject();
                if (object.size() == 0) continue;
                if (yaml.contains(child) && !yaml.isConfigurationSection(child)) continue;
                added += merge(yaml, child, value);
            } else if (!yaml.contains(child)) {
                yaml.set(child, toJava(value));
                added++;
            }
        }
        return added;
    }

    private Object toJava(JsonElement element) {
        if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            List<Object> list = new ArrayList<>();
            for (JsonElement item : array) {
                list.add(toJava(item));
            }
            return list;
        }
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isBoolean()) return primitive.getAsBoolean();
            if (primitive.isNumber()) {
                String text = primitive.getAsString();
                try {
                    return Long.parseLong(text);
                } catch (NumberFormatException e) {
                    return primitive.getAsDouble();
                }
            }
            return primitive.getAsString();
        }
        return null;
    }
}
