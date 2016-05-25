package Server;

import java.io.*;
import java.net.*;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;
import java.util.StringTokenizer;

public class ConnectionHandler extends Thread
{
    DatagramSocket sendSocket;
    DatagramSocket receiveSocket;
    DatagramPacket packet;
    InetAddress sendIP;
    User loggedUser;
    int sendPort;
    int receivePort;
    Data data = new Data();
    int[] header = new int[8];
    
    //Static fields for ServerHandlerOut
    public static User staticUser;
    public static Challenge staticChallenge;
    
    ConnectionHandler(DatagramPacket packetX) throws IOException 
    {
        sendSocket = new DatagramSocket();
        packet = packetX;
        sendIP = packet.getAddress();
        sendPort = packet.getPort();
    }
    
    public void run()
    {
        byte[] receiveData = packet.getData();
        String[] field = breakPDU(receiveData, header);
        
        switch(header[4])
        {
            case 1:
                hello(field);
                break;
            case 2:
                register(field);
                break;
            case 3:
                login(field);
                break;
            case 4:
                System.out.println("Client logging out!");
                break;
            case 7:
                listChallenges();
                break;
            case 8:
                makeChallenge(field);
                break;
            case 9:
                acceptChallenge(field);
                break;
            case 10:
                listPoints();
            default:
                System.err.println("Unknown code from client received!");
        }
    }
    
    private void hello(String [] field)
    {
        String[] fields = new String[1];
        int codeType = 0;
        int numFields = 1;
        fields [0] = " ";

        sendPacket(fields, numFields, codeType, sendPort);
    }
    
    public void register(String [] field)
    {
        String[] fields = new String[1];
        if(data.userExists(field[1]))
        {
            int codeType = 255;
            int numFields = 1;
            fields [0] = "";
            
            sendPacket(fields, numFields, codeType, sendPort);
        }
        else
        {
            staticUser = new User(field [0], field [1], field [2], 0);
            data.addUser(staticUser); 
            int codeType = 0;
            int numFields = 1;
            fields [0] = "";
            
            sendPacket(fields, numFields, codeType, sendPort);
            
            synchronized(ServerHandlerOut.controlAI)
            {
                ServerHandlerOut.controlAI.set(1);
                ServerHandlerOut.controlAI.notifyAll();
            }
        }
    }
    
    public void login(String[] field)
    {        
        String[] fields = new String[2];
        if(data.checkUser(field [0], field [1]))
        {
            System.out.println("Field[1]: " + field[1]);
            loggedUser = data.getUser(field[1]);
            System.out.println("loggedUser.getName: " + loggedUser.getName());
            int codeType = 0;
            int numFields = 2;
            fields [0] = loggedUser.getName();
            fields [1] = Double.toString(loggedUser.getPoints());
                
            sendPacket(fields, numFields, codeType, sendPort);
        }
        else
        {
            int codeType = 255;
            int numFields = 2;
            fields[0] = "";
            fields[1] = "";
            
            sendPacket(fields, numFields, codeType, sendPort);
        }
    }
    
    public void listChallenges()
    {
        if(Data.getChallenges().isEmpty())
        {
            String[] fields = new String[1];
            fields [0] = "";
            int codeType = 255;
            int numFields = 1;
            
            sendPacket(fields, numFields, codeType, sendPort);                    
        }
        else
        {
            String[] fields = new String[(Data.getChallenges().size())*7];
            int i = 0;
            int codeType = 0;
            int numFields = 0;
            
            for(Challenge ch : Data.getChallenges())
            {
                GregorianCalendar aux = ch.getDT();
                fields[i] = ch.getName();
                i++;
                fields[i] = Integer.toString(aux.get(Calendar.YEAR));
                i++;
                fields[i] = Integer.toString(aux.get(Calendar.MONTH));
                i++;
                fields[i] = Integer.toString(aux.get(Calendar.DAY_OF_MONTH));
                i++;
                fields[i] = Integer.toString(aux.get(Calendar.HOUR_OF_DAY));
                i++;
                fields[i] = Integer.toString(aux.get(Calendar.MINUTE));
                i++;
                fields[i] = Integer.toString(aux.get(Calendar.SECOND));
                i++;
                numFields = numFields +7;
            }
            sendPacket(fields, numFields, codeType, sendPort);
        }
    }
    
    public void makeChallenge(String[] field)
    {
        if(data.challengeExists(field[0]))
        {
            String[] fields = new String[1];
            fields [0] = "";
            int codeType = 255;
            int numFields = 1;
            
            sendPacket(fields, numFields, codeType, sendPort);
        }
        else
        {
            Challenge temp;
            String[] fields = new String [1];
            int codeType = 0;
            int numFields = 1;
            
            if(field.length > 2)
            {
                GregorianCalendar aux = new GregorianCalendar(Integer.parseInt(field[1]), Integer.parseInt(field[2]),
                        Integer.parseInt(field[3]), Integer.parseInt(field[4]), Integer.parseInt(field[5]), 
                        Integer.parseInt(field[6]));
                temp = new Challenge(field[0], aux, 1);
                
                data.addChallenge(temp);
                
                loggedUser = data.getUserByName(field[7]);

                Random ran = new Random();
                receivePort = ran.nextInt(10000)+30000;
                System.out.println("Using port number: " + receivePort);
                
                fields [0] = Integer.toString(receivePort);

                sendPacket(fields, numFields, codeType, sendPort);
            }
            else
            {
                GregorianCalendar aux = new GregorianCalendar();
                aux.add(Calendar.MINUTE, 1);
                temp = new Challenge(field[0], aux, 1);
                data.addChallenge(temp);
                
                loggedUser = data.getUserByName(field[1]);

                Random ran = new Random();
                receivePort = ran.nextInt(10000)+30000;
                System.out.println("Using port number: " + receivePort);
                
                fields [0] = Integer.toString(receivePort);

                sendPacket(fields, numFields, codeType, sendPort);
            }
            
            staticChallenge = temp;
            
            synchronized(ServerHandlerOut.controlAI)
            {
                ServerHandlerOut.controlAI.set(3);
                ServerHandlerOut.controlAI.notifyAll();
            }
            
            if(waitFor(temp))
            {
                sendPacket(fields, numFields, codeType, sendPort);
                runChallenge(temp);
            }
            else
            {
                codeType = 255;
                sendPacket(fields, numFields, codeType, sendPort);
            }
        }
    }
    
