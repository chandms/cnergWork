package main;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FilenameUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Server {

    static int port=7000;
    static String s;
    static int flag=0;
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
    public static void StartServer() throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        System.out.println("Server starts at "+port+"!!");
        flag=0;
        server.createContext("/test", new MyHandler());

        server.createContext("/files",new getFileNo());
        server.createContext("/get",new GotHandler());
        server.createContext("/list",new ListHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

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

    static class getFileNo implements HttpHandler{
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            File folder = new File("C:/Users/Pupul/Desktop/Folder");
            int count=0;
            for(File file: folder.listFiles())
            {
                count++;
            }
            String string=Integer.toString(count);
            httpExchange.sendResponseHeaders(200,string.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(string.getBytes());
            os.close();
        }
    }
    static class ListHandler implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException {
            String name = "C:/Users/Pupul/Desktop/Folder";
            File folder = new File(name);
            String st="";
            for(File file : folder.listFiles())
            {
                String x= file.getName();
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
        @Override
        public void handle(HttpExchange t) throws IOException {
            Map<String,String> parms = queryToMap(t.getRequestURI().getQuery());
            s= parms.get("name");
            System.out.println(s+"!!!!");
            String name = "C:/Users/Pupul/Desktop/Folder";
            int g = 0;
            File folder = new File(name);
            for (File file : folder.listFiles()) {
                String x = file.getName();
                if (x.equals(s)) {
                    Headers h = t.getResponseHeaders();
                    String ext1 = FilenameUtils.getExtension(s);
                    if(ext1.equals("jpg") || ext1.equals("png") || ext1.equals("jpeg"))
                        h.add("Content-Type", "image/"+ext1);
                    else if(ext1.equals("txt"))
                        h.add("Content-Type","text/plain");
                    else
                        h.add("Content-Type", "application/"+ext1);

                    File gfile = new File ("C:/Users/Pupul/Desktop/Folder/"+s);
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





}
