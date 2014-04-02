package com.vmware.vim25.mo.samples;

import java.net.URL;

import CONFIG.SJSULAB;

import com.vmware.vim25.ManagedObjectReference;
import com.vmware.vim25.VirtualMachineSnapshotInfo;
import com.vmware.vim25.VirtualMachineSnapshotTree;
import com.vmware.vim25.mo.Folder;
import com.vmware.vim25.mo.InventoryNavigator;
import com.vmware.vim25.mo.ServiceInstance;
import com.vmware.vim25.mo.Task;
import com.vmware.vim25.mo.VirtualMachine;
import com.vmware.vim25.mo.VirtualMachineSnapshot;

/**
 * http://vijava.sf.net
 * @author Steve Jin
 */

public class StartSnapshot 
{
  //public static void main(String[] args) throws Exception 
  public void snap(String urlVal, String usernameVal, String pwdVal, String vmnameVal, String opVal) throws Exception   
  {
//    if(args.length!=5)
//    {
//      System.out.println("Usage: java VMSnapshot <url> " +
//          "<username> <password> <vmname> <op>");
//      System.out.println("op - list, create, remove, " +
//          "removeall, revert");
//      System.exit(0);
//    }
	  
    String vmname = vmnameVal;
    String op = opVal;
    //please change the following three depending your op
    String snapshotname = "Snapshot in Time";
    String desc = "Snapshot in Time";
    boolean removechild = true;
    
    ServiceInstance si = new ServiceInstance(new URL(SJSULAB.getVmwareHostURL()), SJSULAB.getVmwareLogin(), 
    		SJSULAB.getVmwarePassword(), true);

    Folder rootFolder = si.getRootFolder();
    VirtualMachine vm = (VirtualMachine) new InventoryNavigator(
      rootFolder).searchManagedEntity("VirtualMachine", vmname);

    if(vm==null)
    {
      System.out.println("No VM " + vmname + " found");
      si.getServerConnection().logout();
      return;
    }

    if("create".equalsIgnoreCase(op))
    {
      //if(snapshotname == "test") 
      //{
	      //Task task0 = vm.removeAllSnapshots_Task();
    	  Task task = vm.createSnapshot_Task(snapshotname, desc, false, false);
	      if(task.waitForMe()==Task.SUCCESS)
	      {
	        System.out.println("Snapshot was created.");
	      }
      //}
    }
    else if("list".equalsIgnoreCase(op))
    {
      listSnapshots(vm);
    }
    else if(op.equalsIgnoreCase("revert")) 
    {
      VirtualMachineSnapshot vmsnap = getSnapshotInTree(
          vm, snapshotname);
      if(vmsnap!=null)
      {
        Task task = vmsnap.revertToSnapshot_Task(null);
        if(task.waitForMe()==Task.SUCCESS)
        {
          System.out.println("Reverted to snapshot:" 
              + snapshotname);
        }
      }
    }
    else if(op.equalsIgnoreCase("removeall")) 
    {
      Task task = vm.removeAllSnapshots_Task();      
      if(task.waitForMe()== Task.SUCCESS) 
      {
        System.out.println("Removed all snapshots");
      }
    }
    else if(op.equalsIgnoreCase("remove")) 
    {
      VirtualMachineSnapshot vmsnap = getSnapshotInTree(
          vm, snapshotname);
      if(vmsnap!=null)
      {
        Task task = vmsnap.removeSnapshot_Task(removechild);
        if(task.waitForMe()==Task.SUCCESS)
        {
          System.out.println("Removed snapshot:" + snapshotname);
        }
      }
    }
    else 
    {
      System.out.println("Invalid operation");
      return;
    }
    si.getServerConnection().logout();
  }
  
  static VirtualMachineSnapshot getSnapshotInTree(
      VirtualMachine vm, String snapName)
  {
    if (vm == null || snapName == null) 
    {
      return null;
    }

    VirtualMachineSnapshotTree[] snapTree = 
        vm.getSnapshot().getRootSnapshotList();
    if(snapTree!=null)
    {
      ManagedObjectReference mor = findSnapshotInTree(
          snapTree, snapName);
      if(mor!=null)
      {
        return new VirtualMachineSnapshot(
            vm.getServerConnection(), mor);
      }
    }
    return null;
  }

  static ManagedObjectReference findSnapshotInTree(VirtualMachineSnapshotTree[] snapTree, String snapName)
  {
    for(int i=0; i <snapTree.length; i++) 
    {
      VirtualMachineSnapshotTree node = snapTree[i];
      if(snapName.equals(node.getName()))
      {
        return node.getSnapshot();
      } 
      else 
      {
        VirtualMachineSnapshotTree[] childTree = 
            node.getChildSnapshotList();
        if(childTree!=null)
        {
          ManagedObjectReference mor = findSnapshotInTree(
              childTree, snapName);
          if(mor!=null)
          {
            return mor;
          }
        }
      }
    }
    return null;
  }

  static void listSnapshots(VirtualMachine vm)
  {
    if(vm==null)
    {
      return;
    }
    VirtualMachineSnapshotInfo snapInfo = vm.getSnapshot();
    VirtualMachineSnapshotTree[] snapTree = snapInfo.getRootSnapshotList();
    printSnapshots(snapTree);
  }

  static void printSnapshots(VirtualMachineSnapshotTree[] snapTree)
  {
    for (int i = 0; snapTree!=null && i < snapTree.length; i++) 
    {
      VirtualMachineSnapshotTree node = snapTree[i];
      System.out.println("Snapshot Name : " + node.getName());           
      VirtualMachineSnapshotTree[] childTree = node.getChildSnapshotList();
      if(childTree!=null)
      {
        printSnapshots(childTree);
      }
    }
  }
}