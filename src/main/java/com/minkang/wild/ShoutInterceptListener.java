package com.minkang.wild;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.entity.Player;

public class ShoutInterceptListener implements Listener {

    private final RandomWildPlugin plugin;
    private final ShoutManager shoutMgr;

    public ShoutInterceptListener(RandomWildPlugin plugin, ShoutManager mgr) {
        this.plugin = plugin;
        this.shoutMgr = mgr;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (msg == null || !msg.startsWith("/")) return;
        String raw = msg.substring(1).trim();
        if (raw.isEmpty()) return;

        // split label + args
        String[] parts = raw.split("\s+");
        String label = parts[0];
        if (!label.equalsIgnoreCase("확성기") &&
            !label.equalsIgnoreCase("megaphone") &&
            !label.equalsIgnoreCase("shout")) {
            return;
        }

        // from here, we handle the command ourselves to bypass command permission gate (visibility only)
        e.setCancelled(true);

        Player p = e.getPlayer();

        // Build args array (may be empty)
        String[] args = new String[Math.max(0, parts.length - 1)];
        if (args.length > 0) {
            System.arraycopy(parts, 1, args, 0, args.length);
        }

        // Directly invoke the command logic
        new ShoutCommand(plugin, shoutMgr).onCommand(p, null, label, args);
    }
}
