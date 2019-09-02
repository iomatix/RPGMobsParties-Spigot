package iomatix.spigot.RPGParty.command;

import iomatix.spigot.RPGParty.Party;
import com.rit.sucy.config.Filter;
import com.rit.sucy.config.CustomFilter;
import org.bukkit.entity.Player;
import iomatix.spigot.RPGParty.Parties;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;

public class CmdAccept implements IFunction
{
    public void execute(final ConfigurableCommand command, final Plugin plugin, final CommandSender sender, final String[] args) {
        final Parties parties = (Parties)plugin;
        final Player player = (Player)sender;
        final Party party = parties.getParty(player);
        if (party != null && party.isInvited(player)) {
            party.accept(player);
            party.sendMessages(parties.getMessage("Party.player-joined", true, Filter.PLAYER.setReplacement(player.getName())));
        }
        else {
            parties.sendMessage(player, "Errors.no-invites", new CustomFilter[0]);
        }
    }
}
