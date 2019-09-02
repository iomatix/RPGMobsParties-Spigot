package iomatix.spigot.RPGParty;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class UpdateTask extends BukkitRunnable
{
    Parties plugin;
    
    public UpdateTask(final Parties plugin) {
        this.runTaskTimer((Plugin)(this.plugin = plugin), 20L, 20L);
    }
    
    public void run() {
        this.plugin.update();
    }
}
