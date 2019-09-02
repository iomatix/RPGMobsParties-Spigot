package iomatix.spigot.RPGParty;

import java.util.List;
import com.sucy.skill.api.player.PlayerClass;
import com.sucy.skill.api.player.PlayerData;
import com.sucy.skill.api.enums.ExpSource;
import iomatix.spigot.RPGParty.mccore.PartyBoardManager;
import com.rit.sucy.config.Filter;
import com.rit.sucy.config.CustomFilter;
import iomatix.spigot.RPGParty.inject.Server;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.ArrayList;

public class Party implements IParty
{
    private ArrayList<String> members;
    private HashMap<String, Long> invitations;
    private Parties plugin;
    private Player partyLeader;
    private int nextId;
    
    public Party(final Parties plugin, final Player leader) {
        this.members = new ArrayList<String>();
        this.invitations = new HashMap<String, Long>();
        this.nextId = -1;
        this.plugin = plugin;
        this.partyLeader = leader;
        this.members.add(this.partyLeader.getName());
    }
    
    public boolean isFull() {
        this.checkInvitations();
        return this.invitations.size() + this.members.size() >= this.plugin.getMaxSize();
    }
    
    @Override
    public boolean isEmpty() {
        this.checkInvitations();
        return this.invitations.size() + this.members.size() <= 1;
    }
    
    public Player getLeader() {
        return this.partyLeader;
    }
    
    @Override
    public Player getSequentialPlayer() {
        Player member;
        do {
            this.nextId = (this.nextId + 1) % this.members.size();
        } while ((member = Server.getPlayer(this.members.get(this.nextId))) == null);
        return member;
    }
    
    @Override
    public Player getRandomPlayer() {
        Player member;
        do {
            final int id = (int)(Math.random() * this.members.size());
            member = Server.getPlayer(this.members.get(id));
        } while (member == null);
        return member;
    }
    
    public void checkInvitations() {
        final String[] array;
        array = this.invitations.keySet().toArray(new String[this.invitations.size()]);
        for (final String member : array) {
            if (this.invitations.get(member) < System.currentTimeMillis()) {
                this.invitations.remove(member);
                final Player player = Server.getPlayer(member);
                if (player != null) {
                    this.sendMessages(this.plugin.getMessage("Party.no-response", true, Filter.PLAYER.setReplacement(player.getName())));
                    this.plugin.sendMessage(player, "Individual.no-response", new CustomFilter[0]);
                }
            }
        }
    }
    
    public int getPartySize() {
        return this.members.size();
    }
    
    public ArrayList<String> getMembers() {
        return this.members;
    }
    
    public int getOnlinePartySize() {
        int counter = 0;
        for (final String member : this.members) {
            if (Server.isOnline(member)) {
                ++counter;
            }
        }
        return counter;
    }
    
    public boolean isMember(final Player player) {
        return this.members.contains(player.getName());
    }
    
    public boolean isInvited(final Player player) {
        this.checkInvitations();
        return this.invitations.containsKey(player.getName());
    }
    
    public boolean isLeader(final Player player) {
        return this.partyLeader.equals(player);
    }
    
    public void invite(final Player player) {
        if (!this.members.contains(player.getName()) && !this.invitations.containsKey(player.getName())) {
            this.invitations.put(player.getName(), System.currentTimeMillis() + this.plugin.getInviteTimeout());
        }
    }
    
    public void accept(final Player player) {
        if (this.invitations.containsKey(player.getName())) {
            this.invitations.remove(player.getName());
            this.members.add(player.getName());
            if (this.members.size() == 2) {
                PartyBoardManager.applyBoard(this.plugin, this.getLeader());
            }
            PartyBoardManager.applyBoard(this.plugin, player);
        }
    }
    
    public void decline(final Player player) {
        if (this.invitations.containsKey(player.getName())) {
            this.invitations.remove(player.getName());
        }
    }
    
