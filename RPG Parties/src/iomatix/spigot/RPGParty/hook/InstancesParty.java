package iomatix.spigot.RPGParty.hook;

import java.util.ArrayList;
import java.util.List;
import com.rit.sucy.config.Filter;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import org.bukkit.OfflinePlayer;
import com.sucy.skill.SkillAPI;
import com.sucy.skill.api.enums.ExpSource;
import org.bukkit.entity.Player;
import org.cyberiantiger.minecraft.instances.Party;
import iomatix.spigot.RPGParty.Parties;
import iomatix.spigot.RPGParty.IParty;

public class InstancesParty implements IParty
{
	private ArrayList<Player> membersInRange;
    private Parties plugin;
    private Party party;
    private int nextId;
    
    public InstancesParty(final Parties plugin, final Party party) {
        this.nextId = -1;
        this.plugin = plugin;
        this.party = party;
    }
    
    public Party getParty() {
        return this.party;
    }
    
    @Override
    public Player getSequentialPlayer() {
        this.nextId = (this.nextId + 1) % this.party.getMembers().size();
        return this.party.getMembers().get(this.nextId);
    }
    
    @Override
    public Player getRandomPlayer() {
        return this.party.getMembers().get((int)(this.party.getMembers().size() * Math.random()));
    }
    
	@Override
	public int checkMembersInDistance(final Player source) {
		int i = 0;
        if (this.isEmpty()) {
            return i;
        }
		
		this.membersInRange.clear();
		for (final Player member : this.party.getMembers()) {
			if (member != null) {
				if ((this.plugin.getMaxDistance() >= member.getLocation().distance(
						source.getLocation()) && member.getWorld().getName().equals(source.getWorld().getName()))
						|| this.plugin.getMaxDistance() == -1) {
				this.membersInRange.add(member);
				++i;
			}
			}
		}
		return i;
	}
    
    
    @Override
    public boolean giveExp(final Player source, final double amount, final ExpSource expSource) {
    	boolean sharedDone = false;
        if (this.isEmpty()) {
            return sharedDone;
        }
        final int MembersCount = checkMembersInDistance(source);
        
        final double baseAmount = amount / (1.0 + ((MembersCount-1) * this.plugin.getMemberModifier()));
        final PlayerData data = SkillAPI.getPlayerData((OfflinePlayer)source);
        PlayerClass main = data.getMainClass();
        final int level = (main == null) ? 0 : main.getLevel();
        for (final Player member : this.membersInRange) {
            final PlayerData info = SkillAPI.getPlayerData((OfflinePlayer)member);
            main = info.getMainClass();
            final int lvl = (main == null) ? 0 : main.getLevel();
            int exp = (int)Math.ceil(baseAmount);
            if (this.plugin.getLevelModifier() > 0.0) {
                final int dl = lvl - level;
                exp = (int)Math.ceil(baseAmount * Math.pow(2.0, -this.plugin.getLevelModifier() * dl * dl));
            }
            info.giveExp((double)exp, expSource);
            sharedDone = true;
        
        
        }
        return sharedDone;
    }
    @Override
    public boolean giveMoney(final Player source, final double amount) {
    	boolean sharedDone = false;
        if (this.isEmpty()) {
            return sharedDone;
        }
        final int MembersCount = checkMembersInDistance(source);
        
        final double baseAmount = amount / (1.0 + ((MembersCount-1) * this.plugin.getMemberMoneyModifier()));
        final PlayerData data = SkillAPI.getPlayerData((OfflinePlayer)source);
        PlayerClass main = data.getMainClass();
        final int level = (main == null) ? 0 : main.getLevel();
        for (final Player member : this.membersInRange) {
            final PlayerData info = SkillAPI.getPlayerData((OfflinePlayer)member);
            main = info.getMainClass();
            final int lvl = (main == null) ? 0 : main.getLevel();
            double money = Math.ceil(baseAmount);
            if (this.plugin.getLevelMoneyModifier() > 0.0) {
                final int dl = lvl - level;
                money = baseAmount * Math.pow(2.0, -this.plugin.getLevelMoneyModifier() * dl * dl);
            }
            money = 0.01 + Math.round(money * 100)/100;
            Parties.Main.vaultmodule.DepositMoneyToPlayer(member, (double)money);
            sharedDone = true;
        	
        
        }
        return sharedDone;
        }
    
    
    
    @Override
    public void sendMessage(final Player sender, final String message) {
        final List<String> messages = this.plugin.getMessage("Party.chat-message", true, Filter.PLAYER.setReplacement(sender.getName()), Filter.MESSAGE.setReplacement(message));
        for (final String line : messages) {
            this.party.sendAll(line);
        }
    }
    
    @Override
    public boolean isEmpty() {
        return this.party.getMembers().size() == 0;
    }
}
