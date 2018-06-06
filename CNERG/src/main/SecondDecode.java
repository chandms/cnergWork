package main;

import javax.script.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;


import java.io.*;

public class SecondDecode {

    String Destination;
    String decoded;
    String converted;

    public SecondDecode(String dest,String con,String dec)
    {
        Destination=dest;
        converted= con;
        decoded= dec;
    }

    public void SecDecoder() {

        String s = null;

        try {

            ProcessBuilder pb= new ProcessBuilder("/home/viscous/Desktop/SvcP2PStream/jsvm_9.19.5/bin/H264AVCDecoderLibTestStatic", converted,decoded);
            pb.directory(new File(Destination));

            Process p = pb.start();

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            BufferedReader stdError = new BufferedReader(new
                    InputStreamReader(p.getErrorStream()));

            // read the output from the command
            System.out.println("Here is the standard output of the command:\n");
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
            }

            // read any errors from the attempted command
            System.out.println("Here is the standard error of the command (if any):\n");
            while ((s = stdError.readLine()) != null) {
                System.out.println(s);
            }

            System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            //System.exit(-1);
        }
    }
}
