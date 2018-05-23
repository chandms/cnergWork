package main;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.IOException;
import java.util.concurrent.Semaphore;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ClientProgram {



    static String downloadFolder,copyFolder,copyFolder2;
    static int priority_given;
    static ArrayList<String> totalFileList;
    static ArrayList<String> downloadList;
    static int cur_port;
    static Semaphore semaphore;

    /*Parse the html cotent and get the list*/

    public static ArrayList<String> getList() throws IOException {

        Document doc = Jsoup.connect("http://localhost:7000/list").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> fileList = new ArrayList<String>();
        for (Element link : links) {
            String linkText = link.text();
            fileList.add(linkText);
        }
        return fileList;
    }

    /* According to the list of server, files are downloaded*/

    public static void downloadThingy(String downloadFolder)
    {

        for(int ft=0;ft<totalFileList.size();ft++) {
            String x =totalFileList.get(ft);
            String fileURL = "http://localhost:7000/get?name="+x;
            String saveDir = "C:/Users/Pupul/Desktop/"+downloadFolder +"/"+ x;
            try {
                Client.downloadFile(fileURL, saveDir);
                downloadList.add(x);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }



    /* A thread that runs a TCP server in master client which checks the requirement of the other TCP clients and send the corresponding files*/
    public static class SerCli implements Runnable{


        @Override
        public void run() {
            while(true) {
                ServerSocket serverSocket = null;
                try {
                    serverSocket = new ServerSocket(cur_port);
                } catch (IOException e) {
                    e.printStackTrace();
                }


                Socket socket = null;
                try {
                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Accepted connection : " + socket);
                System.out.println("Waiting for a file name");
                DataInputStream in = null;
                try {
                    in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String line = null;
                try {
                    line = in.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Got filename "+line);
                int flag=0;
                String dir="C:/Users/Pupul/Desktop/"+downloadFolder;
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
                    else
                        System.out.println("The file is still not there and filename= "+line);

                }
                if(flag==1) {

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
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("File transfer complete "+line);
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
        String copyingFolder;

        public CliCli(String folder)
        {
            copyingFolder=folder;
        }
        @Override
        public void run() {

            int filesize=2022386;
            int bytesRead = 0;
            int currentTot = 0;


            System.out.println(totalFileList);
            int ii=9000;

            for(int j=0;j<totalFileList.size();j++) {
                try {
                    semaphore.acquire();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Socket socket = null;
                try {
                    socket = new Socket("127.0.0.1", cur_port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                cur_port++;
                if(cur_port>10000)
                    cur_port=9000;
                System.out.println("New file name sent");
                DataOutputStream out = null;
                try {
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String fileName = totalFileList.get(j);
                try {
                    out.writeUTF(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Sent file name");
                byte[] bytearray = new byte[filesize];
                InputStream is = null;
                try {
                    is = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream("C:/Users/Pupul/Desktop/" + copyingFolder + "/" + fileName);
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
                    socket.close();
                    System.out.println("I got one file = "+fileName+"......................");
                    bos.flush();
                    bos.close();
                } catch (Exception e){e.printStackTrace();}
                semaphore.release();
            }

        }
    }

    /* controller main*/
    public static void main(String args[]) throws IOException {

        /*Properties systemSettings = System.getProperties();
        systemSettings.put("proxySet", "true");
        systemSettings.put("http.proxyHost", "172.16.2.30");
        systemSettings.put("http.proxyPort", "8080");
        systemSettings.put("https.proxyHost", "172.16.2.30");
        systemSettings.put("https.proxyPort", "8080");*/

        Server.StartServer();
        totalFileList=getList();
        downloadList=new ArrayList<String>();
        semaphore = new Semaphore(1);
        cur_port=9000;
        //getList();
        System.out.println("Enter 1 for giving priority to Client1 ,2 to give priority to client2 else enter 3");
        int priority;
        Scanner scanner = new Scanner(System.in);
        String s = scanner.nextLine();
        priority = Integer.parseInt(s);
        priority_given=priority;
        if(priority==1)
        {
            downloadFolder="Download";
            copyFolder="Download2";
            copyFolder2="studyFolder";
        }
        else if(priority==2)
        {
            downloadFolder="Download2";
            copyFolder = "Download";
            copyFolder2="studyFolder";
        }
        else
        {
            downloadFolder="studyFolder";
            copyFolder = "Download";
            copyFolder2="Download2";
        }
        clientThread3 clientThread3 = new clientThread3();
        clientThread2 clientThread2 = new clientThread2() ;
        clientThread1 clientThread1 = new clientThread1();
        Thread thread1 = new Thread(clientThread1);
        Thread thread2 = new Thread(clientThread2);
        Thread thread3 = new Thread(clientThread3);
        thread1.start();
        thread2.start();
        thread3.start();
    }

    public static class clientThread1 implements Runnable{

        @Override
        public void run() {

            if(priority_given==1)   /*Priority given to client1*/
            {
                SerCli serCli = new SerCli(); /* requirement checking starts*/

                Thread t1= new Thread(serCli);
                t1.start();
                downloadThingy(downloadFolder);  /* file download starts */

            }
            else {
                CliCli cliCli = new CliCli(copyFolder);  /* priority given to client 2*/
                Thread t2 = new Thread(cliCli); /* starts fetching files from master client */
                t2.start();
                }


        }
    }

    public static class clientThread2 implements Runnable{
        @Override
        public void run() {
            if(priority_given==1)     /*Priority given to client 1*/
            {
                CliCli cliCli = new CliCli(copyFolder);  /*client 2 starts fetching files from master client*/
                Thread t2 = new Thread(cliCli);
                t2.start();

            }
            else if(priority_given==3)
            {
                CliCli cliCli = new CliCli(copyFolder2);  /*client 2 starts fetching files from master client*/
                Thread t2 = new Thread(cliCli);
                t2.start();
            }
            else
            {
               // Master master = new Master();
                SerCli serCli = new SerCli();  /*priority given to client 2*/
                Thread t1= new Thread(serCli); /*requirement checking starts*/
                t1.start();
                downloadThingy(downloadFolder); /*download starts*/

            }
        }
    }

    public static class clientThread3 implements Runnable{
        @Override
        public void run() {
            if(priority_given==2 || priority_given==1)     /*Priority given to client 1*/
            {
                CliCli cliCli = new CliCli(copyFolder2);  /*client 2 starts fetching files from master client*/
                Thread t2 = new Thread(cliCli);
                t2.start();

            }
            else
            {
                SerCli serCli = new SerCli();  /*priority given to client 2*/
                //Master master = new Master();
                Thread t1= new Thread(serCli); /*requirement checking starts*/
                t1.start();
                downloadThingy(downloadFolder); /*download starts*/

            }
        }
    }

}
