/*
 * SteelMissions – A Minecraft missions plugin.
 * Copyright (C) 2025 Stev6
 */

package io.github.stev6.steelmissions.listeners;

import io.github.stev6.steelmissions.MissionManager;
import io.github.stev6.steelmissions.SteelMissions;
import io.github.stev6.steelmissions.mission.Mission;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MissionHeldTimer implements Listener {

    private final SteelMissions plugin;
    private final MissionManager manager;
    private final Map<UUID, BukkitTask> playerTasks = new HashMap<>();

    public MissionHeldTimer(SteelMissions plugin) {
        this.plugin = plugin;
        this.manager = plugin.getMissionManager();

        // Start tasks for currently online players (for reloads)
        for (Player p : Bukkit.getOnlinePlayers()) {
            startTask(p);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        startTask(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        stopTask(e.getPlayer());
    }

    private void stopTask(Player player) {
        BukkitTask task = playerTasks.remove(player.getUniqueId());
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
    }

    private void startTask(Player player) {
        // Run every 1 second (20 ticks)
        // Using Scheduler.runTaskTimer is generally safe for UI updates on the player
        // On strict Folia, use player.getScheduler().runAtFixedRate(...) via reflection or wrapper

        Runnable logic = () -> checkHand(player);

        try {
            // Primitive Folia check/support via standard API if available
            // If strictly using Paper API on Folia, the GlobalRegionScheduler is safer, 
            // but for a per-player UI task, simple Bukkit scheduler usually works on the main thread 
            // OR use the EntityScheduler if available in your API version.

            // For now, standard Bukkit scheduler:
            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, logic, 20L, 20L);
            playerTasks.put(player.getUniqueId(), task);
        } catch (Exception e) {
            // Fallback or log error
        }
    }

    private void checkHand(Player player) {
        if (!player.isOnline()) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        // Optimize: Check material first to avoid NBT parsing on air/blocks
        if (item.getType().isAir() || !item.hasItemMeta()) return;

        Mission m = manager.getMissionOrNull(item);
        if (m != null && m.getExpirationTime() > 0) {
            sendTimerBar(player, m);
        }
    }

    private void sendTimerBar(Player player, Mission m) {
        long remaining = m.getExpirationTime() - System.currentTimeMillis();

        if (remaining <= 0) {
            player.sendActionBar(MiniMessage.miniMessage().deserialize("<red><bold>EXPIRED</bold></red>"));
            // Optionally triggering the fail logic here is risky as it modifies inventory during a read
            // Better to let them try to use it and fail then.
            return;
        }

        long seconds = remaining / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        String timeStr;
        if (hours > 0) timeStr = String.format("%02dh %02dm", hours, minutes % 60);
        else timeStr = String.format("%02dm %02ds", minutes, seconds % 60);

        // Send the bar
        player.sendActionBar(MiniMessage.miniMessage().deserialize(
                "<red><b>TIME LEFT:</b> <yellow>" + timeStr + "</yellow>"
        ));
    }
}