package org.me.pyke.luckydices;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class DiceCommandExecutor implements CommandExecutor {

    private final DiceRoller diceRoller;

    public DiceCommandExecutor(DiceRoller diceRoller) {
        this.diceRoller = diceRoller;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("getdice")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                player.getInventory().addItem(diceRoller.getDiceItem());
                player.sendMessage(diceRoller.receivedDice);
                return true;
            } else {
                sender.sendMessage(diceRoller.onlyPlayers);
                return true;
            }
        }

        if (command.getName().equalsIgnoreCase("dice")) {
            if (args.length == 0) {
                sender.sendMessage(diceRoller.usageMain);
                return true;
            }

            if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("luckydices.reload")) {
                    diceRoller.reloadConfig();
                    sender.sendMessage(diceRoller.configReloaded);
                } else {
                    sender.sendMessage(diceRoller.noPermission);
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("give")) {
                if (args.length >= 3) {
                    Player targetPlayer = Bukkit.getPlayer(args[1]);
                    if (targetPlayer != null) {
                        try {
                            int amount = Integer.parseInt(args[2]);
                            ItemStack diceItem = diceRoller.getDiceItem();
                            diceItem.setAmount(amount);

                            InventoryChecker checker = new InventoryChecker(targetPlayer);
                            int emptySlots = checker.getEmptyInventorySlots();

                            int itemsPerSlot = diceItem.getMaxStackSize();
                            int requiredSlots = (int) Math.ceil((double) amount / itemsPerSlot);

                            if (emptySlots >= requiredSlots) {
                                targetPlayer.getInventory().addItem(diceItem);
                                sender.sendMessage(diceRoller.diceGiven.replace("{amount}", String.valueOf(amount)).replace("{player}", targetPlayer.getName()));
                            } else {
                                // Drop the item at the player's location if not enough space in inventory
                                targetPlayer.getWorld().dropItemNaturally(targetPlayer.getLocation(), diceItem);
                                sender.sendMessage("Player's inventory is full! The item was dropped on the ground.");
                                Bukkit.getLogger().warning(targetPlayer.getName() + " did not have enough space in their inventory, the item dropped on the ground.");
                            }
                        } catch (NumberFormatException e) {
                            sender.sendMessage(diceRoller.invalidAmount);
                        }
                    } else {
                        sender.sendMessage(diceRoller.playerNotFound);
                    }
                } else {
                    sender.sendMessage(diceRoller.usageGive);
                }
                return true;
            }

            sender.sendMessage(diceRoller.usageMain);
            return true;
        }

        return false;
    }
}
