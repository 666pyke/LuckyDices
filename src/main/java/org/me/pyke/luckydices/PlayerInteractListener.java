package org.me.pyke.luckydices;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class PlayerInteractListener implements Listener {

    private final DiceRoller diceRoller;

    public PlayerInteractListener(DiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();

        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            if (itemInHand != null && itemInHand.getType() == diceRoller.getDiceItem().getType()) {
                SkullMeta handMeta = (SkullMeta) itemInHand.getItemMeta();
                SkullMeta diceMeta = (SkullMeta) diceRoller.getDiceItem().getItemMeta();

                if (handMeta != null && diceMeta != null) {
                    if (handMeta.hasDisplayName() && handMeta.getDisplayName().equals(diceMeta.getDisplayName())) {
                        if (handMeta.hasLore() && handMeta.getLore().equals(diceMeta.getLore())) {
                            event.setCancelled(true);
                            diceRoller.rollDice(player);
                        }
                    }
                }
            }
        }
    }
}
