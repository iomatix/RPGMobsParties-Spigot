package iomatix.spigot.RPGParty.inject;

import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.entity.Player;

public class Server
{
    private static InjectInterface context;
    
    public static boolean isOnline(final String playerName) {
        return Server.context.isOnline(playerName);
    }
    
    public static Player getPlayer(final String playerName) {
        return Server.context.getPlayer(playerName);
    }
    
    public static PlayerData getPlayerData(final Player player) {
        return Server.context.getPlayerData(player);
    }
    
    public static PlayerClass getClass(final Player player) {
        return getPlayerData(player).getMainClass();
    }
    
    public static int getLevel(final String name) {
        if (isOnline(name)) {
            final PlayerClass playerClass = getClass(getPlayer(name));
            if (playerClass != null) {
                return playerClass.getLevel();
            }
        }
        return 0;
    }
    
    public static boolean hasClass(final Player player) {
        return getPlayerData(player).hasClass();
    }
    
    static void setContext(final InjectInterface injectedContext) {
        Server.context = injectedContext;
    }
    
    static {
        Server.context = new InjectInterface();
    }
}
