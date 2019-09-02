package iomatix.spigot.RPGParty.hook;

import iomatix.spigot.RPGParty.IParty;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import iomatix.spigot.RPGParty.Parties;

public class Hooks
{
    private static Parties parties;
    
    public static void init(final Parties plugin) {
        Hooks.parties = plugin;
        if (isInstancesActive()) {
            InstancesHook.init(plugin);
        }
    }
    
    public static boolean isInstancesActive() {
        return Bukkit.getPluginManager().getPlugin("Instances") != null;
    }
    
    public static IParty getParty(final Player player) {
        IParty party = null;
        if (isInstancesActive()) {
            party = InstancesHook.getParty(player);
        }
        if (party == null) {
            Hooks.parties.getJoinedParty(player);
        }
        return party;
    }
    
    public static void unload(final Player player) {
        if (isInstancesActive()) {
            InstancesHook.unload(player);
        }
    }
}
