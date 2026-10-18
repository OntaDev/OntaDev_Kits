package com.ontadev.kits.model;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitLayoutTest {

    private final KitLayout layout = new KitLayout(2, 3, 4, 5, 6, 9);

    @Test
    void everyInventorySlotRoundTrips() {
        Set<Integer> used = new HashSet<>();
        for (int slot = 0; slot < KitLayout.INVENTORY_SLOTS; slot++) {
            int gui = layout.toGuiSlot(slot);
            assertTrue(gui >= 0 && gui < KitLayout.GUI_SIZE, "slot " + slot);
            assertTrue(used.add(gui), "duplicate gui slot " + gui);
            assertEquals(slot, layout.toInventorySlot(gui));
        }
    }

    @Test
    void gridHoldsHotbarThenStorage() {
        assertEquals(9, layout.toGuiSlot(0));
        assertEquals(17, layout.toGuiSlot(8));
        assertEquals(18, layout.toGuiSlot(9));
        assertEquals(44, layout.toGuiSlot(35));
    }

    @Test
    void equipmentSitsInTopRow() {
        assertEquals(2, layout.toGuiSlot(39));
        assertEquals(3, layout.toGuiSlot(38));
        assertEquals(4, layout.toGuiSlot(37));
        assertEquals(5, layout.toGuiSlot(36));
        assertEquals(6, layout.toGuiSlot(40));
    }

    @Test
    void otherSlotsAreNotContent() {
        for (int slot : new int[]{0, 1, 7, 8, 45, 49, 53}) {
            assertFalse(layout.isContentSlot(slot), "slot " + slot);
        }
        assertEquals(-1, layout.toGuiSlot(41));
        assertEquals(-1, layout.toGuiSlot(-1));
    }

    @Test
    void contentSlotsStartWithGrid() {
        List<Integer> slots = layout.contentSlots();
        assertEquals(41, slots.size());
        assertEquals(9, slots.get(0));
        assertEquals(44, slots.get(35));
    }
}
