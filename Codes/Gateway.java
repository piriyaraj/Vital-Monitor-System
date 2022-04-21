/**
 * system provide the method for monitoring the vital monitor
 * when the vital monitor start the connection is established to the gateway
 * after that the vital monitor can sent the data to the gateway
 * gateway can handle multiple vital monitor at same time
*/

// import modules
import java.io.*;
import java.net.*;
import java.util.*;

import java.util.concurrent.TimeUnit;

// class handel the vital monitors separately 
class VitalReader extends Thread{
    /**
     * it can handle every vital monitor connection
     * take monitor as a parameter and make TCP connection with the vital monitor
     * when the connection lost the vital monitor is removed from the gateway
     */

    String monitorId;        // vital monitor unique id
    int port;                // vital monitor connection port
    InetAddress ipAddress;   // ip address of the vital monitor
    Monitor monitor;
    String message;
    VitalReader(Monitor monitor){
        /**
         * pass the decoded byte array that received form the socket packet
         */
        this.monitor=monitor;
    }
    public void run(){
        /**
         * the method handle the tread for the every vital monitor
         */

        monitor=this.monitor;

        // get the monitor parameters
        monitorId = monitor.getMonitorID();
        port = monitor.getPort();
        ipAddress = monitor.getIp();
        System.out.println("=== "+monitorId+" vital connected ===");
        // Receiving message from vital monitors
        // InputStreamReader input;
        // BufferedReader readMessage;
        
        Socket vitalMonitorSocket = null;
           // getting the message every time from the vital monitor
            try {
                vitalMonitorSocket = new Socket(ipAddress, port);
                // System.out.println(this.monitorId + " is Connected");

            // Receiving message from vital monitors
            while (true) {
                InputStreamReader in = new InputStreamReader(vitalMonitorSocket.getInputStream());
                BufferedReader br = new BufferedReader(in);
                String str = br.readLine();
                System.out.println("    >>> "+str);
            }
                
            } catch (Exception e) {
                // e.printStackTrace();
                message="=== connection lost from "+monitorId+" ===";
                System.out.println(message);

                monitorIdRemove(monitorId);

            }
            // System.out.println(message);

        
    }
    synchronized void monitorIdRemove(String monitorId){
        /**
         * handle the monitorList adding when the thread handle the veritable
         */
        Gateway.monitorList.remove(monitorId);
    }
    // Custom Delay
    private static void customDelayInSeconds(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class Gateway {
    /**
     * handle the new vital monitor connection
     * create the thread
    */
    public static List<String> monitorList = new ArrayList<>();

    public static void main(String[] args) {
        int BROADCAST_PORT = 6000;                    // broadcast port for reading the UDP connection
        DatagramSocket dSocket = null;

        byte[] buffer  = new byte[10000];             // reading the data packet

        while (true) {                                // lisiting every new connection
            try {
                // creating broadcast socket
                dSocket = new DatagramSocket(BROADCAST_PORT);
                
                // broadcast packet
                DatagramPacket packet = new DatagramPacket(buffer , buffer.length);
                dSocket.receive(packet);

                Monitor monitor = (Monitor) decodeSerialByte(buffer);   // decode the byte array
                String monitorId = monitor.getMonitorID();
                
                // the Algorithm for handle same device and connection ,disconnection and reconnection
                if(!monitorList.contains(monitorId)){
                    VitalReader vr1=new VitalReader(monitor);
                    vr1.start();
                    monitorIdAdd(monitorId);
                }   
            }
            catch(SocketException se){  // error connecting broadcast
                se.getStackTrace();
                System.out.println("there is a error in connecting Socket with the broadcast port");
            }
            catch(ClassNotFoundException cnfe){
                cnfe.getStackTrace();
                System.out.println("there is some class not found");
            }
            // Exception Handlings
            catch (IOException ie) {
                ie.getStackTrace();
            }
            finally{
                dSocket.close();
            }
        } 
    }
    // change received byte data to object
    public static Object decodeSerialByte(byte[] data) throws IOException, ClassNotFoundException {
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(data);
        ObjectInputStream obj = new ObjectInputStream(byteStream);
        return obj.readObject();
    }

    //while handle the monitorList, the list also handle by the thread so synchronized the handling monitorList
    synchronized static void monitorIdAdd(String monitorId){
        monitorList.add(monitorId);
    }
}