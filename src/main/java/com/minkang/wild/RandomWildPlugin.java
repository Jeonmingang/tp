package com.minkang.wild;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomWildPlugin extends JavaPlugin {

    private static RandomWildPlugin instance;
    private CooldownManager cooldowns;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        this.cooldowns = new CooldownManager(this);
        getCommand("야생랜덤").setExecutor(new RandomWildCommand(this));
        getCommand("야생랜덤").setTabCompleter(new RandomWildCommand(this));
        getLogger().info("WildRandom enabled.");
        VaultHook.setup();
        ShoutManager shoutMgr = new ShoutManager();
        if (getCommand("확성기") != null) {
            getCommand("확성기").setExecutor(new ShoutCommand(this, shoutMgr));
            getCommand("확성기").setTabCompleter(new ShoutCommand(this, shoutMgr));
        }
        getServer().getPluginManager().registerEvents(new ShoutInterceptListener(this, shoutMgr), this);
        getServer().getPluginManager().registerEvents(new WildInterceptListener(this), this);
    }

    public static RandomWildPlugin getInstance() {
        return instance;
    }

    public FileConfiguration cfg() {
        return getConfig();
    }

    public World targetWorld() {
        String w = getConfig().getString("world", "world");
        World world = Bukkit.getWorld(w);
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().get(0);
        }
        return world;
    }

    public CooldownManager getCooldowns() {
        return cooldowns;
    }
}
