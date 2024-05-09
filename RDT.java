/*****************************************************************************
 * CS 391 - Spring 2024 - A5
 *
 * File: RDT.java
 *
 * Classes: RDT
 *          Sender (inner class)
 *          Receiver (inner class)
 *
 * FIRST STUDENT'S FULL NAME GOES HERE
 *
 * 2nd STUDENT'S FULL NAME GOES HERE
 *
 * 3rd STUDENT'S FULL NAME GOES HERE (DELETE THIS LINE IF NOT NEEDED)
 
 *****************************************************************************/

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**  
 * Reliable Data Transfer potocol implemented on top of MyDatagramSocket.
 *
 * Important note: this class is NOT aware of (and thus cannot reference) the
 * client/server processes that will be using it. In fact, any client-server
 * application should be able to use this class.
 *
 * The handout explains how an RDT instance's sender thread talks to its
 * peer's receiver thread, and vice-versa.
 */

public class RDT
{
    private InetAddress peerIpAddress;   // IP address of this instance
    private int rcvPortNum;              // port # of this instance's receiver
    private int peerRcvPortNum;          // port # of peer's receiver
    private Thread senderThread;         // sender side of this instance
    private Thread receiverThread;       // receiver side of this instance
    private byte[] dataToSend;           // buffer for data to be sent
                                         // AKA the "send buffer"
    private boolean dataWaitingToBeSent; // flag indicating that there is data
         // yet to be sent in the send buffer and that the app will have to
         // wait before being able to send another message
    private byte[] dataReceived;         // buffer for data received from below
                                         // AKA the "receive buffer"    
    private boolean dataWasReceivedFromBelow; // flag indicating that there is
         // data in the receive buffer that has yet to be grabbed by the app
    private String tag;                  // only for debugging (see handout)
    
    // Do not modify this constructor
    public RDT(String inPeerIP, 
               int inRcvPortNum, 
               int inPeerRcvPortNum,
               String inTag) throws Exception
    {
        rcvPortNum = inRcvPortNum;
        peerRcvPortNum = inPeerRcvPortNum;
        tag = inTag;
        if (inPeerIP == null)
            peerIpAddress = InetAddress.getLoopbackAddress();
        else
            peerIpAddress = InetAddress.getByName(inPeerIP);
        dataWaitingToBeSent = false;
        dataWasReceivedFromBelow = false;
        senderThread = new Thread(new Sender());
        receiverThread = new Thread(new Receiver());
        senderThread.start();
        receiverThread.start();
    }// constructor
      
    /** The application calls this method to send a message to its peer.
     *  The RDT instance simply "copies" this data into its send buffer, but
     *  only after waiting for the data currently in that buffer (if any)
     *  has been sent.
     */
    public void sendData(byte[] data)
    {
        
        while (dataWaitingToBeSent)
            Thread.yield();
        dataToSend = data;
        dataWaitingToBeSent = true;
        
    }// sendData

    /** The application calls this method to receive a message from its peer.
     *  The RDT instance simply returns this data to the app once it appears 
     *  in its receive buffer.
     */
    public byte[] receiveData()
    {
        while (true){
            if (dataWasReceivedFromBelow) {
                dataWasReceivedFromBelow = false;
                return dataReceived;
            } else {
                while(!dataWasReceivedFromBelow)
                    Thread.yield();
            }
        }
    }// receiveData

    /**
     * Computes and returns the checksum (i.e., XORed byte values) over the 
     * first n bytes of the given array
     */
    private byte checkSum(byte[] array, int n) 
    {
        int checkSum = 0;
        for (int i = 0; i < n; i++) {
            checkSum ^= array[i];
        }
        
        return (byte) checkSum; // only here to satisfy the compiler
    }// checkSum

    /***********************************************************************
     * inner class: Receiver
     ***********************************************************************/
    
    private class Receiver implements Runnable
    {
        private MyDatagramSocket rcvSocket;   // socket of the receiver
        private DatagramPacket rcvPacket;     // received packet
        private byte[] rcvData =              // data in the received packet
                new byte[A5.MAX_MSG_SIZE + 2];
        private int expectedSeqNum = 0;       // enough said!

