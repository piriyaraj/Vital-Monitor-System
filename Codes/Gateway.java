import java.io.*;
import java.net.*;
// import java.util.*;
import java.util.concurrent.TimeUnit;


public class Gateway {
    // Create a Datagramsocket with the localhost and port number 6000
    public static void main(String[] args) {
        int BROADCAST_PORT = 6001;
        InetAddress inetAddress;
        String monitorId;
        int port;
        DatagramSocket dSocket = null;
        DatagramPacket packet = null;
        byte[] barray = new byte[40000];
        Socket vitalMonitorSocket = null;
        
        while (true) {
            try {
                // creating broadcast socket
                dSocket = new DatagramSocket(BROADCAST_PORT);

                // broadcast packet
                packet = new DatagramPacket(barray, barray.length);
                dSocket.receive(packet);

                // Deserilize the byte array in to object
                Monitor monitor = (Monitor) deserialize(barray);

                // Monitor properties
                monitorId = monitor.getMonitorID();
                port = monitor.getPort();
                inetAddress = monitor.getIp();
                System.out.println("MonitorID: " + monitorId + " Port Number: " + port + " IP Address: " + inetAddress);

                // TCP socket
                vitalMonitorSocket = new Socket(inetAddress, port);
                System.out.println(monitorId + " is Connected");

                // Receiving message from vital monitors
                String message = messageSerializer(vitalMonitorSocket);
                System.out.println(message);

                customDelayInSeconds(2);
                // dSocket.close();    //Closing the broadcastSocket
            }

            // Exception Handlings
            catch (Exception e) {
                e.getStackTrace();
            }
        } 
    }

    // Custom Delay
    private static void customDelayInSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Byte - Deserilizer 
    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        ObjectInputStream obj = new ObjectInputStream(byteStream);
        return obj.readObject();
    }

    public static String messageSerializer(Socket socket) throws IOException {
            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            BufferedReader br = new BufferedReader(in);
            String str = br.readLine();
            return str;
    }

    }//End of class