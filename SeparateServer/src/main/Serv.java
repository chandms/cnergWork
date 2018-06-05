package main;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.omg.PortableInterceptor.INACTIVE;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.net.InetAddress;
import java.sql.Time;
import java.util.*;
import java.net.InetSocketAddress;



public class Serv {

    static int port=8081;
    static String s;
    static int flag=0;
    static Set<String> arr;
    static int cur_port;
    static int id;
    static HashMap<String,Integer> hmap;
    static HashMap<String,String> PortMap;
    static HashMap <String,Thread> myThread;
    static HashMap<String,Integer> IpMap;



    public static void main(String args[]) throws IOException {

        String Destination = args[0];
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


            System.out.println("Server starts at " + port + "!!");
            flag = 0;
            arr= new HashSet<>();
            cur_port=9000;
            id=1000;
            hmap=new HashMap<String,Integer>();
            PortMap = new HashMap<String,String>();
            myThread = new HashMap<String,Thread>();
            IpMap = new HashMap<String,Integer>();


            server.createContext("/test", new MyHandler());
            server.createContext("/get", new GotHandler(Destination));
            server.createContext("/list", new ListHandler(Destination));
            server.createContext("/ip",new getIp());
            server.createContext("/myip",new getOwnIP());
            server.createContext("/sip",new listIp());
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
    static class listIp implements HttpHandler{
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            int len=hmap.size();
            String tt="";
            for(HashMap.Entry<String,Integer> entry: hmap.entrySet() )
            {
                String ip=entry.getKey();
                int id=entry.getValue();
                String idm= Integer.toString(id);
                int po=IpMap.get(ip);
                String port = Integer.toString(po);
                System.out.println(id+" "+ip+" "+port);
                tt="<h1>"+"ip= "+ip+" "+port+" "+"myownIP"+" "+"id = "+idm+"</h1>";
                tt=tt+ "<br>";
            }
            httpExchange.sendResponseHeaders(200, tt.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(tt.getBytes());
            os.close();
        }
    }

    static class getIp implements HttpHandler{
        @Override
        public void handle(HttpExchange t) throws IOException {
            InputStream is = t.getRequestBody();

            System.out.println("====================");
            InetAddress ff =t.getRemoteAddress().getAddress();
            System.out.println("Hey Chandms "+ff);
            String x= IOUtils.toString(is, "UTF-8");
            if(x!=null && !IpMap.containsKey(ff.toString())) {
                String portNo = Integer.toString(cur_port);
                int myPort = cur_port;
                cur_port++;
                //arr.add("ip= " + ff + " " + portNo + " " + x);
                String md = "";
                int r = 13;
                while (r < x.length()) {
                    md = md + x.charAt(r);
                    r++;
                }
                System.out.println("Yes detecting id "+md);
                //int idm = Integer.parseInt(md);
                PortMap.put(md, ff.toString());
                IpMap.put(ff.toString(),myPort);
                System.out.println(x);
                String tt = "";
                Iterator<String> itr = arr.iterator();
                while (itr.hasNext()) {
                    tt = tt + "<h1>" + itr.next() + "</h1>";
                    tt = tt + "<br>";
                }
                is.close();
                t.sendResponseHeaders(200, tt.length());
                OutputStream os = t.getResponseBody();
                os.write(tt.getBytes());
                os.close();
            }
            else if(IpMap.containsKey(ff.toString()))
                System.out.println("Already there in ip map");
            else
                System.out.println("normal user accessing");
            //t.close();

        }
    }
   public static class Discovery implements Runnable{
        String key;
        int timer;
        public Discovery(String st,int t)
        {
            key=st;
            timer=t;
        }
       @Override
       public void run() {

           while(timer<10)
           {
               System.out.println("my timer "+timer+" "+key);
               timer++;
               try {
                   Thread.sleep(2000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }

           }
           int del=hmap.get(key);
           String delst= Integer.toString(del);
           hmap.remove(key);
           String ipdel=PortMap.get(delst);
           PortMap.remove(delst);
           IpMap.remove(ipdel);

       }
   }
    static class getOwnIP implements HttpHandler{
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            InputStream is = httpExchange.getRequestBody();

            System.out.println("====================");
            InetAddress ff =httpExchange.getRemoteAddress().getAddress();
            System.out.println("Hey macmcs "+ff);
            String searched= ff.toString();
            String sg="";
            if(hmap.containsKey(searched)) {
                System.out.println("Already given key");
                int val=hmap.get(searched);
                sg=Integer.toString(val);
                Thread tf = myThread.get(searched);
                tf.stop();
                Discovery dis = new Discovery(searched,0);
                Thread tn = new Thread(dis);
                myThread.put(searched,tn);
                tn.start();

            }
            else {
                System.out.println("not given key");
                sg = Integer.toString(id);
                hmap.put(searched,id);
                Discovery discovery = new Discovery(searched,0);
                Thread tt = new Thread(discovery);
                tt.start();
                myThread.put(searched,tt);
                id++;
            }
            httpExchange.sendResponseHeaders(200,sg.length());
            String res="";
            for(HashMap.Entry<String,Integer> entry: hmap.entrySet())
            {
                res=res+entry.getKey()+":"+entry.getValue()+"\n";
            }
            OutputStream os = httpExchange.getResponseBody();
            os.write(sg.getBytes());
            os.close();

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