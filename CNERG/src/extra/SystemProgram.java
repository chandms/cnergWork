package extra;

import java.io.*;
import java.net.*;
import java.time.format.DecimalStyle;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import main.Client;
import main.Server;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.security.krb5.internal.crypto.Des;

import javax.swing.*;

public class SystemProgram {



    static String downloadFolder,copyFolder;
    static int priority_given;
    static ArrayList<String> totalFileList;
    static ArrayList<String> downloadList;
    static int cur_port;
    static Semaphore semaphore;

    /*Parse the html cotent and get the list*/

    public static ArrayList<String> getList(String ip) throws IOException {

        Document doc = Jsoup.connect("http://"+ip+":7000/list").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> fileList = new ArrayList<String>();
        for (Element link : links) {
            String linkText = link.text();
            fileList.add(linkText);
        }
        return fileList;
    }

    /* According to the list of server, files are downloaded*/

    public static void downloadThingy(String downloadFolder,String Dest,String ip)
    {

        for(int ft=0;ft<totalFileList.size();ft++) {
            String x =totalFileList.get(ft);
            String fileURL = "http://"+ip+":7000/get?name="+x;
            String saveDir = Dest+downloadFolder +"/"+ x;
            try {
                Client.downloadFile(fileURL, saveDir);
                downloadList.add(x);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    public static class master implements Runnable{
        String Dest;
        public master(String d)
        {
            Dest=d;
        }

        @Override
        public void run() {

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(cur_port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true) {

                //cur_port++;

                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Accepted connection : " + socket);
                SerCli serCli = new SerCli(Dest,socket);
                Thread t = new Thread(serCli);
                t.start();
            }

        }
    }

    /* A thread that runs a TCP server in master client which checks the requirement of the other TCP clients and send the corresponding files*/
    public static class SerCli implements Runnable{
        String Dest;
        Socket socket;

        public SerCli(String d,Socket ss)
        {
            Dest=d;
            socket=ss;
        }

        @Override
        public void run() {
            DataInputStream in = null;
            try {
                in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
            while(true) {
                System.out.println("Waiting for a file name");
                String line = null;
                try {
                    line = in.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Got filename "+line);
                int flag=0;
                String dir=Dest+downloadFolder;
                //File folder = new File(dir);
                while(flag==0) {
                    int cur_len=downloadList.size();
                    for (int fl=0;fl<cur_len;fl++) {
                        if (downloadList.get(fl).equals(line)) {
                            flag++;
                            break;
                        }
                    }
                    if(flag==1)
                        break;
                    /*else
                        System.out.println("The file is still not there and filename= "+line);*/

                }
                if(flag==1) {

                    System.out.println("File in system");

                    File transferFile = new File(dir+"/"+line);
                    byte[] bytearray = new byte[(int) transferFile.length()];
                    System.out.println("Length= "+transferFile.length());
                    FileInputStream fin = null;
                    try {
                        fin = new FileInputStream(transferFile);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    BufferedInputStream bin = new BufferedInputStream(fin);
                    try {
                        bin.read(bytearray, 0, bytearray.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Length= "+bytearray.length);
                    OutputStream os = null;
                    try {
                        os = socket.getOutputStream();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("Sending Files...");
                    try {
                        os.write(bytearray, 0, bytearray.length);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        os.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    /*try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    System.out.println("File transfer complete "+line);
                    try {
                        fin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        bin.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                else
                {
                    System.out.println("No file such that");
                }


            }
        }
    }

    /*non-master client requests master client for fetching file*/
    public static class CliCli implements Runnable{
        String copyingFolder,Dest,ipAddress;

        public CliCli(String folder,String d,String ip)
        {
            copyingFolder=folder;
            Dest=d;
            ipAddress=ip;
        }
        @Override
        public void run() {

            int filesize=2022386;
            int bytesRead = 0;
            int currentTot = 0;


            System.out.println(totalFileList);
            int ii=9000;
            Socket socket = null;
            try {
                socket = new Socket(ipAddress, cur_port);
            } catch (IOException e) {
                e.printStackTrace();
            }

            DataOutputStream out = null;
            try {
                out = new DataOutputStream(socket.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }

            for(int j=0;j<totalFileList.size();j++) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                /*Socket socket = null;
                try {
                    socket = new Socket(ipAddress, cur_port);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                //cur_port++;
                if(cur_port>10000)
                    cur_port=9000;
                //System.out.println("New file name sent");

                String fileName = totalFileList.get(j);
                try {
                    out.writeUTF(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Sent file name "+ fileName);
                byte[] bytearray = new byte[filesize];
                InputStream is = null;
                try {
                    is = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream( Dest + copyingFolder + "/" + fileName);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                try {
                    bytesRead = is.read(bytearray, 0, bytearray.length);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                currentTot = bytesRead;
                try {
                    do {
                        bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
                        if (bytesRead >= 0) currentTot += bytesRead;
                    } while (bytesRead > -1);
                    bos.write(bytearray, 0, currentTot);
                    //socket.close();
                    System.out.println("I got one file = "+fileName+"......................");
                    bos.flush();
                    bos.close();
                } catch (Exception e){e.printStackTrace();}
                semaphore.release();
            }
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    /* controller main*/
    public static void main(String args[]) throws IOException {

       /* Properties systemSettings = System.getProperties();
        systemSettings.put("proxySet", "true");
        systemSettings.put("http.proxyHost", "172.16.2.30");
        systemSettings.put("http.proxyPort", "8080");
        systemSettings.put("https.proxyHost", "172.16.2.30");
        systemSettings.put("https.proxyPort", "8080");*/
        String Destination=args[0];
        String ipAddress=args[1];
        System.out.println(Destination);
        Server.StartServer();
        totalFileList=getList(ipAddress);
        downloadList=new ArrayList<String>();
        semaphore = new Semaphore(1);
        cur_port=9000;
        //getList();
        System.out.println("Enter 1 to make the system master,else enter 0");
        int priority;
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        priority = Integer.parseInt(s);
        priority_given=priority;
        if(priority==1)
        {
            downloadFolder="Download";
            copyFolder="Download2";
        }
        else {
            copyFolder = "Download";
            downloadFolder="Download2";
        }
        clientThread1 clientThread1 = new clientThread1(Destination,ipAddress);
        Thread thread1 = new Thread(clientThread1);
        thread1.start();
        clientThread2 clientThread2 = new clientThread2(Destination,ipAddress);
        Thread thread2 = new Thread(clientThread2);
        thread2.start();
    }

    public static class clientThread1 implements Runnable{
        String Dest;
        String ipAddress;
        public clientThread1(String d,String ip)
        {
            Dest =d;
            ipAddress=ip;
        }

        @Override
        public void run() {

            if(priority_given==1)   /*Priority given to client1*/
            {
                //SerCli serCli = new SerCli(Dest); /* requirement checking starts*/
                master master = new master(Dest);
                Thread t1= new Thread(master);
                t1.start();
                downloadThingy(downloadFolder,Dest,ipAddress);  /* file download starts */

            }
            else {
                CliCli cliCli = new CliCli(copyFolder,Dest,ipAddress);  /* priority given to client 2*/
                Thread t2 = new Thread(cliCli); /* starts fetching files from master client */
                t2.start();
            }


        }
    }

    public static class clientThread2 implements Runnable{
        String Dest;
        String ipAddress;
        public clientThread2(String d,String ip)
        {
            Dest =d;
            ipAddress=ip;
        }

        @Override
        public void run() {

            if(priority_given==1)   /*Priority given to client1*/
            {

                CliCli cliCli = new CliCli(copyFolder,Dest,ipAddress);  /* priority given to client 2*/
                Thread t2 = new Thread(cliCli); /* starts fetching files from master client */
                t2.start();

            }
            else {

                //SerCli serCli = new SerCli(Dest); /* requirement checking starts*/
                master master = new master(Dest);
                Thread t1= new Thread(master);
                t1.start();
                downloadThingy(downloadFolder,Dest,ipAddress);  /* file download starts */

            }


        }
    }

}
