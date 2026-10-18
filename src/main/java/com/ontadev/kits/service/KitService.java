// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.model.ClaimResult;
import com.ontadev.kits.model.Kit;
import com.ontadev.kits.model.KitState;
import com.ontadev.libs.ioc.annotation.stereotype.Service;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
public class KitService {

    private static final Pattern VALID_ID = Pattern.compile("[a-z0-9_-]{1,32}");

    private final Plugin plugin;
    private final Config config;
    private final PlayerDataService data;
    private final WipeService wipe;
    private final File directory;
    private final Map<String, Kit> kits = new ConcurrentHashMap<>();

    public KitService(Plugin plugin, Config config, PlayerDataService data, WipeService wipe) {
        this.wipe = wipe;
        this.plugin = plugin;
        this.config = config;
        this.data = data;
        this.directory = new File(plugin.getDataFolder(), "kits");

        reload();
    }

    public synchronized int reload() {
        if (!directory.exists() && !directory.mkdirs()) {
            plugin.getLogger().severe("Не удалось создать папку " + directory);
        }

        Map<String, Kit> loaded = new TreeMap<>();
        File[] files = directory.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files != null) {
            for (File file : files) {
                try {
                    Kit kit = KitStorage.read(file);
                    loaded.put(kit.getId().toLowerCase(Locale.ROOT), kit);
                } catch (Exception e) {
                    plugin.getLogger().warning("Не удалось загрузить набор " + file.getName() + ": " + e.getMessage());
                }
            }
        }

        if (loaded.isEmpty() && files != null && files.length == 0) {
            Kit example = createExample();
            loaded.put(example.getId(), example);
        }

