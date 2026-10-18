// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.kits.config.Config;
import com.ontadev.kits.config.MenuConfig;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.kits.model.Kit;
import com.ontadev.kits.model.KitLayout;
import com.ontadev.kits.util.TimeFormat;
import com.ontadev.libs.config.YamlConfigLoader;
import com.ontadev.libs.ioc.annotation.stereotype.Service;
import com.ontadev.libs.message.Message;
import com.ontadev.libs.message.Placeholders;
import com.ontadev.libs.serialization.adapters.MessageAdapter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Админские операции, общие для {@code /kit} и {@code /kitadmin}. Каждый метод сам проверяет аргументы
 * и пишет отправителю, если что-то не так.
 */
@Service
public class KitAdminActions {

    private final Plugin plugin;
    private final Config config;
    private final MenuConfig menuConfig;
    private final MessageConfig messages;
    private final KitService kitService;
    private final KitActions actions;
    private final WipeService wipe;
    private final MenuState state;
    private final ConfigMigrator migrator;

    public KitAdminActions(Plugin plugin, Config config, MenuConfig menuConfig, MessageConfig messages,
                           KitService kitService, KitActions actions, WipeService wipe, MenuState state,
                           ConfigMigrator migrator) {
        this.migrator = migrator;
        this.plugin = plugin;
        this.config = config;
        this.menuConfig = menuConfig;
        this.messages = messages;
        this.kitService = kitService;
        this.actions = actions;
        this.wipe = wipe;
        this.state = state;
    }

    public void create(CommandSender sender, String[] args, Set<String> reserved) {
        Player player = playerOf(sender);
        if (player == null) return;

        if (args.length == 0) {
            new Message("<#6B7280>» <#F59E0B>/kitadmin create <id> [слот]").send(sender);
            return;
        }

        String id = args[0].toLowerCase(Locale.ROOT);
        if (!KitService.isValidId(id) || reserved.contains(id)) {
            messages.getInvalidId().send(sender);
            return;
        }
        if (kitService.exists(id)) {
            messages.getKitExists().send(sender, Placeholders.of("kit", id));
            return;
        }

        int slot = -1;
        if (args.length > 1) {
            slot = parseSlot(args[1]);
            if (slot == Integer.MIN_VALUE) {
                messages.getInvalidSlot().send(sender);
                return;
            }
        }
        if (KitService.snapshot(player).isEmpty()) {
            messages.getEmptyInventory().send(sender);
            return;
        }

        Kit kit = kitService.create(id, player, slot);
        messages.getKitCreated().send(sender, Placeholders.of("kit", id).add("count", kit.itemCount()));
        state.openEditor(player, id);
    }

    public void update(CommandSender sender, String[] args) {
        withKit(sender, args, (player, kit) -> {
            if (KitService.snapshot(player).isEmpty()) {
                messages.getEmptyInventory().send(player);
                return;
            }
            kitService.overwriteItems(kit, player);
            messages.getKitUpdated().send(player, Placeholders.of("kit", kit.getId()).add("count", kit.itemCount()));
        });
    }

    public void edit(CommandSender sender, String[] args) {
        withKit(sender, args, (player, kit) -> state.openEditor(player, kit.getId()));
    }

    public void delete(CommandSender sender, String[] args) {
        Kit kit = findKit(sender, args, 0);
        if (kit == null) return;
        kitService.delete(kit);
        messages.getKitDeleted().send(sender, Placeholders.of("kit", kit.getId()));
    }

    public void give(CommandSender sender, String[] args) {
        Kit kit = findKit(sender, args, 0);
        if (kit == null) return;

        Player target;
        if (args.length > 1) {
            target = Bukkit.getPlayerExact(args[1]);
            if (target == null) {
                messages.getPlayerNotFound().send(sender, Placeholders.of("player", args[1]));
                return;
            }
        } else {
            target = playerOf(sender);
            if (target == null) return;
        }

        if (kitService.give(target, kit)) messages.getOverflowDropped().send(target);
        messages.getKitGiven().send(sender, Placeholders.of("kit", kit.getId()).add("player", target.getName()));
        if (target != sender) {
            messages.getKitReceived().send(target, Placeholders.of("name", kit.getDisplayName()));
        }
    }

