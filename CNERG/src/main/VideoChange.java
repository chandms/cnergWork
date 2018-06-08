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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.security.krb5.internal.crypto.Des;

import javax.print.attribute.standard.Destination;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.net.NetworkInterface;
import java.util.concurrent.Semaphore;


public class VideoChange {


    static ArrayList<String> totalFileList;
    static ArrayList<String> downloadList;
    static int FileFlag;
    static int myId;
    static int myPortNo;
    static String DecDestination;
    static ArrayList<ArrayList<String>> layerList;
    static ArrayList<Integer> ids;
    public static int noOfLayers;
    public static int noOfSegments;
    public static String initFile;


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
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Waiting for new name..............");
                String fileName = null;
                try {
                    fileName = in.readUTF();
                } catch (IOException e) {
                    //e.printStackTrace();
                }
                if (fileName == null)
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
                } else {
                    try {
                        out.writeUTF("-1");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }

        }
    }

    public static class ServerForm implements Runnable {
        String Folder;
        String Destination;
        int conPort;

        public ServerForm(String fold, String Dest, int port) {
            Folder = fold;
            Destination = Dest;
            conPort = port;
        }

        @Override
        public void run() {

            ServerSocket serverSocket = null;
            try {
                serverSocket = new ServerSocket(0);
                myPortNo = serverSocket.getLocalPort();
                System.out.println("I have decided my port no " + myPortNo);
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

        Document doc = Jsoup.connect("http://" + ip + ":8081/mpdList").get();
        Elements links = doc.getElementsByTag("h1");
        ArrayList<String> fileList = new ArrayList<String>();
        for (Element link : links) {
            String linkText = link.text();
            fileList.add(linkText);
        }
        return fileList;
    }

    public static ArrayList<String> getCoClient(String ip) {
        Document doc = null;
        try {
            doc = Jsoup.connect("http://" + ip + ":8081/sip").get();
        } catch (IOException e) {
            // e.printStackTrace();
        }
        Elements header = doc.getElementsByTag("h1");
        ArrayList<String> fileList = new ArrayList<String>();
        for (Element head : header) {
            String headText = head.text();
            fileList.add(headText);
        }
        return fileList;
    }

    public static Map<String, Object> parseMpd(String dest,String ip,String mpdFileName)
            throws ParserConfigurationException, SAXException, IOException {
        System.out.println("mpdFile "+mpdFileName);
        File inputFile = new File(mpdFileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        org.w3c.dom.Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        String segmentBase = ((org.w3c.dom.Element) doc.getElementsByTagName("Initialization").item(0)).getAttribute("sourceURL");
        NodeList nodeList = doc.getElementsByTagName("Representation");

        int width = 0;
        int height = 0;
        int duration = 0;
        int numberOfSegments = 0;
        boolean first = true;
        List<Integer> idList = new ArrayList<>();
        List<Double> bwList = new ArrayList<>();
        List<List<String>> segmentUrls = new ArrayList<>();
        for (int itr = 0; itr < nodeList.getLength(); itr++) {
            Node node = nodeList.item(itr);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                org.w3c.dom.Element element = (org.w3c.dom.Element) node;
                org.w3c.dom.Element segElement = (org.w3c.dom.Element) element.getElementsByTagName("SegmentList").item(0);
                if (first) {
                    width = Integer.parseInt(element.getAttribute("width"));
                    height = Integer.parseInt(element.getAttribute("height"));
                    duration = Integer.parseInt(segElement.getAttribute("duration"));
                    numberOfSegments = segElement.getElementsByTagName("SegmentURL").getLength();
                    first = false;
                }

                idList.add(Integer.parseInt(element.getAttribute("id")));
                bwList.add(Double.parseDouble(element.getAttribute("bandwidth")));
                List<String> tempSegUrls = new ArrayList<>();
                NodeList urlNodes = segElement.getElementsByTagName("SegmentURL");
                for (int jtr = 0; jtr < urlNodes.getLength(); jtr++) {
                    Node urlNode = urlNodes.item(jtr);
                    if (urlNode.getNodeType() == Node.ELEMENT_NODE) {
                        tempSegUrls.add(((org.w3c.dom.Element) urlNode).getAttribute("media"));
                    }
                }
                segmentUrls.add(tempSegUrls);
            }
        }


        String fileURL = "http://" + ip + ":8081/get?name=" + segmentBase;
        String saveDir = dest + "SpecialFolder/" + segmentBase;
        try {
            Client.downloadFile(fileURL, saveDir);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        initFile=saveDir;

        Map<String, Object> resultMap = new HashMap<>();
        //System.out.println(height+" "+width+" "+duration+" "+numberOfSegments+" "+idList+" "+bwList+" "+segmentBase+" "+segmentUrls);
        resultMap.put("height", height);
        resultMap.put("width", width);
        resultMap.put("duration", duration);
        resultMap.put("numberOfSegments", numberOfSegments);
        resultMap.put("idList", idList);
        resultMap.put("bwList", bwList);
        resultMap.put("segmentBase", segmentBase);
        resultMap.put("segmentUrls", segmentUrls);
        return resultMap;
    }


    public  static void createFile(String address,String ip,String Destination) throws IOException, ParserConfigurationException, SAXException {
        URL oracle = new URL(address);
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                yc.getInputStream()));
        String inputLine, f = "";
        while ((inputLine = in.readLine()) != null) {
            f = f + inputLine + "\n";
            System.out.println(inputLine);
        }
        in.close();

        File file = new File(Destination + "mympdfile.mpd");
        PrintWriter writer = new PrintWriter(Destination + "mympdfile.mpd", "UTF-8");
        writer.append(f);
        writer.close();
        Map<String, Object> map = parseMpd(Destination,ip,Destination + "mympdfile.mpd");
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if(entry.getKey().equals("idList"))
            {
                List<Integer> list = (List<Integer>) entry.getValue();
                ids= (ArrayList<Integer>) entry.getValue();
                noOfLayers=list.size();

            }
            else if(entry.getKey().equals("numberOfSegments"))
            {
                noOfSegments= (int) entry.getValue();
            }
            // System.out.println(entry.getKey()+": "+entry.getValue());
        }
        System.out.println(noOfLayers+" "+noOfSegments);



    }

    /* According to the list of server, files are downloaded*/

    public static void downloadThingy(String downloadFolder, String Dest, String ip, String filename) throws InterruptedException {

        String fileURL = "http://" + ip + ":8081/get?name=" + filename;
        String saveDir = Dest + downloadFolder + "/" + filename;
        try {
            Client.downloadFile(fileURL, saveDir);
            downloadList.add(filename);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void check(String ip) {

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
        String decodedString = null;
        String ff = "";
        try {
            while ((decodedString = in.readLine()) != null) {
                ff = ff + decodedString;
                System.out.println("heyyapupul" + decodedString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        myId = Integer.parseInt(ff);
        System.out.println("hey yo " + myId);
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


    public static void TestIp(String ipofServer, String address) {
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

    }

    public static void newCli(String copyingFolder, String Dest, String ipAddress, String myPort, String fileName) {
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
        if (fileLen == -1) {
            FileFlag = 0;
        } else {
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

    public static class Discoverer implements Runnable {
        String ipAddress;

        public Discoverer(String ip) {
            ipAddress = ip;
        }

        @Override
        public void run() {
            while (true) {
                check(ipAddress);
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
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
       ids=new ArrayList<Integer>();

        String Destination = args[0];
        String ipAddress = args[1];
        DecDestination = Destination+"SpecialFolder/";
        System.out.println(Destination);
        totalFileList = getList(ipAddress);
        Collections.shuffle(totalFileList);
        downloadList = new ArrayList<String>();
        createFile("http://" + ipAddress + ":8081/mpdFile",ipAddress,Destination);
        layerList= new ArrayList< ArrayList<String>>(ids.size());

        for(int o=0;o<ids.size();o++)
        {
            ArrayList<String> ad= new ArrayList<String>();
            layerList.add(ad);
        }
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

            ServerForm serverForm = new ServerForm(Folder, Dest,0);
            Thread serverThread = new Thread(serverForm);
            serverThread.start();

            check(ipAddress);
            Discoverer discoverer= new Discoverer(ipAddress);
            Thread thread1 = new Thread(discoverer);
            thread1.start();
            String address ="myownIP";
            TestIp(ipAddress,address+" "+"id = "+myId+" "+myPortNo);
            ArrayList<String> st = new ArrayList<String>();
            try {
                st = getCoClient(ipAddress);
            } catch (Exception e) {
                //e.printStackTrace();
            }
            System.out.println("these are my co clients");
            if(st==null)
                System.out.println("No client still now........");
            else {
                for (int u = 0; u < st.size(); u++) {
                    System.out.println(st.get(u));
                }
            }

            for (int j = 0; j < totalFileList.size(); j++) {

                try {
                    st = getCoClient(ipAddress);
                } catch (Exception e) {
                    // e.printStackTrace();
                }
                FileFlag = 0;
                String fileName = totalFileList.get(j);
                for (int jk = 0; jk < st.size(); jk++) {
                    String ipr = "";
                    String myPort = "";
                    if (st.get(jk).indexOf("myownIP") != -1) {
                        if ((st.get(jk).indexOf("id = " + myId) == -1)) {
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

                            System.out.println("I am connecting with " + ipr + "and " + myPort);
                            newCli(Folder, Dest, ipr, myPort, fileName);
                            if (FileFlag == 1)
                                break;
                        }

                    }
                }
                System.out.println("my flag =" + FileFlag);
                if (FileFlag == 0) {

                    try {
                        downloadThingy("SpecialFolder", Dest, ipAddress, fileName);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                /*Random random = new Random();
                int rn = random.nextInt(5000) + 500;
                System.out.println("I am waiting randomly for " + rn + "time");
                try {
                    Thread.sleep(rn);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }*/
                String fileNameWithOutExt = FilenameUtils.removeExtension(fileName);
                int fg=0;
                for(int v=0;v<ids.size();v++)
                {
                    String ch="L"+ids.get(v);
                    if(fileNameWithOutExt.indexOf(ch)!=-1)
                    {
                        layerList.get(ids.get(v)).add(fileName);
                        break;
                    }
                }
                /*String converted="";
                String temp=fileName;
                int uc=0;
                while(uc<temp.length() && temp.charAt(uc)!='-') {
                    converted = converted+temp.charAt(uc);
                    uc++;
                }



                String decoder= fileNameWithOutExt+"-EL3.yuv";
                converted=fileNameWithOutExt+"-EL3.264";
                try {
                    Decode decode = new Decode(DecDestination,"video_1.264.init.svc",fileName,converted,decoder);
                    decode.decoder();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

            }
            for(int j=0;j<layerList.size();j++)
            {
                for(int k=0;k<layerList.get(j).size();k++)
                    System.out.println(layerList.get(j).get(k));
                System.out.println("-------------------------------");
            }
            //initFile=Dest+"factory-III.init.svc";

            String converted="";
            String temp="factory-III.seg0-L0.svc";
            int uc=0,g=0;
            while(uc<temp.length()) {
                if(temp.charAt(uc)=='-')
                    g++;
                converted = converted+temp.charAt(uc);
                if(g==2)
                    break;
                uc++;
            }
            for(int j=0;j<=3;j++)
            {
                String gh="";
                if(j==0)
                {
                    gh="BL";
                }
                else
                    gh="EL"+j;
                String decoded=converted+gh+".yuv";
                String conv =converted+gh+".264";
                try {
                    Decode decode = new Decode(Dest,initFile,temp,conv,decoded,j);
                    decode.decoder();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }
}