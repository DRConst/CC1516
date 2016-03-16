package Server;


import Commons.Serializer;
import Commons.User;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static java.lang.Thread.sleep;

/**
 * Created by drcon on 21/12/2015.
 */
public class Svr_ClientHandler {
    Socket socket, hbCliSocket, pushCliSocket;
    ServerSocket hbSvrSocket, pushSvrSocket;
    boolean timeout = false;
    HashMap<String,String> lastCommand;

    private BufferedReader input, hbIn;
    private PrintWriter output, hbOut;


    private void initHeartbeat() throws IOException {
        hbSvrSocket = new ServerSocket(0);
        int hbPort = hbSvrSocket.getLocalPort();
        hbSvrSocket.setSoTimeout(10000);
        output.println(hbPort);
        output.flush();
        hbCliSocket = hbSvrSocket.accept();
        hbCliSocket.setSoTimeout(10000);

        hbOut = new PrintWriter(new OutputStreamWriter(hbCliSocket.getOutputStream()));
        hbIn = new BufferedReader(new InputStreamReader(hbCliSocket.getInputStream()));

        hbSvrSocket.close();
    }
    public void heartbeat() throws ClientTimedOutException {

        String response;
        while(!timeout)
        {
            hbOut.println("heart");
            hbOut.flush();


            try
            {
                response = hbIn.readLine();
                if(!response.equals("beat"))
                {
                    //Something went wrong, just drop the connection;
                    throw new ClientTimedOutException();
                }
            } catch (IOException e) {
                throw new ClientTimedOutException();
            }


            try {
                sleep(500); //Only ping every half second
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    private void cleanup() {
        timeout = true;
        try{
            this.socket.close();
            this.hbCliSocket.close();
            this.hbSvrSocket.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }
}