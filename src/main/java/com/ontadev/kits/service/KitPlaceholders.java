// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.kits.config.MenuConfig;
import com.ontadev.kits.model.Kit;
import com.ontadev.libs.ioc.annotation.stereotype.Service;
import com.ontadev.libs.message.Placeholders;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@Service
public class KitPlaceholders {

    private final KitService kitService;
    private final KitActions actions;
    private final MenuConfig menuConfig;

    public KitPlaceholders(KitService kitService, KitActions actions, MenuConfig menuConfig) {
        this.kitService = kitService;
        this.actions = actions;
        this.menuConfig = menuConfig;
    }

    /** {@code viewer} может быть {@code null}, тогда остаток задержки не считается. */
    public Placeholders of(Player viewer, Kit kit) {
        String none = menuConfig.getNone();

        List<String> description = new ArrayList<>(kit.getDescription());
        if (description.isEmpty()) {
            description.add(none);
        }

        List<String> commands = new ArrayList<>();
        for (String command : kit.getCommands()) {
            commands.add("<#CBD5E1>/" + command);
        }
        if (commands.isEmpty()) {
            commands.add(none);
        }

        String permission = kitService.requiredPermission(kit);
        long remaining = viewer == null ? 0 : kitService.remainingCooldown(viewer, kit);

        return Placeholders.of("name", kit.getDisplayName())
                .add("id", kit.getId())
                .add("description", description)
                .add("commands", commands)
                .add("cooldown", kit.getCooldown() <= 0 ? none : actions.formatTime(kit.getCooldown()))
                .add("remaining", actions.formatTime(remaining))
                .add("items", kit.itemCount())
                .add("permission", permission == null ? none : permission)
                .add("icon", kit.getIcon().name())
                .add("order", kit.getOrder())
                .add("slot", kit.getSlot() < 0 ? menuConfig.getAuto() : String.valueOf(kit.getSlot()))
                .add("wipe-time", actions.wipeTime())
                .add("none", none);
    }

    public String state(boolean value) {
        return value ? menuConfig.getStateOn() : menuConfig.getStateOff();
    }
}
