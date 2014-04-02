package com.vmware.vim25.mo.samples;

import java.net.URL;
import java.rmi.RemoteException;

import CONFIG.SJSULAB;

import com.vmware.vim25.*;
import com.vmware.vim25.mo.*;
import com.vmware.vim25.mo.util.*;

/**
*<pre>
*This is a sample which creates an Alarm to monitor the virtual
*machine's power state
*
*<b>Parameters:</b>
*vmname     [required] Name of the virtual machine
*alarm      [required] Name of the alarms
*
*</pre>
* @author sjin
*/

public class VmPowerStateAlarm 
{

  //public static void main(String[] args) throws Exception 
  public void setAlarm(String urlVal, String usrVal, String pwdVal, String vnameVal, String alarm) throws Exception	
  {
      //CommandLineParser clp = new CommandLineParser(constructOptions(), args);
//       String urlStr = clp.get_option("url");
//        String username = clp.get_option("username");
//      String password = clp.get_option("password");
//      String vmname = clp.get_option("vmname");
//      String alarmName = clp.get_option("alarm");
	  String urlStr = urlVal;
      String username = usrVal;
      String password = pwdVal;
      String vmname = vnameVal;
      String alarmName = alarm;
     
    ServiceInstance si = new ServiceInstance(new URL(SJSULAB.getVmwareHostURL()), SJSULAB.getVmwareLogin(), SJSULAB.getVmwarePassword(), true);
    AlarmManager am = si.getAlarmManager();
    Folder rootFolder = si.getRootFolder();
    VirtualMachine vm = (VirtualMachine) new InventoryNavigator(rootFolder).searchManagedEntity("VirtualMachine", vmname);
    
    if(vm!=null && am!=null)
    {
      StateAlarmExpression expression = createStateAlarmExpression();
      MethodAction methodAction = createPowerOnAction();
      AlarmAction alarmAction = (AlarmAction) createAlarmTriggerAction(methodAction);
      AlarmSpec alarmSpec = createAlarmSpec(alarmName, alarmAction, expression);
      
      try
      {
        am.createAlarm(vm, alarmSpec);
        System.out.println("Successfully created Alarm: " + alarmName);
      }
      catch(InvalidName in) 
      {
        System.out.println("Alarm name is empty or too long");
      }
      catch(DuplicateName dn)
      {
        System.out.println("Alarm with the name already exists");
      }
      catch(RemoteException re)
      {
        re.printStackTrace();
      }
    }
    else 
    {
      System.out.println("Either VM is not found or Alarm Manager is not available on this server.");
    }
    si.getServerConnection().logout();
  }
  
  static StateAlarmExpression createStateAlarmExpression()
  {   
    StateAlarmExpression sae = new StateAlarmExpression();
    sae.setOperator(StateAlarmOperator.isEqual);
    sae.setRed("poweredOff");
    sae.setYellow(null);
    sae.setStatePath("runtime.powerState");
    sae.setType("VirtualMachine");
    return sae;
  }
   
  static MethodAction createPowerOnAction() 
  {
     MethodAction action = new MethodAction();
     action.setName("PowerOnVM_Task");
     MethodActionArgument argument = new MethodActionArgument();
     argument.setValue(null);
     action.setArgument(new MethodActionArgument[] { argument });
     return action;
  }
   
   static AlarmTriggeringAction createAlarmTriggerAction(MethodAction methodAction) throws Exception 
   {
      AlarmTriggeringAction alarmAction = new AlarmTriggeringAction();
      alarmAction.setYellow2red(true);
      alarmAction.setAction(methodAction);
      return alarmAction;
   }
   
   static AlarmSpec createAlarmSpec(String alarmName, AlarmAction action, AlarmExpression expression) throws Exception 
   {      
     AlarmSpec spec = new AlarmSpec();
     spec.setAction(action);
     spec.setExpression(expression);
     spec.setName(alarmName);
     spec.setDescription("Monitor VM state and send email if VM power's off");
     spec.setEnabled(true);      
     return spec;
   }

  private static OptionSpec[] constructOptions() 
  {
    OptionSpec [] useroptions = new OptionSpec[2];
    useroptions[0] = new OptionSpec("vmname", "String", 1, "Name of the virtual machine", null);
    useroptions[1] = new OptionSpec("alarm","String",1, "Name of the alarm", null);
    return useroptions;
  }
  
  //Extra
  public void disableAlarm(Alarm alarmVal) throws Exception
  {
	  System.out.println("disable");
	  Alarm alarmObj = alarmVal;
	  AlarmSpec spec = new AlarmSpec();
	    
	    StateAlarmExpression expression = 
	      createStateAlarmExpression();
	    AlarmAction methodAction = createAlarmTriggerAction(
	        createPowerOnAction());
	    GroupAlarmAction gaa = new GroupAlarmAction();

	    gaa.setAction(new AlarmAction[]{methodAction});
	    spec.setAction(gaa);
	    spec.setExpression(expression);
	    spec.setName("VmFailoverAlarm");
	    spec.setDescription("Monitor VM state and send email " +
	    		"and power it on if VM powers off");
	    spec.setEnabled(false);    
	    
	    AlarmSetting as = new AlarmSetting();
	    as.setReportingFrequency(0); //as often as possible
	    as.setToleranceRange(0);
	    
	    spec.setSetting(as);
	    alarmObj.reconfigureAlarm(spec);
	    
  }
  
  public void enableAlarm(Alarm alarmVal) throws Exception
  {
	  System.out.println("enable");
	  Alarm alarmObj = alarmVal;
	  AlarmSpec spec = new AlarmSpec();
	    
	    StateAlarmExpression expression = 
	      createStateAlarmExpression();
	    AlarmAction methodAction = createAlarmTriggerAction(
	        createPowerOnAction());
	    GroupAlarmAction gaa = new GroupAlarmAction();

	    gaa.setAction(new AlarmAction[]{methodAction});
	    spec.setAction(gaa);
	    spec.setExpression(expression);
	    spec.setName("VmFailoverAlarm");
	    spec.setDescription("Monitor VM state and send email " +
	    		"and power it on if VM powers off");
	    spec.setEnabled(true);    
	    
	    AlarmSetting as = new AlarmSetting();
	    as.setReportingFrequency(0); //as often as possible
	    as.setToleranceRange(0);
	    
	    spec.setSetting(as);
	    alarmObj.reconfigureAlarm(spec);   
  }
   
}
