package me.caleb.PlotGeneration;

public class Plot {
	public String world,plotName,owner,x,z;
	public Plot(String x, String z, String plotName, String world, String owner) {
		this.x = x;
		this.z = z;
		this.plotName = plotName;
		this.world = world;
		this.owner = owner;
	}

}
