package main;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.InetAddress;
import java.util.*;
import java.net.InetSocketAddress;



public class Serv {

    static int port=8081;
    static String s;
    static int flag=0;
    static Set<String> arr;


    public static void main(String args[]) throws IOException {

        String Destination = args[0];
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


            System.out.println("Server starts at " + port + "!!");
            flag = 0;
            arr= new HashSet<>();




            server.createContext("/test", new MyHandler());
            server.createContext("/get", new GotHandler(Destination));
            server.createContext("/list", new ListHandler(Destination));
            server.createContext("/ip",new getIp());
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

    static class getIp implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();

            System.out.println("====================");
            InetAddress ff =t.getRemoteAddress().getAddress();
            System.out.println("Hey Chandms "+ff);
            String x= IOUtils.toString(is, "UTF-8");
            arr.add("ip= "+ff+" "+x);
            System.out.println(x);
            String tt="";
            Iterator<String> itr = arr.iterator();
            while (itr.hasNext()) {
                tt = tt + "<h1>"+itr.next()+"</h1>";
                tt = tt + "<br>";
            }
            is.close();
            t.sendResponseHeaders(200, tt.length());
            OutputStream os = t.getResponseBody();
            os.write(tt.getBytes());
            os.close();
            //t.close();

        }
    }
    static class ListHandler implements HttpHandler{
        String Destination;
        public ListHandler(String dest)
        {
            Destination=dest;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            String name = Destination+"Folder";
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
        String Destination;
        public GotHandler(String dest)
        {
            Destination=dest;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            Map<String,String> parms = queryToMap(t.getRequestURI().getQuery());
            s= parms.get("name");
            System.out.println(s+"!!!!");
            String name = Destination+"Folder";
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

                    File gfile = new File (Destination+"Folder/"+s);
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
