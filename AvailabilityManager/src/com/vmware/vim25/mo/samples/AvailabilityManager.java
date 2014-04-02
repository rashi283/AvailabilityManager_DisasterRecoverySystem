package com.vmware.vim25.mo.samples;
import CONFIG.*;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.RemoteException;

import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.HostConnectFault;
import com.vmware.vim25.HostConnectSpec;
import com.vmware.vim25.InvalidLogin;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.mo.*;

/**
 * This is an example of simulation of Disaster Recovery System for Virtual Machines and Hosts.
 * This program provides the following features:
 * 1. Statistics about the VM , Host and Data Center
 * 2. Monitoring the health status of the VM and host and providing fail over backup
 * 3. 
 * 
 * @author Rashi Anil Agrawal 
 * @version 1.0
 */
public class AvailabilityManager
{
    // Instance variables for construction of Object to access VM, host and other properties
	private VirtualMachine vm ;
	private String vmname ;
    private ServiceInstance si ;
    
    private ManagedEntity vCenter;
    private HostSystem host;
    private HostSystem host2;
    static ManagedEntity[] hostList;
    
    private String isVMDead; 
    private String isVHostDead;
    
    private Alarm alarm;
  
    //Constructor for objects of class AvailabilityManager
    public AvailabilityManager( String vmname ) 
    {
        // Initialize instance variables
        try 
        {
            this.vmname = vmname ;
            this.si = new ServiceInstance(new URL(SJSULAB.getVmwareHostURL()), SJSULAB.getVmwareLogin(), SJSULAB.getVmwarePassword(), true);
            Folder rootFolder = si.getRootFolder();
            this.vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", this.vmname);
            
            vCenter = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter")[0];
            
            host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem")[1];
            host2 = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem")[0];
            
            hostList = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
            
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }

        if( this.vm==null)
        {
            System.out.println("No VM " + vmname + " found");
            if ( this.si != null)
                this.si.getServerConnection().logout();
        }
    }
    
    public static void main(String args[]) throws Throwable
    {
    	final AvailabilityManager myVM = new AvailabilityManager("Team07_Rashi");
    	//BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		//char userInput;
    	
		//System.out.println("\nAt any point during execution, Enter q to quit AvailabilityManager!");
    	
		//Statistics about the VM
//    	myVM.getStats(myVM);
//    	
//    	//Setting the Alarm
//    	myVM.setAlarm();
//
    	//Snapshot the VM every 10 minutes and delete previous snapshots
		final Thread t1 = new Thread() 
    	{
    		public void run() 
    		{
    			myVM.snapshot();
    		}
    	};
    	t1.start();
    	
//    	final Thread t2 = new Thread() 
//    	{
//    		public void run() 
//    		{
//    			//Ping Host and check health Status
//        		myVM.isVHostDead = myVM.ping("130.65.132.191");
//    		}
//    	};
//    	//t2.start();
//    	
//    	final Thread t3 = new Thread() 
//    	{
//    		public void run() 
//    		{
//    			 //Ping VM and check health Status
//        		 myVM.isVMDead = myVM.ping(myVM.vm.getGuest().getIpAddress());
//    		}
//    	};
//    	//t3.start();
    	
    	try 
    	{
        	do
        	{     
        		//Start pinging the Host
        		myVM.isVHostDead = myVM.ping("130.65.132.191");
        		
        		//If Host is dead, perform Migration to another available host
//                if (myVM.isVHostDead == "true")
//                {            	
//                	//Turn off VM 
//                	myVM.powerOff(myVM);
//                	
//                	Thread.sleep(10000);
//                	
//                	//Migrate the VM
//                    myVM.migrate();
//                    
//                    Thread.sleep(30000);          
//                }
                
        		//Start pinging the VM
                myVM.isVMDead = myVM.ping(myVM.vm.getGuest().getIpAddress());
                
                //If VM is dead, try to Reset the VM , else revert to Snapshot.
                if(myVM.isVMDead == "true")
                {
                	//First Reset the VM
                	//myVM.reset();
                	
                	//Thread.sleep(30000);
                	
                	//Once the VM has been reset, ping the VM again to check if VM alive.
                	myVM.isVMDead = myVM.ping(myVM.vm.getGuest().getIpAddress());
                	
                	//If the VM is still not alive, revert to Snapshot from Cache Backup
                	if(myVM.isVMDead == "true")
                	{
                		myVM.revertToSnapshot();
                	}	
                }
                
                Thread.sleep(3000);
                //userInput = (char) br.read();
                //System.out.println("You entered : " + userInput);
                
        	}while (true);//(userInput != 'q');
        	//System.out.println("You have now quit the Availability Manager successfully");
    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ;}

        //add vHost     
    	//myVM.addHost(myVM);

    	//Logout and destroy the Object
//        myVM.si.getServerConnection().logout();
//        myVM.finalize();
    }
       
