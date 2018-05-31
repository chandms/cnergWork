package extra;

import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardWatchEventKinds;

public class ServerClient {
    String downloadFolder;
    String copyFolder;

    public ServerClient(String x,String y)
    {
       downloadFolder =  x;
       copyFolder =y;
    }


        public  void startServer() throws IOException {

            int ii=9000;
            while(true) {
                ServerSocket serverSocket = new ServerSocket(ii);
                ii++;
                Socket socket = serverSocket.accept();
                System.out.println("Accepted connection : " + socket);
                System.out.println("Waiting for a file name");
                DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
                String line = in.readUTF();
                System.out.println("Got filename "+line);
                int flag=0;
                String dir="C:/Users/Pupul/Desktop/"+downloadFolder;
                File folder = new File(dir);
                for(File file: folder.listFiles())
                {
                    if(file.getName().equals(line))
                    {
                        flag++;
                        break;
                    }
                }
                if(flag==1) {
                    File transferFile = new File(dir+"/"+line);
                    byte[] bytearray = new byte[(int) transferFile.length()];
                    System.out.println("Length= "+transferFile.length());
                    FileInputStream fin = new FileInputStream(transferFile);
                    BufferedInputStream bin = new BufferedInputStream(fin);
                    bin.read(bytearray, 0, bytearray.length);
                    System.out.println("Length= "+bytearray.length);
                    OutputStream os = socket.getOutputStream();
                    System.out.println("Sending Files...");
                    os.write(bytearray, 0, bytearray.length);
                    os.flush();
                    socket.close();
                    System.out.println("File transfer complete");
                }
                else
                {
                    System.out.println("No file such that");
                }


            }
        }
}
