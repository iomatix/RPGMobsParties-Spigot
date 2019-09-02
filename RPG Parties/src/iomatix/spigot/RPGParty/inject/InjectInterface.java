package iomatix.spigot.RPGParty.inject;

import org.bukkit.OfflinePlayer;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class InjectInterface
{
    public boolean isOnline(final String playerName) {
        return this.getPlayer(playerName) != null;
    }
    
    public Player getPlayer(final String playerName) {
        return Bukkit.getPlayer(playerName);
    }
    
    public PlayerData getPlayerData(final Player player) {
        return SkillAPI.getPlayerData((OfflinePlayer)player);
    }
}
