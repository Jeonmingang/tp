package com.minkang.wild;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Random;

public class RandomTeleporter {
    private final Random rng = new Random();

    public Location findSafe(RandomWildPlugin plugin, World world) {
        int min = Math.max(0, plugin.cfg().getInt("min-radius", 200));
        int max = Math.max(min + 1, plugin.cfg().getInt("max-radius", 5000));
        int attempts = Math.max(1, plugin.cfg().getInt("max-attempts", 40));
        @SuppressWarnings("unchecked")
        List<String> unsafe = (List<String>) plugin.cfg().getList("unsafe-blocks");

        Location center = world.getSpawnLocation();
        for (int i = 0; i < attempts; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            int dist = min + rng.nextInt(max - min + 1);
            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * dist);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * dist);

            int y = world.getHighestBlockYAt(x, z);
            if (y <= world.getMinHeight()) continue;

            Block feet = world.getBlockAt(x, y, z);
            Block head = world.getBlockAt(x, y + 1, z);
            Block below = world.getBlockAt(x, y - 1, z);

            if (!feet.isEmpty() || !head.isEmpty()) continue;
            if (isUnsafe(below.getType(), unsafe)) continue;

            return new Location(world, x + 0.5, y, z + 0.5);
        }
        return null;
    }

    private boolean isUnsafe(Material mat, List<String> blacklist) {
        if (mat == null) return true;
        if (blacklist != null) {
            for (String s : blacklist) {
                try { if (mat == Material.valueOf(s)) return true; } catch (IllegalArgumentException ignored) {}
            }
        }
        if (mat == Material.LAVA || mat == Material.FIRE || mat == Material.CAMPFIRE) return true;
        if (mat == Material.WATER) return true;
        if (mat.isAir()) return true;
        return false;
    }

    public void teleportWithTitles(Player p, Location to) {
        p.sendTitle(ChatColor.YELLOW + "이동중...", ChatColor.GRAY + "야생 좌표를 찾는 중", 10, 40, 10);
        p.getWorld().playSound(p.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.6f, 1.2f);
        p.teleport(to);
        String sub = ChatColor.GRAY + String.format("(x:%d, y:%d, z:%d)", to.getBlockX(), to.getBlockY(), to.getBlockZ());
        p.sendTitle(ChatColor.GREEN + "이동 완료!", sub, 10, 40, 10);
        p.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                new net.md_5.bungee.api.chat.TextComponent(ChatColor.AQUA + "야생으로 이동되었습니다."));
        p.getWorld().spawnParticle(Particle.PORTAL, to, 60, 0.8, 1.0, 0.8, 0.1);
        p.getWorld().playSound(to, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 0.9f);
    }
}
