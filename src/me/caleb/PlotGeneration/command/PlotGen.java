package me.caleb.PlotGeneration.command;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.RemovalStrategy;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.caleb.PlotGeneration.Main;
import me.caleb.PlotGeneration.Plot;
import me.caleb.PlotGeneration.utils.Chat;

public class PlotGen implements CommandExecutor,Listener{

	private Main plugin;
	
	public WorldGuardPlugin wg = getWorldGuard();
	
	public WorldEditPlugin we = getWorldEdit();
	
	public String world,plotName,owner,x,z;

	public boolean confirm,isCalled;
	
	ProtectedCuboidRegion region;
	
	Plot p;
	
	Player senderP;
	
	public PlotGen(Main plugin) {
		this.plugin = plugin;
		plugin.getCommand("plotgen").setExecutor(this);
		Bukkit.getPluginManager().registerEvents(this,plugin);
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
				this.plotName = "Plot_" + sender.getName();
				this.owner = sender.getName();
				
				try {
					PreparedStatement stmt = plugin.getConnection().prepareStatement("SELECT * FROM `IslandInfo` WHERE owner=?");
					stmt.setString(1,owner);
					
					ResultSet rs = stmt.executeQuery();
					if(rs.next()) {
						sender.sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r You already have a plot! Aborting..."));
						return false;
					}else {
						plotValues();
						plotGeneration();
						return true;
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}else if(args[0].equalsIgnoreCase("home")) {
				teleportHome(sender);
			}else if(args[0].equalsIgnoreCase("delete")) {
				
				try {
					PreparedStatement stmt = plugin.getConnection().prepareStatement("SELECT * FROM `IslandInfo` WHERE owner=?");
					stmt.setString(1,owner);
					
					ResultSet rs = stmt.executeQuery();
					
					if(!(rs.next())) {
						
						sender.sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r You do not have a plot to delete!"));
						return false;
						
					}else {
						
						Player p = Bukkit.getPlayer(sender.getName());
						String pName = sender.getName();
						
						p.sendMessage(PlotGen.chat("Confirm that you want to delete your plot by typing Yes or Y. Otherwise, type No or N"));
						
						isCalled = true;
						senderP = Bukkit.getPlayer(sender.getName());
						return true;
						
					}
					
				} catch (SQLException e) {
					e.printStackTrace();
				}
	
			}else if(args[0].equalsIgnoreCase("help")) {
				List<String> list = plugin.getConfig().getStringList("Help");
				sender.sendMessage(chat("List of commands: "));
				for(String e : list) {
					sender.sendMessage(Chat.chat("&a " + e));
				}
			}
		}
		
		return false;
	}
	
	@EventHandler
	public void deleteConfirmation(PlayerChatEvent e) {
		
		//Bukkit.getConsoleSender().sendMessage("Player chat event!");
		//Bukkit.getConsoleSender().sendMessage("IsCalled: " + isCalled);
		if(isCalled == true) {
			//Bukkit.getConsoleSender().sendMessage("Confirm is true!");
			isCalled = false;
			if(e.getMessage().equalsIgnoreCase("Y") || e.getMessage().equalsIgnoreCase("Yes")) {
				deletePlot();
				confirm = true;
				//Bukkit.getConsoleSender().sendMessage("He said yes!");
				e.setCancelled(true);
				
			}else if (e.getMessage().equalsIgnoreCase("N") || e.getMessage().equalsIgnoreCase("No")){
				Player p = e.getPlayer();
				p.sendMessage(chat("Player did not confirm. Aborting..."));
				confirm = false;
				e.setCancelled(true);
			}else {
				Player p = e.getPlayer();
				p.sendMessage(chat("Player did not confirm. Aborting..."));
				confirm = false;
			}
		}
	}
	
