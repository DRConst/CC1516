package Server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;
import javax.imageio.ImageIO;

public class SendFiles 
{
    DatagramSocket socket;
    DatagramSocket receiveSocket;
    InetAddress IP;
    int sendPORT;
    int receivePORT;
    String image;
    String song;
    int[] header;
    
    SendFiles(DatagramSocket socketX, InetAddress IPX, int sendPORTX, int receivePORTX, String img, String son)
    {
        socket = socketX;
        IP = IPX;
        sendPORT = sendPORTX;
        receivePORT = receivePORTX;
        image = img;
        song = son;
        header = new int[12];
    }
    
    public void sendSong()
    {
        File file = null;
        DataInputStream dis = null;
        
        try
        { 
            file = new File("musica/" + song);
            dis = new DataInputStream(new FileInputStream(file));
        }
        catch(FileNotFoundException fnfe)
        { System.err.println("Oh darn! Can't load MP3 :o"); }
        
        byte[] fileBytes = new byte[(int)file.length()];
                
        try
        { dis.read(fileBytes); }
        catch(IOException ioe)
        { System.err.println("Oh darn! Error loading MP3 bytes! :o"); }
        
        //ObjectFile of = new ObjectFile(len, fileBytes);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        /*try
        { ObjectOutputStream oos = new ObjectOutputStream(baos); }
        catch(IOException ioe)
        { System.err.println("Oh darn! Something went wrong creating the OOS :'("); }*/
        
        byte[] data = baos.toByteArray();
        
        try
        {
            baos.flush();
            baos.close();
        }
        catch(IOException ioe)
        { System.err.println("Error flushing and closing"); }
        
        float aux = ((fileBytes.length*1.0f)/60000.0f);
        int totalNumPacket = (int)Math.ceil(aux);
        System.out.println("totalNumPacket: " + totalNumPacket);

        for(int i=0; i<totalNumPacket; i++)
        {
            System.out.println("Sending song packet: " + i);
            int size;
            if(i==(totalNumPacket-1))
                size = (fileBytes.length-(i*60000));
            else
                size = 60000;

            byte [] auxByte = new byte[size];

            for(int j=0; j<60000 && j<(fileBytes.length-(i*60000)); j++)
            { auxByte[j] = fileBytes[j+(i*60000)]; }

            sendPacket(auxByte, totalNumPacket, i+1);
            
            byte[] answerData = new byte[65507];
            DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length);
            
            try
            {
                receiveSocket = new DatagramSocket(receivePORT);
                receiveSocket.setSoTimeout(1250);
                receiveSocket.setReuseAddress(true);
                receiveSocket.receive(answerPacket);
                receiveSocket.close();
                
                byte[] receiveDataAnswer = answerPacket.getData();
                int[] answerHeader = new int[8];

                if(header[4] == 255)
                    i--;
            }
            catch(IOException ioe)
            {
                try
                { Thread.sleep(100); }
                catch (InterruptedException ie)
                { System.err.println("Oh Darn! Error while sleeping xD"); }
                i--;
            }
        }
        
        byte[] answerData = new byte[65507];
        DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length);

        try
        {
            receiveSocket = new DatagramSocket(receivePORT);
            receiveSocket.setReuseAddress(true);
            receiveSocket.receive(answerPacket);
            receiveSocket.close();
        }
        catch(IOException ioe)
        { System.err.println ("Oh Darn! Error on last ack! :("); }

        byte[] receiveDataAnswer = answerPacket.getData();
        int[] answerHeader = new int[8];

        System.out.println("Song received by client sucessfully");
    }
    
    public void sendImage()
    {
        byte[] imageInByte = null;
        try
        {
            BufferedImage img = ImageIO.read(new File("imagens/" + image));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(img, "jpg", baos);
            imageInByte = baos.toByteArray();
            baos.flush();
            baos.close();
        }
        catch(IOException ioe)
        { System.err.println("Oh Darn! Can't load picture! :("); }

        float aux = ((imageInByte.length*1.0f)/60000.0f);
        int totalNumPacket = (int)Math.ceil(aux);

        for(int i=0; i<totalNumPacket; i++)
        {
            int size;
            if(i==(totalNumPacket-1))
                size = (imageInByte.length-(i*60000));
            else
                size = 60000;


            byte [] auxByte = new byte[size];

            for(int j=0; j<60000 && j<(imageInByte.length-(i*60000)); j++)
            { auxByte[j] = imageInByte[j+(i*60000)]; }

            sendPacket(auxByte, totalNumPacket, i+1);

            byte[] answerData = new byte[65507];
            DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length);

            try
            {
                receiveSocket = new DatagramSocket(receivePORT);
                receiveSocket.setSoTimeout(1250);
                receiveSocket.setReuseAddress(true);
                receiveSocket.receive(answerPacket);
                receiveSocket.close();
                
                byte[] receiveDataAnswer = answerPacket.getData();
                int[] answerHeader = new int[8];

                if(header[4] == 255)
                    i--;
            }
            catch(IOException ioe)
            {
                try
                { Thread.sleep(100); }
                catch (InterruptedException ie)
                { System.err.println("Oh Darn! Error while sleeping xD"); }
                i--;
            }
        }

        byte[] answerData = new byte[65507];
        DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length);

        try
        {
            receiveSocket = new DatagramSocket(receivePORT);
            receiveSocket.setReuseAddress(true);
            receiveSocket.receive(answerPacket);
            receiveSocket.close();
        }
        catch(IOException ioe)
        { System.err.println ("Oh Darn! Error on last ack! :("); }

        byte[] receiveDataAnswer = answerPacket.getData();
        int[] answerHeader = new int[8];

        System.out.println("Image received by client sucessfully");
    }
    
    public void sendPacket(byte[] file, int totalPackets, int numPacket)
    {
        byte[] sendData = buildPDU(0, 0, header[2]*255+header[3], 0, 0, totalPackets, numPacket, file);
            
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, sendPORT);
        try
        { socket.send(sendPacket); }
        catch (IOException ioe)
        { System.err.println("Oh Darn, can't send packet! :'( "); }
    }
    
    public byte[] buildPDU(int ver, int sec, int label, int type, int numFields, int totalPackets, int numPacket, byte[] file)
    {        
        byte[] PDU = new byte[12 + file.length];
        PDU[0] = (byte) (ver-128);
        PDU[1] = (byte) (sec-128);
        
        PDU[2] = (byte) (((label*1.0)/255)-129);
        PDU[3] = (byte) ((label % 255)-128);
          
        PDU[4] = (byte) (type-128);
        PDU[5] = (byte) (numFields-128);
        
        PDU[6] = (byte) (((file.length*1.0)/255)-129);
        PDU[7] = (byte) ((file.length % 255)-128);
        
        PDU[8] = (byte) (((totalPackets*1.0)/255)-129);
        PDU[9] = (byte) ((totalPackets % 255)-128);
        
        PDU[10] = (byte) (((numPacket*1.0)/255)-129);
        PDU[11] = (byte) ((numPacket % 255)-128);
        
        System.arraycopy(file, 0, PDU, 12, file.length);
        
        return PDU;
    }
    
    /*public String[] breakPDU(byte[] pdu, int[] header)
    {
        int k = 0;
        for(; k < 8; k++)
            header[k] = pdu[k]+128;
        
        String[] fieldList = new String[header[5]];
        String aux = new String(pdu, 8, pdu.length-8);
        StringTokenizer st = new StringTokenizer(aux, "\0");
        int i = 0;
        while(st.hasMoreElements())
        { fieldList[i++] = st.nextElement().toString(); }
        return fieldList;
    }*/
}
