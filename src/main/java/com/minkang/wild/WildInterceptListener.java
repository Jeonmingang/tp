package com.minkang.wild;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class WildInterceptListener implements Listener {

    private final RandomWildPlugin plugin;

    public WildInterceptListener(RandomWildPlugin plugin) { this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (msg == null || !msg.startsWith("/")) return;
        String[] parts = msg.substring(1).trim().split("\\s+");
        if (parts.length == 0) return;
        String label = parts[0];
        if (!label.equalsIgnoreCase("야생랜덤") &&
            !label.equalsIgnoreCase("wildrandom") &&
            !label.equalsIgnoreCase("wild")) return;

        Player p = e.getPlayer();

        // if args exist -> try original executor (requires others perm)
        if (parts.length >= 2) return;

        e.setCancelled(true);
        TeleportFlow.start(plugin, p);
    }
}
