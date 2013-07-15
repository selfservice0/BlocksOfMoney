package org.minecraftsmp.bom;

import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

import org.minecraftsmp.bom.BlockChangeListener;

public final class BlocksOfMoney extends JavaPlugin {
	private static Economy economy = null;
	private boolean doDebug;
	
	public void onEnable() {
		if (!(Files.exists(Paths.get("plugins/BlocksOfMoney/config.yml")))) {
			this.saveDefaultConfig();
			getLogger().info("Generated fresh configuration file.");
		}
		doDebug = this.getConfig().getBoolean("internal.debug");
		debug("Debug output enabled.");
		
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			getLogger().severe("Missing Vault!");
			getServer().getPluginManager().disablePlugin(this);
			return;
        	} else {
        		debug("Vault detected!");
        	}
		if (!setupEconomy()) {
			getLogger().severe("Missing a Vault Economy plugin!");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			debug("Vault Economy detected!");
		}
		
		getServer().getPluginManager().registerEvents(new BlockChangeListener(this), this);
		debug("Registered block place and block break listeners.");
	}
	
	public void onDisable() {
		HandlerList.unregisterAll(this);
		debug("Unregistered all owned listeners.");
	}
	
	
	
	private boolean setupEconomy() {
	        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	        if (rsp == null) {
			return false;
	        }
		economy = rsp.getProvider();
		return (economy != null);
	}
	
	public void debug(String message) {
		if (doDebug) {
			getLogger().info("Debug: " + message);
		}
	}
	
	public void pay(String player, double amount)
	{economy.bankDeposit(player, amount);}
}
