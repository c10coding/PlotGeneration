package me.caleb.PlotGeneration.command;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;

import me.caleb.PlotGeneration.Main;

public class PlotGen implements CommandExecutor{

	private Main plugin;
	
	public PlotGen(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("plotgen").setExecutor(this);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//  /plotgen create (world) (name for plot)
		//  /plotgen list (world)
		//  /plotgen remove (name of plot)
		//  /plotgen help

		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to run this command!");
		}else {
			if(args[0].equalsIgnoreCase("create")) {
				
				String world = args[1];
				String name = args[2];
				
				Random rand = new Random();	
				Random rand2 = new Random();
				
				int y = rand.nextInt(5000);
				int x = rand.nextInt(5000);
				
				
			}
		}
		
		return false;
	}
	public WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		
		if(p instanceof WorldEditPlugin) return (WorldEditPlugin) p;
		else return null;
		
	}

}
