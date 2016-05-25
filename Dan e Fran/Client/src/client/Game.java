package client;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.StringTokenizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import javax.imageio.ImageIO;

public class Game 
{
    String question;
    String user;
    String answer;
    int i = 0;
    int PORT;
    int[] header = new int[8];
    int time;
    DatagramSocket socket;
    InetAddress IP;
    
    Game()
    {
        socket = ConnectionHandler.mainSocket; 
        PORT = ConnectionHandler.sendAnswerPORT;
        IP = ConnectionHandler.ip;
        user = ConnectionHandler.user;
    }
    
    public void run()
    {
        
        BufferedImage image = receiveImage();
        
        System.out.println("Image received successfully, send ack");
        sendACK();
        
        receiveSong();
        sendACK();
        
        String[] values = receive();
        JFrameQuiz jf = new JFrameQuiz (user, values[0], values[1], values[2], values[3], image, this);
        jf.setVisible(true);
    }
    
    private BufferedImage receiveImage()
    {
        boolean errorOnReceive;
        BufferedImage image = null;
        //talvez tem de começar a 0!
        byte[] imageInBytes = new byte[0];
        // receive first image packet
        
        int totalSizePacket = 1;
        for(int z = 1; z < totalSizePacket+1; z++)
        {
            errorOnReceive = false;
            byte[] data = new byte[65507];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            
            try
            {
                socket.setSoTimeout(750);
                socket.receive(packet);
            }
            catch(IOException ioe)
            {
                errorOnReceive = true;
                try
                { Thread.sleep(100); }
                catch (InterruptedException ie)
                { System.err.println("Oh Darn! Error while sleeping xD"); }
                z--;
                
                String[] fields = new String[1];
                int codeType = 255;
                fields [0] = " ";
                int numFields = 1;
            
                sendPacket(fields, numFields, codeType);
            }
            if(!errorOnReceive)
            {
                byte[] receiveData = packet.getData();
                int[] h = new int[12];
                byte[] aux = breakPDUFile(receiveData, h);
                byte[] old = imageInBytes;

                totalSizePacket = h[8]*255 + h[9];
                int packetNum = h[10]*255 + h[11];
                
                if(packetNum == z)
                {
                    imageInBytes = new byte[old.length + aux.length];

                    System.arraycopy(old, 0, imageInBytes, 0, old.length);
                    System.arraycopy(aux, 0, imageInBytes, old.length, aux.length);
                }
            }
            
            String[] fields = new String[1];
            int codeType = 0;
            fields [0] = " ";
            int numFields = 1;
            
            sendPacket(fields, numFields, codeType);
        }
        
        InputStream in = new ByteArrayInputStream(imageInBytes);
        
        try{
            image = ImageIO.read(in);
            ImageIO.write(image, "jpg", new File("imageAtClient.jpg"));
        }catch(IOException e){
            System.err.println("ImageIO.read(in) failed!!");
        }
        
        return image;
    }
    
    public void receiveSong()
    {
        System.out.println("Going to receive song");
        byte[] song = new byte[0];
        boolean errorOnReceive;        
        int totalSizePacket = 1;
        
        for(int z = 1; z < totalSizePacket+1; z++)
        {
            errorOnReceive = false;
                    
            System.out.println("Receiving song packet: " + z);
            byte[] data = new byte[65507];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            
            try
            {
                socket.setSoTimeout(750);
                socket.receive(packet);
            }
            catch(IOException ioe)
            {
                errorOnReceive = true;
                try
                { Thread.sleep(100); }
                catch (InterruptedException ie)
                { System.err.println("Oh Darn! Error while sleeping xD"); }
                z--;
                
                String[] fields = new String[1];
                int codeType = 255;
                fields [0] = " ";
                int numFields = 1;
            
                sendPacket(fields, numFields, codeType);
            }
            
            if(!errorOnReceive)
            {
                byte[] receiveData = packet.getData();
                int[] h = new int[12];
                byte[] aux = breakPDUFile(receiveData, h);
                byte[] old = song;

                totalSizePacket = h[8]*255 + h[9];
                int packetNum = h[10]*255 + h[11];
                
                if(packetNum == z)
                {
                    song = new byte[old.length + aux.length];

                    System.arraycopy(old, 0, song, 0, old.length);
                    System.arraycopy(aux, 0, song, old.length, aux.length);
                }
                else
                    z--;
            }
            
            String[] fields = new String[1];
            int codeType = 0;
            fields [0] = " ";
            int numFields = 1;
            
            sendPacket(fields, numFields, codeType);
        }
        
        //ByteArrayInputStream bais = new ByteArrayInputStream(song);
        //ObjectInputStream ois = null;
        //ObjectFile of = null;
        /*try
        { 
            ois = new ObjectInputStream(bais); 
            of = (ObjectFile)ois.readObject();
        }
        catch(IOException | ClassNotFoundException ioe)
        { System.err.println("Oh darn! Error with ois! :o"); }*/
        
        
        File file = new File("song.mp3");
        try
        {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(song);
            fos.flush();
            fos.close();
        }
        catch(IOException ioe)
        {}
    }
    
