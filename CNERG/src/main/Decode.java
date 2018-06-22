package main;


import java.io.File;


import java.io.*;
import java.util.ArrayList;

public class Decode {

    String Destination;
    String initFile;
    String videoSegment;
    String converted;
    String dec;
    String des;
    int in;


    public Decode(String dest,String init,String seg,String con,String dd,int k) throws IOException {
        des=dest;
        Destination = dest+"decode";
        initFile =init;
        videoSegment=seg;
        converted = con;
        dec=dd;
        in=k;
    }


    public void decoder()
    {


        String s = null;

        try {

            // run the Unix "ps -ef" command
            // using the Runtime exec method:
            /*String command = "cmd.exe /c start python /home/viscous/Desktop/Project/numbers.py";
            Process p = Runtime.getRuntime().exec(command);*/
           // ProcessBuilder pb = new ProcessBuilder("python", "numbers.py");
            //ProcessBuilder pb = new ProcessBuilder("python", "svc_merge.py");
            /*ProcessBuilder pb = new ProcessBuilder("python",
                    "svc_merge.py",
                    "video_1.264.seg0-EL3.264","video_1.264.init.svc","video_1.264.seg0-L0.svc");
            pb.directory(new File("/home/viscous/Desktop/cnergWork/CNERG/src/decoded/"));*/
            String temp=videoSegment;
            String normal="";
            int uc=0,g=0;
            while(uc<temp.length()) {
                if(temp.charAt(uc)=='-')
                    g++;
                normal = normal+temp.charAt(uc);
                if(g==1)
                    break;
                uc++;
            }
            System.out.println("hey normal "+normal);
            ArrayList<String> myArr= new ArrayList<String>();
            for(int j=1;j<=in;j++)
            {
                String innSt= normal+"L"+j+".svc";
                myArr.add(innSt);
            }
            System.out.println("hey my array size "+ String.valueOf(myArr.size()));
            ProcessBuilder pb =new ProcessBuilder();
            if(myArr.size()==0)
                pb = new ProcessBuilder("python", des+"svc_merge.py", converted,initFile,des+"SpecialFolder/"+videoSegment);
            else if(myArr.size()==1)
                pb = new ProcessBuilder("python", des+"svc_merge.py", converted,initFile,des+"SpecialFolder/"+videoSegment,des+"SpecialFolder/"+myArr.get(0));
            else if(myArr.size()==2)
                pb = new ProcessBuilder("python", des+"svc_merge.py", converted,initFile,des+"SpecialFolder/"+videoSegment,des+"SpecialFolder/"+myArr.get(0),des+"SpecialFolder/"+myArr.get(1));
            else if(myArr.size()==3)
                pb = new ProcessBuilder("python", des+"svc_merge.py", converted,initFile,des+"SpecialFolder/"+videoSegment,des+"SpecialFolder/"+myArr.get(0),des+"SpecialFolder/"+myArr.get(1),des+"SpecialFolder/"+myArr.get(2));
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

            SecondDecode secondDecode = new SecondDecode(Destination,converted,dec);
            secondDecode.SecDecoder();

            //System.exit(0);
        }
        catch (IOException e) {
            System.out.println("exception happened - here's what I know: ");
            e.printStackTrace();
            //System.exit(-1);
        }
    }
}
