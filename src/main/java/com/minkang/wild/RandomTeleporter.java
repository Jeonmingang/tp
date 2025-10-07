package com.minkang.wild;

import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomTeleporter {
    private final Random rng = new Random();
    private static final Set<Material> NOT_GROUND = new HashSet<>();

    static {
        NOT_GROUND.add(Material.WATER);
        NOT_GROUND.add(Material.LAVA);
        NOT_GROUND.add(Material.CACTUS);
        NOT_GROUND.add(Material.FIRE);
        NOT_GROUND.add(Material.CAMPFIRE);
        NOT_GROUND.add(Material.MAGMA_BLOCK);
        NOT_GROUND.add(Material.SWEET_BERRY_BUSH);
        for (Material m : Material.values()) {
            String n = m.name();
            if (n.endsWith("_LEAVES") || n.endsWith("_GLASS") || n.endsWith("_ICE")) {
                NOT_GROUND.add(m);
            }
        }
    }

    public Location findSafe(RandomWildPlugin plugin, World world) {
        int min = Math.max(0, plugin.cfg().getInt("min-radius", 200));
        int max = Math.max(min + 1, plugin.cfg().getInt("max-radius", 5000));
        int attempts = Math.max(1, plugin.cfg().getInt("max-attempts", 80));
        @SuppressWarnings("unchecked")
        List<String> unsafe = (List<String>) plugin.cfg().getList("unsafe-blocks");

        Location center = world.getSpawnLocation();
        for (int i = 0; i < attempts; i++) {
            double angle = rng.nextDouble() * Math.PI * 2;
            int dist = min + rng.nextInt(max - min + 1);
            int x = center.getBlockX() + (int) Math.round(Math.cos(angle) * dist);
            int z = center.getBlockZ() + (int) Math.round(Math.sin(angle) * dist);

            int surfaceY = world.getHighestBlockYAt(x, z);
            if (surfaceY <= world.getMinHeight()) continue;

            int feetY = surfaceY + 1;
            int headY = surfaceY + 2;

            Block below = world.getBlockAt(x, surfaceY, z);
            Block feet = world.getBlockAt(x, feetY, z);
            Block head = world.getBlockAt(x, headY, z);

            if (!feet.getType().isAir() || !head.getType().isAir()) continue;
            if (!isSolidGround(below.getType(), unsafe)) continue;

            return new Location(world, x + 0.5, feetY, z + 0.5);
        }
        return null;
    }

    private boolean isSolidGround(Material mat, List<String> blacklist) {
        if (mat == null) return false;
        if (mat.isAir()) return false;
        if (NOT_GROUND.contains(mat)) return false;
        if (blacklist != null) {
            for (String s : blacklist) {
                try { if (mat == Material.valueOf(s)) return false; } catch (IllegalArgumentException ignored) {}
            }
        }
        return mat.isSolid();
    }

    public void teleportWithTitles(org.bukkit.entity.Player p, Location to) {
        p.sendTitle(ChatColor.YELLOW + "이동중...", ChatColor.GRAY + "야생 좌표를 찾는 중", 5, 20, 5);
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
