/*
 * SteelMissions – A Minecraft missions plugin.
 * Copyright (C) 2025 Stev6
 */

package io.github.stev6.steelmissions.listeners;

import io.github.stev6.steelmissions.SteelMissions;
import io.github.stev6.steelmissions.config.records.MainConfig;
import io.github.stev6.steelmissions.event.MissionProgressEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public record MissionFeedbackListener(SteelMissions plugin) implements Listener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProgress(MissionProgressEvent e) {
        MainConfig.Feedback config = plugin.getConfigManager().getMainConfig().feedback();

        // 1. Audio Feedback
        if (config.soundEnabled()) {
            // Using String-based playSound to avoid Enum deprecation/issues and allow custom sounds
            e.getPlayer().playSound(
                    e.getPlayer().getLocation(),
                    config.soundKey(),
                    config.soundVolume(),
                    config.soundPitch()
            );
        }

        // 2. Visual Feedback (Action Bar)
        if (config.actionBarEnabled()) {
            // We use the NEW progress from the event, not the old one in the mission object
            TagResolver resolver = TagResolver.resolver(
                    Placeholder.unparsed("mission", e.getMission().getConfigID()),
                    Placeholder.unparsed("progress", String.valueOf(e.getNewProgress())),
                    Placeholder.unparsed("requirement", String.valueOf(e.getMission().getRequirement()))
            );

            e.getPlayer().sendActionBar(
                    MiniMessage.miniMessage().deserialize(config.actionBarFormat(), resolver)
            );
        }
    }
}