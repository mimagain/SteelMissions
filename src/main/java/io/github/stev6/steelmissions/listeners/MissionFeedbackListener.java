package io.github.stev6.steelmissions.listeners;

import io.github.stev6.steelmissions.SteelMissions;
import io.github.stev6.steelmissions.event.MissionProgressEvent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.audience.Audience;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MissionFeedbackListener implements Listener {

    private final SteelMissions plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MissionFeedbackListener(SteelMissions plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMissionProgress(MissionProgressEvent event) {
        if (event.isCancelled()) return;

        Player player = event.getPlayer();
        FileConfiguration config = plugin.getConfig();

        // --- 1. Audio Feedback (Fixed) ---
        if (config.getBoolean("feedback.sound.enabled", true)) {
            // Use the standard Minecraft key (e.g., "block.note_block.bell")
            String soundKey = config.getString("feedback.sound.sound", "block.note_block.hat");
            float volume = (float) config.getDouble("feedback.sound.volume", 0.5);
            float pitch = (float) config.getDouble("feedback.sound.pitch", 1.5);

            // This method accepts a String directly, supporting custom sounds and avoiding Enum errors
            player.playSound(player.getLocation(), soundKey, volume, pitch);
        }

        // --- 2. Visual Feedback (Action Bar) ---
        if (config.getBoolean("feedback.action_bar.enabled", true)) {
            String format = config.getString("feedback.action_bar.format", "<gold><mission>: <green><progress>/<requirement>");

            String missionName = event.getMission().getConfigID();
            // Ensure you are casting these to int/long to avoid "10.0" decimals if using doubles
            int progress = (int) event.getNewProgress();
            int requirement = (int) event.getMission().getRequirement();

            String message = format
                    .replace("<mission>", missionName)
                    .replace("<progress>", String.valueOf(progress))
                    .replace("<requirement>", String.valueOf(requirement));

            // Paper/Adventure API for Action Bar
            player.sendActionBar(miniMessage.deserialize(message));
        }
    }
}