/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cc_server;

/**
 *
 * @author Diogo
 */
public class User {
    private String username;
    private String pass;
    private Integer id;
    public boolean logged;
    
    public User(String username, String pass, Integer id) {
        this.username = username;
        this.pass = pass;
        this.id = id;
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
