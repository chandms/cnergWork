package extra;

import main.Client;
import working.Server;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Downloader {

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

    public static void main(String[] args) throws IOException {

        Properties systemSettings = System.getProperties();
        /*systemSettings.put("proxySet", "true");
        systemSettings.put("http.proxyHost", "172.16.2.30");
        systemSettings.put("http.proxyPort", "8080");
        systemSettings.put("https.proxyHost", "172.16.2.30");
        systemSettings.put("https.proxyPort", "8080");
        String fileURL = "https://jdbc.postgresql.org/download/postgresql-42.2.2.jar";*/
        File folder = new File("C:/Users/Pupul/Desktop/Folder");
        Server.StartServer();
        for(File file: folder.listFiles()) {
            String x =file.getName();
            String fileURL = "http://localhost:8745/get?name="+x;
            String f;
            f = fileURL.substring(fileURL.indexOf('?') + 1);
            Map<String, String> map = queryToMap(f);
            String s = map.get("name");

            String saveDir = "C:/Users/Pupul/Desktop/Download" +"/"+ s;
            try {
                Client.downloadFile(fileURL, saveDir);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
}
