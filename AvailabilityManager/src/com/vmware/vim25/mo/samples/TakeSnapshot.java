package com.vmware.vim25.mo.samples;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class TakeSnapshot 
{
	long delay = 300*1000; // delay in ms : 10 * 1000 ms = 10 sec.
	LoopTask takeSnapshot = new LoopTask();
	Timer timer = new Timer();
	
	public void startSnapshot() 
	{
		timer.cancel();
	    timer = new Timer("startSnapshot");
	    Date now = new Date(); 
	    now.setTime(now.getTime() + 5000);
	    timer.scheduleAtFixedRate(takeSnapshot, now, delay);
	}
	
	public class LoopTask extends TimerTask 
	{
	    public void run() 
	    {
	        System.out.println("\nStarting the Snapshot process to backup cache.");
	        	    	
	        StartSnapshot snapnew = new StartSnapshot();
	        try 
	        {
	        		snapnew.snap("130.65.132.190", "administrator", "12!@qwQW", "Team07_Rashi", "create");
	        		snapnew.snap("130.65.132.190", "administrator", "12!@qwQW", "Team07_Rashi", "create");
	        		snapnew.snap("130.65.132.190", "administrator", "12!@qwQW", "Team07_Rashi", "remove");
				    //timer.cancel();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
}



