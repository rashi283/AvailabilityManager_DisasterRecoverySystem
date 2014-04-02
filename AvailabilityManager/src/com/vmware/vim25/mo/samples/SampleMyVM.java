package com.vmware.vim25.mo.samples;
import CONFIG.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Thread.State;
import java.net.InetAddress;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.vmware.vim25.DuplicateName;
import com.vmware.vim25.GuestInfo;
import com.vmware.vim25.InvalidName;
import com.vmware.vim25.ManagedEntityStatus;
import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.RuntimeFault;
import com.vmware.vim25.VirtualMachineToolsRunningStatus;
import com.vmware.vim25.mo.*;
import com.vmware.vim25.mo.samples.alarm.*;
//import com.vmware.vim25.mo.Folder;
//import com.vmware.vim25.mo.HostSystem;
//import com.vmware.vim25.mo.InventoryNavigator;
//import com.vmware.vim25.mo.ServerConnection;
//import com.vmware.vim25.mo.ServiceInstance;
//import com.vmware.vim25.mo.Task;
//import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.*;
import com.vmware.vim25.mo.util.*;

/**
 * This is an example of simulation of Disaster Recovery System for Virtual Machines and Hosts.
 * This program provides the following features:
 * 1. Statistics about the VM , Host and Data Center
 * 2. Monitoring the health status of the VM and host and providing fail over backup
 * 3. Refresh the backup cache update every 10 minute.
 * 4. When a VM fails with ping heartbeat, then failover to another VM host/resource pool using VMDK image format (Cold migration)
 * 5. If the vHost is not alive, try to make it alive. If even after a fixed number of attempts, the vHost does not come up, remove the vHost from the list.
 * 6. Add a vHost to the vCenter if there exist only one vHost which is not alive.
 * 7. Setup alarm on VM power off. If a VM is powered off by a user, then it should be able to prevent a failover from occurring. (A VM is not failed in this case by powered off by a user)
 * 
 * @author Rashi Anil Agrawal 
 * @version 1.0
 */
public class SampleMyVM
{
    // Instance variables 
    private String vmname ;
    private ServiceInstance si ;
    private VirtualMachine vm ;
    
    //private ResourcePool rp;
    //private ManagedEntity rp;
    private VirtualMachine[] vmList;
    private HostSystem host;
    private HostSystem host2;
    private ManagedEntity vCenter;
    //private HostSystem[] hostList;
    static ManagedEntity[] hostList;
    
    private AlarmManager alarmMgr;
    private AlarmSpec spec;
    private String isVMDead; 
    private String isVHostDead;
  
    //Constructor for objects of class MyVM
    public SampleMyVM( String vmname ) 
    {
        // initialize instance variables
        try 
        {
            this.vmname = vmname ;
            this.si = new ServiceInstance(new URL(SJSULAB.getVmwareHostURL()), SJSULAB.getVmwareLogin(), SJSULAB.getVmwarePassword(), true);
            Folder rootFolder = si.getRootFolder();
            this.vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", this.vmname);
            
            //this.rp =  (ResourcePool) new InventoryNavigator(rootFolder).searchManagedEntity("ResourcePool", this.vmname);
            host = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem")[1];
            host2 = (HostSystem) new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem")[0];
            
            vCenter = new InventoryNavigator(rootFolder).searchManagedEntities("Datacenter")[0];
            
            hostList = new InventoryNavigator(rootFolder).searchManagedEntities("HostSystem");
            
            //Alarm
            this.alarmMgr = si.getAlarmManager();
            this.spec = new AlarmSpec();
            
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }

        if( this.vm==null)
        {
            System.out.println("No VM " + vmname + " found");
            if ( this.si != null)
                this.si.getServerConnection().logout();
        }
    }
    
//    public Folder getHostFolder() throws InvalidProperty, RuntimeFault, RemoteException  
//    {
//      return (Folder) this.getManagedObject("hostFolder");
//    }