        kits.clear();
        kits.putAll(loaded);
        return kits.size();
    }

    public static boolean isValidId(String id) {
        return id != null && VALID_ID.matcher(id.toLowerCase(Locale.ROOT)).matches();
    }

    public Kit get(String id) {
        return id == null ? null : kits.get(id.toLowerCase(Locale.ROOT));
    }

    public boolean exists(String id) {
        return get(id) != null;
    }

    public List<Kit> all() {
        List<Kit> list = new ArrayList<>(kits.values());
        list.sort(Comparator.comparingInt(Kit::getOrder).thenComparing(Kit::getId));
        return list;
    }

    public Collection<String> ids() {
        return new ArrayList<>(kits.keySet());
    }

    public Kit create(String id, Player source) {
        return create(id, source, -1);
    }

    public Kit create(String id, Player source, int slot) {
        Kit kit = new Kit(id.toLowerCase(Locale.ROOT));
        kit.setSlot(slot);
        kit.setCooldown(config.getDefaultCooldown());
        kit.setDisplayName("<white>" + kit.getId());
        kit.setItems(snapshot(source));
        kits.put(kit.getId(), kit);
        save(kit);
        return kit;
    }

    public void overwriteItems(Kit kit, Player source) {
        kit.setItems(snapshot(source));
        save(kit);
    }

    public void delete(Kit kit) {
        kits.remove(kit.getId());
        data.forgetKit(kit.getId());
        File file = fileOf(kit);
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Не удалось удалить файл " + file);
        }
    }

    public void save(Kit kit) {
        try {
            KitStorage.write(fileOf(kit), kit);
        } catch (IOException e) {
            plugin.getLogger().severe("Не удалось сохранить набор " + kit.getId() + ": " + e.getMessage());
        }
    }

    public static Map<Integer, ItemStack> snapshot(Player player) {
        PlayerInventory inventory = player.getInventory();
        Map<Integer, ItemStack> items = new TreeMap<>();
        for (int slot = 0; slot < 41; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR) {
                items.put(slot, item.clone());
            }
        }
        return items;
    }

    private File fileOf(Kit kit) {
        return new File(directory, kit.getId() + ".yml");
    }

    private Kit createExample() {
        Kit kit = new Kit("starter");
        kit.setDisplayName("<#34D399>Стартовый набор");
        kit.setDescription(new ArrayList<>(Arrays.asList(
                "<#CBD5E1>Базовый набор для новичка.",
                "<#6B7280>Измени его: /kit edit starter")));
        kit.setIcon(Material.IRON_SWORD);
        kit.setCooldown(config.getDefaultCooldown());

        Map<Integer, ItemStack> items = new TreeMap<>();
        items.put(0, new ItemStack(Material.STONE_SWORD));
        items.put(1, new ItemStack(Material.STONE_PICKAXE));
        items.put(2, new ItemStack(Material.STONE_AXE));
        items.put(3, new ItemStack(Material.BREAD, 16));
        items.put(39, new ItemStack(Material.LEATHER_HELMET));
        items.put(38, new ItemStack(Material.LEATHER_CHESTPLATE));
        kit.setItems(items);

        save(kit);
        return kit;
    }

    public String requiredPermission(Kit kit) {
        String permission = kit.getPermission();
        if (permission != null && !permission.trim().isEmpty()) {
            return permission.trim();
        }
        return config.isAutoPermission() ? "kits.kit." + kit.getId() : null;
    }

    public boolean hasAccess(Player player, Kit kit) {
        String permission = requiredPermission(kit);
        return permission == null
                || player.hasPermission(permission)
                || player.hasPermission("kits.bypass.permission");
    }

    public long remainingCooldown(Player player, Kit kit) {
        if (kit.getCooldown() <= 0 || player.hasPermission("kits.bypass.cooldown")) return 0;
        Long last = data.lastClaim(player.getUniqueId(), kit.getId());
        if (last == null) return 0;
        long left = kit.getCooldown() - (System.currentTimeMillis() - last) / 1000;
        return Math.max(0, left);
    }

    public boolean alreadyClaimedOneTime(Player player, Kit kit) {
        return kit.isOneTime()
                && !player.hasPermission("kits.bypass.cooldown")
                && data.lastClaim(player.getUniqueId(), kit.getId()) != null;
    }

    public KitState stateOf(Player player, Kit kit) {
        if (!kit.isEnabled()) return KitState.DISABLED;
        if (!hasAccess(player, kit)) return KitState.LOCKED;
        if (wipe.isActive() && !player.hasPermission("kits.bypass.wipe")) return KitState.WIPE;
        if (alreadyClaimedOneTime(player, kit)) return KitState.CLAIMED;
        if (remainingCooldown(player, kit) > 0) return KitState.COOLDOWN;
        return KitState.AVAILABLE;
    }

    public ClaimResult check(Player player, Kit kit) {
        switch (stateOf(player, kit)) {
            case DISABLED:
                return ClaimResult.DISABLED;
            case LOCKED:
                return ClaimResult.NO_PERMISSION;
            case CLAIMED:
                return ClaimResult.ALREADY_CLAIMED;
            case COOLDOWN:
                return ClaimResult.COOLDOWN;
            case WIPE:
                return ClaimResult.WIPE;
            default:
                return ClaimResult.SUCCESS;
        }
    }

    /** Выдаёт набор с проверками. Только из главного потока. */
    public ClaimResult claim(Player player, Kit kit) {
        data.refresh(player.getUniqueId(), kit.getId());
        ClaimResult check = check(player, kit);
        if (check != ClaimResult.SUCCESS) {
            return check;
        }
        if (config.isRequireSpace() && !fits(player, kit)) {
            return ClaimResult.NO_SPACE;
        }

        give(player, kit);
        data.mark(player.getUniqueId(), kit.getId());
        return ClaimResult.SUCCESS;
    }

    /** Выдаёт набор без проверок и без записи получения. Возвращает true, если что-то упало на землю. */
    public boolean give(Player player, Kit kit) {
        PlayerInventory inventory = player.getInventory();
        boolean dropped = false;

        for (Map.Entry<Integer, ItemStack> entry : kit.getItems().entrySet()) {
            ItemStack item = entry.getValue().clone();
            int slot = entry.getKey();

            if (config.isPreserveSlots() && slot >= 0 && slot <= 40) {
                ItemStack current = inventory.getItem(slot);
                if (current == null || current.getType() == Material.AIR) {
                    inventory.setItem(slot, item);
                    continue;
                }
            }

            for (ItemStack left : inventory.addItem(item).values()) {
                if (config.isDropOverflow()) {
                    player.getWorld().dropItem(player.getLocation(), left);
                    dropped = true;
                }
            }
        }

        for (String command : kit.getCommands()) {
            String line = command.trim();
            if (line.startsWith("/")) line = line.substring(1);
            if (line.isEmpty()) continue;
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                    line.replace("<player>", player.getName()).replace("%player%", player.getName()));
        }
        return dropped;
    }

    private boolean fits(Player player, Kit kit) {
        PlayerInventory inventory = player.getInventory();
        Inventory simulation = Bukkit.createInventory(null, 36);
        simulation.setContents(Arrays.copyOf(inventory.getStorageContents(), 36));

        for (Map.Entry<Integer, ItemStack> entry : kit.getItems().entrySet()) {
            int slot = entry.getKey();
            if (config.isPreserveSlots() && slot >= 0 && slot <= 40) {
                ItemStack current = inventory.getItem(slot);
                if (current == null || current.getType() == Material.AIR) {
                    if (slot < 36) {
                        simulation.setItem(slot, entry.getValue().clone());
                    }
                    continue;
                }
            }
            if (!simulation.addItem(entry.getValue().clone()).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public void resetCooldown(java.util.UUID player, String kitId) {
        data.reset(player, kitId);
    }
}
