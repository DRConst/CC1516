/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Commons;

import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;

/**
 *
 * @author Diogo
 */
public class User implements Serializable{
    private String username;
    private String pass;
    private Integer id;
    private Integer port;
    private InetAddress ip;
    public boolean logged;
    
    public User(String username, String pass, Integer id) {
        this.username = username;
        this.pass = pass;
        this.id = id;
    }
    public User(String username, String pass, Integer id, Integer port) {
        this.username = username;
        this.pass = pass;
        this.id = id;
        this.port = port;
    }

    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getPass() {
        return pass;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public void setPass (String pass){
        this.pass = pass;
    }
    
    public void setId (Integer id) {
        this.id = id;
    }
    
     public boolean isLogged() {
        return logged;
    }

    public void setLogged(boolean logged) {
        this.logged = logged;
    }


    
    
}
