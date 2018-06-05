package extra;

import sun.security.krb5.internal.crypto.Des;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.Properties;

public class conf
{
    Properties prop;
    String Dest;
    public conf(String destination)
    {
        Dest = destination;
        prop = new Properties();
        InputStream input =null;
        try {
            input =new FileInputStream(Dest+"mycfg.properties");
            prop.load(input);
        }catch(Exception eta){
            eta.printStackTrace();
        }
    }

    public String getProperty(String key)
    {
        String value = prop.getProperty(key);
        return value;
    }
}