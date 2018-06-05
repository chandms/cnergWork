package extra;

import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;

public class watcher {


    ///////////////////extra usable
    public static void copyThingy(String downloadFolder,String copyFolder)
    {
        Path faxFolder = Paths.get("C:/Users/Pupul/Desktop/"+downloadFolder);
        WatchService watchService = null;
        try {
            watchService = FileSystems.getDefault().newWatchService();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            faxFolder.register(watchService, StandardWatchEventKinds.ENTRY_CREATE,StandardWatchEventKinds.ENTRY_DELETE,StandardWatchEventKinds.ENTRY_MODIFY);
        } catch (IOException e) {
            e.printStackTrace();
        }

        boolean valid = true;
        do {
            WatchKey watchKey = null;
            try {
                watchKey = watchService.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (WatchEvent event : watchKey.pollEvents()) {
                WatchEvent.Kind kind = event.kind();
                if (StandardWatchEventKinds.ENTRY_CREATE.equals(event.kind())) {
                    String fileName = event.context().toString();
                    System.out.println("File Created:" + fileName);
                    String dir= "C:/Users/Pupul/Desktop/"+downloadFolder;
                    File file = new File(dir+"/"+fileName);
                    File source = new File("C:/Users/Pupul/Desktop/"+downloadFolder);
                    File dest = new File("C:/Users/Pupul/Desktop/"+copyFolder+"/"+fileName);
                    FileChannel sourceChannel = null;
                    FileChannel destChannel = null;
                    try {
                        sourceChannel = new FileInputStream(file).getChannel();
                        destChannel = new FileOutputStream(dest).getChannel();
                        destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally{
                        try {
                            sourceChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            destChannel.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (StandardWatchEventKinds.ENTRY_DELETE.equals(event.kind())) {
                    String fileName = event.context().toString();
                    System.out.println("File Deleted:" + fileName);
                }
            }
            valid = watchKey.reset();

        } while (valid);
    }


}
