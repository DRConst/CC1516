package Commons;

import Server.Packet;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;

import java.io.*;
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

    public void transmitData(byte data[]) throws IOException {
        int numPackets = (int)(Math.ceil(data.length * 1.0f/ (48 * 1024 - 8)*1.0f));
        int currentPacket = 0;
        int lastPacket = 0;
        byte[] recv, toTrans;
        int bytesRemaining = data.length;
        DatagramPacket datagramPacketSent;
        DatagramPacket datagramPacketResponse;
        ByteArrayOutputStream byteArrayOutputStream;
        DataOutputStream dataOutputStream;
        ByteArrayInputStream byteArrayInputStream;
        DataInputStream dataInputStream;


        while(lastPacket == 0)
        {
            byteArrayOutputStream = new ByteArrayOutputStream();
            dataOutputStream = new DataOutputStream(byteArrayOutputStream);
            if(currentPacket == numPackets - 1)
            {
                lastPacket = 1;
            }

            //Write the first two bytes of the header
            dataOutputStream.writeInt(currentPacket);
            dataOutputStream.writeInt(lastPacket);

            if(lastPacket == 0)
            {

                toTrans = new byte[48*1024 - 12];
                for(int i = 0; i < 48*1024 - 12; i++)
                {
                    toTrans[i] = data[currentPacket * (48*1024 - 12) + i];
                }
                //Write the ammount of bytes to be read in the packet
                dataOutputStream.writeInt(48*1024 - 12);
                bytesRemaining -= 48*1024 - 12;
            }else
            {
                toTrans = new byte[bytesRemaining];
                for(int i = 0; i < bytesRemaining; i++)
                {
                    toTrans[i] = data[currentPacket * (48*1024 - 12) + i];
                }
                //Write the ammount of bytes to be read in the packet
                dataOutputStream.writeInt(bytesRemaining);
            }

            //Write the data
            try {
                dataOutputStream.write(toTrans);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] tmp = byteArrayOutputStream.toByteArray();
            datagramPacketSent = new DatagramPacket(tmp, tmp.length, InetAddress.getByName("localhost"), port);

            try {
                socket.setSoTimeout(10000);

            } catch (SocketException e) {
                e.printStackTrace();
            }

            try {
                System.out.println("Sending packet " + currentPacket);
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

            byteArrayInputStream = new ByteArrayInputStream(recv);
            dataInputStream = new DataInputStream(byteArrayInputStream);

            int lastAck = dataInputStream.readInt();
            int successfulDataRead = dataInputStream.readInt();
            int corruptPacket = dataInputStream.readInt();

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

    public File receiveData(String filename) throws IOException {
        File toRet = new File("./Music/" +filename);

        try {
            socket.setSoTimeout(10000);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        int currentPacket = 0;
        int lastPacket = 0;
        int fullDataRead = 0;
        byte[] buffer;
        ByteArrayOutputStream fileStream = new ByteArrayOutputStream();
        ByteArrayOutputStream outputStream = null;
        ByteArrayInputStream byteArrayInputStream;
        DataInputStream dataInputStream;
        DataOutputStream dataOutputStream;
        while(lastPacket == 0)
        {

            outputStream = new ByteArrayOutputStream();
            dataOutputStream = new DataOutputStream(outputStream);
            buffer = new byte[48*1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            try {
                socket.receive(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            byteArrayInputStream = new ByteArrayInputStream(buffer);
            dataInputStream = new DataInputStream(byteArrayInputStream);
            int packetSent = dataInputStream.readInt();
            lastPacket = dataInputStream.readInt();
            int dataLength = dataInputStream.readInt();

            if(buffer.length - 12 == dataLength)
            {
                fullDataRead = 1;
            }

            //Ack the packet and confirm the read
            if(lastPacket == 0)
            {
                dataOutputStream.writeInt(currentPacket);
                dataOutputStream.writeInt(fullDataRead);
                dataOutputStream.writeInt(0);
            }
            if(fullDataRead == 1 && currentPacket == packetSent)
            {
                fileStream.write(buffer, 12, buffer.length - 12);
                currentPacket++;
            }

            fullDataRead = 0;
            byte[] sendBuffer = outputStream.toByteArray();
            DatagramPacket responsePacket = new DatagramPacket(sendBuffer, sendBuffer.length, packet.getAddress(), packet.getPort());
            socket.send(responsePacket);

        }

        byte[] pack = fileStream.toByteArray();
        FileOutputStream stream = new FileOutputStream("./Music/" + filename);
        stream.write(pack);
        stream.close();

        System.out.println("File Transfer Complete : " + filename);

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
