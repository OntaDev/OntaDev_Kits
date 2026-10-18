// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.util;

import com.ontadev.libs.message.Message;
import com.ontadev.libs.message.Placeholders;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Собирает предметы меню: подставляет плейсхолдеры и убирает курсив, который Minecraft ставит по умолчанию.
 */
public final class Items {

    private Items() {
    }

    public static ItemStack create(Material material, int amount, Message name, Message lore,
                                   Placeholders placeholders, boolean glow, Integer customModelData) {
        ItemStack item = new ItemStack(material == null ? Material.STONE : material, Math.max(1, Math.min(amount, 64)));
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return item;
        }

        if (name != null && !name.getRawMessage().isEmpty()) {
            Component component = placeholders == null
                    ? name.getFirstComponentParsed()
                    : name.getFirstComponentParsed(placeholders);
            if (component != null) {
                meta.displayName(noItalic(component));
            }
        }

        if (lore != null && !lore.getRawMessage().isEmpty()) {
            List<Component> lines = lore.getComponents(placeholders).stream()
                    .map(Items::noItalic)
                    .collect(Collectors.toCollection(ArrayList::new));
            meta.lore(lines);
        }

        if (customModelData != null) {
            meta.setCustomModelData(customModelData);
        }

        if (glow) {
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_POTION_EFFECTS);
        item.setItemMeta(meta);
        return item;
    }

    private static Component noItalic(Component component) {
        // на старом Paper нет decorationIfAbsent
        return component.decoration(TextDecoration.ITALIC) == TextDecoration.State.NOT_SET
                ? component.decoration(TextDecoration.ITALIC, false)
                : component;
    }
}
