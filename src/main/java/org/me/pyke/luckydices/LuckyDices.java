package org.me.pyke.luckydices;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class LuckyDices extends JavaPlugin {

    private FileConfiguration config;
    private DiceRoller diceRoller;

    @Override
    public void onEnable() {
        getLogger().info("LuckyDices enabled successfully!");
        saveDefaultConfig();
        config = getConfig();
        diceRoller = new DiceRoller(this, config);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(diceRoller), this);
        getCommand("getdice").setExecutor(new DiceCommandExecutor(diceRoller));
        getCommand("dice").setExecutor(new DiceCommandExecutor(diceRoller));
    }

    @Override
    public void onDisable() {
        diceRoller.cleanup();
        getLogger().info("LuckyDices disabled successfully!");
    }
}
