package org.minecraftsmp.bom;

import org.bukkit.plugin.java.JavaPlugin;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;

import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;

import org.bukkit.plugin.RegisteredServiceProvider;

import net.milkbowl.vault.economy.Economy;

public final class BlocksOfMoney extends JavaPlugin implements Listener {
	private double WORTH_PER_BLOCK;
	private static Economy economy = null;
	
	private boolean onBreak, onPlace;
	private boolean doDebug;
	private boolean doChancedPay;
	
	private double chanceToPay;
	
	public void onEnable() {
		if (!(Files.exists(Paths.get("plugins/BlocksOfMoney/config.yml")))) {
			this.saveDefaultConfig();
			getLogger().info("Generated fresh configuration file.");
		}
		
		doDebug = this.getConfig().getBoolean("internal.debug");
		debug("Debug messages are ON.");
		
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
		
		WORTH_PER_BLOCK = this.getConfig().getDouble("economy.worth_per_block");
		debug("Each block is worth " + WORTH_PER_BLOCK + ".");
		
		onPlace = this.getConfig().getBoolean("economy.on_block_place");
		debug("Payout on block place is " + onPlace + ".");
		onBreak = this.getConfig().getBoolean("economy.on_block_break");
		debug("Payout on block break is " + onBreak + ".");
		
		doChancedPay = this.getConfig().getBoolean("economy.chanced_payout");
		debug("Chanced payout is " + doChancedPay + ".");
		
		chanceToPay = this.getConfig().getDouble("economy.percent_chance_to_pay");
		debug("Chance to pay (decimal): " + chanceToPay + ".");
		
		getServer().getPluginManager().registerEvents(this, this);
		debug("Registered listeners.");
	}
	
	public void onDisable() {
		HandlerList.unregisterAll((Listener)this);
		debug("Unregistered all owned listeners.");
	}
	
	
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		debug("Block place event caught.");
		if (
			doChancedPay &&
			(
				chanceToPay < (Math.random() * 100)
			)
		) {
			debug("Roll failed, will not pay out.");
			return;
		}
		if (onPlace) {
			debug("Will pay out.");
			economy.bankDeposit(event.getPlayer().getName(), WORTH_PER_BLOCK);
		} else {
			debug("Will not react to this event.");
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		debug("Block break event caught.");
		if (
			doChancedPay &&
			(
				chanceToPay < (Math.random() * 100)
			)
		) {
			debug("Roll failed, will not pay out.");
			return;
		}
		if (onBreak) {
			debug("Will pay out.");
			economy.bankDeposit(event.getPlayer().getName(), WORTH_PER_BLOCK);
		} else {
			debug("Will not react to this event.");
		}
	}
	
	private boolean setupEconomy() {
	        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
	        if (rsp == null) {
			return false;
	        }
		economy = rsp.getProvider();
		return (economy != null);
	}
	
	private void debug(String message) {
		if (doDebug) {
			getLogger().info("Debug: " + message);
		}
	}
}
