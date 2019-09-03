package iomatix.spigot.RPGParty.hook;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;

import iomatix.spigot.RPGParty.Parties;
import iomatix.spigot.rpgleveledmobs.mobscaling.MoneyScalingModule;
import net.milkbowl.vault.economy.Economy;

public class VaultHandler implements Listener {
	private Economy economy = null;
	private boolean VaultOnline = false;
	
	public VaultHandler() {
		
		Bukkit.getPluginManager().registerEvents((Listener) this, (Plugin) Parties.Main);
		RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServicesManager()
				.getRegistration(net.milkbowl.vault.economy.Economy.class);
		this.economy = economyProvider.getProvider();
		VaultOnline = true;
		
	}
	
	public Economy getVaultProvider() {
		return this.economy;
	}
	
	public boolean isVaultOnline() {
		return this.VaultOnline;
	}
	
	public void DepositMoneyToPlayer(OfflinePlayer player,double amount) {
		economy.depositPlayer(player,amount);
		try{
		MoneyScalingModule.SendMoneyMessageToPlayer(amount,(Player) player);
		}catch(Exception e) {}
	}
	
}
