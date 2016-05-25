/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package client;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Hashtable;
import java.util.StringTokenizer;

/**
 *
 * @author Francisco
 */
public class ConnectionHandler {
    
    static int sendPORT = 10000;
    static int sendMulticastPORT = 7500;
    static int sendAnswerPORT;
    static InetAddress ip;
    static DatagramSocket mainSocket;
    static String question;
    static String user;
    
    static InetAddress group;    
    
    ConnectionHandler()
    {
        try
        { 
            ip = InetAddress.getByName("localhost"); 
            group = InetAddress.getByName("225.0.0.1"); 
        }
        catch (UnknownHostException uhe)
        { System.err.println("Oh Darn, something went wrong assigning the IP :o "); }
    }
    
    public void sendHello()
    {
        System.out.println("Sending Hello request!");
        sendHelloRequest();
        
        /*System.out.println("Receiving Hello reply!");

        InetAddress newServerIP = receiveHelloReply();
        
        if( newServerIP != null)
        {
            System.out.println("IP changed!");
            ip = newServerIP;
        }*/
        
        System.out.println("Server IP: " + ip);
    }
    private void sendHelloRequest()
    {
        try{
            DatagramPacket packetToSend;
            
            // buffers size calculation
            int bufferSize = 8;
            
            byte[] sendData = new byte[bufferSize];
            
            String[] fieldList = new String[1];
            fieldList[0] = " ";
            
            // buildPDU(ver, sec, label, type, numFields, fieldList)
            sendData = buildPDU(0, 0, 1, 1, 1, fieldList);
            
            packetToSend = new DatagramPacket(sendData, sendData.length, group, sendMulticastPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private InetAddress receiveHelloReply()
    {
        InetAddress newIP = null;
        
        try
        {
            byte[] data = new byte[65507];
            DatagramPacket packetReceived = new DatagramPacket(data, data.length); 
           
            mainSocket.setSoTimeout(3000);
            mainSocket.receive(packetReceived);

            byte[] receiveData = packetReceived.getData();
            
            int[] header = new int[8];
            String[] fieldListOut = breakPDU(receiveData, header);
            
            if(header[4] == 0)
                newIP = packetReceived.getAddress();
            
        }
        catch(IOException e)
        {
            System.err.println("Oh Darn! Error in receiveHelloReply!! :'( ");
            newIP = null;
        }
        
        return newIP;
    }
    
    public void InitOnce()
    {
        try
        { mainSocket = new DatagramSocket(); }
        catch (IOException ioe)
        { System.err.println("Oh Darn, something went wrong creating the mainSocket :o " + ioe); } 
    }
    
    public String login(String str, char[] pass){
        
        sendHello(); 
        
        String name = "";
        
        sendLoginRequest(str, pass);
        name = receiveLoginReply();
        
        return name;
    }
    
    public boolean register(String name, String username, char[] pass){
        
        sendHello(); 
       
        boolean res = false;
        
        sendRegisterRequest(name, username, pass);
        res = receiveRegisterReply();
        
        return res;
    }
    
    private void sendRegisterRequest(String name, String username, char[] pass){
        try{
            DatagramPacket packetToSend;
            
            // buffers size calculation
            int bufferSize = 8 + username.length() + pass.length + name.length();
            
            byte[] sendData = new byte[bufferSize];
            
            String[] fieldList = new String[3];
            fieldList[0] = name;
            fieldList[1] = username;
            fieldList[2] = new String(pass);
            
            // buildPDU(ver, sec, label, type, numFields, fieldList)
            sendData = buildPDU(0, 0, 1, 2, 3, fieldList);
            
            System.out.println("Sending register request on ip: " + ip);
            
            packetToSend = new DatagramPacket(sendData, sendData.length, ip, sendPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
       
    }
    
    private boolean receiveRegisterReply(){
        boolean res = false;
        
        try{
            byte[] data = new byte[65507];
            DatagramPacket packetReceived = new DatagramPacket(data, data.length); 
           
            mainSocket.setSoTimeout(3000);
            mainSocket.receive(packetReceived);

            byte[] receiveData = packetReceived.getData();
            
            int[] header = new int[8];
            String[] fieldListOut = breakPDU(receiveData, header);
            
            if(header[4] == 0)
                res = true;
            
        }catch(IOException e){
            e.printStackTrace();
        }
        return res;
    }
    
    private void sendLoginRequest(String str, char[] pass){
        try{
            DatagramPacket packetToSend;
            
            // buffers size calculation
            int bufferSize = 8 + str.length() + pass.length;
            
            byte[] sendData = new byte[bufferSize];
            
            String[] fieldList = new String[2];
            fieldList[0] = str;
            fieldList[1] = new String(pass);
            
            // buildPDU(ver, sec, label, type, numFields, fieldList)
            sendData = buildPDU(0, 0, 1, 3, 2, fieldList);
            
            packetToSend = new DatagramPacket(sendData, sendData.length, ip, sendPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
       
    }
    
    private String receiveLoginReply(){
        String userName = null;
        
        try{
            byte[] data = new byte[65507];
            DatagramPacket packetReceived = new DatagramPacket(data, data.length); 
           
            mainSocket.setSoTimeout(10000);
            mainSocket.receive(packetReceived);

            byte[] receiveData = packetReceived.getData();
            
            int[] header = new int[8];
            String[] fieldListOut = breakPDU(receiveData, header);

            
            if(header[4] == 255)
            {
                return "";
            }
            else if(header[4] == 0)
            {
                userName = fieldListOut[0];
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return userName;
    }
    
    public String[] getChallengesList(){
        
        challengeListRequest();
        
        String[] challengeList = challengeListReply();
   
        return challengeList; 
    }
    
    private void challengeListRequest(){
        try{
            DatagramPacket packetToSend;
            
            // buffers size calculation
            int bufferSize = 9;
            
            byte[] sendData = new byte[bufferSize];
            
            String[] fieldList = new String[1];
            fieldList[0] = "";
            
            // buildPDU(ver, sec, label, type, numFields, fieldList)
            sendData = buildPDU(0, 0, 1, 7, 1, fieldList);
            
            packetToSend = new DatagramPacket(sendData, sendData.length, ip, sendPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private String[] challengeListReply(){
        
        String[] cL = null;

        try{
            byte[] data = new byte[65507];
            DatagramPacket packetReceived = new DatagramPacket(data, data.length); 
           
            //receiveSocket.setSoTimeout(10000);
            
            mainSocket.receive(packetReceived);
            
            byte[] receiveData = packetReceived.getData();
            
            int[] header = new int[8];
            String[] fieldListOut = breakPDU(receiveData, header);
            
            if(header[4] == 255)
            {
                return null;
            }
            else if(header[4] == 0)
            {
                cL = fieldListOut;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
        return cL;
         
    }
    
    public boolean createChallenge(String name, String ano, String mes, String dia, String hora, String min, String seg){
        
        createChallengeRequest(name, ano, mes, dia, hora, min, seg);
       
        System.out.println("Enviou pedido de criação de um desafio!");
        
        boolean res = createChallengeReply();
   
        return res; 
    }
    
    private void createChallengeRequest(String name, String ano, String mes, String dia, String hora, String min, String seg){
        try{
            DatagramPacket packetToSend;
            
            byte[] sendData = null;
            String[] fieldList = null;
           
            
            if(!ano.equals("") && !mes.equals("") && !dia.equals("") && !hora.equals("") && !min.equals("") && !seg.equals("")){
                // buffers size calculation
                int bufferSize = 8 + name.length() + ano.length() + mes.length() + dia.length() + hora.length() + min.length() + seg.length() + user.length();

                sendData = new byte[bufferSize];

                fieldList = new String[8];
                fieldList[0] = name;
                fieldList[1] = ano;
                fieldList[2] = mes;
                fieldList[3] = dia;
                fieldList[4] = hora;
                fieldList[5] = min;
                fieldList[6] = seg;
                fieldList[7] = user;
                
                // buildPDU(ver, sec, label, type, numFields, fieldList)
                sendData = buildPDU(0, 0, 1, 8, 8, fieldList);
            }
            else{
                
                // buffers size calculation
                int bufferSize = 8 + name.length() + user.length();

                sendData = new byte[bufferSize];

                fieldList = new String[2];
                fieldList[0] = name;
                fieldList[1] = user;
                
                // buildPDU(ver, sec, label, type, numFields, fieldList)
                sendData = buildPDU(0, 0, 1, 8, 2, fieldList);
            }
            
            packetToSend = new DatagramPacket(sendData, sendData.length, ip, sendPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private boolean createChallengeReply()
    {
        byte[] data;
        DatagramPacket packetReceived;
        byte[] receiveData;
        int[] header;
        String[] fieldListOut;
                
        try
        {
            data = new byte[65507];
            packetReceived = new DatagramPacket(data, data.length); 
           
            mainSocket.setSoTimeout(10000);
            mainSocket.receive(packetReceived);
            
            receiveData = packetReceived.getData();
            
            header = new int[8];
            fieldListOut = breakPDU(receiveData, header);

            sendAnswerPORT = Integer.parseInt(fieldListOut[0]);
            System.out.println("sendAnswerPort: " + sendAnswerPORT);
            
            if(header[4] == 255)
                return false;
            else
            {
                data = new byte[65507];
                packetReceived = new DatagramPacket(data, data.length); 
                mainSocket.setSoTimeout(0);
                mainSocket.receive(packetReceived);
                receiveData = packetReceived.getData();
                header = new int[8];
                breakPDU(receiveData, header);
                
                if(header[4] == 0)
                    return true;
                else 
                    if(header[4] == 255)
                        return false;
            }
        }
        catch(IOException e)
        { e.printStackTrace(); }
        
        return false;
    }
    
    
    public boolean acceptChallenge(String challengeName) 
    {
        acceptChallengeRequest(challengeName);
       
        System.out.println("Enviou pedido de aceitação de um desafio!");
        
        boolean res = acceptChallengeReply();
   
        return res; 
    }
    
    private void acceptChallengeRequest(String challengeName){
        try{
            DatagramPacket packetToSend;
            
            byte[] sendData = null;
            String[] fieldList = null;
           
            
            // buffers size calculation
            int bufferSize = 8 + challengeName.length() + user.length();
           

            sendData = new byte[bufferSize];

            fieldList = new String[2];
            fieldList[0] = challengeName;
            fieldList[1] = user;
            // buildPDU(ver, sec, label, type, numFields, fieldList)
            sendData = buildPDU(0, 0, 1, 9, 2, fieldList);
            
            packetToSend = new DatagramPacket(sendData, sendData.length, ip, sendPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private boolean acceptChallengeReply()
    {
        byte[] data;
        DatagramPacket packetReceived;
        byte[] receiveData;
        int[] header;
        String[] fieldListOut;
                
        try
        {
            data = new byte[65507];
            packetReceived = new DatagramPacket(data, data.length); 
           
            mainSocket.setSoTimeout(10000);
            mainSocket.receive(packetReceived);
            
            receiveData = packetReceived.getData();
            
            header = new int[8];
            fieldListOut = breakPDU(receiveData, header);

            sendAnswerPORT = Integer.parseInt(fieldListOut[0]);
            System.out.println("sendAnswerPort: " + sendAnswerPORT);
            
            if(header[4] == 255)
                return false;
            else
            {
                System.out.println("Receiving second packet");
                data = new byte[65507];
                packetReceived = new DatagramPacket(data, data.length); 
                mainSocket.setSoTimeout(0);
                mainSocket.receive(packetReceived);
                receiveData = packetReceived.getData();
                header = new int[8];
                breakPDU(receiveData, header);
                System.out.println("Checking header");
                System.out.println("Header is: " + header[4]);
                
                if(header[4] == 0)
                    return true;
                else 
                    if(header[4] == 255)
                        return false;
            }
        }
        catch(IOException e)
        { e.printStackTrace(); }
        
        return false;
    }
    
    public Hashtable<String, String> getRankings()
    {
        
        sendRankingRequest();
        
        return receiveRankingReply();
    }
    
    private void sendRankingRequest()
    {
        try{
            DatagramPacket packetToSend;
            
            // buffers size calculation
            int bufferSize = 8;
            
            byte[] sendData = new byte[bufferSize];
            
            String[] fieldList = new String[1];
            fieldList[0] = " ";
            
            // buildPDU(ver, sec, label, type, numFields, fieldList)
            sendData = buildPDU(0, 0, 1, 10, 1, fieldList);
            
            packetToSend = new DatagramPacket(sendData, sendData.length, ip, sendPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    
    private Hashtable<String, String> receiveRankingReply()
    {
        Hashtable<String, String> newRanking = new Hashtable<String, String>();
        
        try{
            byte[] data = new byte[65507];
            DatagramPacket packetReceived = new DatagramPacket(data, data.length); 
           
            //receiveSocket.setSoTimeout(10000);
            
            mainSocket.receive(packetReceived);
            
            byte[] receiveData = packetReceived.getData();
            
            int[] header = new int[8];
            String[] fieldListOut = breakPDU(receiveData, header);
            
            if(header[4] == 255)
            {
                return null;
            }
            else if(header[4] == 0)
            {
                int i = 0;
                String username = "";
                String pontos = "";
                
                for(String str : fieldListOut)
                {
                    if((i%2) == 0){
                        username = str;
                    }
                    else
                    {
                        pontos = str;
                        newRanking.put(username, pontos);
                    }
                    
                    i++;
                }
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
        for(String str : newRanking.keySet())
            System.out.println(str + " :" + newRanking.get(str));
        
        return newRanking;
    }
    
    /*public void waitForGameStart()
    {

        try{
            byte[] data = new byte[65507];
            DatagramPacket packetReceived = new DatagramPacket(data, data.length);
           
            //mainSocket.setSoTimeout(10000);
            
            System.out.println("Socket current port: " + mainSocket.getPort());
            System.out.println("Waiting for question");
            mainSocket.receive(packetReceived);
            
            byte[] receiveData = packetReceived.getData();
            
            int[] header = new int[8];
            String[] fieldListOut = breakPDU(receiveData, header);

            for(int i = 0; i < 8; i++)
                System.out.println(header[i]);
            for(String str : fieldListOut)
                System.out.println(str);
            
            System.out.println("Resposta do servidor recebida");
            
            if(header[4] == 255)
            {
                
                //return "FAIL!!";
            }
            else if(header[4] == 0)
            {
                question = fieldListOut[0];
                 //return fieldListOut[0];
            }
            //return "HOLY SHIT";
        }catch(IOException e){
            e.printStackTrace();
        }
        
        //return null;
    }
    
    public void nextQuestion(){
        waitForGameStart();
        new JFrameQuiz(null, ConnectionHandler.question).setVisible(true);
    }*/
    
    /*public void sendAnswer(String ans, double time){
        try{
            DatagramPacket packetToSend;
            
            byte[] sendData = null;
            String[] fieldList = null;
           
            String t = Double.toString(time);
            
            // buffers size calculation
            int bufferSize = 8 + ans.length() + t.length();

            sendData = new byte[bufferSize];

            fieldList = new String[2];
            fieldList[0] = ans;
            fieldList[1] = t;


            // buildPDU(ver, sec, label, type, numFields, fieldList)
            sendData = buildPDU(0, 0, 1, 11, 2, fieldList);
            
            packetToSend = new DatagramPacket(sendData, sendData.length, ip, sendAnswerPORT);
            mainSocket.send(packetToSend);
        }catch(IOException e){
            e.printStackTrace();
        }
    }*/
    
    
    public byte[] buildPDU(int ver, int sec, int label, int type, int numFields, String[] fieldList){
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
        for(String str : fieldList){
            byte[] aux = str.getBytes();    
            for(byte b : aux)
                auxFieldList[i++] = b;
            auxFieldList[i++] = '\0';
        }
        
        PDU[6] = (byte) (((size*1.0)/255)-129);
        PDU[7] = (byte) ((size % 255)-128);
        
        for( i = 0; i < size; i++){
            PDU[i+8] = auxFieldList[i];
        }
        
        return PDU;
    }
    
    public String[] breakPDU(byte[] pdu, int[] header){
        int k = 0;
        for(; k < 8; k++)
            header[k] = pdu[k]+128;
        
        String[] fieldList = new String[header[5]];
        String aux = new String(pdu, 8, pdu.length-8);
        StringTokenizer st = new StringTokenizer(aux, "\0");
        int i = 0;
        while(st.hasMoreElements()){
            fieldList[i++] = st.nextElement().toString();
        }
        return fieldList;
    }
    
    public void closeSocket()
    { mainSocket.close(); }

}
