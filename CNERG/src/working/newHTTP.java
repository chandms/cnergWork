package working;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.Client;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;


public class newHTTP {


    static String downloadFolder, copyFolder,copyFolder2;
    static int priority_given;
    static ArrayList<String> totalFileList;
    static ArrayList<String> downloadList;
    static int cur_port;
    //static Semaphore semaphore;


    /*Parse the html cotent and get the list*/

    public static class ClonedSer {


        static int port;
        static String ip;
        static String s;
        static int flag = 0;
        static String dest;
        static String folder;

        public ClonedSer(String ii, String de, String fold) throws IOException {

            ip = ii;
            port = 10000;
            dest = de;
            folder = fold;
        }


        public static Map<String, String> queryToMap(String query) {

            Map<String, String> result = new HashMap<String, String>();
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                if (pair.length > 1) {
                    result.put(pair[0], pair[1]);
                } else {
                    result.put(pair[0], "");
                }
            }
            return result;
        }

        static class getFileNo implements HttpHandler {

            @Override
            public void handle(HttpExchange httpExchange) throws IOException {

                String string = Integer.toString(downloadList.size());
                httpExchange.sendResponseHeaders(200, string.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(string.getBytes());
                os.close();
            }
        }

        static class ListHandler implements HttpHandler {
            String ip;

            ListHandler(String ii) {
                ip = ii;
            }

            @Override
            public void handle(HttpExchange t) throws IOException {
                String st = "";
                for (int p = 0; p < downloadList.size(); p++) {
                    String x = downloadList.get(p);
                    st = st + "<a href=\"/get?name=" + x + "\">" + x + "</a>";
                    st = st + "<br>";


                }
                t.sendResponseHeaders(200, st.length());
                OutputStream os = t.getResponseBody();
                os.write(st.getBytes());
                os.close();
            }
        }

        static class MyHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange t) throws IOException {
                String response = "Server starts at port no= " + port;

                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        }

        static class GotHandler implements HttpHandler {

            String destination;
            String Folder;

            public GotHandler(String ds, String folder) {
                destination = ds;
                Folder = folder;
            }

            @Override
            public void handle(HttpExchange t) throws IOException {
                Map<String, String> parms = queryToMap(t.getRequestURI().getQuery());
                s = parms.get("name");
                System.out.println(s + "!!!!");
                int g = 0;
                for (int p = 0; p < downloadList.size(); p++) {
                    String x = downloadList.get(p);
                    if (x.equals(s)) {
                        Headers h = t.getResponseHeaders();
                        String ext1 = FilenameUtils.getExtension(s);
                        if (ext1.equals("jpg") || ext1.equals("png") || ext1.equals("jpeg"))
                            h.add("Content-Type", "image/" + ext1);
                        else if (ext1.equals("txt"))
                            h.add("Content-Type", "text/plain");
                        else
                            h.add("Content-Type", "application/" + ext1);

                        File gfile = new File(destination + Folder + "/" + s);
                        byte[] bytearray = new byte[(int) gfile.length()];
                        FileInputStream fis = new FileInputStream(gfile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        bis.read(bytearray, 0, bytearray.length);

                        t.sendResponseHeaders(200, gfile.length());

                        OutputStream os = t.getResponseBody();
                        os.write(bytearray, 0, bytearray.length);

                        os.close();
                        g = 1;
                        break;

                    }
                }
                if (g == 0) {
                    String response = "No file of that name!!!!";

                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                }
                flag = 1;

            }
        }
        public static class SerCli implements  Runnable{
            DataInputStream in;
            OutputStream os;
            DataOutputStream out;
            Socket socket;
            public SerCli(DataInputStream din,DataOutputStream dos, OutputStream o,Socket ss)
            {
                in=din;
                out=dos;
                os=o;
                socket=ss;
            }

