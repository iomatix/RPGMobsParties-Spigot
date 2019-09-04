package iomatix.spigot.RPGParty;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import com.rit.sucy.config.FilterType;
import java.util.List;
import com.rit.sucy.config.CustomFilter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.event.HandlerList;
import com.rit.sucy.config.parse.DataSection;
import com.rit.sucy.commands.CommandManager;
import iomatix.spigot.RPGParty.command.CmdToggle;
import iomatix.spigot.RPGParty.command.CmdMsg;
import iomatix.spigot.RPGParty.command.CmdLeave;
import iomatix.spigot.RPGParty.command.CmdInvite;
import iomatix.spigot.RPGParty.command.CmdInfo;
import iomatix.spigot.RPGParty.command.CmdDecline;
import iomatix.spigot.RPGParty.command.CmdAccept;
import iomatix.spigot.RPGParty.mccore.PartyBoardManager;
import iomatix.spigot.RPGParty.hook.Hooks;
import iomatix.spigot.RPGParty.hook.VaultHandler;

import com.rit.sucy.commands.IFunction;
import com.rit.sucy.commands.ConfigurableCommand;
import com.rit.sucy.commands.SenderType;
import com.rit.sucy.config.CommentedConfig;
import com.rit.sucy.config.CommentedLanguageConfig;
import java.util.ArrayList;
import org.bukkit.plugin.java.JavaPlugin;

public class Parties extends JavaPlugin {
	public static Parties Main;

	public VaultHandler vaultmodule;

