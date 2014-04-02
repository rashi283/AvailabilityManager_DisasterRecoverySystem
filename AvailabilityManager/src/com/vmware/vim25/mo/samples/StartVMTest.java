package com.vmware.vim25.mo.samples;

import java.util.Date;
import java.util.Timer;
//import java.util.TimerTask;
import java.util.TimerTask;

//import com.vmware.vim25.mo.samples.SampleMyVM.LoopTask;

public class StartVMTest 
{
	long delay = 15*1000; // delay in ms : 10 * 1000 ms = 10 sec.
	LoopTask checkVMAlive = new LoopTask();
	Timer timer = new Timer();
	String vmCheck = null;
	
	public void startTesting() 
	{
		timer.cancel();
	    timer = new Timer("checkVMStatus");
	    Date now = new Date(); // no params = now
	    now.setTime(now.getTime() + 10000);
	    timer.scheduleAtFixedRate(checkVMAlive, now, delay);
	}
	
	public void setVmCheck (String value)
	{
		vmCheck = value;
	}
	
	public String getVmCheck () 
	{
		//System.out.println("vmCheck in getter" + vmCheck);
		return vmCheck;
	}
	
	public class LoopTask extends TimerTask 
	{
		//private String name;
		
	    public void run() 
	    {
	        System.out.println("\nPinging the VM to check it's health.");
	        
	        PingCommand command = new PingCommand();
	        vmCheck = command.ping("130.65.133.249");
	
	    	if (vmCheck == "true")
	        {
	    		System.out.println("\nCancelling timer as VM dead");
	        }
	    	timer.cancel();
	    }
	}
}

