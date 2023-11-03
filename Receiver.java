import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.SwingUtilities;

import org.jdom2.Document;

import ObjectPool.ArrayOfObjects;
import ObjectPool.SimpleObject;

public class Receiver
{
    private static int port = 4000;
    
    public static void main(String[] args)
    {     
        try {
            System.out.println(
                "\nThe Receiver program listens for socket connections from " + //
                "Sender program to\nconvert incoming byte stream into JDOM " + //
                "document deserialize the document into\nobjects display the " + //
                "objects to screen\n"+String.format("%-80s", "").replace(' ', '-'));

            ServerSocket sock = new ServerSocket(port);
            System.out.println("Server listening...");

            while(true)
            {
                Socket conn = sock.accept();
                ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
                System.out.println("Connected.");

                System.out.println("Deserializing received object...");
                Object obj = new Deserializer().deserialize((Document) in.readObject());

                System.out.println("Visualizing object...");
                runInspection(obj, "deserialized.txt", true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void runInspection(Object obj, String file, boolean recursive) 
    {
        try {
            FileOutputStream outFStream = new FileOutputStream(new File(file));
            PrintStream pStream = new PrintStream(outFStream);
            System.setOut(pStream);

            //SwingUtilities.invokeLater(() -> { new ObjectVisualizer(obj).setVisible(true); });

            new ObjectVisualizer(obj).setVisible(true);

            pStream.flush();
            outFStream.flush();
            pStream.close();
            outFStream.close();
            System.setOut(System.out);

            // print file to console
            BufferedReader in = new BufferedReader(new FileReader(file));
            in.lines().forEach(line -> { System.out.println(line); });
            in.close();

        } catch (IOException ioe) {
            System.err.println("WARNING: Unable to open file: " + file);

        } catch (Exception e) {
            System.err.println("WARNING: Unable to finish running test: " + obj);
            e.printStackTrace();
        }
    }
}