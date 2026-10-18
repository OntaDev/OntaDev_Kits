// OntaDev_Kits Plugin
// Авторские права (c) 2026 OntaDev
// Лицензия: MIT

package com.ontadev.kits.config;

import com.google.gson.annotations.SerializedName;
import com.ontadev.kits.util.Items;
import com.ontadev.libs.message.Message;
import com.ontadev.libs.message.Placeholders;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemTemplate {

    private Material material = Material.STONE;
    private Message name;
    private Message lore;
    private int amount = 1;

    @SerializedName("custom-model-data")
    private Integer customModelData;

    private boolean glow;

    private int slot = -1;

    public ItemTemplate(Material material, String name, int slot, String... lore) {
        this.material = material;
        this.name = new Message(name);
        this.lore = lore.length == 0 ? null : new Message(lore);
        this.slot = slot;
    }

    public ItemTemplate glow() {
        this.glow = true;
        return this;
    }

    public ItemStack build(Placeholders placeholders) {
        return Items.create(material, amount, name, lore, placeholders, glow, customModelData);
    }

    public ItemStack build() {
        return build(null);
    }
}
