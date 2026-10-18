// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.menu;

import com.ontadev.kits.config.ItemTemplate;
import com.ontadev.kits.config.MenuConfig;
import com.ontadev.kits.config.MessageConfig;
import com.ontadev.kits.model.Kit;
import com.ontadev.kits.model.KitLayout;
import com.ontadev.kits.service.KitActions;
import com.ontadev.kits.service.KitPlaceholders;
import com.ontadev.kits.service.KitService;
import com.ontadev.kits.service.MenuState;
import com.ontadev.kits.util.TimeFormat;
import com.ontadev.libs.ioc.annotation.menu.Menu;
import com.ontadev.libs.item.ItemModel;
import com.ontadev.libs.menu.AbstractMenu;
import com.ontadev.libs.message.Message;
import com.ontadev.libs.message.Placeholders;
import com.ontadev.libs.player.PlayerSnapshot;
import com.ontadev.libs.service.ChatInputService;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * Редактор набора. Раскладка слотов как в предпросмотре, содержимое сохраняется при закрытии окна.
 */
@Menu
public class KitEditorMenu extends AbstractMenu {

    private final MenuConfig menuConfig;
    private final MessageConfig messages;
    private final KitService kitService;
    private final KitActions actions;
    private final KitPlaceholders kitPlaceholders;
    private final MenuState state;
    private final ChatInputService chatInput;

    private final Map<Inventory, String> open = new ConcurrentHashMap<>();

    public KitEditorMenu(MenuConfig menuConfig, MessageConfig messages, KitService kitService, KitActions actions,
                         KitPlaceholders kitPlaceholders, MenuState state, ChatInputService chatInput) {
        this.menuConfig = menuConfig;
        this.messages = messages;
        this.kitService = kitService;
        this.actions = actions;
        this.kitPlaceholders = kitPlaceholders;
        this.state = state;
        this.chatInput = chatInput;
        this.editable = true;
    }

    @Override
    public String id() {
        return MenuState.EDITOR;
    }

    @Override
    public int size() {
        return KitLayout.GUI_SIZE;
    }

    @Override
    public Message title(PlayerSnapshot snapshot) {
        Kit kit = kitService.get(state.editorKit(snapshot.uuid()));
        List<String> lines = new ArrayList<>();
        for (String line : menuConfig.getEditor().getTitle().getRawMessage()) {
            lines.add(line.replace("<name>", kit == null ? "?" : kit.getDisplayName()));
        }
        return new Message(lines);
    }

    @Override
    public void onOpen(PlayerSnapshot snapshot, Player player, Inventory inventory) {
        Kit kit = kitService.get(state.editorKit(snapshot.uuid()));
        if (kit == null) return;

        open.put(inventory, kit.getId());
        for (Map.Entry<Integer, ItemStack> entry : kit.getItems().entrySet()) {
            int slot = layout().toGuiSlot(entry.getKey());
            if (slot >= 0) {
                inventory.setItem(slot, entry.getValue().clone());
            }
        }
    }

    @Override
    public void onClose(PlayerSnapshot snapshot, Player player, Inventory inventory) {
        String kitId = open.remove(inventory);
        Kit kit = kitId == null ? null : kitService.get(kitId);
        if (kit == null) return;

        Map<Integer, ItemStack> items = new HashMap<>();
        KitLayout layout = layout();
        for (int slot = 0; slot < KitLayout.GUI_SIZE; slot++) {
            ItemStack item = inventory.getItem(slot);
            int inventorySlot = layout.toInventorySlot(slot);
            if (inventorySlot >= 0 && item != null && item.getType() != Material.AIR) {
                items.put(inventorySlot, item.clone());
            }
        }
        kit.setItems(items);
        kitService.save(kit);
    }

