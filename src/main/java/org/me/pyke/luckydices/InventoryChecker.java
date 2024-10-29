package org.me.pyke.luckydices;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class InventoryChecker {
    private Player player;

    public InventoryChecker(Player player) {
        this.player = player;
    }

    public int getEmptyInventorySlots() {
        int emptySlots = 0;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            if (contents[i] == null) {
                emptySlots++;
            }
        }
        return emptySlots;
    }
}
