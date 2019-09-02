package iomatix.spigot.RPGParty.hook;

import iomatix.spigot.RPGParty.IParty;
import org.cyberiantiger.minecraft.instances.Party;
import org.bukkit.entity.Player;
import java.util.HashMap;
import org.cyberiantiger.minecraft.instances.Instances;
import iomatix.spigot.RPGParty.Parties;

public class InstancesHook
{
    private static Parties parties;
    private static Instances instances;
    private static HashMap<Player, InstancesParty> playerMap;
    private static HashMap<Party, InstancesParty> conversion;
    
    public static void init(final Parties plugin) {
        InstancesHook.parties = plugin;
        InstancesHook.instances = (Instances)Instances.getPlugin((Class)Instances.class);
    }
    
    public static IParty getParty(final Player player) {
        InstancesParty watcher = InstancesHook.playerMap.get(player);
        if (watcher != null && !watcher.isEmpty()) {
            return watcher;
        }
        if (watcher != null) {
            InstancesHook.conversion.remove(watcher.getParty());
        }
        final Party party = InstancesHook.instances.getParty(player);
        if (party == null) {
            return null;
        }
        watcher = InstancesHook.conversion.get(party);
        if (watcher == null) {
            watcher = new InstancesParty(InstancesHook.parties, party);
            InstancesHook.conversion.put(party, watcher);
        }
        InstancesHook.playerMap.put(player, watcher);
        return watcher;
    }
    
    public static void unload(final Player player) {
        InstancesHook.playerMap.remove(player);
    }
    
    static {
        InstancesHook.playerMap = new HashMap<Player, InstancesParty>();
        InstancesHook.conversion = new HashMap<Party, InstancesParty>();
    }
}
