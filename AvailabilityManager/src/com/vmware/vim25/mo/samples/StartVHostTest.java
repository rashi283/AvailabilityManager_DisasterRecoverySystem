package com.vmware.vim25.mo.samples;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class StartVHostTest
{
	long delay = 15*1000; // delay in ms : 10 * 1000 ms = 10 sec.
	LoopTask checkVHostAlive = new LoopTask();
	Timer timer = new Timer();
	String vhCheck = null;
	
	public void startTesting(String hostName) 
	{
		timer.cancel();
	    timer = new Timer("checkVMStatus");
	    Date now = new Date(); 
	    now.setTime(now.getTime() + 10000);
	    //String host = hostName;
	    timer.scheduleAtFixedRate(checkVHostAlive, now, delay);
	}
	    
	public void setVhCheck (String value)
	{
		vhCheck = value;
	}
	
	public String getVhCheck () 
	{
		return vhCheck;
	}
	
    public class LoopTask extends TimerTask 
	{	
	    public void run() 
	    {
	        System.out.println("\nPinging the VHost to check it's health.");
	        
	        PingCommand command = new PingCommand();
	    	if (command.ping("130.65.132.191") == "true")
	    	{	
	    		vhCheck = "true";
	    		//timer.cancel();
	    	}
	    	else if (command.ping("130.65.132.191") == "false")
	    	{
	    		vhCheck = "false";
	    	}
	    	timer.cancel();
	    	
//			    	isVMDead = true;
//			    	
//			    	if(isVMDead == true)
//			    	{	
//			    		System.out.println("VM is dead");
//			    		timer.cancel();
//			    	}
	    }
	}
}