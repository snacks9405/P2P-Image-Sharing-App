import java.net.DatagramPacket;
import java.net.InetAddress;

/**************************************************
 * CS 391 - Spring 2024 - A5
 *
 * File: A5.java
 *
 * You may NOT modify this class.
 **************************************************/

public class A5
{
    // Application Message Types
    public static final byte MSG_REQUEST_IMG_FILE      = 1;
    public static final byte MSG_FILE_NAME             = 2;
    public static final byte MSG_FILE_DATA             = 3;
    public static final byte MSG_FILE_DONE             = 4;
    public static final byte MSG_NO_IMG_FILE_AVAILABLE = 5;
    
    // Application message maximum sizes
    public static final int MAX_DATA_SIZE              = 8192;
    public static final int MAX_MSG_SIZE               = MAX_DATA_SIZE + 1;
    
    // Receiver port numbers for the client and server
    public static int CLIENT_RCV_PORT_NUM              = 8888;
    public static int CLIENT_PEER_RCV_PORT_NUM         = 7777;
    public static int SERVER_RCV_PORT_NUM              = 7777;
    public static int SERVER_PEER_RCV_PORT_NUM         = 8888;
    
    // Image subfolder
    public static String IMG_SUBFOLDER = "./images/";

    // sends the given message to the console preceded by a time stamp
    // with some indentation if tag == "S"
    public static void print(String tag, String message)
    {
        System.out.printf(
          (tag.equals("S") ? "                         " : "") +
          String.format("[%6.3f]",
                        (System.currentTimeMillis() % 100000)/1000.0)
          + " " + message + "\n");      
    }// print

    public static void main(String[] args) {
        //sendFileName("image.png");
        // byte[] rcvData = {0, 0, 0, 1};
        // int checkSum = rcvData[0]; 
        // for (int i = 1; i < rcvData.length; i++) {
        //     checkSum ^=  rcvData[i];
        // }
        // System.out.println("From alg: " + checkSum);
        // checkSum = checkSum(rcvData, rcvData.length);
        // System.out.println("From method: " + checkSum);
        // for (int i = 1; i < rcvData.length; i++) {
        //     checkSum ^= rcvData[i];
        // }
        // System.out.println(checkSum);
        // System.out.println(1^2^3^4);
        // System.out.println(1^2^3^4^4);
        // System.out.println(checkSum);
        // if (rcvData[0] != 1)
        //         System.out.println("Fail 0");
        // int test = checkSum;
        // for (int i = 0; i < rcvData.length; i++) {
        //     test = test ^ rcvData[i];
        // }
        // System.out.println(test);


        // System.out.print((byte) 1);

        int i = 0;
        // while (true) {
        //     byte[] testArr = {(byte)i, 2, 3, 4, 0};
        //     testArr[4] = checkSum(testArr, 4);
        //     System.out.println(checkSum(testArr, 5));

        //     try {
        //         MyDatagramSocket mds = new MyDatagramSocket();
        //         DatagramPacket message = new DatagramPacket(testArr, testArr.length, InetAddress.getByName("localhost"), 7777);
        //         mds.send(message);
        //         mds.receive(message);
        //         testArr = message.getData();
        //         System.out.println(testArr[0]);
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //     }
        //     i = i == 0 ? 1 : 0;
        // }

        // while (true) {
        //     byte[] testArr = {(byte)i, 2, 3, 4};
        //     testArr[3] = checkSum(testArr, 3);

        //     System.out.println(checkSum(testArr, testArr.length));
        //     try {
        //         byte[] results = new byte[5]; 
        //         MyDatagramSocket mds = new MyDatagramSocket(8889);
        //         DatagramPacket message = new DatagramPacket(results, 5);
        //         mds.receive(message);
        //         System.out.println(results[0] + " " + results[1]);
        //         mds.send(new DatagramPacket(testArr, testArr.length, message.getAddress(), message.getPort()));
        //         mds.close();
        //     } catch (Exception e) {
        //         e.printStackTrace();
        //         break;
        //     }
        //     i = i == 0 ? 1 : 0;
        // }

        byte[] arr = "ACK".getBytes();
        System.out.println(arr.length);




    }
    static byte checkSum(byte[] array, int n) 
    {
        int checkSum = 0;
        for (int i = 0; i < n; i++) {
            checkSum ^= array[i];
        }
        
        return (byte) checkSum; // only here to satisfy the compiler
    }

}// A5