    @SuppressWarnings("deprecation")
    public void reset(CommandSender sender, String[] args) {
        if (args.length == 0) {
            new Message("<#6B7280>» <#F59E0B>/kitadmin resetcooldown <набор|*> [игрок]").send(sender);
            return;
        }

        // основной порядок <набор|*> [игрок], но старый <игрок> [набор] тоже работает
        String[] normalized = args;
        boolean kitFirst = args[0].equals("*") || kitService.exists(args[0]);
        if (!kitFirst && (args.length == 1 || kitService.exists(args[1]) || args[1].equals("*"))) {
            normalized = args.length == 1 ? new String[]{"*", args[0]} : new String[]{args[1], args[0]};
        }
        args = normalized;

        String kitId = null;
        if (!args[0].equals("*")) {
            Kit kit = findKit(sender, args, 0);
            if (kit == null) return;
            kitId = kit.getId();
        }

        OfflinePlayer target;
        if (args.length > 1) {
            target = Bukkit.getOfflinePlayer(args[1]);
        } else {
            target = playerOf(sender);
            if (target == null) return;
        }

        kitService.resetCooldown(target.getUniqueId(), kitId);
        messages.getKitReset().send(sender, Placeholders.of("player", target.getName() == null ? args.length > 1 ? args[1] : "?" : target.getName())
                .add("kit", kitId == null ? "*" : kitId));
    }

    public void setCooldown(CommandSender sender, String[] args) {
        if (args.length < 2) {
            new Message("<#6B7280>» <#F59E0B>/kitadmin setcooldown <набор> <время>").send(sender);
            return;
        }
        Kit kit = findKit(sender, args, 0);
        if (kit == null) return;

        long seconds = TimeFormat.parse(String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)));
        if (seconds < 0) {
            messages.getInvalidTime().send(sender);
            return;
        }

        kit.setCooldown(seconds);
        kitService.save(kit);
        messages.getCooldownSet().send(sender, Placeholders.of("kit", kit.getId())
                .add("time", seconds == 0 ? menuConfig.getNone() : config.formatTime(seconds)));
    }

    public void wipe(CommandSender sender, String[] args) {
        String action = args.length == 0 ? "status" : args[0].toLowerCase(Locale.ROOT);

        switch (action) {
            case "on":
            case "вкл": {
                long seconds = config.getWipe().getDefaultSeconds();
                if (args.length > 1) {
                    seconds = TimeFormat.parse(String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)));
                    if (seconds < 0) {
                        messages.getInvalidTime().send(sender);
                        return;
                    }
                }
                wipe.start(seconds);
                messages.getWipeStarted().send(sender, Placeholders.of("time", actions.wipeTime()));
                break;
            }
            case "off":
            case "выкл":
                if (!wipe.isActive()) {
                    messages.getWipeNotActive().send(sender);
                    return;
                }
                wipe.stop();
                messages.getWipeStopped().send(sender);
                break;
            default:
                if (wipe.isActive()) {
                    messages.getWipeStatus().send(sender, Placeholders.of("time", actions.wipeTime()));
                } else {
                    messages.getWipeNotActive().send(sender);
                }
                break;
        }
    }

    public void reload(CommandSender sender) {
        int added = config.isAutoUpdateConfigs() ? migrator.migrate() : 0;
        if (added > 0) {
            messages.getConfigUpdated().send(sender, Placeholders.of("count", String.valueOf(added)));
        }

        // новый загрузчик читает файлы с диска, мимо кэша; адаптер Message нужен, чтобы разобрать строки
        YamlConfigLoader fresh = new YamlConfigLoader(plugin.getDataFolder().toPath(),
                Map.of(Message.class, new MessageAdapter()));

        config.applyFrom(fresh.loadFromClass(Config.class));
        menuConfig.applyFrom(fresh.loadFromClass(MenuConfig.class));
        messages.applyFrom(fresh.loadFromClass(MessageConfig.class));

        int count = kitService.reload();
        messages.getPluginReloaded().send(sender, Placeholders.of("count", String.valueOf(count)));
    }

    public interface PlayerKitAction {
        void run(Player player, Kit kit);
    }

    public void withKit(CommandSender sender, String[] args, PlayerKitAction action) {
        Player player = playerOf(sender);
        if (player == null) return;
        Kit kit = findKit(sender, args, 0);
        if (kit != null) action.run(player, kit);
    }

    public Kit findKit(CommandSender sender, String[] args, int index) {
        if (args.length <= index) {
            messages.getKitNotFound().send(sender, Placeholders.of("kit", "?"));
            return null;
        }
        Kit kit = kitService.get(args[index]);
        if (kit == null) {
            messages.getKitNotFound().send(sender, Placeholders.of("kit", args[index]));
        }
        return kit;
    }

    public Player playerOf(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player) sender;
        }
        messages.getOnlyPlayers().send(sender);
        return null;
    }

    private static int parseSlot(String input) {
        try {
            int slot = Integer.parseInt(input.trim());
            return slot >= -1 && slot < KitLayout.GUI_SIZE ? slot : Integer.MIN_VALUE;
        } catch (NumberFormatException e) {
            return Integer.MIN_VALUE;
        }
    }
}