        /**
         * Implements the receiver's FSM for RDT 2.2. More precisely:
         * 1) create the receiver's socket with the appropriate port number
         * 2) in an infinite loop: receive data from below and process it 
         *    adequately according to the FSM, that is:
         *    + if bad data is received, resend the "other" ACK
         *    + if good data is received, place it in the receive buffer and
         *      wait for the app layer to grab it, then send the corresponding
         *      ACK.
         *    Make sure to keep the state of the receiver up to date at all 
         *    times.
         */
        @Override
        public void run()
        {
            try {
                rcvSocket = new MyDatagramSocket(rcvPortNum);
                while(true){
                    rcvPacket = new DatagramPacket(rcvData, rcvData.length);
                    rcvSocket.receive(rcvPacket);
                    rcvData = rcvPacket.getData();
                    if (dataOK()) {
                        dataReceived = new byte[rcvData.length - 2];
                        System.arraycopy(rcvData, 1, dataReceived, 0, rcvData.length - 2);
                        dataWasReceivedFromBelow = true;
                        while(dataWasReceivedFromBelow) 
                            Thread.yield();
                        A5.print(tag, "RECEIVER sent ACK: " + expectedSeqNum);
                        sendAck(expectedSeqNum);
                        Thread.yield();
                        expectedSeqNum = expectedSeqNum == 0 ? 1 : 0;
                    } else {
                        A5.print(tag, "RECEIVER got bad data: resent previous ACK");
                        sendAck(expectedSeqNum == 0 ? 1 : 0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }// run

        /**
         * Returns true except when:
         * + the checksum of the received packet is not zero
         *   or
         * + the received sequence number does not match the expected one
         * As always, the format of the messages sent to the console is given
         * in the handout.
         */
        private boolean dataOK()
        {
            if ((int)rcvData[0] != expectedSeqNum) {       //turn on when headers/checksum are enabled.
                A5.print(tag, "RECEIVER bad ACK seq num; expected: " + expectedSeqNum);
                return false;
            }
            if (checkSum(rcvData, rcvData.length) != 0) {
                A5.print(tag, "RECEIVER checksum error on packet " + expectedSeqNum);
                return false;
            }
            A5.print(tag, "RECEIVER got good data: send new ACK");
            return true; // only here to satisfy the compiler       
        }// dataOK

        /**
         * Sends an acknowledgment packet with the given number
         */
        private void sendAck(int number)
        {
            byte[] ackArr = {(byte) number};
            DatagramPacket ackDP = new DatagramPacket(ackArr, 1, rcvPacket.getAddress(), rcvPacket.getPort());
            try {
                rcvSocket.send(ackDP);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }// sendAck
    }// Receiver

    /***********************************************************************
     * inner class: Sender
     ***********************************************************************/

    private class Sender implements Runnable
    {
        private MyDatagramSocket senderSocket;  // socket of the sender
        private DatagramPacket rcvPacket;       // received packet
        private byte[] rcvData = new byte[4];   // data in the received packet
        private int curSeqNum = 0;              // enough said!

        /**
         * Implements the sender's FSM for RDT 3.0. More precisely:
         * 1) create the sender's socket with an OS-generated port number
         * 2) in an infinite loop: send messages from the app to the receiver,
         *    that is:
         *    + wait for data from above, then send the packet to the peer
         *    + start the socket's timer using the call: setSoTimeout(500)
         *      and wait for the ACK to come in
         *      - if the ACK comes in okay and in good time, go back to the top
         *        of the loop
         *      - if the ACK is not okay, keep waiting for the next ACK
         *      - if the timer goes off, resend the message and keep waiting
         *        for the ACK
         *    Make sure to keep the state of the sender up to date at all 
         *    times.
         */
        @Override
        public void run()
        {
            try {
                senderSocket = new MyDatagramSocket();
                while (true) {
                    while (!dataWaitingToBeSent)
                            Thread.yield();
                        A5.print(tag, "SENDER got data from above");
                        rcvPacket = new DatagramPacket(rcvData, rcvData.length);
                        while(true) {
                            try{
                                sendPacket();
                                senderSocket.setSoTimeout(500);
                                senderSocket.receive(rcvPacket);
                                // rcvData = rcvPacket.getData();
                                if (ackPacketOK()) {
                                    A5.print(tag, "SENDER got GOOD ACK: " + curSeqNum);
                                    curSeqNum = curSeqNum == 0 ? 1 : 0;
                                    dataWaitingToBeSent = false;
                                    break;
                                }
                            } catch (SocketTimeoutException e) {
                                A5.print(tag, "SENDER timed out waiting for ACK, resending packet");
                                continue;
                            }
                        }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }// run

        /**
         * Returns true except when:
         * + the checksum of the received packet is not zero
         *   or
         * + the received sequence number does not match the expected one
         * As always, the format of the messages sent to the console is given
         * in the handout.
         */
        private boolean ackPacketOK()
        {
            if ((int) rcvData[0] != curSeqNum){
                A5.print(tag, "SENDER bad ACK seq num; expected: " + curSeqNum);
                return false;
            }
            if (checkSum(rcvData, rcvData.length) != 0) 
                return false;
            return true; // only here to satisfy the compiler
        }// ackPacketOK

        /**
         * Sends to the peer's receiver a packet containing the data in the 
         * send buffer
         */     
        private void sendPacket() throws Exception
        {
            //add header&checksum
            byte[] message = new byte[dataToSend.length+2];
            message[0] = (byte) curSeqNum;
            System.arraycopy(dataToSend, 0, message, 1, dataToSend.length);
            message[message.length-1] = checkSum(message, message.length);
            System.out.println("checksum before sending: " + checkSum(message, message.length));
            System.out.println("seq num before sending: " + curSeqNum);
            senderSocket.send(new DatagramPacket(message, message.length, peerIpAddress, peerRcvPortNum));
        }// sendPacket
    }// Sender
}// RDT


