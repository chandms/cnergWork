package main;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.omg.PortableInterceptor.INACTIVE;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.xml.sax.SAXException;
import sun.security.krb5.internal.crypto.Des;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.InetAddress;
import java.sql.Time;
import java.util.*;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicReferenceArray;


public class ServerCredit {

    static int port=8081;
    static String s;
    static int flag=0;
    static Set<String> arr;
    static int id;
    static HashMap<String,Integer> hmap;
    static HashMap<String,String> PortMap;
    static HashMap <String,Thread> myThread;
    static HashMap<String,Integer> IpMap;
    static String mpdFileName="output.mpd";
    static int maxCredit;
    static HashMap<Integer,Integer> creditMap;
    static Integer numOfSegment;


    public static void main(String args[]) throws IOException, ParserConfigurationException, SAXException {

        String Destination = args[0];
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);


        System.out.println("Server starts at " + port + "!!");
        flag = 0;
        arr= new HashSet<>();
        id=1000;
        hmap=new HashMap<String,Integer>();
        PortMap = new HashMap<String,String>();
        myThread = new HashMap<String,Thread>();
        IpMap = new HashMap<String,Integer>();
        creditMap = new HashMap<Integer, Integer>();
        maxCredit=0;
        numOfSegment=0;


        server.createContext("/test", new MyHandler());
        server.createContext("/get", new GotHandler(Destination));
        server.createContext("/list", new ListHandler(Destination));
        server.createContext("/ip",new getIp());
        server.createContext("/myip",new getOwnIP());
        server.createContext("/sip",new listIp());
        server.createContext("/mpdList",new getMpdList(Destination));
        server.createContext("/mpdFile",new uploadMpdFile(Destination));
        server.createContext("/allow",new allowable(Destination));
        server.setExecutor(null); // creates a default executor
        server.start();

        Map <String,Object> res= Utils.parseMpd(Destination+mpdFileName);
        for(HashMap.Entry<String,Object> entry: res.entrySet())
        {
            if(entry.getKey().equals("numberOfSegments"))
            {
                numOfSegment= (Integer) entry.getValue();
            }
        }
        for(int j=0;j<numOfSegment;j++)
            creditMap.put(j,0);




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
    static class allowable implements HttpHandler{
        String Destination;
        public allowable(String dest){Destination=dest;}

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {

            InputStream is = httpExchange.getRequestBody();
            String x= IOUtils.toString(is, "UTF-8");
            int pos=x.indexOf("seg");
            pos=pos+3;

            String segNo="";
            int i=pos;
            while (x.charAt(i)!='-') {
                segNo = segNo + x.charAt(i);
                i++;
            }

            int seg = Integer.parseInt(segNo);
            int cc= creditMap.get(seg);
            i=i+2;
            String lId="";
            while (x.charAt(i)!='.') {
                lId = lId + x.charAt(i);
                i++;
            }
            int layerId = Integer.parseInt(lId);

            cc=cc+layerId+1;
            System.out.println("check current: "+seg+" "+layerId+" "+creditMap.get(seg)+" "+maxCredit+" "+cc);
            String resp="";
            if(cc>maxCredit)
                resp="No";
            else
                resp="Yes";

            if(resp.equals("Yes"))
            {
                if(layerId==0)
                    creditMap.put(seg,creditMap.get(seg)+1);
                else if(layerId==1)
                    creditMap.put(seg,creditMap.get(seg)+2);
                else
                    creditMap.put(seg,creditMap.get(seg)+3);
            }
            System.out.println("Server's credit:  ");

            for(int j=0;j<creditMap.size();j++)
                System.out.println("segment "+j +" = "+creditMap.get(j));

            httpExchange.sendResponseHeaders(200,resp.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(resp.getBytes());
            os.close();




        }
    }
    static class getMpdList implements HttpHandler{
        String Destination;
        public getMpdList(String dest)
        {
            Destination=dest;
        }
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            ArrayList <String > mpdList = new ArrayList<String>();

            try {
                mpdList=Utils.getVideoUrls(Destination+mpdFileName);
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
            String res="";
            for(int j=0;j<mpdList.size();j++) {
                res = res + "<h1>"+mpdList.get(j)+"</h1>";
                res=res+"<br>";
            }
            httpExchange.sendResponseHeaders(200, res.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(res.getBytes());
            os.close();
        }


    }
    static class uploadMpdFile implements HttpHandler{
        String Destination;
        public uploadMpdFile(String dest)
        {
            Destination=dest;
        }
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new FileReader(Destination+mpdFileName))){
                String sCurrentLine;
                while ((sCurrentLine = br.readLine()) != null) {
                    sb.append(sCurrentLine);
                }

            }

            String f= sb.toString();
            httpExchange.sendResponseHeaders(200, f.length());
            OutputStream os = httpExchange.getResponseBody();
            os.write(f.getBytes());
            os.close();


        }
    }
    static class listIp implements HttpHandler{
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            int len=hmap.size();
            System.out.println("hmap length = "+len);
            String tt="";
            for(HashMap.Entry<String,Integer> entry: hmap.entrySet() )
            {
                String ip=entry.getKey();
                int id=entry.getValue();
                String idm= Integer.toString(id);
                int po=IpMap.get(ip);
                String port = Integer.toString(po);
                System.out.println(id+" "+ip+" "+port);
                tt=tt+"<h1>"+"ip= "+ip+" "+port+" "+"myownIP"+" "+"id = "+idm+"</h1>";
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
            String tt="";
            String md = "";
            int r = 13;
            while (r < x.length() && x.charAt(r)!=' ') {
                md = md + x.charAt(r);
                r++;
            }
            r++;
            String port="";
            while(r< x.length() && x.charAt(r)!=' ')
            {
                port=port+x.charAt(r);
                r++;
            }
            if(md!="" && !IpMap.containsKey(ff.toString())) {

                maxCredit++;
                System.out.println("now maxcredit is "+maxCredit);
                int myPort = Integer.parseInt(port);

                System.out.println("Yes detecting id "+md);
                PortMap.put(md, ff.toString());
                IpMap.put(ff.toString(),myPort);
                System.out.println(x);
                is.close();

            }
            else if(IpMap.containsKey(ff.toString()))
                System.out.println("Already there in ip map");
            else
                System.out.println("normal user accessing");
            t.sendResponseHeaders(200, tt.length());
            OutputStream os = t.getResponseBody();
            os.write(tt.getBytes());
            os.close();
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
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
            maxCredit--;
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
            //sg=sg+" "+ff;
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
        public GotHandler(String dest) throws IOException {
            Destination=dest;
        }
        @Override
        public void handle(HttpExchange t) throws IOException {
            Map<String,String> parms = queryToMap(t.getRequestURI().getQuery());
            s= parms.get("name");
            System.out.println(s+"!!!!");
                String name = Destination + "Folder";
                int g=0;
                File folder = new File(name);
                for (File file : folder.listFiles()) {
                    String x = file.getName();
                    if (x.equals(s)) {
                        Headers h = t.getResponseHeaders();
                        String ext1 = FilenameUtils.getExtension(s);
                        System.out.println("my Extension is " + ext1);
                        if (ext1.equals("jpg") || ext1.equals("png") || ext1.equals("jpeg"))
                            h.add("Content-Type", "image/" + ext1);
                        else if (ext1.equals("txt"))
                            h.add("Content-Type", "text/plain");
                        else if (ext1.equals("svc"))
                            h.add("Content-Type", "application/vnd.dvb.service");
                        else
                            h.add("Content-Type", "application/" + ext1);

                        File gfile = new File(Destination + "Folder/" + s);
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
