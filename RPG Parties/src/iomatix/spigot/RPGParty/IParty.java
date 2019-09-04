package iomatix.spigot.RPGParty;

import com.sucy.skill.api.enums.ExpSource;

import org.bukkit.entity.Player;

public interface IParty
{
    Player getSequentialPlayer();
    
    Player getRandomPlayer();
    
    boolean giveExp(final Player p0, final double p1, final ExpSource p2);
    
    boolean giveMoney(final Player p0, final double p1);
    
    int checkMembersInDistance(final Player p0);
    
    void sendMessage(final Player p0, final String p1);
    
    boolean isEmpty();
}
