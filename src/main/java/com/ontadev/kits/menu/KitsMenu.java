// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.menu;

import com.ontadev.kits.config.MenuConfig;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.kits.model.Kit;
import com.ontadev.kits.model.KitState;
import com.ontadev.kits.service.KitActions;
import com.ontadev.kits.service.KitPlaceholders;
import com.ontadev.kits.service.KitService;
import com.ontadev.kits.service.MenuState;
import com.ontadev.kits.util.Items;
import com.ontadev.libs.ioc.annotation.menu.Menu;
import com.ontadev.libs.item.ItemModel;
import com.ontadev.libs.menu.AbstractMenu;
import com.ontadev.libs.menu.enums.InteractionType;
import com.ontadev.libs.message.Message;
import com.ontadev.libs.message.Placeholders;
import com.ontadev.libs.player.PlayerSnapshot;
import com.ontadev.libs.service.ChatInputService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Menu
public class KitsMenu extends AbstractMenu {

    private final MenuConfig menuConfig;
    private final MessageConfig messages;
    private final KitService kitService;
    private final KitActions actions;
    private final KitPlaceholders kitPlaceholders;
    private final MenuState state;
    private final ChatInputService chatInput;

    public KitsMenu(MenuConfig menuConfig, MessageConfig messages, KitService kitService, KitActions actions,
                    KitPlaceholders kitPlaceholders, MenuState state, ChatInputService chatInput) {
        this.menuConfig = menuConfig;
        this.messages = messages;
        this.kitService = kitService;
        this.actions = actions;
        this.kitPlaceholders = kitPlaceholders;
        this.state = state;
        this.chatInput = chatInput;
    }

    @Override
    public String id() {
        return MenuState.MAIN;
    }

    @Override
    public int size() {
        return Math.max(1, Math.min(6, menuConfig.getMain().getRows())) * 9;
    }

    @Override
    public Message title(PlayerSnapshot snapshot) {
        Player player = Bukkit.getPlayer(snapshot.uuid());
        List<Kit> visible = new ArrayList<>();
        int slotCount = 0;
        if (player != null) {
            List<Kit> all = visibleKits(player);
            List<Kit> pinned = pinnedKits(all, size());
            visible.addAll(all);
            visible.removeAll(pinned);
            slotCount = kitSlots(size(), pinned).size();
        }
        int pages = pageCount(visible.size(), slotCount);
        int page = Math.min(state.page(snapshot.uuid()), pages - 1);

        List<String> lines = new ArrayList<>();
        for (String line : menuConfig.getMain().getTitle().getRawMessage()) {
            lines.add(line.replace("<page>", String.valueOf(page + 1)).replace("<pages>", String.valueOf(pages)));
        }
        return new Message(lines);
    }

    @Override
    public Map<Integer, ItemModel> dynamicItems(PlayerSnapshot snapshot) {
        Player player = Bukkit.getPlayer(snapshot.uuid());
        if (player == null) {
            return new HashMap<>();
        }

        MenuConfig.Main cfg = menuConfig.getMain();
        int size = size();
        boolean admin = player.hasPermission("kits.admin");

        List<Kit> all = visibleKits(player);
        List<Kit> pinned = pinnedKits(all, size);
        List<Kit> visible = new ArrayList<>(all);
        visible.removeAll(pinned);
        List<Integer> slots = kitSlots(size, pinned);
        int perPage = Math.max(1, slots.size());
        int pages = pageCount(visible.size(), slots.size());
        int page = Math.min(state.page(player.getUniqueId()), pages - 1);
        Placeholders pagePh = Placeholders.of("page", String.valueOf(page + 1)).add("pages", pages);

        Map<Integer, ItemModel> items = new HashMap<>();

        if (cfg.isFillerEnabled()) {
            ItemStack filler = cfg.getFiller().build();
            for (int slot = 0; slot < size; slot++) {
                items.put(slot, ItemModel.of(filler));
            }
        }

        int from = page * perPage;
        for (int i = 0; i < slots.size() && from + i < visible.size(); i++) {
            Kit kit = visible.get(from + i);
            items.put(slots.get(i), kitItem(player, kit, admin));
        }

        for (Kit kit : pinned) {
            items.put(kit.getSlot(), kitItem(player, kit, admin));
        }

        if (all.isEmpty()) {
            put(items, cfg.getEmptyPage(), size, null, null);
        }

        if (page > 0) {
            put(items, cfg.getPrevious(), size, pagePh, (p, e) -> {
                actions.playClick(p);
                state.setPage(p.getUniqueId(), state.page(p.getUniqueId()) - 1);
                state.openMain(p);
            });
        }
        if (page < pages - 1) {
            put(items, cfg.getNext(), size, pagePh, (p, e) -> {
                actions.playClick(p);
                state.setPage(p.getUniqueId(), state.page(p.getUniqueId()) + 1);
                state.openMain(p);
            });
        }

        put(items, cfg.getClose(), size, null, (p, e) -> p.closeInventory());
        put(items, cfg.getInfo(), size, null, null);

        if (admin) {
            put(items, cfg.getAdminCreate(), size, null, (p, e) -> promptCreate(p));
        }

        return items;
    }

