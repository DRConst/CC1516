/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author Diogo
 */
public class Client {
    public static void main(String[] args){
        try{
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
            Socket c = new Socket("localhost", 123);
            BufferedReader in = new BufferedReader (new InputStreamReader(c.getInputStream()));
            PrintWriter out = new PrintWriter (c.getOutputStream(),true);
            System.out.println("Conexão efetuada!\n"
                + "Menu\n"
                + "Registar" + "...\n");
            String s,resp;
            resp = "";
            while(!resp.equals("Saiu do sistema")){
                s = keyboard.readLine();
                out.println(s);
                resp = in.readLine();
                System.out.println(resp);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
