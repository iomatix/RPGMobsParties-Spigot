package iomatix.spigot.RPGParty.hook;

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
import iomatix.spigot.RPGParty.inject.Server;
import iomatix.spigot.RPGParty.IParty;

public class InstancesParty implements IParty
{
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
    public void giveExp(final Player source, final double amount, final ExpSource expSource) {
        if (this.isEmpty()) {
            return;
        }
        
        final double baseAmount = amount / (1.0 + (this.party.getMembers().size() - 1) * this.plugin.getMemberModifier());
        final PlayerData data = SkillAPI.getPlayerData((OfflinePlayer)source);
        PlayerClass main = data.getMainClass();
        final int level = (main == null) ? 0 : main.getLevel();
        for (final Player member : this.party.getMembers()) {
            final PlayerData info = SkillAPI.getPlayerData((OfflinePlayer)member);
            main = info.getMainClass();
            final int lvl = (main == null) ? 0 : main.getLevel();
            int exp = (int)Math.ceil(baseAmount);
            if (this.plugin.getLevelModifier() > 0.0) {
                final int dl = lvl - level;
                exp = (int)Math.ceil(baseAmount * Math.pow(2.0, -this.plugin.getLevelModifier() * dl * dl));
            }
            info.giveExp((double)exp, expSource);
        }
    }
    @Override
    public void giveMoney(final Player source, final double amount) {
        if (this.isEmpty()) {
            return;
        }
        final double baseAmount = amount / (1.0 + (this.party.getMembers().size() - 1) * this.plugin.getMemberModifier());
        

        final int level = (source == null) ? 0 : source.getLevel();
        for (final Player member : this.party.getMembers()) {
            final int lvl = (member == null) ? 0 : member.getLevel();
            int money = (int)Math.ceil(baseAmount);
            if (this.plugin.getLevelModifier() > 0.0) {
                final int dl = lvl - level;
                money = (int)Math.ceil(baseAmount * Math.pow(2.0, -this.plugin.getLevelModifier() * dl * dl));
            }
                Parties.Main.vaultmodule.DepositMoneyToPlayer(source, (double)money);
                
            }
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
