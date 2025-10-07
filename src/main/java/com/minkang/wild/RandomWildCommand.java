package com.minkang.wild;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RandomWildCommand implements CommandExecutor, TabCompleter {

    private final RandomWildPlugin plugin;
    private final RandomTeleporter teleporter = new RandomTeleporter();

    public RandomWildCommand(RandomWildPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        Player target = null;

        if (args.length >= 1) {
            if (!sender.hasPermission("wildrandom.others")) {
                sender.sendMessage("§c권한이 없습니다.");
                return true;
            }
            target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                sender.sendMessage("§c해당 플레이어를 찾을 수 없습니다.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§c콘솔은 /야생랜덤 <플레이어> 를 사용하세요.");
                return true;
            }
            target = (Player) sender;
            /* 무권한 사용 허용: 자기 자신은 권한 검사 없음 */
        }

        // 쿨다운 확인
        long remain = plugin.getCooldowns().getRemaining(target);
        if (remain > 0) {
            long sec = Math.max(1, Math.round(remain / 1000.0));
            sender.sendMessage("§e잠시 후 다시 사용 가능: §c" + sec + "초");
            return true;
        }

        final Player fTarget = target;
        final World world = plugin.targetWorld();
        if (world == null) {
            sender.sendMessage("§c대상 월드를 찾을 수 없습니다. config.yml 의 world 값을 확인하세요.");
            return true;
        }

                // 텔레포트 플로우 시작 (카운트다운 + 비동기 탐색)
        TeleportFlow.start(plugin, fTarget);

        /*
        new BukkitRunnable() {
            @Override
            public void run() {
                // 안전 좌표 탐색
                final org.bukkit.Location safe = teleporter.findSafe(plugin, world);
                if (safe == null) {
                    fTarget.sendTitle("§c실패", "§7안전한 지점을 찾지 못했습니다. 잠시 후 재시도하세요.", 10, 40, 10);
                    return;
                }
                // 메인 스레드에서 TP 실행
                Bukkit.getScheduler().runTask(plugin, () -> {
                    teleporter.teleportWithTitles(fTarget, safe);
                    plugin.getCooldowns().stamp(fTarget);
                });
            }
        }.runTaskAsynchronously(plugin);
        */
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("wildrandom.others")) {
            List<String> names = new ArrayList<>();
            for (Player p : Bukkit.getOnlinePlayers()) names.add(p.getName());
            return names;
        }
        return Collections.emptyList();
    }
}
