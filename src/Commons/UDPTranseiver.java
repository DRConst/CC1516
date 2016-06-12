package Commons;

import Server.Packet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * Created by NoobLevler on 11/06/2016.
 */
public class UDPTranseiver {

    int port;
    DatagramSocket socket;
    InetAddress ip;
    int maxConsecutiveTimeouts = 5;
    public UDPTranseiver(int port, InetAddress ip) throws SocketException {
        this.port = port;
        this.ip = ip;
        socket = new DatagramSocket(port);
    }

    public UDPTranseiver() throws SocketException {
        socket = new DatagramSocket();
        ip = socket.getLocalAddress();
        port = socket.getLocalPort();
    }

    public void transmitData(byte data[]) throws UnknownHostException {
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

                toTrans = new byte[48*1024 - 12];
                for(int i = 0; i < 48*1024 - 12; i++)
                {
                    toTrans[i] = data[currentPacket * (48*1024 - 12) + i];
                }
                //Write the ammount of bytes to be read in the packet
                byteArrayOutputStream.write(48*1024 - 5);
                bytesRemaining -= 48*1024 - 5;
            }else
            {
                toTrans = new byte[bytesRemaining];
                for(int i = 0; i < bytesRemaining; i++)
                {
                    toTrans[i] = data[currentPacket * (48*1024 - 12) + i];
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
            byte[] tmp = byteArrayOutputStream.toByteArray();
            datagramPacketSent = new DatagramPacket(tmp, tmp.length, InetAddress.getByName("localhost"), port);

            try {
                socket.setSoTimeout(2000);

            } catch (SocketException e) {
                e.printStackTrace();
            }

            try {
                socket.send(datagramPacketSent);
            } catch (IOException e) {
                System.out.println("Timeout on send");
                maxConsecutiveTimeouts --;
                e.printStackTrace();
                if(maxConsecutiveTimeouts == 0)
                    return;
            }maxConsecutiveTimeouts = 5;
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

            int lastAck = byteBuffer.getInt();
            int successfulDataRead = byteBuffer.getInt();
            int corruptPacket = byteBuffer.getInt();

            if(lastAck == currentPacket && successfulDataRead == 1)
                currentPacket++;
            else if(currentPacket == numPackets && corruptPacket == 1)
            {
                currentPacket = 0;
                lastPacket = 0;
            }else
            {
                currentPacket = lastAck;
            }


        }

    }

    public Packet receiveData()
    {
        Packet toRet;

        try {
            socket.setSoTimeout(10000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        int currentPacket = 0;
        int lastPacket = 0;
        int fullDataRead = 0;
        byte[] buffer;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream;
        while(lastPacket == 0)
        {

            outputStream = new ByteArrayOutputStream();
            buffer = new byte[48*1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
            IntBuffer b = byteBuffer.asIntBuffer();
            int packetSent = b.get();
            lastPacket = b.get();
            int dataLenght = b.get();

            if(buffer.length - 12 == dataLenght)
            {
                fullDataRead = 1;
            }

            //Ack the packet and confirm the read
            if(lastPacket == 0)
            {
                outputStream.write(currentPacket);;
                outputStream.write(fullDataRead);
                outputStream.write(1);
            }
            if(fullDataRead == 1 && currentPacket == packetSent)
            {
                byteArrayOutputStream.write(buffer, 12, buffer.length - 12);
                currentPacket++;
            }

            fullDataRead = 0;

        }

        byte[] pack = byteArrayOutputStream.toByteArray();

        toRet = (Packet)Serializer.unserializeFromString(new String(pack));

        return toRet;
    }

    public int getPort() {
        return port;
    }

    public DatagramSocket getSocket() {
        return socket;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }
}
