// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.model;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Набор: снимок инвентаря плюс настройки выдачи. Лежит в {@code kits/<id>.yml}.
 */
@Getter
@Setter
public class Kit {

    private final String id;

    private String displayName;
    private List<String> description = new ArrayList<>();
    private Material icon = Material.CHEST;

    /** Секунды, 0 - без задержки. */
    private long cooldown;
    private boolean oneTime;

    /** Пусто - доступен всем (или {@code kits.kit.<id>} при auto-permission). */
    private String permission = "";
    private boolean enabled = true;

    private int order;

    /** -1 - ставится автоматически. */
    private int slot = -1;

    private List<String> commands = new ArrayList<>();

    /** Слот игрока (см. {@link KitLayout}) -> предмет. */
    private Map<Integer, ItemStack> items = new TreeMap<>();

    public Kit(String id) {
        this.id = id;
        this.displayName = "<white>" + id;
    }

    public void setItems(Map<Integer, ItemStack> items) {
        this.items = new TreeMap<>(items);
    }

    public int itemCount() {
        return items.size();
    }
}
