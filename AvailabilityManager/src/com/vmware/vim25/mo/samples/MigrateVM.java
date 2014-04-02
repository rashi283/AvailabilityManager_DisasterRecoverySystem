package com.vmware.vim25.mo.samples;

import java.net.URL;

import CONFIG.SJSULAB;

import com.vmware.vim25.HostVMotionCompatibility;
import com.vmware.vim25.TaskInfo;
import com.vmware.vim25.VirtualMachineMovePriority;
import com.vmware.vim25.VirtualMachinePowerState;
import com.vmware.vim25.mo.ComputeResource;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.HostSystem;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;

/**
 * http://vijava.sf.net
 * @author Steve Jin
 */

public class MigrateVM
{
  //public static void main(String[] args) throws Exception
  public void startMigrate(String vnameVal, String hostnameVal) throws Exception
  {
//    if(args.length!=5)
//    {
//      System.out.println("Usage: java MigrateVM <url> " +
//      "<username> <password> <vmname> <newhost>");
//      System.exit(0);
//    }

    String vmname =  vnameVal; //args[3];
    String newHostName = hostnameVal; //args[4];

    ServiceInstance si = new ServiceInstance(new URL(SJSULAB.getVmwareHostURL()), SJSULAB.getVmwareLogin(), SJSULAB.getVmwarePassword(), true);

    Folder rootFolder = si.getRootFolder();
    VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
        rootFolder).searchManagedEntity(
            "VirtualMachine", vmname);
    
    //Rashi
    //VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmname);
    HostSystem newHost = (HostSystem) new InventoryNavigator(
        rootFolder).searchManagedEntity(
            "HostSystem", newHostName);
    ComputeResource cr = (ComputeResource) newHost.getParent();
    
    String[] checks = new String[] {"cpu", "software"};
    HostVMotionCompatibility[] vmcs =
      si.queryVMotionCompatibility(vm, new HostSystem[] 
         {newHost},checks );
    
    String[] comps = vmcs[0].getCompatibility();
    if(checks.length != comps.length)
    {
      System.out.println("CPU/software NOT compatible. Exit.");
      si.getServerConnection().logout();
      return;
    }
    
    Task task = vm.migrateVM_Task(cr.getResourcePool(), newHost,
        VirtualMachineMovePriority.highPriority, VirtualMachinePowerState.poweredOff);
     //   VirtualMachinePowerState.poweredOn);
  
    if(task.waitForMe()==Task.SUCCESS)
    {
      System.out.println("The VM has been successfully migrated to the new Host");
    }
    else
    {
      System.out.println("VMotion failed!");
      TaskInfo info = task.getTaskInfo();
      System.out.println(info.getError().getFault());
    }
    si.getServerConnection().logout();
  }
}
