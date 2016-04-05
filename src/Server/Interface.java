package Server;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Diogo
 */
public class Interface implements Runnable{
    private Socket client;
    private BufferedReader in;
    private PrintWriter out;
    private Users utilizadores;
    private Login login;
    private User activeUser = null;
    
    public Interface(Socket client, Users utilizadores, Login login) throws IOException{
        this.client=client;
        this.in= new BufferedReader(new InputStreamReader(client.getInputStream()));
        this.out= new PrintWriter(client.getOutputStream(),true);
        this.utilizadores=utilizadores;
        this.login = login;
    }
    
    public int handle() throws IOException, InterruptedException{
        int flag=1;
        switch(in.readLine()) {
            case "Registar":
                String user, pass;
                out.println("Introduza o seu username");
                user = in.readLine();
                out.println("Introduza a sua password");
                pass = in.readLine();

                out.println("Utilizador registado! Selecione nova opção.");
                try {
                    this.login.registerUser(user, pass);
                } catch (NoSuchAlgorithmException ex) {
                    Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
                } catch (UserRegisteredException ex) {
                    Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

        case "Login":  String u, p;
                out.println("Introduza o seu username");
                u=in.readLine();
                out.println("Introduza a sua password");
                p=in.readLine();
            try {
                activeUser = login.authenticateUser(u,p);
                out.println("Login correcto");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UserNotFoundException e) {
                e.printStackTrace();
            } catch (LoginFailedException e) {
                out.println("Login errado");
                break;
            }
            break;
            case "Sair": flag=0;
                         if(activeUser!=null)
                                 this.login.setLoggedIn(activeUser.getUsername(),false);
                         out.println("Saiu do sistema");
                         break;
                         
            default: out.println("Comando errado");
        }
        return flag;
    }
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        try {  
            try {
                while(handle()!=0);
            } catch (InterruptedException ex) {
                Logger.getLogger(Interface.class.getName()).log(Level.SEVERE, null, ex);
            }
            this.in.close();
            this.out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
          }
    }
    
}
