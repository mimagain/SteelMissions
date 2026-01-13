/*
 * SteelMissions – A Minecraft missions plugin.
 * Copyright (C) 2025 Stev6
 */

package io.github.stev6.steelmissions.listeners;

import io.github.stev6.steelmissions.MissionManager;
import io.github.stev6.steelmissions.config.records.MissionConfig;
import io.github.stev6.steelmissions.mission.Mission;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public record MissionFailListener(MissionManager manager) implements Listener {

    // Helper to scan inventory and fail matching missions
    private void checkFailConditions(Player p, String trigger, String reasonMessage) {
        Inventory inv = p.getInventory();

        // Loop by index so we can safely clear slots if needed (though removeItem is usually fine)
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack item = inv.getItem(i);
            if (item == null) continue;

            Mission m = manager.getMissionOrNull(item);
            if (m == null || m.isCompleted()) continue;

            MissionConfig config = manager.getMissionConfigOrNull(m);
            if (config == null) continue;

            // Check if this mission has the trigger
            if (config.failConditions().contains(trigger)) {
                manager.failMission(p, item, m, reasonMessage);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDeath(PlayerDeathEvent e) {
        checkFailConditions(e.getEntity(), "DEATH", "died");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent e) {
        if (e.getEntity() instanceof Player p) {
            // You might want to filter small damage, but strict is default
            if (e.getFinalDamage() > 0) {
                checkFailConditions(p, "TAKE_DAMAGE", "took damage");
            }
        }
    }
}