            @Override
            public void run() {

                while(true)
                {

                    System.out.println("Waiting for new name..............");
                    String fileName = null;
                    try {
                        fileName = in.readUTF();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    int len = downloadList.size();
                    int fg = 0;
                    for (int j = 0; j < len; j++) {
                        if (downloadList.get(j).equals(fileName)) {
                            fg++;
                            break;
                        }
                    }
                    if (fg > 0) {
                        String dir = dest + folder;
                        System.out.println("file is there in other system " + fileName);
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

                }

            }
        }

        public static void StartServer() throws IOException, InterruptedException {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server starts at " + port + "!!");
            flag = 0;
            server.createContext("/test", new ClonedSer.MyHandler());

            server.createContext("/files", new ClonedSer.getFileNo());
            server.createContext("/get", new ClonedSer.GotHandler(dest, folder));
            server.createContext("/list", new ClonedSer.ListHandler(ip));
            server.setExecutor(null); // creates a default executor
            server.start();

            ServerSocket serverSocket = new ServerSocket(cur_port);
            while(true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client is accepted " + clientSocket);
                DataInputStream in = null;
                in = new DataInputStream(clientSocket.getInputStream());
                OutputStream os = null;
                try {
                    os = clientSocket.getOutputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                DataOutputStream dos = null;
                dos = new DataOutputStream(clientSocket.getOutputStream());
                SerCli serCli = new SerCli(in, dos, os, clientSocket);
                Thread th = new Thread(serCli);
                th.start();
            }
        }


    }


    public static ArrayList<String> getList(String ip) throws IOException {

        Document doc = Jsoup.connect("http://" + ip + ":7000/list").get();
        Elements links = doc.getElementsByTag("a");
        ArrayList<String> fileList = new ArrayList<String>();
        for (Element link : links) {
            String linkText = link.text();
            fileList.add(linkText);
        }
        return fileList;
    }

    /* According to the list of server, files are downloaded*/

    public static void downloadThingy(String downloadFolder, String Dest, String ip) {

        for (int ft = 0; ft < totalFileList.size(); ft++) {
            String x = totalFileList.get(ft);
            String fileURL = "http://" + ip + ":7000/get?name=" + x;
            String saveDir = Dest + downloadFolder + "/" + x;
            try {
                Client.downloadFile(fileURL, saveDir);
                downloadList.add(x);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }

    public static class newCli implements Runnable {
        String copyingFolder, Dest, ipAddress;

        public newCli(String folder, String d, String ip) {
            copyingFolder = folder;
            Dest = d;
            ipAddress = ip;
        }

        @Override
        public void run() {

            int filesize = 2022386;
            int bytesRead = 0;
            int currentTot = 0;


            System.out.println(totalFileList);
            int ii = 9000;
            Socket socket = null;
            int myPort=cur_port;
            try {
                socket = new Socket(ipAddress, myPort);
            } catch (IOException e) {
                e.printStackTrace();
            }
            /*try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            cur_port++;
            semaphore.release();*/

            for (int j = 0; j < totalFileList.size(); j++) {

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
                DataInputStream din=null;
                try {
                    din=new DataInputStream(socket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String fileName = totalFileList.get(j);
                try {
                    out.writeUTF(fileName);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Sent file name " + fileName);
                String fileLength = null;
                try {
                    fileLength= din.readUTF();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("got file size "+fileLength);

                int fileLen = Integer.parseInt(fileLength);
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
                //System.out.println("I am alive..................");
                try {
                    while(bytesRead>0) {
                        bytesRead = is.read(bytearray, currentTot, (bytearray.length - currentTot));
                        if (bytesRead >= 0) {
                            currentTot += bytesRead;
                        }
                        //System.out.println("I am Stucked.................." + bytesRead);
                    }
                    //System.out.println("I am done printing ............");
                    bos.write(bytearray, 0, currentTot);
                    System.out.println("I got one file = " + fileName + "......................");

                    bos.flush();
                    bos.close();
                    fos.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
            /*try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
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
            String Destination = args[0];
            String ipAddress = args[1];
            System.out.println(Destination);
            Server.StartServer();
            totalFileList = getList(ipAddress);
            downloadList = new ArrayList<String>();
            cur_port = 9000;
            //semaphore = new Semaphore(1);
            //getList();
            System.out.println("Enter 1 for client1, 2 for client2 and 3 for client3 to make any of them master client");
            int priority;
            Scanner scanner = new Scanner(System.in);
            String s = scanner.nextLine();
            priority = Integer.parseInt(s);
            priority_given = priority;
            if (priority == 1) {
                downloadFolder = "Download";
                copyFolder = "Download2";
                copyFolder2="studyFolder";
            } else if(priority==2) {
                copyFolder = "Download";
                downloadFolder = "Download2";
                copyFolder2="studyFolder";
            }
            else
            {
                copyFolder = "Download";
                downloadFolder = "studyFolder";
                copyFolder2="Download2";
            }
            clientThread1 clientThread1 = new clientThread1(Destination, ipAddress);
            Thread thread1 = new Thread(clientThread1);
            thread1.start();
            clientThread2 clientThread2 = new clientThread2(Destination, ipAddress);
            Thread thread2 = new Thread(clientThread2);
            thread2.start();
            clientThread3 clientThread3 = new clientThread3(Destination,ipAddress);
            Thread thread3 = new Thread(clientThread3);
            thread3.start();
        }

        public static class clientThread1 implements Runnable {
            String Dest;
            String ipAddress;

            public clientThread1(String d, String ip) {
                Dest = d;
                ipAddress = ip;
            }

            @Override
            public void run() {

                if (priority_given == 1)   /*Priority given to client1*/ {
                    downloadThingy(downloadFolder, Dest, ipAddress);  /* file download starts */
                    ClonedSer clonedServer = null;
                    try {
                        clonedServer = new ClonedSer(ipAddress, Dest, downloadFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        clonedServer.StartServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else {

                    newCli newCli = new newCli(copyFolder, Dest, ipAddress);
                    Thread t = new Thread(newCli);
                    t.start();
                }


            }
        }

        public static class clientThread2 implements Runnable {
            String Dest;
            String ipAddress;

            public clientThread2(String d, String ip) {
                Dest = d;
                ipAddress = ip;
            }

            @Override
            public void run() {

                if (priority_given == 2)   /*Priority given to client1*/ {
                    downloadThingy(downloadFolder, Dest, ipAddress);  /* file download starts */
                    ClonedSer clonedServer = null;
                    try {
                        clonedServer = new ClonedSer(ipAddress, Dest, downloadFolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        clonedServer.StartServer();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                } else if(priority_given==1) {

                    newCli newCli = new newCli(copyFolder, Dest, ipAddress);
                    Thread t = new Thread(newCli);
                    t.start();
                }
                else
                {
                    newCli newCli = new newCli(copyFolder2, Dest, ipAddress);
                    Thread t = new Thread(newCli);
                    t.start();
                }


            }
        }

    public static class clientThread3 implements Runnable {
        String Dest;
        String ipAddress;

        public clientThread3(String d, String ip) {
            Dest = d;
            ipAddress = ip;
        }

        @Override
        public void run() {

            if (priority_given == 3)   /*Priority given to client1*/ {
                downloadThingy(downloadFolder, Dest, ipAddress);  /* file download starts */
                ClonedSer clonedServer = null;
                try {
                    clonedServer = new ClonedSer(ipAddress, Dest, downloadFolder);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    clonedServer.StartServer();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {

                newCli newCli = new newCli(copyFolder2, Dest, ipAddress);
                Thread t = new Thread(newCli);
                t.start();
            }


        }
    }




    }