package com.vmware.vim25.mo.samples;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class PingCommand 
{
	public String ping(String ipValue) 
	{
		String ip = ipValue;
		String pingResult = "";
		String checkVM = null;

      String pingCmd = "ping " + ip;
      try 
      {
          Runtime r = Runtime.getRuntime();
          Process p = r.exec(pingCmd);

          BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
          String inputLine;
          while ((inputLine = in.readLine()) != null) 
          {
              System.out.println(inputLine);
              pingResult += inputLine;
          }
          in.close();
          
          //Checking if VM is dead
          if(pingResult.contains("Request timed out.Request timed out.Request timed out.Request timed out.") == true 
        		  || pingResult.contains("unreachable") == true)
          {
        	  checkVM = "true";
          }
          else
          {
        	  checkVM = "false";
          }
          System.out.println("\nIs the pinged Entity dead ? : " + checkVM);
          return checkVM;

      } catch (IOException e) {
          System.out.println(e);
      }
      return checkVM;     
	}
}
