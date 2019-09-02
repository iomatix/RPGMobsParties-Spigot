package iomatix.spigot.RPGParty.mccore;

import iomatix.spigot.RPGParty.Party;
import iomatix.spigot.RPGParty.inject.Server;
import java.util.ArrayList;
import org.bukkit.entity.Player;
import iomatix.spigot.RPGParty.Parties;
import com.rit.sucy.scoreboard.StatHolder;

public class PartyStats implements StatHolder
{
    private final Parties plugin;
    private final Player player;
    private final boolean level;
    
    public PartyStats(final Parties plugin, final Player player, final boolean level) {
        this.plugin = plugin;
        this.player = player;
        this.level = level;
    }
   
    public ArrayList<String> getNames() {
        final ArrayList<String> stats = new ArrayList<String>();
        if (this.player.isOnline()) {
            final Party pt = this.plugin.getParty(this.player);
            if (pt != null && !pt.isEmpty()) {
                for (final String member : pt.getMembers()) {
                    if (Server.isOnline(member)) {
                        stats.add(member);
                    }
                }
            }
        }
        return stats;
    }
    
    public ArrayList<Integer> getValues() {
        final ArrayList<Integer> stats = new ArrayList<Integer>();
        if (this.player.isOnline()) {
            final Party pt = this.plugin.getParty(this.player);
            if (pt != null && !pt.isEmpty()) {
                for (final String member : pt.getMembers()) {
                    if (this.level) {
                        Server.getLevel(member);
                    }
                    else {
                        stats.add((int)Math.ceil(Server.getPlayer(member).getHealth()));
                    }
                }
            }
        }
        return stats;
    }
}
