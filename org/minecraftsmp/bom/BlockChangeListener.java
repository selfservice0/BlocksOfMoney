package org.minecraftsmp.bom;

import java.util.List;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;

import org.bukkit.entity.Player;

import org.bukkit.Material;

import org.bukkit.block.Block;

import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;

import org.bukkit.configuration.ConfigurationSection;

import org.minecraftsmp.bom.BlocksOfMoney;

public class BlockChangeListener implements Listener {
	private BlocksOfMoney plugin;
	
	private double blockPrice;
	private int blocksUntilPay;
	
	private String[] players;
	private int[] breaks;
	
	private boolean useBlacklist, useWhitelist;
	private List<String> blockBlacklist;
	
	private boolean onBreak, onPlace;
	private boolean doChancedPay;
	
	private boolean doPayMessage;
	private String payMessage;
	
	private double chanceToPay;
	
	public BlockChangeListener(BlocksOfMoney plugin) {
		this.plugin = plugin;
		ConfigurationSection config = plugin.getConfig();
		
		blockPrice = config.getDouble("economy.worth_per_block");
		debug("Each block or successful roll is worth " + blockPrice + ".");
		
		onBreak = config.getBoolean("economy.on_block_break");
		if (onBreak) {debug("Configured to pay on block break.");} else {debug("Configured not to pay on block break.");}
		
		onPlace = config.getBoolean("economy.on_block_place");
		if (onPlace) {debug("Configured to pay on block place.");} else {debug("Configured not to pay on block place.");}
		
		doChancedPay = config.getBoolean("economy.chanced_payout");
		chanceToPay = config.getDouble("economy.percent_chance_to_pay");
		if (doChancedPay) {debug("Will pay on a " + chanceToPay + "% chance.");} else {debug("Will pay every block change.");}
		
		doPayMessage = config.getBoolean("lang.message_on_pay");
		payMessage = config.getString("lang.message").replace("&", "§").replace("%money%", (new Double(blockPrice)).toString());
		
		blocksUntilPay = config.getInt("economy.pay_after_blocks");
		
		useBlacklist = config.getBoolean("blacklist.use");
		useWhitelist = config.getBoolean("blacklist.use_as_whitelist");
		if (!useBlacklist) {useWhitelist = false;}
		if (config.isList("blacklist.blocks")) {
			blockBlacklist = config.getStringList("blacklist.blocks");
		} else {
			blockBlacklist = null;
		}
		
		players = new String[1];
		breaks = new int[1];
		players[0] = "_null_";
		breaks[0] = 0;
	}
	
	
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		debug("Block place event caught.");
		if (!onPlace) {
			debug("Will not react to this event.");
			return;
		}
		
		if (!triggerPay(event.getPlayer(), event.getBlock())) {
			return;
		}
		
		debug("Will pay out.");
		plugin.pay(event.getPlayer().getName(), blockPrice);
		if (doPayMessage) {
			event.getPlayer().sendRawMessage(payMessage);
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		debug("Block break event caught.");
		if (!onBreak) {
			debug("Will not react to this event.");
			return;
		}
		
		if (!triggerPay(event.getPlayer(), event.getBlock())) {
			return;
		}
		
		debug("Will pay out.");
		plugin.pay(event.getPlayer().getName(), blockPrice);
		if (doPayMessage) {
			event.getPlayer().sendRawMessage(payMessage);
		}
	}
	
	private boolean triggerPay(Player player, Block theBlock) {
		if (useWhitelist) {
			debug("Checking block whitelist.");			
			boolean passGo = false;
			for (String block : blockBlacklist) {
				block = block.toUpperCase();
				if (Material.getMaterial(block) == theBlock.getType()) {
					debug(theBlock.getType() + " was found in the whitelist.");
					passGo = true;
				}
			}
			if (!passGo) {
				debug(theBlock.getType() + " was not found on the whitelist.");
				return false;
			}
		} else if (useBlacklist) {
			debug("Checking block blacklist.");
			for (String block : blockBlacklist) {
				block = block.toUpperCase();
				if (Material.getMaterial(block) == theBlock.getType()) {
					debug(theBlock.getType() + " was found on the blacklist.");
					return false;
				}
			}
		}
		
		if (
			doChancedPay &&
			(
				chanceToPay < (Math.random() * 100)
			)
		) {
			debug("Roll failed, will not pay out.");
			return false;
		}
		if (!doChancedPay) {
			int index = ArrayHandler.getIn(players, player.getName());
			if (index == -1) {
				players = ArrayHandler.add(players, player.getName());
				breaks = ArrayHandler.add(breaks, 1);
				debug("Added entry for " + player.getName() + ".");
				index = ArrayHandler.getIn(players, player.getName());
			} else {
				breaks[index] += 1;
				debug(player.getName() + " has placed & broken " + breaks[index] + ".");
			}
			debug("Blocks needed to pay: " + blocksUntilPay);
			debug("Breaks by " + player.getName() + ": " + breaks[index]);
			if (blocksUntilPay > breaks[index]) {
				debug("Not enough breaks to pay.");
				return false;
			}
			debug("Resetting " + player.getName() + "'s block count.");
			breaks[index] = 0;
		}
		return true;
	}
	
	private void debug(String message) {
		plugin.debug(message);
	}
	
	private static class ArrayHandler {
		public static int getIn(String[] array, String obj) {
			if (array.length == 0)
			{return -1;}
			
			int index = -1;
			for (int i = 0; i < array.length; i++) {
				if (array[i].equals(obj)) {index = i;}
			}
			return index;
		}
		public static String[] add(String[] array, String newObject) {
			String[] newArray = new String[array.length + 1];
			for (int i = 0; i < array.length; i++) {
				newArray[i] = array[i];
			}
			newArray[array.length] = newObject;
			return newArray;
		}
		public static int[] add(int[] array, int newObject) {
			int[] newArray = new int[array.length + 1];
			for (int i = 0; i < array.length; i++) {
				newArray[i] = array[i];
			}
			newArray[array.length] = newObject;
			return newArray;
		}
	}
}
