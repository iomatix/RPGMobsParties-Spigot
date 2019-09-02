package iomatix.spigot.RPGParty.command;

import iomatix.spigot.RPGParty.Party;
import com.rit.sucy.config.CustomFilter;
import org.bukkit.entity.Player;
import iomatix.spigot.RPGParty.Parties;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;

public class CmdMsg implements IFunction
{
    public void execute(final ConfigurableCommand command, final Plugin plugin, final CommandSender sender, final String[] args) {
        final Parties parties = (Parties)plugin;
        final Player player = (Player)sender;
        if (args.length == 0) {
            command.displayHelp(sender, 1);
            return;
        }
        final Party party = parties.getParty(player);
        if (party != null && !party.isEmpty()) {
            String text = args[0];
            for (int i = 1; i < args.length; ++i) {
                text = text + " " + args[i];
            }
            party.sendMessage(player, text);
        }
        else {
            parties.sendMessage(player, "Errors.no-party", new CustomFilter[0]);
        }
    }
}
