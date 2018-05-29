package main;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import sun.security.krb5.internal.crypto.Des;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.Semaphore;


public class changeINClient {



    static ArrayList<String> totalFileList, coClientList;
    static ArrayList<String> downloadList;
    static int cur_port;
    static  int FileFlag;
    static int myId;



    /*Parse the html cotent and get the list*/

    public static class SerCli implements Runnable {
        DataInputStream in;
        OutputStream os;
        DataOutputStream out;
        Socket socket;
        String Folder;
        String dest;

        public SerCli(DataInputStream din, DataOutputStream dos, OutputStream o, Socket ss, String folder, String destination) {
            in = din;
            out = dos;
            os = o;
            socket = ss;
            Folder = folder;
            dest = destination;
        }

        @Override
        public void run() {

            while (true) {

                System.out.println("Waiting for new name..............");
                String fileName = null;
                try {
                    fileName = in.readUTF();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                if(fileName==null)
                    continue;
                int len = downloadList.size();
                int fg = 0;
                for (int j = 0; j < len; j++) {
                    if (downloadList.get(j).equals(fileName)) {
                        fg++;
                        break;
                    }
                }

                if (fg > 0) {
                    String dir = dest + Folder;
                    File transferFile = new File(dir + "/" + fileName);
                    byte[] bytearray = new byte[(int) transferFile.length()];
                    System.out.println("Length= " + transferFile.length());
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
                    System.out.println("Length= " + bytearray.length);
                    try {
                        out.writeUTF(String.valueOf(transferFile.length()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    System.out.println("file length is sent");
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
                    System.out.println("File transfer complete " + fileName);
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
                    try {
                        out.writeUTF("-1");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
               /* String df = "";
                try {
                    df = in.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (df.equals("1")) {
                    try {
                        System.out.println("Socket close korlam");
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                }*/

            }

        }
    }

    public static class ServerForm implements Runnable {
        String Folder;
        String Destination;
        int conPort;

        public ServerForm(String fold, String Dest,int port) {
            Folder = fold;
            Destination = Dest;
            conPort=port;
        }

        @Override
        public void run() {

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(conPort);
            } catch (IOException e) {
                e.printStackTrace();
            }

            while (true) {
                System.out.println("Still waiting...........");
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Client is accepted " + clientSocket);
                DataInputStream in = null;
                try {
                    in = new DataInputStream(clientSocket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                OutputStream os = null;
                try {
                    os = clientSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DataOutputStream dos = null;
                try {
                    dos = new DataOutputStream(clientSocket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                SerCli serCli = new SerCli(in, dos, os, clientSocket, Folder, Destination);
                Thread th = new Thread(serCli);
                th.start();
            }
        }

    }


    public static ArrayList<String> getList(String ip) throws IOException {

        Document doc = Jsoup.connect("http://" + ip + ":8081/list").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> fileList = new ArrayList<String>();
        for (Element link : links) {
            String linkText = link.text();
            fileList.add(linkText);
        }
        return fileList;
    }

    public static ArrayList<String> getCoClient(String ip) throws Exception {
        Document doc = Jsoup.connect("http://" + ip + ":8081/ip").get();
        Elements header = doc.getElementsByTag("h1");
        ArrayList<String> fileList = new ArrayList<String>();
        for (Element head : header) {
            String headText = head.text();
            fileList.add(headText);
        }
        return fileList;
    }

    /* According to the list of server, files are downloaded*/

    public static void downloadThingy(String downloadFolder, String Dest, String ip,String filename) throws InterruptedException {

        /*for (int ft = 0; ft < totalFileList.size(); ft++) {
            String x = totalFileList.get(ft);*/
            String fileURL = "http://" + ip + ":8081/get?name=" + filename;
            String saveDir = Dest + downloadFolder + "/" + filename;
            try {
                Client.downloadFile(fileURL, saveDir);
                downloadList.add(filename);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            Thread.sleep(500);

        /*}*/
    }
    public static void check(String ip)
    {

        URL url = null;
        try {
            url = new URL("http://" + ip + ":8081/myip");
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            e.printStackTrace();
        }
        connection.setDoOutput(true);
        System.out.println("Accessed once........");
        try {
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(
                    connection.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            out.write("myid");
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BufferedReader in = null;
        try {
            in = new BufferedReader(
                    new InputStreamReader(
                            connection.getInputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String decodedString=null;
        String ff="";
        try {
            while ((decodedString = in.readLine()) != null) {
                ff=ff+decodedString;
                System.out.println("heyyapupul"+decodedString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        myId=Integer.parseInt(ff);
        System.out.println("hey yo "+myId);
        try {
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }



    public static class TestIp implements Runnable {
        String ipofServer, address;

        public TestIp(String ip, String add) {
            ipofServer = ip;
            address = add;
        }

        @Override
        public void run() {

            //while (true) {

                URL url = null;
                try {
                    url = new URL("http://" + ipofServer + ":8081/ip");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                URLConnection connection = null;
                try {
                    connection = url.openConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connection.setDoOutput(true);
                try {
                    connection.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                OutputStreamWriter out = null;
                try {
                    out = new OutputStreamWriter(
                            connection.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.write(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader in = null;
                try {
                    in = new BufferedReader(
                            new InputStreamReader(
                                    connection.getInputStream()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                String decodedString;
                try {
                    while ((decodedString = in.readLine()) != null) {
                        System.out.println(decodedString);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            //}
        }
    }

    public static class newCli implements Runnable {
        String copyingFolder, Dest, ipAddress,fileName;
        String myPort;


        public newCli(String folder, String d, String ip,String port,String name) {
            copyingFolder = folder;
            Dest = d;
            ipAddress = ip;
            fileName = name;
            myPort=port;
        }

        @Override
        public void run() {

            int filesize = 2022386;
            int bytesRead = 0;
            int currentTot = 0;



            int ii = 9000;
            Socket socket = null;

            try {
                socket = new Socket(ipAddress, Integer.parseInt(myPort));
            } catch (IOException e) {
                e.printStackTrace();
            }

                DataOutputStream out = null;
                try {
                    out = new DataOutputStream(socket.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                InputStream is = null;
                try {
                    is = socket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DataInputStream din = null;
                try {
                    din = new DataInputStream(socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.writeUTF(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Sent file name " + fileName);
                String fileLength = null;
                try {
                    fileLength = din.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("got file size " + fileLength);

                int fileLen = Integer.parseInt(fileLength);
                if(fileLen==-1)
                {
                    FileFlag=0;
                }
                else {
                    FileFlag = 1;
                    byte[] bytearray = new byte[fileLen];
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(Dest + copyingFolder + "/" + fileName);
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
                        while (bytesRead > 0) {
                            bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
                            if (bytesRead >= 0) {
                                currentTot += bytesRead;
                            }
                        }

                        bos.write(bytearray, 0, currentTot);
                        System.out.println("I got one file = " + fileName + "......................");

                        bos.flush();
                        bos.close();
                        fos.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
        }
    }

    /* controller main*/
    public static void main(String args[]) throws Exception {

       /* Properties systemSettings = System.getProperties();
        systemSettings.put("proxySet", "true");
        systemSettings.put("http.proxyHost", "172.16.2.30");
        systemSettings.put("http.proxyPort", "8080");
        systemSettings.put("https.proxyHost", "172.16.2.30");
        systemSettings.put("https.proxyPort", "8080");*/
        String Destination = args[0];
        String ipAddress = args[1];
        System.out.println(Destination);
        totalFileList = getList(ipAddress);
        downloadList = new ArrayList<String>();
        coClientList = getCoClient(ipAddress);
        cur_port = 9000;
        clientThreadnew clientThreadnew = new clientThreadnew(Destination,ipAddress,"SpecialFolder");
        Thread t1= new Thread(clientThreadnew);
        t1.start();
    }

    public static class clientThreadnew implements Runnable {
        String Dest;
        String ipAddress;
        String Folder;

        public clientThreadnew(String d, String ip, String folder) {
            Dest = d;
            ipAddress = ip;
            Folder = folder;
        }

        @Override
        public void run() {

            InetAddress ip = null;
            try {
                ip = InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            System.out.println("Current IP address : " + ip.getHostAddress());
            //String myownIp = ip.getHostAddress();
            check(ipAddress);
            //System.out.println("my own ip heyya......................"+myownIp);
            String address ="myownIP";
            TestIp testIp = new TestIp(ipAddress, address+" "+"id = "+myId);
            Thread th = new Thread(testIp);
            th.start();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            ArrayList<String> st = new ArrayList<String>();
            try {
                st = getCoClient(ipAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("these are my co clients");
            for (int u = 0; u < st.size(); u++) {
                System.out.println(st.get(u));
            }
            int connectPort = 0;
            for (int y = 0; y < st.size(); y++) {
                String ipr = "";
                String myPort = "";
                String idd="";

                if(st.get(y).indexOf("myownIP")!=-1) {
                    System.out.println("Entered......");
                    if ((st.get(y).indexOf("id = "+myId) != -1)) {
                        String f = st.get(y);
                        System.out.println(f+"!!!!!");
                        int u = 5;
                        while (u < f.length() && f.charAt(u) != ' ') {
                            ipr = ipr + f.charAt(u);
                            u++;
                        }

                        u++;
                        if (u < f.length()) {
                            while (u < f.length() && f.charAt(u) != ' ') {
                                myPort = myPort + f.charAt(u);
                                u++;
                            }
                        }
                            connectPort = Integer.parseInt(myPort);
                            u++;
                            if (u < f.length())
                            {
                                while(u<f.length() && f.charAt(u)!=' ')
                                     u++;
                            }
                            /*u++;
                            if(u<f.length())
                            {
                                while(u<f.length() && f.charAt(u)!=' ')
                                {
                                    idd=idd+f.charAt(u);
                                    u++;
                                }
                            }
                            myId= Integer.parseInt(idd);*/


                        break;
                    }
                }

            }

            if (connectPort != 0) {
                System.out.println("I got my port no " + connectPort);
                System.out.println("I got my id "+myId);
                ServerForm serverForm = new ServerForm(Folder, Dest, connectPort);
                Thread serverThread = new Thread(serverForm);
                serverThread.start();
                for (int j = 0; j < totalFileList.size(); j++) {


                    FileFlag = 0;
                    String fileName = totalFileList.get(j);
                    for (int jk = 0; jk < st.size(); jk++) {
                        String ipr = "";
                        String myPort = "";
                        if (st.get(jk).indexOf("myownIP") != -1) {
                            if ((st.get(jk).indexOf("id = "+myId) == -1)) {
                                String f = st.get(jk);

                                int u = 5;
                                while (f.charAt(u) != ' ') {
                                    ipr = ipr + f.charAt(u);
                                    u++;
                                }
                                u++;
                                while (f.charAt(u) != ' ') {
                                    myPort = myPort + f.charAt(u);
                                    u++;
                                }
                                System.out.println("I am connecting with "+ipr+"and "+myPort);
                                newCli newCli = new newCli(Folder, Dest, ipr, myPort, fileName);
                                Thread t = new Thread(newCli);
                                t.start();
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (FileFlag == 1)
                                    break;
                            }

                        }
                    }
                    System.out.println("my flag ="+FileFlag);
                        if (FileFlag == 0) {

                            try {
                                downloadThingy("SpecialFolder", Dest, ipAddress, fileName);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                }
            }
            else
                System.out.println("I have still not got my port and id");
        }



    }
}