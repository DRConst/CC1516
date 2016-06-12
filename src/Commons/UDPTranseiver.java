package Commons;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * Created by NoobLevler on 11/06/2016.
 */
public class UDPTranseiver {

    int port;
    DatagramSocket socket;
    InetAddress ip;
    public UDPTranseiver(int port, InetAddress ip) throws SocketException {
        this.port = port;
        this.ip = ip;
        socket = new DatagramSocket(port);
    }

    public void transmitData(byte data[]) {
        int numPackets = (int)(Math.ceil(data.length * 1.0f/ (48 * 1024 - 8)*1.0f));
        int currentPacket = 0;
        int lastPacket = 0;
        byte[] recv, toTrans;
        int bytesRemaining = data.length;
        DatagramPacket datagramPacketSent;
        DatagramPacket datagramPacketResponse;
        ByteArrayOutputStream byteArrayOutputStream;


        while(lastPacket == 0)
        {
            byteArrayOutputStream = new ByteArrayOutputStream();
            if(currentPacket == numPackets - 1)
            {
                lastPacket = 1;
            }

            //Write the first two bytes of the header
            byteArrayOutputStream.write(currentPacket);
            byteArrayOutputStream.write(lastPacket);

            if(lastPacket == 0)
            {

                toTrans = new byte[48*1024];
                for(int i = 0; i < 48*1024 - 5; i++)
                {
                    toTrans[i] = data[currentPacket * (48*1024 - 5) + i];
                }
                //Write the ammount of bytes to be read in the packet
                byteArrayOutputStream.write(48*1024 - 5);
                bytesRemaining -= 48*1024 - 5;
            }else
            {
                toTrans = new byte[bytesRemaining];
                for(int i = 0; i < bytesRemaining; i++)
                {
                    toTrans[i] = data[currentPacket * (48*1024 - 5) + i];
                }
                //Write the ammount of bytes to be read in the packet
                byteArrayOutputStream.write(bytesRemaining);
            }

            //Write the data
            try {
                byteArrayOutputStream.write(toTrans);
            } catch (IOException e) {
                e.printStackTrace();
            }

            datagramPacketSent = new DatagramPacket(byteArrayOutputStream.toByteArray(), byteArrayOutputStream.toByteArray().length, ip, port);

            try {
                socket.setSoTimeout(2000);

            } catch (SocketException e) {
                e.printStackTrace();
            }

            try {
                socket.send(datagramPacketSent);
            } catch (IOException e) {
                System.out.println("Timeout on send");
                e.printStackTrace();

                return;
            }
            recv = new byte[12];
            datagramPacketResponse = new DatagramPacket(recv, 12);

            try {
                socket.receive(datagramPacketResponse);
            } catch (IOException e) {
                System.out.println("Timeout on receive");
                e.printStackTrace();
            }
            ByteBuffer byteBuffer = ByteBuffer.wrap(recv);

            byteBuffer.asIntBuffer();

            int packetAckd = byteBuffer.getInt();
            int successfulDataRead = byteBuffer.getInt();
            int corruptPacket = byteBuffer.getInt();

            if(packetAckd == currentPacket && successfulDataRead == 1)
                currentPacket++;
            else if(currentPacket == numPackets && corruptPacket == 1)
            {
                currentPacket = 0;
                lastPacket = 0;
            }


        }

    }
}
