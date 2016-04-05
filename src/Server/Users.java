/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Server;

import java.io.Serializable;
import java.util.TreeMap;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author Diogo
 */
public class Users implements Serializable{
    private TreeMap < Integer, User> utilizadores;
    private Lock l = new ReentrantLock (); 
    
    public Users () {
        this.utilizadores = new TreeMap <Integer, User>();
    }
    public TreeMap<Integer, User> getUtilizadores() {
        return utilizadores;
    }

    public void setUtilizadores(TreeMap<Integer, User> utilizadores) {
        this.utilizadores = utilizadores;
    }
    
    public synchronized void addUser(User u){
            this.utilizadores.put(u.getId(), u);
    }
    
    public synchronized void removeUser(String username){
        if(this.utilizadores.containsKey(username)){
            this.utilizadores.remove(username);
        }
    }
    
    public Integer lastKey() {
        if (utilizadores.isEmpty())
            return 0;
        return utilizadores.lastKey();
    }
}
