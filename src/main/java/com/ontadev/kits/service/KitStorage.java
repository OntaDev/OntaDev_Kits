// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.kits.model.Kit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

/**
 * Читает и пишет наборы в YAML, который удобно править руками.
 */
final class KitStorage {

    private KitStorage() {
    }

    static Kit read(File file) {
        String id = file.getName().substring(0, file.getName().length() - ".yml".length());
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);

        Kit kit = new Kit(id);
        kit.setDisplayName(yaml.getString("display-name", kit.getDisplayName()));
        kit.setDescription(new java.util.ArrayList<>(yaml.getStringList("description")));
        Material icon = Material.matchMaterial(yaml.getString("icon", "CHEST"));
        kit.setIcon(icon == null ? Material.CHEST : icon);
        kit.setCooldown(Math.max(0, yaml.getLong("cooldown", 0)));
        kit.setOneTime(yaml.getBoolean("one-time", false));
        kit.setPermission(yaml.getString("permission", ""));
        kit.setEnabled(yaml.getBoolean("enabled", true));
        kit.setOrder(yaml.getInt("order", 0));
        kit.setSlot(yaml.getInt("slot", -1));
        kit.setCommands(new java.util.ArrayList<>(yaml.getStringList("commands")));

        Map<Integer, ItemStack> items = new TreeMap<>();
        ConfigurationSection section = yaml.getConfigurationSection("items");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                int slot;
                try {
                    slot = Integer.parseInt(key);
                } catch (NumberFormatException e) {
                    continue;
                }
                ItemStack item = section.getItemStack(key);
                if (item != null && item.getType() != Material.AIR) {
                    items.put(slot, item);
                }
            }
        }
        kit.setItems(items);
        return kit;
    }

    static void write(File file, Kit kit) throws IOException {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.options().header("Набор " + kit.getId() + ". Можно редактировать вручную, затем /kit reload.\n"
                + "items: ключ - слот инвентаря (0-35 хранилище, 36-39 ботинки..шлем, 40 вторая рука).");
        yaml.set("display-name", kit.getDisplayName());
        yaml.set("description", kit.getDescription());
        yaml.set("icon", kit.getIcon().name());
        yaml.set("cooldown", kit.getCooldown());
        yaml.set("one-time", kit.isOneTime());
        yaml.set("permission", kit.getPermission());
        yaml.set("enabled", kit.isEnabled());
        yaml.set("order", kit.getOrder());
        yaml.set("slot", kit.getSlot());
        yaml.set("commands", kit.getCommands());
        for (Map.Entry<Integer, ItemStack> entry : kit.getItems().entrySet()) {
            yaml.set("items." + entry.getKey(), entry.getValue());
        }
        yaml.save(file);
    }
}
