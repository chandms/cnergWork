package main;


import java.io.File;


import java.io.*;

public class Decode {

    String Destination;
    String initFile;
    String videoSegment;
    String converted;
    String dec;


    public Decode(String dest,String init,String seg,String con,String dd) throws IOException {
        Destination = dest;
        initFile =init;
        videoSegment=seg;
        converted = con;
        dec=dd;
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

            ProcessBuilder pb = new ProcessBuilder("python", "svc_merge.py", converted,initFile,videoSegment);
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