    public static void main(String args[]) throws Exception
    {
    	final SampleMyVM myVM = new SampleMyVM("Team07_Rashi");
    	
    	//Statistics about the VM
    	System.out.println("Statistics about the VM");
    	System.out.println("vCenter : " + myVM.vCenter.getName());
    	System.out.println("System V Host : " + myVM.host.getName());
    	System.out.println("System V Host 2: " + myVM.host2.getName());
    	for(int i = 0; i < hostList.length ; i++ )
    		System.out.println("V-host in this V-center" + hostList[i].getName() ); 
    	System.out.println("Stats : "+ myVM.vm.getConfig());
    	
    	//Alarm Setting
//    	System.out.println("\nSetting the Alarm");
//    	VmPowerStateAlarm alarmObj = new VmPowerStateAlarm();
//    	alarmObj.setAlarm("130.65.132.191", "administrator", "12!@qwQW", "Team07_Rashi", "RashiAlarm");
    	
    	//Snapshot the VM every few mins and delete previous snapshots
//        TakeSnapshot newSnap = new TakeSnapshot();
//        newSnap.startSnapshot();
        
    	//Ping VM and check health Status
    	System.out.println("\nStarting VM health check");
    	
    	final Thread t1 = new Thread() 
    	{
    		public void run() 
    		{
    			//Check if VM is dead
    	    	StartVMTest vmtest = new StartVMTest();
    	    	vmtest.startTesting();
    	    	
    	        while(true)
    	    	{
    	        	myVM.isVMDead = vmtest.getVmCheck();
    	        	if (myVM.isVMDead == null || myVM.isVMDead == "false")
    	        	{	
    	        		//System.out.println("vmcheck is still null");
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
    		}
    	};
    	
    	//t1.start();
    	
        Thread t2 = new Thread()
        {
        	public void run()
        	{
        		//Check if Host is dead
        		System.out.println("Is VM still dead? : " + myVM.isVMDead);
                if(myVM.isVMDead == "true" || myVM.isVMDead == null)
                {
                	System.out.println("\n VM is already dead");
                	//myVM.wait(10000);
                	
                	StartVHostTest vhtest = new StartVHostTest();
                	vhtest.startTesting(myVM.host.getName());
                	
        	        while(true)
        	    	{
        	        	myVM.isVHostDead = vhtest.getVhCheck();
        	        	
        	        	if (myVM.isVHostDead == null || myVM.isVHostDead == "false")
        	        	{	
        	        		continue;
        	        	}
        		        else if (myVM.isVHostDead == "true") 
        		        {
        		        	System.out.println("\nHost is dead"); 
        		        	break;
        		        	//When Host is down, directly migrate the VM from one host to another
        		        }
        	    	}
                }
        	}
        };
        
        //myVM.powerOff();
        
        //t2.sleep(35000);
        //t2.start();
        
//        while(true)
//        {
//        	if(t1.isAlive() == true)
//        	{	
//        		System.out.println("t1 alive");
//        		continue;
//        	}	
//        	else
//        	{
//        		System.out.println("\nstarting t2 now");
//        		t2.start();
//        	}
//        }
    	
        //Migrate the VM
        System.out.println("\nStarting the migration process");
        MigrateVM migrateObj = new MigrateVM();
        migrateObj.startMigrate("Team07_Rashi", "130.65.132.191");
        
        //change host and migrate snapshot
//        VmSnapRevert revert = new VmSnapRevert(myVM.si.getServerConnection(), myVM.vm.getMOR());
//        System.out.println("starting revert");
//        revert.revertToSnapshot_Task(myVM.host2);
        
        //inventory view
                      
//        HostSystem hostSystem = new HostSystem(null, null);
//        this.vmList = hostSystem.getVms();
    		
    	//VirtualMachineToolsRunningStatus status = new VirtualMachineToolsRunningStatus();
               
        //add vHost
        
        //HostInventoryFull hostInv = new HostInventoryFull();
        
        //HostConfigInfo info = myVM.host.getConfig();
        //System.out.println("host cofig " + info.);
        
//        ClusterComputeResource newHost = new ClusterComputeResource(myVM.si.getServerConnection(), myVM.vm.getMOR());
//        HostConnectSpec spec = new HostConnectSpec();
//        spec.setHostName("130.65.132.193");
//        //spec.setPort(80);
//        spec.setPassword("12!@qwQW");
//        Boolean asConnected = true;
//        
//        //myVM.rp = myVM.vm.getResourcePool();
//        ResourcePool rp = myVM.vm.getResourcePool();
//        System.out.println("Resource Pool : " + rp);
//        //String license = "";
//        //newHost.addHost_Task(spec, asConnected, rp , null);
//        
        //remove vHost
        
        //Alarm
//        System.out.println("Set Alarm");
//        StateAlarmExpression expression = createStateAlarmExpression();
//        AlarmAction emailAction = createAlarmTriggerAction(createEmailAction());
//        AlarmAction methodAction = createAlarmTriggerAction(createPowerOnAction());
//        GroupAlarmAction gaa = new GroupAlarmAction();
//        
//        gaa.setAction(new AlarmAction[]{emailAction, methodAction});
//        myVM.spec.setAction(gaa);
//        myVM.spec.setExpression(expression);
//        myVM.spec.setName("VmPowerStateAlarm");
//        myVM.spec.setDescription("Monitor VM state and send email " + "and power it on if VM powers off");
//        myVM.spec.setEnabled(true);    
//        
//        AlarmSetting as = new AlarmSetting();
//        as.setReportingFrequency(0); //as often as possible
//        as.setToleranceRange(0);
//        
//        myVM.spec.setSetting(as);
//        System.out.println(myVM.vm);
//        //myVM.alarmMgr.createAlarm(myVM.host, myVM.spec);
//        myVM.alarmMgr.createAlarm(myVM.host, myVM.spec);
//        myVM.si.getServerConnection().logout();
    	
        
      //myVM.rename();
        //myVM.wait(15000);
        
        
        //myVM.si.getServerConnection().logout();
    //Rashi
    }
    
//    static StateAlarmExpression createStateAlarmExpression()
//    {
//      StateAlarmExpression expression = 
//        new StateAlarmExpression();
//      expression.setType("VirtualMachine");
//      expression.setStatePath("runtime.powerState");
//      expression.setOperator(StateAlarmOperator.isEqual);
//      expression.setRed("poweredOff");
//      return expression;
//    }
//    
//    static MethodAction createPowerOnAction() 
//    {
//      MethodAction action = new MethodAction();
//      action.setName("PowerOnVM_Task");
//      MethodActionArgument argument = new MethodActionArgument();
//      argument.setValue(null);
//      action.setArgument(new MethodActionArgument[] { argument });
//      return action;
//    }
//    
//    static AlarmTriggeringAction createAlarmTriggerAction(
//    	      Action action) 
//    	  {
//    	    AlarmTriggeringAction alarmAction = 
//    	      new AlarmTriggeringAction();
//    	    alarmAction.setYellow2red(true);
//    	    alarmAction.setAction(action);
//    	    return alarmAction;
//    	  }
//    
//    static SendEmailAction createEmailAction() 
//    {
//      SendEmailAction action = new SendEmailAction();
//      action.setToList("rashi283@gmail.com");
//      action.setSubject("Alarm - {alarmName} on {targetName}\n");
//      action.setBody("Description:{eventDescription}\n"
//          + "TriggeringSummary:{triggeringSummary}\n"
//          + "newStatus:{newStatus}\n"
//          + "oldStatus:{oldStatus}\n"
//          + "target:{target}");
//      return action;
//    }
//    
    //Power On the Virtual Machine
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
    public void powerOff() 
    {
        try {
            System.out.println("command: powered off");
            Task task = vm.powerOffVM_Task();
            if(task.waitForMe()==Task.SUCCESS)
            {
                System.out.println(vmname + " powered off");
            }
        } catch ( Exception e ) 
        { System.out.println( e.toString() ) ; }
    }

    //Reset the Virtual Machine
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
    
    /**
     * Destructor for objects of class MyVM
     */
    protected void finalize() throws Throwable
    {
        this.si.getServerConnection().logout(); //do finalization here
        super.finalize(); //not necessary if extending Object.
    } 
    
}


