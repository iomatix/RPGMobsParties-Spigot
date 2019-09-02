package iomatix.spigot.RPGParty;

import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import iomatix.spigot.RPGParty.mccore.PartyBoardManager;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.EventPriority;
import com.sucy.skill.api.enums.ExpSource;
import com.sucy.skill.api.event.PlayerExperienceGainEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.EventHandler;
import iomatix.spigot.RPGParty.hook.Hooks;
import iomatix.spigot.rpgleveledmobs.events.RPGMobsGainExperience;
import iomatix.spigot.rpgleveledmobs.events.RPGMobsGainMoney;
import org.bukkit.entity.Projectile;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.Listener;

public class PartyListener implements Listener
{
    private Parties plugin;
    private boolean shared;
    private boolean sharedMoney;
    
    public PartyListener(final Parties plugin) {
        this.shared = false;
        this.sharedMoney = false;
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)plugin);
    }
    
    
    
    @EventHandler
    public void onDamage(final EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player target = (Player)event.getEntity();
            Player attacker = null;
            if (event.getDamager() instanceof Player) {
                attacker = (Player)event.getDamager();
            }
            else if (event.getDamager() instanceof Projectile) {
                final Projectile projectile = (Projectile)event.getDamager();
                if (projectile.getShooter() != null && projectile.getShooter() instanceof Player) {
                    attacker = (Player)projectile.getShooter();
                }
            }
            if (attacker != null) {
                final IParty targetParty = Hooks.getParty(target);
                final IParty attackerParty = Hooks.getParty(attacker);
                if (targetParty != null && targetParty == attackerParty) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onChat(final AsyncPlayerChatEvent event) {
        if (this.plugin.isToggled(event.getPlayer().getName())) {
            final IParty party = Hooks.getParty(event.getPlayer());
            if (party == null || party.isEmpty()) {
                this.plugin.toggle(event.getPlayer().getName());
                return;
            }
            event.setCancelled(true);
            party.sendMessage(event.getPlayer(), event.getMessage());
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRPGMobsExperience(final RPGMobsGainExperience event) {
            if (this.plugin.isDebug()) {
                this.plugin.getLogger().info("(RPGMOBS) Exp sharing - " + event.getEntity().getName());
            }
            if (this.shared) {
                return;
            }
                       
            final IParty party = Hooks.getParty(event.getEntity());
            if (this.plugin.isDebug()) {
                this.plugin.getLogger().info(event.getEntity().getName() + " has a party? " + (party != null));
            }
            if (party != null) {
                this.shared = true;
                event.setCancelled(true);
                party.giveExp(event.getEntity(), event.getExp(), ExpSource.MOB);    
                this.shared = false;
                if (this.plugin.isDebug()) {
                    this.plugin.getLogger().info("(RPGMOBS) Exp was shared!");
                }
            } 	
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onRPGMobsGetMoney(final RPGMobsGainMoney event) {
            if (this.plugin.isDebug()) {
                this.plugin.getLogger().info("(RPGMOBS) Money sharing - " + event.getEntity().getName());
            }
            if (this.sharedMoney) {
                return;
            }
                        
            final IParty party = Hooks.getParty(event.getEntity());
            if (this.plugin.isDebug()) {
                this.plugin.getLogger().info(event.getEntity().getName() + " has a party? " + (party != null));
            }
            if (party != null) {
                this.sharedMoney = true;
                event.setCancelled(true);
                party.giveMoney(event.getEntity(),event.getMoney());
                this.sharedMoney = false;
                if (this.plugin.isDebug()) {
                    this.plugin.getLogger().info("(RPGMOBS) Money was shared!");
                }
            } 	
    }
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onExpGain(final PlayerExperienceGainEvent event) {
    	if(Hooks.isRPGLeveledMobsActive() == true && event.getSource() == ExpSource.MOB) { event.setCancelled(true); return;}
        if (event.getSource() == ExpSource.COMMAND) {
            return;
        }
        if (this.plugin.isDebug()) {
            this.plugin.getLogger().info("Exp sharing - " + event.getPlayerData().getPlayerName());
        }
        if (this.shared) {
            return;
        }
        
        
        final IParty party = Hooks.getParty(event.getPlayerData().getPlayer());
        if (this.plugin.isDebug()) {
            this.plugin.getLogger().info(event.getPlayerData().getPlayerName() + " has a party? " + (party != null));
        }
        if (party != null) {
            this.shared = true;
            party.giveExp(event.getPlayerData().getPlayer(), event.getExp(), event.getSource());    
            event.setCancelled(true);
            this.shared = false;
            if (this.plugin.isDebug()) {
                this.plugin.getLogger().info("Exp was shared!");
            }
        }
    }
    
    
        

    
    @EventHandler
    public void onJoin(final PlayerJoinEvent event) {
        final Party party = this.plugin.getParty(event.getPlayer());
        if (party != null && !party.isEmpty()) {
            PartyBoardManager.applyBoard(this.plugin, event.getPlayer());
        }
    }
    
    @EventHandler
    public void onQuit(final PlayerQuitEvent event) {
        final Party party = this.plugin.getParty(event.getPlayer());
        if (party != null) {
            if (party.isInvited(event.getPlayer())) {
                party.decline(event.getPlayer());
            }
            else if (this.plugin.isRemoveOnDc()) {
                party.removeMember(event.getPlayer());
            }
            else if (this.plugin.isNewLeaderOnDc()) {
                party.changeLeader();
            }
            if (party.getOnlinePartySize() == 0) {
                this.plugin.removeParty(party);
            }
        }
    }
    
    @EventHandler
    public void onPickup(final EntityPickupItemEvent event) {
    	LivingEntity e = event.getEntity();
  
    	if(!(e instanceof Player)) return;
    	final Player thePlayer = (Player) e;
        final IParty party = Hooks.getParty(thePlayer);
        if (party != null) {
            final ItemStack item = event.getItem().getItemStack();
            final String mode = this.plugin.getShareMode().toLowerCase();
            if (mode.equals("sequential")) {
                final int count = item.getAmount();
                item.setAmount(1);
                for (int i = 0; i < count; ++i) {
                    party.getSequentialPlayer().getInventory().addItem(new ItemStack[] { item });
                }
            }
            else if (mode.equals("random")) {
                final int count = item.getAmount();
                item.setAmount(1);
                for (int i = 0; i < count; ++i) {
                    party.getRandomPlayer().getInventory().addItem(new ItemStack[] { item });
                }
            }
            else if (mode.equals("sequential-stack")) {
                party.getSequentialPlayer().getInventory().addItem(new ItemStack[] { item });
            }
            else {
                if (!mode.equals("random-stack")) {
                    return;
                }
                party.getRandomPlayer().getInventory().addItem(new ItemStack[] { item });
            }
            event.setCancelled(true);
            event.getItem().remove();
        }
    }
}