    //Power On the Virtual Machine
    @SuppressWarnings("deprecation")
	public void powerOn() 
    {
        try {
            System.out.println("command: powered on");
            Task task = vm.powerOnVM_Task(null);
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " powered on");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }

    //Power Off the Virtual Machine
    @SuppressWarnings("deprecation")
	public boolean powerOff(AvailabilityManager vmVal) 
    {
        try {
            AvailabilityManager myVM = vmVal;
            myVM.disableAlarm(myVM);
        	System.out.println("command: powered off");
            Task task = vm.powerOffVM_Task();
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " powered off");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
        return true;
    }

    //Reset the Virtual Machine
	@SuppressWarnings("deprecation")
	public void reset() 
    {
        try {
            System.out.println("command: reset");
            Task task = vm.resetVM_Task();
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " reset");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }

    //Suspend the Virtual Machine
    @SuppressWarnings("deprecation")
	public void suspend() 
    {
        try {
            System.out.println("command: suspend");
            Task task = vm.suspendVM_Task();
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " suspended");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }

    //Put VM & Guest OS on Standby
    public void standBy() 
    {
        try {
            System.out.println("command: stand by");
            vm.standbyGuest();
            System.out.println(vmname + " guest OS stoodby");
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }
    
    //Rename the VM
    @SuppressWarnings("deprecation")
	public void rename() throws IOException, DuplicateName, RuntimeFault, RemoteException
    {
    	Task task = vm.rename_Task("Team07_Renamed");
        String result = task.waitForMe();
        
        if(result == Task.SUCCESS)
        {
          System.out.println("The name has been successfully changed.");
        }
        else
        {
          System.out.println("The name cannot be changed.");
        }
    }
    
    public boolean getStats(AvailabilityManager vm)
    {
    	AvailabilityManager myVM = vm;
    	try
    	{
    		PrintWriter out = new PrintWriter(new FileWriter("C:\\outputfile.txt")); 
    		
    		out.println("\nStatistics about the VM, Host and DataCenter");
    		out.println("\nDataCenter   : " + myVM.vCenter.getName());
    		out.println("vHost 1      : " + myVM.host.getName());
    		out.println("vHost 2      : " + myVM.host2.getName());
    		out.println("Guest OS     : " + myVM.vm.getGuest().guestFullName);
    		out.println("VM Version   : " + myVM.vm.getConfig().version);
        	Datastore[] ds = myVM.vm.getDatastores();
        	for(int i = 0; i < ds.length ; i++)
        		out.println("Datastore    : " + ds[i].getName());
        	out.println("Tools Status : " + myVM.vm.getGuest().getToolsVersionStatus());
        	out.println("DNS name     : " + myVM.vm.getGuest().hostName);
        	out.println("Host IP      : " + myVM.vm.getRuntime());
        	out.println("State        : " + myVM.vm.getGuest().guestState);
        	
        	out.close();
        	System.out.println("Statistics File successfully created at : C:\\outputfile.txt");
    	}
    	catch (Exception e)
    	{System.out.println( e.toString() ) ; }
    	return true;

    }
    //Set Alarm for VM for fail-over power off.
    public boolean setAlarm()
    {
    	try
    	{
    		System.out.println("\nSetting the Alarm");
        	VmPowerStateAlarm alarmObj = new VmPowerStateAlarm();
        	alarmObj.setAlarm("130.65.132.191", "administrator", "12!@qwQW", "Team07_Rashi", "VMFailoverAlarm");
    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ; }
    	return true;
    }
    
    public boolean disableAlarm(AvailabilityManager vm)
    {
    	AvailabilityManager myVM = vm;
    	VmPowerStateAlarm alarmObj = new VmPowerStateAlarm();
    	try
    	{
    		 AlarmManager alarmMgr = si.getAlarmManager();
    		 com.vmware.vim25.mo.Alarm[] alarms = alarmMgr.getAlarm(myVM.vm);
    	     for (int i = 0; i < alarms.length; i++) 
    	     {
    	    	 if (alarms[i].getAlarmInfo().getName().equalsIgnoreCase("VMFailoverAlarm")) 
    	    	 {
    	    		 myVM.alarm = alarms[i];
    	    	 }
    	     }
    	     alarmObj.disableAlarm(myVM.alarm);
    	}
    	catch(Exception e)
    	{ System.out.println( e.toString() ) ; }
    	return true;
    }
    
    public boolean enableAlarm(AvailabilityManager vm)
    {
    	AvailabilityManager myVM = vm;
    	VmPowerStateAlarm alarmObj = new VmPowerStateAlarm();
    	try
    	{
    		 AlarmManager alarmMgr = si.getAlarmManager();
    		 com.vmware.vim25.mo.Alarm[] alarms = alarmMgr.getAlarm(myVM.vm);
    	     for (int i = 0; i < alarms.length; i++) 
    	     {
    	    	 if (alarms[i].getAlarmInfo().getName().equalsIgnoreCase("VMFailoverAlarm")) 
    	    	 {
    	    		 myVM.alarm = alarms[i];
    	    	 }
    	     }
    	     alarmObj.enableAlarm(myVM.alarm);
    	}
    	catch(Exception e)
    	{ System.out.println( e.toString() ) ; }
    	return true;
    }
    
    //Create Snapshot of the VM every 10 mins and delete previous snapshots.
    public boolean snapshot()
    {
    	try
    	{
    		TakeSnapshot newSnap = new TakeSnapshot();
            newSnap.startSnapshot();
    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ; }
    	return true;
    }
    
    //Ping the VM to check its health
    public String pingVM(AvailabilityManager vm)
    {
    	AvailabilityManager myVM = vm;
    	try
    	{
    		System.out.println("\nStarting VM health check");
  
    		//Check if VM is dead
	    	StartVMTest vmtest = new StartVMTest();
	    	vmtest.startTesting();
	    	
	        while(true)
	    	{
	        	myVM.isVMDead = vmtest.getVmCheck();
	        	if (myVM.isVMDead == null || myVM.isVMDead == "false")
	        	{	
	        		continue;
	        	}
		        else if (myVM.isVMDead == "true") 
		        {
		        	System.out.println("\nVM is dead"); 
		        	//Revive VM 
		            //myVM.reset();
		        	//Power off and power on
		        	//Revert to snapshot
		        	break;
		        }
	    	}
	        //return myVM.isVMDead;
    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ; }
		return myVM.isVMDead;
    }
    
    //Ping the Host to check its health
    public String pingHost(AvailabilityManager vm)
    {
    	AvailabilityManager myVM = vm;
    	try
    	{
    		//Start pinging the Host and check if Host is dead.
        	StartVHostTest vhtest = new StartVHostTest();
        	vhtest.startTesting(myVM.host.getName());
        	
	        while(true)
	    	{
	        	myVM.isVHostDead = vhtest.getVhCheck();
	        	
	        	//Ping will respond false, if Host is alive
	        	if (myVM.isVHostDead == null || myVM.isVHostDead == "false")
	        	{	
	        		continue;
	        	}
	        	//Ping will respond True if Host is dead
		        else if (myVM.isVHostDead == "true") 
		        {
		        	System.out.println("\nHost is dead"); 
		        	break;
		        }
	    	}

    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ; }
    	return myVM.isVHostDead;
    }
    
    public void revertToSnapshot()
    {
    	try
    	{
    		System.out.println("\nStarting revert");
        	StartSnapshot snapnew = new StartSnapshot();
        	snapnew.snap("130.65.132.190", "administrator", "12!@qwQW", "Team07_Rashi", "revert");
    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ; }
    }
    
    //Migrate the VM to another healthy host.
    public boolean migrate()
    {
    	try
    	{
    		System.out.println("\nStarting the migration process");
            MigrateVM migrateObj = new MigrateVM();
            migrateObj.startMigrate("Team07_Rashi", "130.65.132.191");
    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ; }
    	return true;
    }
    
    public String ping(String ipVal)
    {
    	String ip = ipVal;
		String pingResult = "";
		String checkIfDead = null;
		
		String pingCmd = "ping " + ip;
    	try
    	{
    		Runtime r = Runtime.getRuntime();
            Process p = r.exec(pingCmd);

            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) 
            {
                //System.out.println(inputLine);
                pingResult += inputLine;
            }
            in.close();
            
            //Checking if VM or Host is dead
            if(pingResult.contains("Request timed out.Request timed out.Request timed out.Request timed out.") == true 
          		  || pingResult.contains("unreachable") == true || ipVal == null)
            {
            	checkIfDead = "true";
            	System.out.println("Ping Failure for : 130.65.133.249 . Entity Dead");
            }
            else
            {
            	checkIfDead = "false";
            	System.out.println("Ping Success for : " + ipVal + " Entity Alive");
            }
    	}
    	catch (Exception e)
    	{ System.out.println( e.toString() ) ; }
    	return checkIfDead;
    }
    
    public void addHost(AvailabilityManager vm) throws InvalidLogin, HostConnectFault, RuntimeFault, RemoteException
    {
    	AvailabilityManager myVM = vm;
    	ClusterComputeResource newHost = new ClusterComputeResource(myVM.si.getServerConnection(), myVM.vm.getMOR());
        HostConnectSpec spec = new HostConnectSpec();
        spec.setHostName("130.65.132.193");
        spec.setPort(80);
        spec.setPassword("12!@qwQW");
        Boolean asConnected = true;
     
        ResourcePool rp = myVM.vm.getResourcePool();
        System.out.println("Resource Pool : " + rp);
        newHost.addHost_Task(spec, asConnected, rp , null);
    }
    
    /**
     * Destructor for objects of class AvailabilityManager
     */
    protected void finalize() throws Throwable
    {
        this.si.getServerConnection().logout(); //do finalization here
        super.finalize(); //not necessary if extending Object.
    } 
    
}


