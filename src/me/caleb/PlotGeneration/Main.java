package me.caleb.PlotGeneration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{
	
	private int port;
	private Connection connection;
	private String host, database, username, password;
	
	@Override
	public void onEnable() {
		saveDefaultConfig();
		
		host = this.getConfig().getString("Host");
		port = this.getConfig().getInt("Port");
		password = this.getConfig().getString("Password");
		database = this.getConfig().getString("Database");
		username = this.getConfig().getString("Username");

		//
		try {     
            openConnection();
            Statement statement = connection.createStatement();          
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
		
	}
	
	@Override
	public void onDisable() {}	
	
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
	    }
	}
}