    @Override
    public boolean onClick(PlayerSnapshot snapshot, InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        int raw = event.getRawSlot();

        // двойной клик собрал бы предметы и с кнопок, поэтому запрещаем
        if (event.getClick() == ClickType.DOUBLE_CLICK) {
            event.setCancelled(true);
            return true;
        }

        if (raw >= top.getSize()) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
                moveToFreeSlot(event, top);
            }
            return true;
        }

        // в слотах с содержимым двигаем свободно, кнопки обрабатывает ItemModel
        return raw >= 0 && layout().isContentSlot(raw);
    }

    @Override
    public boolean onDrag(PlayerSnapshot snapshot, InventoryDragEvent event) {
        int topSize = event.getView().getTopInventory().getSize();
        KitLayout layout = layout();
        for (int raw : event.getRawSlots()) {
            if (raw < topSize && !layout.isContentSlot(raw)) {
                event.setCancelled(true);
                break;
            }
        }
        return true;
    }

    private void moveToFreeSlot(InventoryClickEvent event, Inventory top) {
        ItemStack current = event.getCurrentItem();
        if (current == null || current.getType() == Material.AIR) return;

        for (int slot : layout().contentSlots()) {
            ItemStack existing = top.getItem(slot);
            if (existing == null || existing.getType() == Material.AIR) {
                top.setItem(slot, current.clone());
                event.setCurrentItem(null);
                return;
            }
        }
    }

    @Override
    public Map<Integer, ItemModel> dynamicItems(PlayerSnapshot snapshot) {
        Map<Integer, ItemModel> items = new HashMap<>();
        Player player = Bukkit.getPlayer(snapshot.uuid());
        Kit kit = kitService.get(state.editorKit(snapshot.uuid()));
        MenuConfig.Editor cfg = menuConfig.getEditor();

        KitLayout layout = layout();
        ItemStack filler = cfg.getFiller().build();
        for (int slot = 0; slot < KitLayout.GUI_SIZE; slot++) {
            if (!layout.isContentSlot(slot)) {
                items.put(slot, ItemModel.of(filler));
            }
        }

        put(items, cfg.getBack(), null, (p, e) -> {
            actions.playClick(p);
            state.openMain(p);
        });

        if (kit == null || player == null) {
            return items;
        }

        String id = kit.getId();
        Placeholders ph = kitPlaceholders.of(player, kit);

        put(items, cfg.getName(), ph, (p, e) -> prompt(p, id, messages.getPromptName(), "name", input -> {
            kit.setDisplayName(input);
            return true;
        }));

        put(items, cfg.getIcon(), ph, (p, e) -> {
            ItemStack cursor = e instanceof InventoryClickEvent ? ((InventoryClickEvent) e).getCursor() : null;
            if (cursor == null || cursor.getType() == Material.AIR) {
                messages.getHandEmpty().send(p);
                actions.playError(p);
                return;
            }
            kit.setIcon(cursor.getType());
            changed(p, kit, "icon");
        });

        put(items, cfg.getCooldown(), ph, (p, e) -> prompt(p, id, messages.getPromptCooldown(), "cooldown", input -> {
            long seconds = TimeFormat.parse(input);
            if (seconds < 0) {
                messages.getInvalidTime().send(p);
                return false;
            }
            kit.setCooldown(seconds);
            return true;
        }));

        put(items, cfg.getOneTime(), ph.add("state", kitPlaceholders.state(kit.isOneTime())), (p, e) -> {
            kit.setOneTime(!kit.isOneTime());
            changed(p, kit, "one-time");
        });

        put(items, cfg.getPermission(), ph, (p, e) -> prompt(p, id, messages.getPromptPermission(), "permission", input -> {
            kit.setPermission(input.equals("-") ? "" : input);
            return true;
        }));

        put(items, cfg.getCommands(), ph, (p, e) -> {
            ClickType click = clickOf(e);
            if (click == ClickType.SHIFT_RIGHT) {
                kit.getCommands().clear();
                changed(p, kit, "commands");
            } else if (click == ClickType.RIGHT) {
                List<String> commands = kit.getCommands();
                if (!commands.isEmpty()) commands.remove(commands.size() - 1);
                changed(p, kit, "commands");
            } else {
                prompt(p, id, messages.getPromptCommand(), "commands", input -> {
                    kit.getCommands().add(input.startsWith("/") ? input.substring(1) : input);
                    return true;
                });
            }
        });

        put(items, cfg.getDescription(), ph, (p, e) -> {
            if (clickOf(e).isRightClick()) {
                List<String> description = kit.getDescription();
                if (!description.isEmpty()) description.remove(description.size() - 1);
                changed(p, kit, "description");
            } else {
                prompt(p, id, messages.getPromptDescription(), "description", input -> {
                    kit.getDescription().add(input);
                    return true;
                });
            }
        });

        put(items, cfg.getSlot(), ph, (p, e) -> {
            if (clickOf(e).isRightClick()) {
                prompt(p, id, messages.getPromptOrder(), "order", input -> {
                    try {
                        kit.setOrder((int) Double.parseDouble(input.trim().replace(',', '.')));
                        return true;
                    } catch (NumberFormatException ex) {
                        messages.getInvalidNumber().send(p);
                        return false;
                    }
                });
                return;
            }
            prompt(p, id, messages.getPromptSlot(), "slot", input -> {
            try {
                int slot = Integer.parseInt(input.trim());
                if (slot < -1 || slot >= KitLayout.GUI_SIZE) throw new NumberFormatException();
                kit.setSlot(slot);
                return true;
            } catch (NumberFormatException ex) {
                messages.getInvalidSlot().send(p);
                return false;
            }
            });
        });

        put(items, cfg.getEnabled(), ph.add("state", kitPlaceholders.state(kit.isEnabled())), (p, e) -> {
            kit.setEnabled(!kit.isEnabled());
            changed(p, kit, "enabled");
        });

        put(items, cfg.getLoadInventory(), ph, (p, e) -> {
            Inventory top = e.getView().getTopInventory();
            for (int slot : layout().contentSlots()) {
                top.setItem(slot, null);
            }
            for (Map.Entry<Integer, ItemStack> entry : KitService.snapshot(p).entrySet()) {
                int slot = layout().toGuiSlot(entry.getKey());
                if (slot >= 0) top.setItem(slot, entry.getValue());
            }
            actions.playSuccess(p);
            messages.getEditorChanged().send(p, Placeholders.of("what", "inventory"));
        });

        put(items, cfg.getDelete(), ph, (p, e) -> {
            if (clickOf(e) != ClickType.SHIFT_LEFT) return;
            kitService.delete(kit);
            messages.getKitDeleted().send(p, Placeholders.of("kit", id));
            actions.playSuccess(p);
            state.openMain(p);
        });

        put(items, cfg.getClose(), ph, (p, e) -> p.closeInventory());
        return items;
    }

    /** Сохраняет настройку и переоткрывает редактор, чтобы заодно сохранилось содержимое. */
    private void changed(Player player, Kit kit, String what) {
        player.closeInventory();
        kitService.save(kit);
        messages.getEditorChanged().send(player, Placeholders.of("what", what));
        actions.playClick(player);
        state.openEditor(player, kit.getId());
    }

    /**
     * Закрывает окно, спрашивает значение в чате и применяет его. Если {@code apply} вернёт false
     * (ввод не подошёл), редактор откроется без изменений.
     */
    private void prompt(Player player, String kitId, Message prompt, String what, java.util.function.Predicate<String> apply) {
        player.closeInventory();
        chatInput.requestText(player, prompt, input -> {
            Kit kit = kitService.get(kitId);
            if (kit != null && apply.test(input.trim())) {
                kitService.save(kit);
                messages.getEditorChanged().send(player, Placeholders.of("what", what));
                actions.playSuccess(player);
            }
            state.openEditor(player, kitId);
        }, () -> {
            messages.getEditorCancelled().send(player);
            state.openEditor(player, kitId);
        });
    }

    private KitLayout layout() {
        return menuConfig.getPreview().getSlots().layout();
    }

    private static ClickType clickOf(InventoryInteractEvent event) {
        return event instanceof InventoryClickEvent ? ((InventoryClickEvent) event).getClick() : ClickType.UNKNOWN;
    }

    private void put(Map<Integer, ItemModel> items, ItemTemplate template, Placeholders ph,
                     BiConsumer<Player, InventoryInteractEvent> handler) {
        int slot = template.getSlot();
        if (slot < 0 || slot >= KitLayout.GUI_SIZE || layout().isContentSlot(slot)) return;

        ItemModel model = ItemModel.of(template.build(ph));
        if (handler != null) {
            model.onAnyInteraction(handler);
        }
        items.put(slot, model);
    }
}
