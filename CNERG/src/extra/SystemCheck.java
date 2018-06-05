package extra;

import java.io.*;
import java.net.*;
import java.util.*;
import java.io.IOException;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.Client;
import working.Server;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SystemCheck {



    static String downloadFolder,copyFolder;
    static int priority_given;
    static ArrayList<String> totalFileList;
    static ArrayList<String> downloadList;
    static int cur_port;


    /*Parse the html cotent and get the list*/

    public static class ClonedSer {


        static int port;
        static String ip;
        static String s;
        static int flag=0;
        //static ArrayList<String > downloadList;
        static String dest;
        static String folder;

        public ClonedSer(String ii,String de,String fold)
        {
            ip=ii;
            port=10000;
            //downloadList=dl;
            dest=de;
            folder=fold;
        }

        /*public static void main(String[] args) throws Exception {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server starts at "+port+"!!");
            flag=0;
            server.createContext("/test", new MyHandler());

            server.createContext("files",new getFileNo());
            server.createContext("/get",new GotHandler());
            server.createContext("/list",new ListHandler());
            server.setExecutor(null); // creates a default executor
            server.start();


        }*/


        public static Map<String, String> queryToMap(String query){

            Map<String, String> result = new HashMap<String, String>();
            for (String param : query.split("&")) {
                String pair[] = param.split("=");
                if (pair.length>1) {
                    result.put(pair[0], pair[1]);
                }else{
                    result.put(pair[0], "");
                }
            }
            return result;
        }

        static class getFileNo implements HttpHandler {

            @Override
            public void handle(HttpExchange httpExchange) throws IOException {

                String string=Integer.toString(downloadList.size());
                httpExchange.sendResponseHeaders(200,string.length());
                OutputStream os = httpExchange.getResponseBody();
                os.write(string.getBytes());
                os.close();
            }
        }
        static class ListHandler implements HttpHandler{
            String ip;
            ListHandler(String ii)
            {
                ip=ii;
            }
            @Override
            public void handle(HttpExchange t) throws IOException {
                //String name = "C:/Users/Pupul/Desktop/Folder";
                //File folder = new File(name);
                String st="";
                for(int p=0;p<downloadList.size();p++)
                {
                    String x= downloadList.get(p);
                    st =st+"<a href=\"/get?name="+x+"\">"+x+"</a>";
                    st=st+"<br>";


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
                String response = "Server starts at port no= "+port;

                t.sendResponseHeaders(200, response.length());
                OutputStream os = t.getResponseBody();
                os.write(response.getBytes());
                os.close();
            }

        }

        static class GotHandler implements HttpHandler{

            String destination;
            String Folder;
            public GotHandler(String ds,String folder)
            {
                destination=ds;
                Folder=folder;
            }
            @Override
            public void handle(HttpExchange t) throws IOException {
                Map<String,String> parms = queryToMap(t.getRequestURI().getQuery());
                s= parms.get("name");
                System.out.println(s+"!!!!");
                //String name = "C:/Users/Pupul/Desktop/Folder";
                int g = 0;
                //File folder = new File(name);
                for (int p=0;p<downloadList.size();p++) {
                    String x = downloadList.get(p);
                    if (x.equals(s)) {
                        Headers h = t.getResponseHeaders();
                        String ext1 = FilenameUtils.getExtension(s);
                        if(ext1.equals("jpg") || ext1.equals("png") || ext1.equals("jpeg"))
                            h.add("Content-Type", "image/"+ext1);
                        else if(ext1.equals("txt"))
                            h.add("Content-Type","text/plain");
                        else
                            h.add("Content-Type", "application/"+ext1);

                        File gfile = new File (destination+Folder+"/"+s);
                        byte [] bytearray  = new byte [(int)gfile.length()];
                        FileInputStream fis = new FileInputStream(gfile);
                        BufferedInputStream bis = new BufferedInputStream(fis);
                        bis.read(bytearray, 0, bytearray.length);

                        t.sendResponseHeaders(200, gfile.length());

                        OutputStream os = t.getResponseBody();
                        os.write(bytearray,0,bytearray.length);

                        os.close();
                        g=1;
                        break;

                    }
                }
                if (g == 0){
                    String response = "No file of that name!!!!";

                    t.sendResponseHeaders(200, response.length());
                    OutputStream os = t.getResponseBody();
                    os.write(response.getBytes());
                    os.close();

                }
                flag=1;

            }
        }

        public static void StartServer() throws IOException {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            System.out.println("Server starts at "+port+"!!");
            flag=0;
            server.createContext("/test", new ClonedSer.MyHandler());

            server.createContext("/files",new ClonedSer.getFileNo());
            server.createContext("/get",new ClonedSer.GotHandler(dest,folder));
            server.createContext("/list",new ClonedSer.ListHandler(ip));
            server.setExecutor(null); // creates a default executor
            server.start();
        }





    }


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
    public static void downloadforclient(String downloadFolder,String Dest,String ip) throws InterruptedException {

        for(int ft=0;ft<totalFileList.size();ft++) {
            while(true) {
                String x = totalFileList.get(ft);
                int flag = 0;
                int len=downloadList.size();
                System.out.println("Size= "+len);
                for (int j = 0; j < len; j++) {
                    //System.out.println(downloadList.get(j)+"Hi");
                    if (downloadList.get(j).equals(x)) {
                        flag++;
                        break;
                    }
                }
                if (flag == 1) {
                    String fileURL = "http://" + ip + ":10000/get?name=" + x;
                    String saveDir = Dest + downloadFolder + "/" + x;
                    try {
                        Client.downloadFile(fileURL, saveDir);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                } else
                    System.out.println("File not there still now "+ x);
                //Thread.sleep(1000);
                if(flag==1)
                    break;
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
        cur_port=10000;
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
        }
        else {
            copyFolder = "Download";
        }
        clientThread1 clientThread1 = new clientThread1(Destination,ipAddress);
        Thread thread1 = new Thread(clientThread1);
        thread1.start();
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
                downloadThingy(downloadFolder,Dest,ipAddress);  /* file download starts */

            }
            else {
                ClonedSer clonedServer = new ClonedSer(ipAddress,Dest,downloadFolder);
                try {
                    clonedServer.StartServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    downloadforclient(copyFolder,Dest,ipAddress);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }


        }
    }



}
