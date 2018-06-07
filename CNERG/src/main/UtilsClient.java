package main;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.*;

import javax.rmi.CORBA.Util;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import sun.misc.IOUtils;
import sun.nio.ch.IOUtil;

public class UtilsClient {

    public static int noOfLayers;
    public static int noOfSegments;

    public static Map<String, Object> parseMpd(String mpdFileName)
            throws ParserConfigurationException, SAXException, IOException {
        System.out.println("mpdFile "+mpdFileName);
        File inputFile = new File(mpdFileName);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();
        String segmentBase = ((Element) doc.getElementsByTagName("Initialization").item(0)).getAttribute("sourceURL");
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
                Element element = (Element) node;
                Element segElement = (Element) element.getElementsByTagName("SegmentList").item(0);
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
                        tempSegUrls.add(((Element) urlNode).getAttribute("media"));
                    }
                }
                segmentUrls.add(tempSegUrls);
            }
        }

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


    public  static void createFile(String address,String Destination) throws IOException, ParserConfigurationException, SAXException {
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
        Map<String, Object> map = parseMpd(Destination + "mympdfile.mpd");
        for (Map.Entry<String, Object> entry : map.entrySet())
        {
            if(entry.getKey().equals("idList"))
            {
                List<Integer> list = (List<Integer>) entry.getValue();
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



}
