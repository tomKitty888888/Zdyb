package com.zdyb.module_diagnosis.help;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Help {

     public final static Character ser = '\0';


     public static void disableSelinux() {
          try {
               Process process = Runtime.getRuntime().exec("su");
               BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
               StringBuilder output = new StringBuilder();
               String line;
               while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
               }
               process.waitFor();
               reader.close();
          } catch (IOException e) {
               e.printStackTrace();
          } catch (InterruptedException e) {
               e.printStackTrace();
          }
     }
}