    public void acceptChallenge(String[] field)
    {
        Challenge ch = data.getChallenge(field[0]);
        String[] fields = new String[1];
        int codeType = 0;
        int numFields = 1;
        
        loggedUser = data.getUserByName(field[1]);
        
        Random ran = new Random();
        receivePort = ran.nextInt(10000)+30000;
        System.out.println("Using port number: " + receivePort);

        fields [0] = Integer.toString(receivePort);

        sendPacket(fields, numFields, codeType, sendPort);
        
        try
        { Thread.sleep(100); }
        catch (InterruptedException ie)
        { System.err.println("Oh Darn! Error while sleeping xD"); }
        
        if(waitFor(ch))
        {
            sendPacket(fields, numFields, codeType, sendPort);
            runChallenge(ch);
        }
        else
        {
            codeType = 255;
            sendPacket(fields, numFields, codeType, sendPort);
        }
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
        int i = 0;
        while(st.hasMoreElements())
        { fieldList[i++] = st.nextElement().toString(); }
        return fieldList;
    }
    
    public void sendPacket(String[] fields, int numFields, int codeType, int PORT)
    {
        byte[] sendData = buildPDU(0, 0, header[2]*255+header[3], codeType, numFields, fields);
            
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, sendIP, PORT);
        try
        { sendSocket.send(sendPacket); }
        catch (IOException ioe)
        { System.err.println("Oh Darn, can't send packet! :'( "); }
    }
    
    public boolean waitFor(Challenge ch)
    {
        if(System.currentTimeMillis() > ch.getDT().getTimeInMillis())
        {
            System.out.println("Challenge is too old! Deleting!");
            data.removeChallente(ch);
            return false;
        }
        while(System.currentTimeMillis() <= ch.getDT().getTimeInMillis())
        {   
            try 
            { sleep(5000); } 
            catch (InterruptedException ex) 
            { System.err.println("Oh darn, something went wrong while trying to sleep!"); }
        }
        return true;
    }
 
    public void runChallenge(Challenge ch)
    {
        PrepQuiz pq = new PrepQuiz("desafio 1.txt");
        double points = 0;
        
        for(Question q : pq.getQuiz())
        {
            SendFiles sf = new SendFiles(sendSocket, sendIP, sendPort, receivePort, q.getImageFileName(), q.getSongFileName());
            System.out.println("Sending image");
            sf.sendImage();
            System.out.println("Sending song");
            sf.sendSong();
            System.out.println("Sending question");
            String[] fields = new String[4];
            int codeType = 0;
            fields [0] = q.getQuestion();
            fields [1] = q.getQ1();
            fields [2] = q.getQ2();
            fields [3] = q.getQ3();
            int numFields = 4;

            sendPacket(fields, numFields, codeType, sendPort);
            
            System.out.println("Question sent, going to receive answer now!");
            byte[] answerData = new byte[65507];
            DatagramPacket answerPacket = new DatagramPacket(answerData, answerData.length);
            
            try
            { 
                receiveSocket = new DatagramSocket(receivePort);
                receiveSocket.setReuseAddress(true);
                receiveSocket.receive(answerPacket);
                receiveSocket.close();
            }
            catch (IOException ioe)
            { System.err.println("Oh darn! Somthing went wrong receiving answers! :o" + ioe); }
            
            byte[] receiveDataAnswer = answerPacket.getData();
            int[] answerHeader = new int[8];
            String[] field = breakPDU(receiveDataAnswer, answerHeader);
            
            if(q.getNumAns() == Integer.parseInt(field[0]))
            {
                System.out.println("Question: " + q.getQuestion() + " was answered correctly in: " + field[1] + " second!");
                points = points + (60-Double.parseDouble(field[1]));
            }
            else
                System.out.println("Question: " + q.getQuestion() + " was answered incorrectly in: " + field[1] + " second!");
        }
        
        loggedUser.addPoints(points);
        staticUser = loggedUser;
        
        synchronized(ServerHandlerOut.controlAI)
        {
            ServerHandlerOut.controlAI.set(2);
            ServerHandlerOut.controlAI.notifyAll();
        }
        
        String[] fields = new String[1];
        fields[0] = Double.toString(points);
        int numFields = 1;
        int codeType = 0;
        sendPacket(fields, numFields, codeType, sendPort);
        
    }
    
    public void listPoints()
    {
        if(Data.getUsers().isEmpty())
        {
            String[] fields = new String[1];
            fields [0] = "";
            int codeType = 255;
            int numFields = 1;
            
            sendPacket(fields, numFields, codeType, sendPort);                    
        }
        else
        {
            String[] fields = new String[(Data.getUsers().size())*2];
            int i = 0;
            int codeType = 0;
            int numFields = 0;
            
            for(User us : Data.getUsers())
            {
                fields[i] = us.getUserName();
                i++;
                fields[i] = String.valueOf(us.getPoints());
                i++;
                numFields = numFields + 2;
            }
            sendPacket(fields, numFields, codeType, sendPort);
        }
    }
}