    public void sendACK(){
        String[] fields = new String[1];
        fields[0] = " ";
        sendPacket(fields, 1, 0);
    }
    
    public void getAnswerFromFrame(String answerX, double timeX)
    {
        answer = answerX;
        time = (int)(timeX/1000);
        System.out.println("Answer time: " + time);
        sendAnswer(answer, time);
        i++;
        if(i<10)
            run();
        else{
            System.out.println("Done");
            displayPoints();
        }
    }
    
    private void displayPoints(){
        String[] fields = receive();
        new JFramePoints(user, Double.parseDouble(fields[0])).setVisible(true);
    }
    
    public String[] receive()
    {
        byte[] data = new byte[65507];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try
        { socket.receive(packet); }
        catch (IOException ioe) 
        { System.err.println("Oh darn! Error receiving packet in Game! :o"); }
        byte[] receiveData = packet.getData();
        
        return breakPDU(receiveData, header);
    }
   
    
    public void sendAnswer(String ans, double time)
    {
        DatagramPacket packetToSend;

        byte[] sendData = null;
        String[] fieldList = null;

        String t = Double.toString(time);

        int bufferSize = 8 + ans.length() + t.length() + user.length();

        sendData = new byte[bufferSize];

        fieldList = new String[3];
        fieldList[0] = ans;
        fieldList[1] = t;
        fieldList[2] = user;

        sendPacket(fieldList, 3, 11);
    }
    
    public void sendPacket(String[] fields, int numFields, int codeType)
    {
        byte[] sendData = buildPDU(0, 0, header[2]*255+header[3], codeType, numFields, fields);
            
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, PORT);
        try
        { socket.send(sendPacket); }
        catch (IOException ioe)
        { System.err.println("Oh Darn, can't send packet in Game! :'( "); }
    }
    
    public byte[] buildPDU(int ver, int sec, int label, int type, int numFields, String[] fieldList)
    {
        int size = 0;
        
        for(String str : fieldList)
            size += str.length() + 1;
        
        byte[] PDU = new byte[8 + size];
        PDU[0] = (byte) (ver-128);
        PDU[1] = (byte) (sec-128);
        
        PDU[2] = (byte) (((label*1.0)/255)-129);
        PDU[3] = (byte) ((label % 255)-128);
          
        PDU[4] = (byte) (type-128);
        PDU[5] = (byte) (numFields-128);
        
        byte[] auxFieldList = new byte[size];
        int i = 0;
        for(String str : fieldList)
        {
            byte[] aux = str.getBytes();    
            for(byte b : aux)
                auxFieldList[i++] = b;
            auxFieldList[i++] = '\0';
        }
        
        PDU[6] = (byte) (((size*1.0)/255)-129);
        PDU[7] = (byte) ((size % 255)-128);
        
        for( i = 0; i < size; i++)
        { PDU[i+8] = auxFieldList[i]; }
        
        return PDU;
    }
    
    public String[] breakPDU(byte[] pdu, int[] header)
    {
        int k = 0;
        for(; k < 8; k++)
            header[k] = pdu[k]+128;
        
        String[] fieldList = new String[header[5]];
        String aux = new String(pdu, 8, pdu.length-8);
        StringTokenizer st = new StringTokenizer(aux, "\0");
        int w = 0;
        while(st.hasMoreElements())
        { fieldList[w++] = st.nextElement().toString(); }
        return fieldList;
    }
    
    public byte[] breakPDUFile(byte[] pdu, int[] header)
    {
        int k = 0;
        for(; k < 12; k++)
            header[k] = pdu[k]+128;
        
        int size = header[6]*255 + header[7];
        byte[] file = new byte[size];
        
        System.arraycopy(pdu, 12, file, 0, size);
        
        return file;
    }
}