	public void teleportHome(CommandSender sender) {
		Player p = Bukkit.getPlayer(sender.getName());
		String pName = sender.getName();
		
		int x,z;
		double y;
		String world = plugin.getConfig().getString("genWorld");
		
		try {
			PreparedStatement stmt = plugin.getConnection().prepareStatement("SELECT * FROM `IslandInfo` WHERE owner=?");
			stmt.setString(1, pName);
			
			ResultSet rs = stmt.executeQuery();
		
			while(rs.next()) {
				
				x = Integer.parseInt(rs.getString(2));
				y = plugin.getConfig().getInt("Level") + 0.5;
				z = Integer.parseInt(rs.getString(3));
				
				x+=4;
				z+=4;
				y+=1;
				
				p.sendMessage(chat("Teleporting you to your plot..."));
				
				World targetWorld = plugin.getServer().getWorld(world);
				
				Location loc = new Location(targetWorld,x,y,z);
				
				p.teleport(loc);
				
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	public static String chat(String s) {
		return Chat.chat("&l[&bPlot&aGen&r&l]&r " + s);
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
	
	public WorldGuardPlugin getWorldGuard() {
		Plugin p = Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
		
		if(p instanceof WorldGuardPlugin) return (WorldGuardPlugin) p;
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
			Bukkit.getConsoleSender().sendMessage("&l[&bPlot&aGen&r&l]&r This block is not air!");
			
			String name = getPlot().owner;
			try {
				PreparedStatement stmt = plugin.getConnection().prepareStatement("DELETE FROM `IslandInfo` WHERE plotName=?");
				stmt.setString(1,name);
				stmt.execute();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			while(!(m.equals(Material.AIR))) {
					
				plotValues();
				newX = Integer.parseInt(x);
				newZ = Integer.parseInt(z);
				
				loc = new Location(targetWorld,newX,height,newZ);
				
				b = loc.getBlock();
				m = b.getType();
				
				Bukkit.getConsoleSender().sendMessage("&l[&bPlot&aGen&r&l]&r This block is still not air! Generating new coordinates... " + m);
				
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
				e.printStackTrace();
			}
			
			Bukkit.getConsoleSender().sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r This block is air! Generating a plot of land..."));
			placing();
			
		}else {
			
			Bukkit.getConsoleSender().sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r This block is air! Generating a plot of land..."));
			placing();
			
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
				e.printStackTrace();
			}
		}
	}
	
	private void placing() {
		
		p = getPlot();
		int x = Integer.parseInt(p.x);
		int z = Integer.parseInt(p.z);
		String plotName = p.plotName;
		String plotWorld = p.world;
		World world = plugin.getServer().getWorld(plotWorld);
		String owner = p.owner;
		Player player = Bukkit.getPlayer(owner);
		int height = plugin.getConfig().getInt("Level");
		int size = plugin.getConfig().getInt("Size");
		
		player.sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r Generating plot..."));
		
		Location loc = new Location(world,x,height,z);
		Location firstLoc = new Location(world,x,height,z);
		Block b = loc.getBlock();
		
		b.setType(Material.GRASS_BLOCK);

		//Bukkit.getConsoleSender().sendMessage("New plot created at these coordinates: " + loc.getX() + " Y: " + loc.getY() + " Z: " + loc.getZ());
		
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
		
		//Protects the area
		
		int minX = Integer.parseInt(p.x);
		int minY = plugin.getConfig().getInt("Level");
		int minZ = Integer.parseInt(p.z);
		
		int maxX = minX + 21;
		int maxY = 256;
		int maxZ = minZ + 21;
		
		Location min = new Location(world,minX,minY,minZ);
		Location max = new Location(world,maxX,maxY,maxZ);
		
		BlockVector3 bv = BlockVector3.at(minX, minY, minZ);
		BlockVector3 bv2 = BlockVector3.at(maxX, maxY, maxZ);
		
		makeRegion(bv,bv2,targetPlayer, world);
		
	}
	
	public void makeRegion(BlockVector3 bv, BlockVector3 bv2, Player p, World world) {
		
		region = new ProtectedCuboidRegion("Plot_" + p.getName(),bv,bv2);
		//p.sendMessage("A new region has been made for you!");
		region.setPriority(10);
		
		DefaultDomain members = region.getMembers();
		
		LocalPlayer lp = wg.wrapPlayer(p);
		members.addPlayer(lp);
		
		region.setOwners(members);
		
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(world));
		regions.addRegion(region);
		
		region.setFlag(Flags.TNT, StateFlag.State.DENY);
		region.setFlag(Flags.PVP, StateFlag.State.DENY);
		p.sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r Welcome to your new plot! If you ever want to get back here, you can do &6/plotgen home"));
		//region.setFlag(Flags.GREET_MESSAGE, "Welcome to your new plot!");

	}
	
	public void deletePlot(){
		
		Player temp = senderP;
		temp.sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r Deleting plot..."));
		
		//Destroys the physical plot
		PreparedStatement stmt;
		
		try {
			
			stmt = plugin.getConnection().prepareStatement("SELECT * FROM `IslandInfo` WHERE owner=?");
			stmt.setString(1, temp.getName());
			
			ResultSet rs = stmt.executeQuery();	
			
			while(rs.next()) {
				
				int	x = Integer.parseInt(rs.getString(2)) + 1;
				int	y = plugin.getConfig().getInt("Level");
				int	z = Integer.parseInt(rs.getString(3)) + 1;
				World world = plugin.getServer().getWorld(plugin.getConfig().getString("genWorld"));
				Location startLoc = new Location(world,x,y,z);
				
				
				int maxX = startLoc.getBlockX() + 19;
				int maxY = startLoc.getBlockY() + 19;
				int maxZ = startLoc.getBlockZ() + 19;
				
				Location max = new Location(world,maxX,maxY,maxZ);
				
				temp.sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r Teleporting you back to spawn..."));
				temp.performCommand("spawn");
				
				for(int a = x; a<=maxX; a++) {
					for(int bb = y; bb<=maxY; bb++) {
						for(int c = z; c<=maxZ;c++) {
							Block b = startLoc.getWorld().getBlockAt(new Location(startLoc.getWorld(),a,bb,c));
							b.setType(Material.AIR);
						}
					}
				}
				
			}
			
			
		}catch (SQLException e){
			e.printStackTrace();
		}
		
		//Removes the region
		World world = plugin.getServer().getWorld(plugin.getConfig().getString("genWorld"));
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		RegionManager regions = container.get(BukkitAdapter.adapt(world));
		regions.removeRegion("Plot_" + temp.getName(), RemovalStrategy.UNSET_PARENT_IN_CHILDREN);
		
		//Removes the plot from the database
		try {
			stmt = plugin.getConnection().prepareStatement("DELETE FROM `IslandInfo` WHERE owner=?");
			stmt.setString(1, temp.getName());
			
			stmt.execute();
			Bukkit.getConsoleSender().sendMessage(Chat.chat("&l[&bPlot&aGen&r&l]&r Deleting the plot..."));
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

}
