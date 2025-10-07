package com.minkang.wild;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.*;

public class ShoutCommand implements CommandExecutor, TabCompleter {

    private final RandomWildPlugin plugin;
    private final ShoutManager manager;

    public ShoutCommand(RandomWildPlugin plugin, ShoutManager mgr) {
        this.plugin = plugin;
        this.manager = mgr;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) { sender.sendMessage("§c플레이어만 사용할 수 있습니다."); return true; }
        Player p = (Player) sender;

        if (!plugin.getConfig().getBoolean("megaphone.enabled", true)) {
            p.sendMessage("§c확성기가 비활성화되어 있습니다."); return true;
        }
        if (args.length == 0) { p.sendMessage("§e사용법: /확성기 <메시지>"); return true; }

        String raw = String.join(" ", args);
        int maxLen = Math.max(10, plugin.getConfig().getInt("megaphone.message-max-length", 120));
        if (raw.length() > maxLen) { p.sendMessage("§c메시지가 너무 깁니다. 최대 " + maxLen + "자"); return true; }

        int dupSec = Math.max(0, plugin.getConfig().getInt("megaphone.anti-duplicate-seconds", 10));
        String last = manager.getLastMsg(p.getUniqueId());
        if (dupSec > 0 && last != null && last.equalsIgnoreCase(raw)) {
            p.sendMessage("§c같은 메시지를 연속으로 보낼 수 없습니다."); return true;
        }

        int cd = Math.max(0, plugin.getConfig().getInt("megaphone.cooldown-seconds", 30));
        long remain = manager.remaining(p.getUniqueId(), cd);
        if (remain > 0 && !p.hasPermission("shout.bypass.cooldown")) {
            p.sendMessage("§e잠시 후 다시 사용 가능: §c" + Math.max(1, Math.round(remain/1000.0)) + "초");
            return true;
        }

        boolean useVault = plugin.getConfig().getBoolean("megaphone.use-vault", true);
        double cost = plugin.getConfig().getDouble("megaphone.cost-per-use", 0.0);
        String curName = plugin.getConfig().getString("megaphone.currency-name", "코인");
        if (useVault && cost > 0 && !p.hasPermission("shout.bypass.cost")) {
            if (!VaultHook.hasEconomy()) { p.sendMessage("§c경제 플러그인이 없어 비용을 차감할 수 없습니다."); return true; }
            if (!VaultHook.economy().has(p, cost)) { p.sendMessage("§c잔액 부족: " + cost + " " + curName); return true; }
            VaultHook.economy().withdrawPlayer(p, cost);
            p.sendMessage("§7사용 비용 차감: §e" + cost + " " + curName);
        }

        boolean allowColor = plugin.getConfig().getBoolean("megaphone.allow-color", true) || p.hasPermission("shout.color");
        boolean allowHex = plugin.getConfig().getBoolean("megaphone.allow-hex", true) && p.hasPermission("shout.color");
        String text = TextUtil.colorize(raw, allowColor, allowHex);

        String prefix = plugin.getConfig().getString("megaphone.prefix", "&6[확성기] &f{player}&7: ");
        prefix = TextUtil.colorize(prefix.replace("{player}", p.getName()), true, allowHex);

        String hoverTpl = plugin.getConfig().getString("megaphone.hover", "&e{player}님의 확성기\n&7{time}");
        String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        String hover = TextUtil.colorize(hoverTpl.replace("{player}", p.getName()).replace("{time}", now), true, allowHex);

        String suggest = plugin.getConfig().getString("megaphone.click-suggest", "/귓 {player} ").replace("{player}", p.getName());

        TextComponent prefixComp = new TextComponent(prefix);
        TextComponent msgComp = new TextComponent(text);
        msgComp.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(hover).create()));
        msgComp.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, suggest));

        for (Player t : Bukkit.getOnlinePlayers()) t.spigot().sendMessage(prefixComp, msgComp);
        Bukkit.getConsoleSender().sendMessage(prefix + text);

        if (plugin.getConfig().getBoolean("megaphone.sound.enabled", true)) {
            String name = plugin.getConfig().getString("megaphone.sound.name", "UI_TOAST_CHALLENGE_COMPLETE");
            float vol = (float) plugin.getConfig().getDouble("megaphone.sound.volume", 0.7);
            float pit = (float) plugin.getConfig().getDouble("megaphone.sound.pitch", 1.0);
            try {
                Sound s = Sound.valueOf(name);
                for (Player t : Bukkit.getOnlinePlayers()) t.playSound(t.getLocation(), s, vol, pit);
            } catch (IllegalArgumentException ignored) {}
        }

        manager.stamp(p.getUniqueId());
        manager.setLastMsg(p.getUniqueId(), raw);
        return true;
    }

    @Override public List<String> onTabComplete(CommandSender s, Command c, String a, String[] args) {
        return Collections.emptyList();
    }
}
