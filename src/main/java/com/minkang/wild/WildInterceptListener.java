package com.minkang.wild;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class WildInterceptListener implements Listener {

    private final RandomWildPlugin plugin;
    private final RandomTeleporter teleporter = new RandomTeleporter();

    public WildInterceptListener(RandomWildPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onPreprocess(PlayerCommandPreprocessEvent e) {
        String msg = e.getMessage();
        if (msg == null) return;

        // Normalize
        msg = msg.trim();
        if (!msg.startsWith("/")) return;
        String raw = msg.substring(1);

        // Only exact base command (no args) or with one arg; accept aliases too
        String[] parts = raw.split("\\s+");
        if (parts.length == 0) return;

        String label = parts[0];
        if (!label.equalsIgnoreCase("야생랜덤") &&
            !label.equalsIgnoreCase("wildrandom") &&
            !label.equalsIgnoreCase("wild")) {
            return;
        }

        final Player p = e.getPlayer();

        // Allow non-OPs to use even though command is "invisible" to them
        /* 무권한 사용 허용: 가시성은 OP 전용이지만, 커맨드 직접 입력시 전원 사용 가능 */

        // We will handle it ourselves and cancel dispatch so Bukkit's permission gate won't block
        e.setCancelled(true);

        // If they added a player name, fall back to normal executor (OP-only action)
        if (parts.length >= 2) {
            if (p.hasPermission("wildrandom.others")) {
                // dispatch through actual command executor for proper permission/argument handling
                String forward = "/야생랜덤 " + parts[1];
                p.performCommand(forward.substring(1));
            } else {
                p.sendMessage("§c권한이 없습니다.");
            }
            return;
        }

                // No-arg self teleport path using TeleportFlow (countdown + async search)
        TeleportFlow.start(plugin, p);
        /*
        new BukkitRunnable() {
            @Override
            public void run() {
                final org.bukkit.Location safe = teleporter.findSafe(plugin, world);
                if (safe == null) {
                    p.sendTitle("§c실패", "§7안전한 지점을 찾지 못했습니다. 잠시 후 재시도하세요.", 10, 40, 10);
                    return;
                }
                Bukkit.getScheduler().runTask(plugin, () -> {
                    teleporter.teleportWithTitles(p, safe);
                    plugin.getCooldowns().stamp(p);
                });
            }
        }.runTaskAsynchronously(plugin);
        */
    }
}
