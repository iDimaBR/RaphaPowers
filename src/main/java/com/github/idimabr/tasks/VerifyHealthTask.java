package com.github.idimabr.tasks;

import com.github.idimabr.RaphaPowers;
import com.github.idimabr.storage.SQLManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.Map;
import java.util.UUID;

public class VerifyHealthTask extends BukkitRunnable {
    private RaphaPowers plugin;

    public VerifyHealthTask(RaphaPowers plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        final Map<UUID, Long> timers = SQLManager.TIMERS;
        for (Map.Entry<UUID, Long> entry : timers.entrySet()) {
            final UUID uuid = entry.getKey();
            final Long time = entry.getValue();
            final long actualTime = System.currentTimeMillis();
            if(actualTime < time) continue;

            final Player player = Bukkit.getPlayer(uuid);
            final int health = (int) player.getMaxHealth();

            player.setMaxHealth(20);
            plugin.getConfig().getStringList("Messages.Health.Finished").stream().map($ -> $
                    .replace("&","§")
                    .replace("%life%",health+"")
            ).forEach(player::sendMessage);
            RaphaPowers.getPlugin().getSQL().delete(uuid);
            timers.remove(uuid);
        }
    }
}