	private void loadModules() {
		Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin) this, (Runnable) new Runnable() {
			@Override
			public void run() {
				if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
					Parties.this.vaultmodule = new VaultHandler();
				}
			}
		}, 40L);
	}

	private ArrayList<Party> parties;
	private ArrayList<String> toggled;
	private CommentedLanguageConfig language;
	private UpdateTask task;
	private String sharing;
	private boolean removeOnDc;
	private boolean newLeaderOnDc;
	private boolean leaderInviteOnly;
	private boolean useScoreboard;
	private boolean levelScoreboard;
	private boolean debug;
	private double memberModifier;
	private double levelModifier;
	private double memberModifierMoney;
	private double levelModifierMoney;
	private long inviteTimeout;
	private int maxSize;
	private double maxDistance;

	public Parties() {
		this.parties = new ArrayList<Party>();
		this.toggled = new ArrayList<String>();
	}

	public void onEnable() {
		(Parties.Main = this).loadModules();

		this.task = new UpdateTask(this);
		final CommentedConfig config = new CommentedConfig((JavaPlugin) this, "config");
		config.saveDefaultConfig();
		config.trim();
		config.checkDefaults();
		config.save();
		final DataSection settings = config.getConfig();
		this.language = new CommentedLanguageConfig((JavaPlugin) this, "language");
		this.sharing = settings.getString("item-sharing");
		this.removeOnDc = settings.getBoolean("remove-on-dc");
		this.newLeaderOnDc = settings.getBoolean("new-leader-on-dc");
		this.leaderInviteOnly = settings.getBoolean("only-leader-invites");
		this.useScoreboard = settings.getBoolean("use-scoreboard");
		this.levelScoreboard = settings.getBoolean("level-scoreboard");
		this.memberModifier = settings.getDouble("exp-modifications.members");
		this.levelModifier = settings.getDouble("exp-modifications.level");
		this.memberModifierMoney = settings.getDouble("money-modifications.members");
		this.levelModifierMoney = settings.getDouble("money-modifications.level");
		this.inviteTimeout = settings.getInt("invite-timeout") * 1000L;
		this.maxSize = settings.getInt("max-size");
		this.debug = settings.getBoolean("debug-messages");
		this.maxDistance = settings.getDouble("distance-sharing");
		new PartyListener(this);
		final ConfigurableCommand root = new ConfigurableCommand((JavaPlugin) this, "pt", SenderType.ANYONE);
		root.addSubCommands(new ConfigurableCommand[] {
				new ConfigurableCommand((JavaPlugin) this, "accept", SenderType.PLAYER_ONLY,
						(IFunction) new CmdAccept(), "Accepts a party request", "", "party.general"),
				new ConfigurableCommand((JavaPlugin) this, "decline", SenderType.PLAYER_ONLY,
						(IFunction) new CmdDecline(), "Declines a party request", "", "party.general"),
				new ConfigurableCommand((JavaPlugin) this, "info", SenderType.PLAYER_ONLY, (IFunction) new CmdInfo(),
						"Views party information", "", "party.general"),
				new ConfigurableCommand((JavaPlugin) this, "invite", SenderType.PLAYER_ONLY,
						(IFunction) new CmdInvite(), "Invites a player to a party", "<player>", "party.general"),
				new ConfigurableCommand((JavaPlugin) this, "leave", SenderType.PLAYER_ONLY, (IFunction) new CmdLeave(),
						"Leaves your party", "", "party.general"),
				new ConfigurableCommand((JavaPlugin) this, "say", SenderType.PLAYER_ONLY, (IFunction) new CmdMsg(),
						"Sends a message to your party", "<message>", "party.general"),
				new ConfigurableCommand((JavaPlugin) this, "toggle", SenderType.PLAYER_ONLY,
						(IFunction) new CmdToggle(), "Toggles party chat on/off", "", "party.general") });
		CommandManager.registerCommand(root);
		Hooks.init(this);
	}

	public void onDisable() {
		this.task.cancel();
		PartyBoardManager.clearBoards(this);
		HandlerList.unregisterAll((Plugin) this);
		this.parties.clear();
	}

	public double getMaxDistance() {
		return this.maxDistance;
	}

	public String getShareMode() {
		return this.sharing;
	}

	public boolean isRemoveOnDc() {
		return this.removeOnDc;
	}

	public boolean isNewLeaderOnDc() {
		return this.newLeaderOnDc;
	}

	public boolean isLeaderInviteOnly() {
		return this.leaderInviteOnly;
	}

	public boolean isUsingScoreboard() {
		return this.useScoreboard;
	}

	public boolean isLevelScoreboard() {
		return this.levelScoreboard;
	}

	public long getInviteTimeout() {
		return this.inviteTimeout;
	}

	public int getMaxSize() {
		return this.maxSize;
	}

	public double getMemberModifier() {
		return this.memberModifier;
	}

	public double getLevelModifier() {
		return this.levelModifier;
	}
	
	public double getMemberMoneyModifier() {
		return this.memberModifierMoney;
	}

	public double getLevelMoneyModifier() {
		return this.levelModifierMoney;
	}

	public boolean isDebug() {
		return this.debug;
	}

	public Party getJoinedParty(final Player player) {
		for (final Party party : this.parties) {
			if (party.isMember(player)) {
				return party;
			}
		}
		return null;
	}

	public Party getParty(final Player player) {
		for (final Party party : this.parties) {
			if (party.isMember(player) || party.isInvited(player)) {
				return party;
			}
		}
		return null;
	}

	public void addParty(final Party party) {
		this.parties.add(party);
	}

	public void removeParty(final Party party) {
		this.parties.remove(party);
	}

	public void update() {
		for (final Party party : this.parties) {
			party.checkInvitations();
		}
	}

	public boolean isToggled(final String playerName) {
		return this.toggled.contains(playerName.toLowerCase());
	}

	public void toggle(final String playerName) {
		if (this.isToggled(playerName)) {
			this.toggled.remove(playerName.toLowerCase());
		} else {
			this.toggled.add(playerName.toLowerCase());
		}
	}

	public List<String> getMessage(final String key, final boolean player, final CustomFilter... filters) {
		return (List<String>) this.language.getMessage(key, player, FilterType.COLOR, filters);
	}

	public void sendMessage(final Player target, final String key, final CustomFilter... filters) {
		this.language.sendMessage(key, (CommandSender) target, FilterType.COLOR, filters);
	}
}
