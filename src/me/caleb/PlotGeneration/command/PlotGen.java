package me.caleb.PlotGeneration.command;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.regions.CuboidRegion;

import me.caleb.PlotGeneration.Main;
import me.caleb.PlotGeneration.Plot;

public class PlotGen implements CommandExecutor{

	private Main plugin;
	
	public String world,plotName,owner,x,z;
	
	
	public PlotGen(Main plugin) {
		this.plugin = plugin;
		
		plugin.getCommand("plotgen").setExecutor(this);
	}
	
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//  /plotgen create (name for plot)
		//  /plotgen list (world)
		//  /plotgen remove (name of plot)
		//  /plotgen help

		if(!(sender instanceof Player)) {
			sender.sendMessage("You must be a player to run this command!");
		}else {
			if(args[0].equalsIgnoreCase("create")) {
				
				this.world = plugin.getConfig().getString("genWorld");
				this.plotName = sender.getName() + "Plot";
				this.owner = sender.getName();
				plotValues();
				plotGeneration();
				return true;
				
			}
		}
		
		return false;
	}
	
	public void plotValues() {
		//GENERATES VALUES
		
		Random rand = new Random();	
		int randZ = rand.nextInt(5000);
		int randX = rand.nextInt(5000);
		
		this.x = Integer.toString(randX);
		this.z = Integer.toString(randZ);
	}
	
	public WorldEditPlugin getWorldEdit() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
		
		if(p instanceof WorldEditPlugin) return (WorldEditPlugin) p;
		else return null;
		
	}
	
	public Plot getPlot() {
		Plot thisPlot = new Plot(x,z,plotName,world, owner);
		return thisPlot;
	}
	
	public void plotGeneration() {
		
		int height = plugin.getConfig().getInt("Level");
		int newX = Integer.parseInt(x);
		int newZ = Integer.parseInt(z);
		
		World targetWorld = plugin.getServer().getWorld(world);
		Location loc = new Location(targetWorld,newX,height,newZ);
		Block b = loc.getBlock();
		Material m = b.getType();
		
		if(!(m.equals(Material.AIR))) {
			Bukkit.getConsoleSender().sendMessage("This block is not air!");
			
			String name = getPlot().owner;
			try {
				PreparedStatement stmt = plugin.getConnection().prepareStatement("DELETE FROM `IslandInfo` WHERE plotName=?");
				stmt.setString(1,name);
				stmt.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while(!(m.equals(Material.AIR))) {
					
				plotValues();
				newX = Integer.parseInt(x);
				newZ = Integer.parseInt(z);
				
				loc = new Location(targetWorld,newX,height,newZ);
				
				b = loc.getBlock();
				m = b.getType();
				
				Bukkit.getConsoleSender().sendMessage("This block is still not air! " + m);
				
			}	
			
			try {
				//AFTER FINALLY FINDING A BLOCK OF AIR
				PreparedStatement stmt = plugin.getConnection().prepareStatement("INSERT INTO `IslandInfo`(x,z,plotName,world,owner) VALUES(?, ?, ?, ?, ?);");
				
				stmt.setString(1, x);
				stmt.setString(2, z);
				stmt.setString(3, plotName);
				stmt.setString(4, world);
				stmt.setString(5, owner);
				stmt.execute();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Bukkit.getConsoleSender().sendMessage("This block is air!");
			placing();
		}else {
			Bukkit.getConsoleSender().sendMessage("This block is air!");
			placing();
		}
	}
	
	private void placing() {
		
		Plot p = getPlot();
		int x = Integer.parseInt(p.x);
		int z = Integer.parseInt(p.z);
		String plotName = p.plotName;
		String plotWorld = p.world;
		World world = plugin.getServer().getWorld(plotWorld);
		String owner = p.owner;
		int height = plugin.getConfig().getInt("Level");
		int size = plugin.getConfig().getInt("Size");
		
		Location loc = new Location(world,x,height,z);
		Location firstLoc = new Location(world,x,height,z);
		Block b = loc.getBlock();
		BlockState state = b.getState();
		
		b.setType(Material.GRASS_BLOCK);

		Bukkit.getConsoleSender().sendMessage("This block should be grass at X: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
		
		Player targetPlayer = Bukkit.getPlayer(owner);
		
		int tempX = x + 4;
		int tempZ = z + 4;
		int tempHeight = height + 1;
		Location teleportLocation = new Location(world,tempX,tempHeight,tempZ);
		
		for(int i = 0;i<=19;i++) {
			x = Integer.parseInt(p.x);
			z++;
			for(int a = 0; a<=19;a++) {
				x++;
				loc = new Location(world,x,height,z);
				b = loc.getBlock();
				b.setType(Material.GRASS_BLOCK);
			}
		}
		
		Block b2 = firstLoc.getBlock();
		b2.setType(Material.AIR);
		
		targetPlayer.teleport(teleportLocation);
	}

}