    public void removeMember(final Player player) {
        if (this.members.contains(player.getName())) {
            this.members.remove(player.getName());
        }
        if (this.isLeader(player) && this.members.size() > 0) {
            this.changeLeader();
        }
        PartyBoardManager.clearBoard(this.plugin, player);
    }
    
    public void changeLeader() {
        for (final String member : this.members) {
            if (Server.isOnline(member)) {
                this.partyLeader = Server.getPlayer(member);
                this.sendMessages(this.plugin.getMessage("Party.new-leader", true, Filter.PLAYER.setReplacement(this.partyLeader.getName())));
            }
        }
    }
    
    public void removeBoards() {
        for (final String member : this.members) {
            final Player player = Server.getPlayer(member);
            if (player != null) {
                PartyBoardManager.clearBoard(this.plugin, player);
            }
        }
    }
    
    @Override
    public void giveExp(final Player source, final double amount, final ExpSource expSource) {
        if (this.getOnlinePartySize() == 0) {
            return;
        }
        final double baseAmount = amount / (1.0 + (this.getOnlinePartySize() - 1) * this.plugin.getMemberModifier());
        final int level = Server.getLevel(source.getName());
        for (final String member : this.members) {
            final Player player = Server.getPlayer(member);
            if (player != null) {
                final PlayerData info = Server.getPlayerData(player);
                final PlayerClass main = info.getMainClass();
                final int lvl = (main == null) ? 0 : main.getLevel();
                int exp = (int)Math.ceil(baseAmount);
                if (this.plugin.getLevelModifier() > 0.0) {
                    final int dl = lvl - level;
                    exp = (int)Math.ceil(baseAmount * Math.pow(2.0, -this.plugin.getLevelModifier() * dl * dl));
                }
                info.giveExp((double)exp, expSource);
            }
        }
    }
    
    @Override
    public void giveMoney(final Player source, final double amount) {
        if (this.getOnlinePartySize() == 0) {
            return;
        }
        final double baseAmount = amount / (1.0 + (this.getOnlinePartySize() - 1) * this.plugin.getMemberModifier());
        final int level = Server.getLevel(source.getName());
        for (final String member : this.members) {
            final Player player = Server.getPlayer(member);
            if (player != null) {
                final PlayerData info = Server.getPlayerData(player);
                final PlayerClass main = info.getMainClass();
                final int lvl = (main == null) ? 0 : main.getLevel();
                int money = (int)Math.ceil(baseAmount);
                if (this.plugin.getLevelModifier() > 0.0) {
                    final int dl = lvl - level;
                    money = (int)Math.ceil(baseAmount * Math.pow(2.0, -this.plugin.getLevelModifier() * dl * dl));
                }
                Parties.Main.vaultmodule.DepositMoneyToPlayer(source, (double)money);
                
            }
        }
    
    }
    
    
    
    public void sendMessage(final String message) {
        for (final String member : this.members) {
            if (Server.isOnline(member)) {
                Server.getPlayer(member).sendMessage(message);
            }
        }
    }
    
    public void sendMessages(final List<String> messages) {
        for (final String member : this.members) {
            if (Server.isOnline(member)) {
                Server.getPlayer(member).sendMessage((String[])messages.toArray(new String[messages.size()]));
            }
        }
    }
    
    @Override
    public void sendMessage(final Player sender, final String message) {
        this.sendMessages(this.plugin.getMessage("Party.chat-message", true, Filter.PLAYER.setReplacement(sender.getName()), Filter.MESSAGE.setReplacement(message)));
    }
    
    public void clearBoard(final Player player) {
        PartyBoardManager.clearBoard(this.plugin, player);
        if (this.isEmpty()) {
            this.removeBoards();
        }
    }
    
    public void updateBoards() {
        this.removeBoards();
        for (final String member : this.members) {
            PartyBoardManager.applyBoard(this.plugin, Server.getPlayer(member));
        }
    }
}
