import java.io.*;
import java.net.*;
import java.util.*;
// import java.util.*;
import java.util.concurrent.TimeUnit;

class VitalReader extends Thread{
    byte[] barray;
    String monitorId;
    int port;
    Socket vitalMonitorSocket = null;
    InetAddress inetAddress;
    Monitor monitor;
    DatagramSocket dSocket;
    String message;
    VitalReader(byte[] barray,DatagramSocket dSocket){
        this.barray=barray;
        this.dSocket=dSocket;
    }
    public void run(){
        try {
            monitor = (Monitor) deserialize(barray);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        monitorId = monitor.getMonitorID();
        port = monitor.getPort();
        inetAddress = monitor.getIp();
        //System.out.println("MonitorID: " + monitorId + " Port Number: " + port + " IP Address: " + inetAddress);

        // TCP socket
        try{
            vitalMonitorSocket = new Socket(inetAddress, port);
        }
        catch(Exception e){
            e.printStackTrace();
        }

        // Receiving message from vital monitors
        InputStreamReader in;
        BufferedReader br;
        try {
            in = new InputStreamReader(vitalMonitorSocket.getInputStream());
            br = new BufferedReader(in);
            message = br.readLine();
            
        } catch (Exception e) {
            // e.printStackTrace();
            message="Not Received";
            //TODO: handle exception
        }
        System.out.println(message);

        customDelayInSeconds(2);
            //Closing the broadcastSocket
    }
    public static void customDelayInSeconds(int seconds) {
    try {
        TimeUnit.SECONDS.sleep(seconds);} 
    catch (InterruptedException e) {
        e.printStackTrace();}
    }
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
}

public class Gateway {
    // Create a Datagramsocket with the localhost and port number 6000
    public static void main(String[] args) {
        int BROADCAST_PORT = 6001;
        Monitor monitor;
        String monitorId;
        DatagramSocket dSocket = null;
        DatagramPacket packet = null;
        byte[] barray = new byte[40000];
        VitalReader vr1=null;
        List<String> monitorList = new ArrayList<>();
        while (true) {
            try {
                // creating broadcast socket
                dSocket = new DatagramSocket(BROADCAST_PORT);
                
                // broadcast packet
                packet = new DatagramPacket(barray, barray.length);
                dSocket.receive(packet);
                monitor = (Monitor) deserialize(barray);
                monitorId = monitor.getMonitorID();
                if(!monitorList.contains(monitorId)){
                    vr1=new VitalReader(barray,dSocket);
                    vr1.start();
                    monitorList.add(monitorId);

                }
                dSocket.close();

                // Monitor properties
                
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