package com.minkang.wild;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CooldownManager {
    private final Plugin plugin;
    private final Map<UUID, Long> lastUse = new HashMap<>();

    public CooldownManager(Plugin plugin) { this.plugin = plugin; }

    public long getRemaining(Player p) {
        int cd = plugin.getConfig().getInt("cooldown-seconds", 0);
        if (cd <= 0) return 0L;
        long now = System.currentTimeMillis();
        long next = lastUse.getOrDefault(p.getUniqueId(), 0L) + (cd * 1000L);
        return Math.max(0L, next - now);
    }
    public void stamp(Player p) { lastUse.put(p.getUniqueId(), System.currentTimeMillis()); }
}
