package me.caleb.PlotGeneration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.caleb.PlotGeneration.command.PlotGen;
import me.caleb.PlotGeneration.utils.Chat;

public class Main extends JavaPlugin{
	
	private int port;
	public Connection connection;
	private String host, database, username, password;
	
	@Override
	public void onEnable() {
		mysqlSetup();
		loadConfig();
		new PlotGen(this);
	}
	
	public void loadConfig() {
		getConfig().options().copyDefaults(true);
		saveConfig();
	}
	
	public void mysqlSetup() {
		
		host = this.getConfig().getString("Host");
		port = this.getConfig().getInt("Port");
		password = this.getConfig().getString("Password");
		database = this.getConfig().getString("Database");
		username = this.getConfig().getString("Username");

		try {     
            openConnection();
                  
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
	}
	
	public Connection getConnection() {
		return connection;
	}
	
	@Override
	public void onDisable() {
		try {
			if(connection != null && !connection.isClosed()) {
				connection.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}	
	
	public void openConnection() throws SQLException, ClassNotFoundException {
	    if (connection != null && !connection.isClosed()) {
	        return;
	    }
	 
	    synchronized (this) {
	        if (connection != null && !connection.isClosed()) {
	            return;
	        } 
	        Class.forName("com.mysql.jdbc.Driver");
	        connection = DriverManager.getConnection("jdbc:mysql://" + this.host+ ":" + this.port + "/" + this.database, this.username, this.password);
	        Bukkit.getConsoleSender().sendMessage(Chat.chat("MYSQL CONNECTED!"));
	    }
	}
}
