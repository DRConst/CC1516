/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.IOException;

/**
 *
 * @author Diogo
 */
public class Main {
    
      public static void main(String[] args) throws IOException {
          Users utilizadores=new Users(); 
          Thread server = new Thread(new Server(utilizadores));
          server.start();
    }
}
