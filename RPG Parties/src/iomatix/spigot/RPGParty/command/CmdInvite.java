package iomatix.spigot.RPGParty.command;

import com.rit.sucy.config.Filter;
import iomatix.spigot.RPGParty.Party;
import iomatix.spigot.RPGParty.inject.Server;
import com.rit.sucy.config.CustomFilter;
import org.bukkit.entity.Player;
import iomatix.spigot.RPGParty.Parties;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;

public class CmdInvite implements IFunction
{
    public void execute(final ConfigurableCommand command, final Plugin plugin, final CommandSender sender, final String[] args) {
        final Parties parties = (Parties)plugin;
        final Player player = (Player)sender;
        if (args.length == 0) {
            command.displayHelp(sender, 1);
            return;
        }
        if (args[0].equalsIgnoreCase(player.getName())) {
            parties.sendMessage(player, "Errors.no-invite-self", new CustomFilter[0]);
            return;
        }
        final Player target = Server.getPlayer(args[0]);
        if (target == null) {
            parties.sendMessage(player, "Errors.not-online", new CustomFilter[0]);
            return;
        }
        Party party = parties.getParty(player);
        if (party != null) {
            if (party.isFull()) {
                parties.sendMessage(player, "Errors.party-full", new CustomFilter[0]);
                return;
            }
            if (((Parties)plugin).isLeaderInviteOnly() && !party.isLeader(player)) {
                parties.sendMessage(player, "Errors.not-leader", new CustomFilter[0]);
                return;
            }
        }
        final Party targetParty = parties.getParty(target);
        if (targetParty != null && !targetParty.isEmpty()) {
            parties.sendMessage(player, "Errors.in-other-party", new CustomFilter[0]);
            return;
        }
        if (targetParty != null) {
            parties.removeParty(targetParty);
        }
        if (party == null) {
            party = new Party(parties, player);
            parties.addParty(party);
        }
        party.invite(target);
        party.sendMessages(parties.getMessage("Party.player-invited", true, Filter.PLAYER.setReplacement(target.getName())));
        parties.sendMessage(target, "Individual.invited", Filter.PLAYER.setReplacement(player.getName()));
    }
}