    private ItemModel kitItem(Player player, Kit kit, boolean admin) {
        MenuConfig.Main cfg = menuConfig.getMain();
        KitState kitState = kitService.stateOf(player, kit);

        List<String> lines = new ArrayList<>(loreFor(kitState).getRawMessage());
        if (admin) {
            lines.addAll(cfg.getLore().getAdminFooter().getRawMessage());
        }

        ItemStack stack = Items.create(
                kit.getIcon(), 1, cfg.getKitName(), new Message(lines),
                kitPlaceholders.of(player, kit),
                cfg.isGlowAvailable() && kitState == KitState.AVAILABLE, null);

        ItemModel model = ItemModel.of(stack);

        model.onInteraction((p, e) -> {
            if (kitService.get(kit.getId()) == null) return;
            actions.claim(p, kit);
            state.refresh(p);
        }, InteractionType.LEFT_CLICK);

        model.onInteraction((p, e) -> {
            actions.playClick(p);
            state.openPreview(p, kit.getId());
        }, InteractionType.RIGHT_CLICK);

        if (admin) {
            model.onInteraction((p, e) -> {
                if (!p.hasPermission("kits.admin")) return;
                actions.playClick(p);
                state.openEditor(p, kit.getId());
            }, InteractionType.SHIFT_RIGHT_CLICK);

            model.onInteraction((p, e) -> {
                if (!p.hasPermission("kits.admin")) return;
                kit.setEnabled(!kit.isEnabled());
                kitService.save(kit);
                actions.playClick(p);
                state.refresh(p);
            }, InteractionType.SHIFT_LEFT_CLICK);
        }
        return model;
    }

    private Message loreFor(KitState kitState) {
        MenuConfig.Lore lore = menuConfig.getMain().getLore();
        switch (kitState) {
            case COOLDOWN:
                return lore.getCooldown();
            case CLAIMED:
                return lore.getClaimed();
            case LOCKED:
                return lore.getLocked();
            case WIPE:
                return lore.getWipe();
            case DISABLED:
                return lore.getDisabled();
            default:
                return lore.getAvailable();
        }
    }

    private void promptCreate(Player player) {
        player.closeInventory();
        chatInput.requestText(player, messages.getPromptNewId(), input -> {
            String id = input.trim().toLowerCase(Locale.ROOT);
            if (!KitService.isValidId(id) || id.equals("create")) {
                messages.getInvalidId().send(player);
                state.openMain(player);
                return;
            }
            if (kitService.exists(id)) {
                messages.getKitExists().send(player, Placeholders.of("kit", id));
                state.openMain(player);
                return;
            }
            if (KitService.snapshot(player).isEmpty()) {
                messages.getEmptyInventory().send(player);
                state.openMain(player);
                return;
            }
            Kit kit = kitService.create(id, player);
            messages.getKitCreated().send(player, Placeholders.of("kit", id).add("count", kit.itemCount()));
            state.openEditor(player, id);
        }, () -> state.openMain(player));
    }

    private List<Kit> visibleKits(Player player) {
        boolean admin = player.hasPermission("kits.admin");
        boolean showLocked = menuConfig.getMain().isShowLocked();

        List<Kit> result = new ArrayList<>();
        for (Kit kit : kitService.all()) {
            if (!kit.isEnabled()) {
                if (admin) result.add(kit);
            } else if (showLocked || kitService.hasAccess(player, kit)) {
                result.add(kit);
            }
        }
        return result;
    }

    private List<Integer> kitSlots(int size, List<Kit> pinned) {
        List<Integer> slots = new ArrayList<>();
        for (Integer slot : menuConfig.getMain().getKitSlots()) {
            if (slot == null || slot < 0 || slot >= size || slots.contains(slot)) continue;
            boolean taken = false;
            for (Kit kit : pinned) {
                if (kit.getSlot() == slot) {
                    taken = true;
                    break;
                }
            }
            if (!taken) slots.add(slot);
        }
        return slots;
    }

    private List<Kit> pinnedKits(List<Kit> kits, int size) {
        List<Kit> pinned = new ArrayList<>();
        List<Integer> used = new ArrayList<>();
        for (Kit kit : kits) {
            int slot = kit.getSlot();
            if (slot >= 0 && slot < size && !used.contains(slot)) {
                used.add(slot);
                pinned.add(kit);
            }
        }
        return pinned;
    }

    private int pageCount(int kits, int slotCount) {
        int perPage = Math.max(1, slotCount);
        return Math.max(1, (kits + perPage - 1) / perPage);
    }

    private void put(Map<Integer, ItemModel> items, com.ontadev.kits.config.ItemTemplate template, int size,
                     Placeholders placeholders, java.util.function.BiConsumer<Player, org.bukkit.event.inventory.InventoryInteractEvent> handler) {
        int slot = template.getSlot();
        if (slot < 0 || slot >= size) return;

        ItemModel model = ItemModel.of(template.build(placeholders));
        if (handler != null) {
            model.onAnyInteraction(handler);
        }
        items.put(slot, model);
    }
}
