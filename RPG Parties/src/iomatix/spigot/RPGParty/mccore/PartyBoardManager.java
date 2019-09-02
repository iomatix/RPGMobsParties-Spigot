package iomatix.spigot.RPGParty.mccore;

import com.rit.sucy.scoreboard.PlayerBoards;
import com.rit.sucy.scoreboard.Board;
import com.rit.sucy.scoreboard.BoardManager;
import com.rit.sucy.scoreboard.StatHolder;
import com.rit.sucy.scoreboard.StatBoard;
import com.rit.sucy.config.CustomFilter;
import org.bukkit.entity.Player;
import iomatix.spigot.RPGParty.Parties;

public class PartyBoardManager
{
    public static void applyBoard(final Parties plugin, final Player player) {
        if (!plugin.isUsingScoreboard()) {
            return;
        }
        final String title = plugin.getMessage("Party.scoreboard-title", false, new CustomFilter[0]).get(0);
        final StatBoard board = new StatBoard(title, plugin.getName());
        board.addStats((StatHolder)new PartyStats(plugin, player, plugin.isLevelScoreboard()));
        final PlayerBoards boards = BoardManager.getPlayerBoards(player.getName());
        boards.removeBoards(plugin.getName());
        boards.addBoard((Board)board);
    }
    
    public static void clearBoard(final Parties plugin, final Player player) {
        if (!plugin.isUsingScoreboard()) {
            return;
        }
        BoardManager.getPlayerBoards(player.getName()).removeBoards(plugin.getName());
    }
    
    public static void clearBoards(final Parties plugin) {
        if (!plugin.isUsingScoreboard()) {
            return;
        }
        BoardManager.clearPluginBoards(plugin.getName());
    }
}
