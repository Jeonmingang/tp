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

        if (getCommand("야생랜덤") != null) {
            RandomWildCommand wildCmd = new RandomWildCommand(this);
            getCommand("야생랜덤").setExecutor(wildCmd);
            getCommand("야생랜덤").setTabCompleter(wildCmd);
        } else {
            getLogger().warning("Command /야생랜덤 not found in plugin.yml");
        }

        try { VaultHook.setup(); } catch (Throwable t) { getLogger().warning("Vault setup skipped: " + t.getMessage()); }

        ShoutManager shoutMgr = new ShoutManager();
        if (getCommand("확성기") != null) {
            ShoutCommand shout = new ShoutCommand(this, shoutMgr);
            getCommand("확성기").setExecutor(shout);
            getCommand("확성기").setTabCompleter(shout);
        } else {
            getLogger().warning("Command /확성기 not found in plugin.yml");
        }

        try { getServer().getPluginManager().registerEvents(new WildInterceptListener(this), this); } catch (Throwable ignored) {}
        try { getServer().getPluginManager().registerEvents(new ShoutInterceptListener(this, shoutMgr), this); } catch (Throwable ignored) {}

        getLogger().info("WildRandom enabled.");
    }

    public static RandomWildPlugin getInstance() { return instance; }
    public FileConfiguration cfg() { return getConfig(); }

    public World targetWorld() {
        String w = getConfig().getString("world", "world");
        World world = Bukkit.getWorld(w); // always use configured world
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            getLogger().warning("Target world '" + w + "' not found; using default world.");
            world = Bukkit.getWorlds().get(0);
        }
        return world;
    }

    public CooldownManager getCooldowns() { return cooldowns; }
}
