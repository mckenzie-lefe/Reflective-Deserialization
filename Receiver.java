import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import org.jdom2.Document;


public class Receiver
{
    private static int port = 4000;
    
    public static void main(String[] args) {     
        ServerSocket sock;
        try {
            System.out.println(
                "\nThe Receiver program listens for socket connections from " + //
                "Sender program to\nconvert incoming byte stream into JDOM " + //
                "document deserialize the document into\nobjects display the " + //
                "objects to screen\n"+String.format("%-80s", "").replace(' ', '-'));

            sock = new ServerSocket(port);

            while(true) {
                System.out.println("\nServer listening...");
                Socket conn = sock.accept();
                ObjectInputStream in = new ObjectInputStream(conn.getInputStream());
                System.out.println("Connected.");

                System.out.println("Deserializing received object...");
                Object obj = new Deserializer().deserialize((Document) in.readObject());

                System.out.println("Visualizing object...");
                runInspection(obj, "deserialized.txt");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static void runInspection(Object obj, String file) {
        try {
            ObjectVisualizer objVisual = new ObjectVisualizer(obj);
            objVisual.saveTree(file);

        } catch (Exception e) {
            System.err.println("WARNING: Unable to visualize " + obj);
            e.printStackTrace();
        }
    }
}