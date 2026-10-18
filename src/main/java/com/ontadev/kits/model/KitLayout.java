// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Раскладка слотов GUI: сетка из 36 слотов (сначала хотбар, потом хранилище) плюс броня и вторая рука
 * там, где их задали в конфиге. Номера слотов игрока как в {@code PlayerInventory}: 0-35 хранилище,
 * 36-39 броня (ботинки..шлем), 40 вторая рука.
 */
public final class KitLayout {

    public static final int INVENTORY_SLOTS = 41;
    public static final int GUI_SIZE = 54;
    public static final int GRID_SIZE = 36;

    private final int head;
    private final int chest;
    private final int legs;
    private final int boots;
    private final int leftHand;
    private final int gridStart;

    public KitLayout(int head, int chest, int legs, int boots, int leftHand, int gridStart) {
        this.head = head;
        this.chest = chest;
        this.legs = legs;
        this.boots = boots;
        this.leftHand = leftHand;
        this.gridStart = gridStart;
    }

    public int toGuiSlot(int inventorySlot) {
        int slot = -1;
        if (inventorySlot >= 0 && inventorySlot <= 35) slot = gridStart + inventorySlot;
        else if (inventorySlot == 39) slot = head;
        else if (inventorySlot == 38) slot = chest;
        else if (inventorySlot == 37) slot = legs;
        else if (inventorySlot == 36) slot = boots;
        else if (inventorySlot == 40) slot = leftHand;
        return slot >= 0 && slot < GUI_SIZE ? slot : -1;
    }

    public int toInventorySlot(int guiSlot) {
        if (guiSlot < 0 || guiSlot >= GUI_SIZE) return -1;
        for (int inventorySlot = 0; inventorySlot < INVENTORY_SLOTS; inventorySlot++) {
            if (toGuiSlot(inventorySlot) == guiSlot) return inventorySlot;
        }
        return -1;
    }

    public boolean isContentSlot(int guiSlot) {
        return toInventorySlot(guiSlot) >= 0;
    }

    public boolean isGridSlot(int guiSlot) {
        return guiSlot >= gridStart && guiSlot < gridStart + GRID_SIZE;
    }

    /** Слоты с предметами набора: сначала сетка, потом броня и рука. */
    public List<Integer> contentSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int slot = gridStart; slot < gridStart + GRID_SIZE && slot < GUI_SIZE; slot++) {
            slots.add(slot);
        }
        for (int inventorySlot : new int[]{39, 38, 37, 36, 40}) {
            int slot = toGuiSlot(inventorySlot);
            if (slot >= 0 && !slots.contains(slot)) slots.add(slot);
        }
        return slots;
    }
}
