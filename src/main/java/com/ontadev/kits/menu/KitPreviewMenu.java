// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.menu;

import com.ontadev.kits.config.ItemTemplate;
import com.ontadev.kits.config.MenuConfig;
import com.ontadev.kits.model.Kit;
import com.ontadev.kits.model.KitLayout;
import com.ontadev.kits.service.KitActions;
import com.ontadev.kits.service.KitPlaceholders;
import com.ontadev.kits.service.KitService;
import com.ontadev.kits.service.MenuState;
import com.ontadev.libs.ioc.annotation.menu.Menu;
import com.ontadev.libs.item.ItemModel;
import com.ontadev.libs.menu.AbstractMenu;
import com.ontadev.libs.message.Message;
import com.ontadev.libs.message.Placeholders;
import com.ontadev.libs.player.PlayerSnapshot;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Предпросмотр набора. Слоты брони, рук и сетки берутся из menu.yml (preview.slots).
 */
@Menu
public class KitPreviewMenu extends AbstractMenu {

    private final MenuConfig menuConfig;
    private final KitService kitService;
    private final KitActions actions;
    private final KitPlaceholders kitPlaceholders;
    private final MenuState state;

    public KitPreviewMenu(MenuConfig menuConfig, KitService kitService, KitActions actions,
                          KitPlaceholders kitPlaceholders, MenuState state) {
        this.menuConfig = menuConfig;
        this.kitService = kitService;
        this.actions = actions;
        this.kitPlaceholders = kitPlaceholders;
        this.state = state;
    }

    @Override
    public String id() {
        return MenuState.PREVIEW;
    }

    @Override
    public int size() {
        return KitLayout.GUI_SIZE;
    }

    @Override
    public Message title(PlayerSnapshot snapshot) {
        Kit kit = kitService.get(state.previewKit(snapshot.uuid()));
        List<String> lines = new ArrayList<>();
        for (String line : menuConfig.getPreview().getTitle().getRawMessage()) {
            lines.add(line.replace("<name>", kit == null ? "?" : kit.getDisplayName()));
        }
        return new Message(lines);
    }

    @Override
    public Map<Integer, ItemModel> dynamicItems(PlayerSnapshot snapshot) {
        Map<Integer, ItemModel> items = new HashMap<>();
        Player player = Bukkit.getPlayer(snapshot.uuid());
        Kit kit = kitService.get(state.previewKit(snapshot.uuid()));
        MenuConfig.Preview cfg = menuConfig.getPreview();

        KitLayout layout = cfg.getSlots().layout();

        ItemStack filler = cfg.getFiller().build();
        for (int slot = 0; slot < KitLayout.GUI_SIZE; slot++) {
            if (!layout.isGridSlot(slot)) {
                items.put(slot, ItemModel.of(filler));
            }
        }

        put(items, cfg.getBack(), null, (p, e) -> {
            actions.playClick(p);
            state.openMain(p);
        });
        put(items, cfg.getClose(), null, (p, e) -> p.closeInventory());

        if (kit == null || player == null) {
            return items;
        }

        ItemStack emptySlot = cfg.getEmptySlot().build();
        for (int inventorySlot : new int[]{39, 38, 37, 36, 40}) {
            int slot = layout.toGuiSlot(inventorySlot);
            if (slot >= 0) {
                items.put(slot, ItemModel.of(emptySlot));
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : kit.getItems().entrySet()) {
            int slot = layout.toGuiSlot(entry.getKey());
            if (slot >= 0) {
                items.put(slot, ItemModel.of(entry.getValue().clone()));
            }
        }

        Placeholders ph = kitPlaceholders.of(player, kit);
        put(items, cfg.getClaim(), ph, (p, e) -> {
            actions.claim(p, kit);
            state.refresh(p);
        });

        if (player.hasPermission("kits.admin")) {
            put(items, cfg.getEdit(), ph, (p, e) -> {
                if (!p.hasPermission("kits.admin")) return;
                actions.playClick(p);
                state.openEditor(p, kit.getId());
            });
        }

        if (!kit.getCommands().isEmpty()) {
            put(items, cfg.getCommands(), ph, null);
        }
        return items;
    }

    private void put(Map<Integer, ItemModel> items, ItemTemplate template, Placeholders ph,
                     java.util.function.BiConsumer<Player, org.bukkit.event.inventory.InventoryInteractEvent> handler) {
        int slot = template.getSlot();
        if (slot < 0 || slot >= KitLayout.GUI_SIZE) return;

        ItemModel model = ItemModel.of(template.build(ph));
        if (handler != null) {
            model.onAnyInteraction(handler);
        }
        items.put(slot, model);
    }
}
