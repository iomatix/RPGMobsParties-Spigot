package iomatix.spigot.RPGParty.command;

import iomatix.spigot.RPGParty.Party;
import com.rit.sucy.text.TextSizer;
import org.bukkit.ChatColor;
import com.rit.sucy.config.CustomFilter;
import org.bukkit.entity.Player;
import iomatix.spigot.RPGParty.Parties;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.IFunction;

public class CmdInfo implements IFunction
{
    public void execute(final ConfigurableCommand command, final Plugin plugin, final CommandSender sender, final String[] args) {
        final Parties parties = (Parties)plugin;
        final Player player = (Player)sender;
        final Party party = parties.getParty(player);
        if (party != null && party.isMember(player)) {
            final StringBuilder members = new StringBuilder();
            for (final String member : party.getMembers()) {
                members.append(member);
                members.append(", ");
            }
            parties.sendMessage(player, "Individual.info", new CustomFilter("{leader}", party.getLeader().getName()), new CustomFilter("{members}", members.substring(0, members.length() - 2)), new CustomFilter("{size}", party.getPartySize() + ""), new CustomFilter("{break}", TextSizer.createLine("", "-", ChatColor.DARK_GRAY)));
        }
        else {
            parties.sendMessage(player, "Errors.no-party", new CustomFilter[0]);
        }
    }
}
