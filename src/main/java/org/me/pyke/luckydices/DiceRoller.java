package org.me.pyke.luckydices;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.scheduler.BukkitRunnable;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class DiceRoller {

    private final JavaPlugin plugin;
    private FileConfiguration config;
    private ItemStack diceItem;
    private ItemDisplay currentItemDisplay;
    private boolean isRemovingEntity = false;
    private final Map<UUID, Long> lastRollTimes = new HashMap<>();

    // Mesaje
    public String receivedDice;
    public String onlyPlayers;
    public String noPermission;
    public String configReloaded;
    public String playerNotFound;
    public String invalidAmount;
    public String usageGive;
    public String usageMain;
    public String diceGiven;

    public String noInventory;

    public DiceRoller(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        this.diceItem = createDiceItem();
        loadMessages();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
        this.config = plugin.getConfig();
        loadMessages();
        updateDiceItem();
    }

    private void loadMessages() {
        this.receivedDice = ChatColor.translateAlternateColorCodes('&', config.getString("messages.received_dice"));
        this.onlyPlayers = ChatColor.translateAlternateColorCodes('&', config.getString("messages.only_players"));
        this.noPermission = ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_permission"));
        this.configReloaded = ChatColor.translateAlternateColorCodes('&', config.getString("messages.config_reloaded"));
        this.playerNotFound = ChatColor.translateAlternateColorCodes('&', config.getString("messages.player_not_found"));
        this.invalidAmount = ChatColor.translateAlternateColorCodes('&', config.getString("messages.invalid_amount"));
        this.usageGive = ChatColor.translateAlternateColorCodes('&', config.getString("messages.usage_give"));
        this.usageMain = ChatColor.translateAlternateColorCodes('&', config.getString("messages.usage_main"));
        this.diceGiven = ChatColor.translateAlternateColorCodes('&', config.getString("messages.dice_given"));
        this.noInventory = ChatColor.translateAlternateColorCodes('&', config.getString("messages.no_inventory_space"));
    }

    private void updateDiceItem() {
        this.diceItem = createDiceItem();
    }

    private ItemStack createDiceItem() {
        String urlTexture = "6e22c298e7c6336af17909ac1f1ee6834b58b1a3cc99aba255ca7eaeb476173"; // Replace with actual texture URL
        ItemStack diceItem = generateSkullWithURLTexture(urlTexture); // Generate the custom head

        SkullMeta meta = (SkullMeta) diceItem.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(config.getString("dice-item.name").replace("&", "§"));
            List<String> lore = config.getStringList("dice-item.lore");
            for (int i = 0; i < lore.size(); i++) {
                lore.set(i, lore.get(i).replace("&", "§"));
            }
            meta.setLore(lore);
            diceItem.setItemMeta(meta);
        }
        return diceItem;
    }

    public ItemStack getDiceItem() {
        return diceItem;
    }

    public void rollDice(Player player) {
        UUID playerId = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long cooldownTime = 5000;
        String cooldownMessageTemplate = ChatColor.translateAlternateColorCodes('&', config.getString("messages.cooldown"));

        if (lastRollTimes.containsKey(playerId)) {
            long lastRollTime = lastRollTimes.get(playerId);
            long timeSinceLastRoll = currentTime - lastRollTime;
            if (timeSinceLastRoll < cooldownTime) {
                long timeLeft = (cooldownTime - timeSinceLastRoll) / 1000; // Convert to seconds
                String cooldownMessage = cooldownMessageTemplate.replace("{time}", String.valueOf(timeLeft));
                playCooldownSound(player);
                player.sendMessage(cooldownMessage);
                return;
            }
        }

        // Actualizează timestamp-ul ultimei aruncări
        lastRollTimes.put(playerId, currentTime);

        Random random = new Random();
        int roll = random.nextInt(6) + 1; // Random number between 1 and 6
        List<String> actions = config.getStringList("roll-actions." + roll);
        if (actions != null) {
            for (String action : actions) {
                action = action.replace("{user}", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), action);
            }
            playDiceRollSound(player);
            displayDiceRoll(player, roll);
            removeDiceFromInventory(player);
        }
    }

    private void removeDiceFromInventory(Player player) {
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        if (itemInHand.getAmount() > 1) {
            itemInHand.setAmount(itemInHand.getAmount() - 1);
        } else {
            player.getInventory().setItemInMainHand(null);
        }
    }

    private void playDiceRollSound(Player player) {
        Location location = player.getLocation();
        player.playSound(location, "minecraft:block.stone.hit", 1.0f, 1.0f);
        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(location, "minecraft:block.wood.step", 1.0f, 1.0f);
            }
        }.runTaskLater(plugin, 5L); // 5 ticks delay to simulate rolling sound

        new BukkitRunnable() {
            @Override
            public void run() {
                player.playSound(location, "minecraft:entity.experience_orb.pickup", 1.0f, 1.0f);
            }
        }.runTaskLater(plugin, 7L); // 7 ticks delay to simulate rolling sound
    }

    private void playCooldownSound(Player player) {
        Location location = player.getLocation();
        player.playSound(location, "minecraft:block.anvil.place", 1.0f, 1.0f);
    }
    
    private ItemStack createCustomHead(int roll) {
        String urlTexture = getTextureForRoll(roll);

        ItemStack customHead = generateSkullWithURLTexture(urlTexture);
        SkullMeta meta = (SkullMeta) customHead.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§aDice Roll: " + roll);
            customHead.setItemMeta(meta);
        }
        return customHead;
    }

    private String getTextureForRoll(int roll) {
        switch (roll) {
            case 1:
                return "6e22c298e7c6336af17909ac1f1ee6834b58b1a3cc99aba255ca7eaeb476173"; // Replace with actual texture URL
            case 2:
                return "71b7a73fc934c9de9160c0fd59df6e42efd5d0378e342b68612cfec3e894834a"; // Replace with actual texture URL
            case 3:
                return "abe677a1e163a9f9e0afcfcde0c95365553478f99ab11152a4d97cf85dbe66f"; // Replace with actual texture URL
            case 4:
                return "af2996efc2bb054f53fb0bd106ebae675936efe1fef441f653c2dce349738e"; // Replace with actual texture URL
            case 5:
                return "e0d2a3ce4999fed330d3a5d0a9e218e37f4f57719808657396d832239e12"; // Replace with actual texture URL
            case 6:
                return "41a2c088637fee9ae3a36dd496e876e657f509de55972dd17c18767eae1f3e9"; // Replace with actual texture URL
            default:
                return "";
        }
    }

    public ItemStack generateSkullWithURLTexture(String URLTexture) {
        // Attempt bug fixes for bad input strings
        URLTexture = URLTexture.toLowerCase();
        URLTexture = URLTexture.replace("http://textures.minecraft.net/texture/", "");
        URLTexture = URLTexture.replace("https://textures.minecraft.net/texture/", "");

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
        PlayerProfile pp = Bukkit.createPlayerProfile(UUID.fromString("4fbecd49-c7d4-4c18-8410-adf7a7348728"));
        PlayerTextures pt = pp.getTextures();
        try {
            pt.setSkin(new URL("http://textures.minecraft.net/texture/" + URLTexture));
        } catch (MalformedURLException e) {e.printStackTrace();}
        pp.setTextures(pt);
        skullMeta.setOwnerProfile(pp);
        item.setItemMeta(skullMeta);
        return item;
    }

    private void displayDiceRoll(Player player, int roll) {
        if (currentItemDisplay != null && !currentItemDisplay.isDead()) {
            isRemovingEntity = true;
            currentItemDisplay.remove();
            currentItemDisplay = null;
        }

        new BukkitRunnable() {
            @Override
            public void run() {
                isRemovingEntity = false;
                Location loc = player.getLocation();

                // Get yaw in radians
                double yaw = Math.toRadians(loc.getYaw());

                // Calculate x and z offsets using cosine and sine functions
                double xOffset = -Math.sin(yaw) * 3; // Distance in front of the player
                double zOffset = Math.cos(yaw) * 3;  // Distance in front of the player

                loc.add(xOffset, 1, zOffset);

                ItemStack customHead = createCustomHead(roll);
                currentItemDisplay = (ItemDisplay) player.getWorld().spawn(loc, ItemDisplay.class);
                currentItemDisplay.setItemStack(customHead);
                currentItemDisplay.setGravity(false);
                currentItemDisplay.setPersistent(false);
                currentItemDisplay.setCustomName("§8« §4" + roll + " §8»");
                currentItemDisplay.setCustomNameVisible(true);

                currentItemDisplay.setRotation(0, 0);

                new BukkitRunnable() {
                    @Override
                    public void run() {
                        if (currentItemDisplay != null) {
                            currentItemDisplay.remove();
                            currentItemDisplay = null;
                        }
                    }
                }.runTaskLater(plugin, 20L * 3); // Display for 3 seconds
            }
        }.runTaskLater(plugin, 10L); // Wait for 0.5 second before spawning a new entity
    }

    public void cleanup() {
        if (currentItemDisplay != null && !currentItemDisplay.isDead()) {
            currentItemDisplay.remove();
        }
    }
}
