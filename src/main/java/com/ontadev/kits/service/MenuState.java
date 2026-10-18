// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.service;

import com.ontadev.libs.ioc.annotation.stereotype.Component;
import com.ontadev.libs.menu.manager.MenuManager;
import com.ontadev.libs.player.PlayerResolver;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import com.ontadev.libs.ioc.annotation.AutoListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Состояние меню игрока (страница, выбранный набор). Меню в Libs это синглтоны, поэтому всё, что
 * относится к игроку, хранится здесь.
 */
@Component
@AutoListener
public class MenuState implements Listener {

    public static final String MAIN = "kits-main";
    public static final String PREVIEW = "kits-preview";
    public static final String EDITOR = "kits-editor";

    private final Plugin plugin;
    private final MenuManager menuManager;
    private final PlayerResolver playerResolver;

    private final Map<UUID, Integer> pages = new ConcurrentHashMap<>();
    private final Map<UUID, String> previews = new ConcurrentHashMap<>();
    private final Map<UUID, String> editors = new ConcurrentHashMap<>();

    public MenuState(Plugin plugin, MenuManager menuManager, PlayerResolver playerResolver) {
        this.plugin = plugin;
        this.menuManager = menuManager;
        this.playerResolver = playerResolver;
    }

    public int page(UUID player) {
        return pages.getOrDefault(player, 0);
    }

    public void setPage(UUID player, int page) {
        pages.put(player, Math.max(0, page));
    }

    public String previewKit(UUID player) {
        return previews.get(player);
    }

    public String editorKit(UUID player) {
        return editors.get(player);
    }

    public void openMain(Player player) {
        open(MAIN, player);
    }

    public void openPreview(Player player, String kitId) {
        previews.put(player.getUniqueId(), kitId);
        open(PREVIEW, player);
    }

    public void openEditor(Player player, String kitId) {
        editors.put(player.getUniqueId(), kitId);
        open(EDITOR, player);
    }

    public void refresh(Player player) {
        menuManager.refresh(playerResolver.getPlayerSnapshot(player));
    }

    /** Открытие откладывается на тик: подменять инвентарь прямо в обработчике клика небезопасно. */
    private void open(String menuId, Player player) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (player.isOnline()) {
                menuManager.open(menuId, playerResolver.getPlayerSnapshot(player));
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        pages.remove(uuid);
        previews.remove(uuid);
        editors.remove(uuid);
    }
}
