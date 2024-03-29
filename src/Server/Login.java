package Server;

import Commons.User;

import java.util.Map;
 /**
 * Created by drcon on 21/12/2015.
 */

        import java.io.ByteArrayOutputStream;
        import java.io.IOException;
        import java.io.Serializable;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.security.SecureRandom;
        import java.util.Arrays;
        import java.util.HashMap;

public class Login implements Serializable
{
    private HashMap<String, byte[]> hashes, salts;
    private HashMap<String, User> users;
    private HashMap<String, Boolean> loggedIn;
    private HashMap<String, Integer> loggedInServerID;
    private Users userStorage;


    public Login()
    {
        hashes = new HashMap<>();
        salts = new HashMap<>();
        users = new HashMap<>();
        loggedIn = new HashMap<>();
        loggedInServerID = new HashMap<>();
    }

    public Login(Login l)
    {
        hashes = l.getHashes();
        salts = l.getSalts();
        users = l.getUsers();
        loggedIn = l.getLoggedIn();
        loggedInServerID = l.getLoggedInServerID();
    }

    public synchronized  void  registerUser(String userName, String password) throws IOException, NoSuchAlgorithmException, UserRegisteredException {
        byte[] salt = genSalt();
        byte[] hash = genHash(salt, password);
        if(users.containsKey(userName))
        {
            throw new UserRegisteredException("Commons.User " + userName + " already registered.");
        }else
        {
            registerSalt(userName, salt);
            registerPw(userName, hash);
            User u = new User(userName, password, userStorage.lastKey());
            users.put(userName, u);
            userStorage.addUser(u);
            loggedIn.put(userName,false);
            loggedInServerID.put(userName, -1);

        }
    }
    
    public void setUserStorage(Users u){
        userStorage = u;
    }

    public boolean checkRegistration(String userName, String password) throws IOException, NoSuchAlgorithmException {

        byte[] salt;
        byte[] hash, hash2;

        if(!salts.containsKey(userName) || !hashes.containsKey(userName))
            return false;

        salt = salts.get(userName);
        hash = genHash(salt, password);
        hash2 = hashes.get(userName);
        if(Arrays.equals(hash2, hash))
            return true;
        else
            return false;
    }

    public void deleteUser(String email) throws UserNotFoundException
    {
        if(users.containsKey(email))
        {
            users.remove(email);
            salts.remove(email);
            hashes.remove(email);
        }else
        {
            throw new UserNotFoundException("Commons.User " + email + " not found.");
        }
    }

    public synchronized User authenticateUser(String userName, String password) throws IOException, NoSuchAlgorithmException, UserNotFoundException, LoginFailedException {
        User usr;
        if(checkRegistration(userName, password)) {
            usr =  users.get(userName);
            loggedIn.replace(userName, true);
            return usr;

        }else
        {
            if(!users.containsKey(userName))
                throw new UserNotFoundException("Commons.User " + userName + " not found.");
            else
                throw new LoginFailedException("Server.Login Failed.");
        }
    }

    public synchronized void setLoggedIn(String userName, Boolean status, int serverID)
    {
        if(users.containsKey(userName))
        {
            loggedIn.replace(userName, status);
            if(!status)
                serverID = -1;
            loggedInServerID.replace(userName, serverID);
        }

    }

    public User getRegisteredUser(String userName)
    {
        return users.get(userName);
    }

    private synchronized void registerPw(String userName, byte[] hash)
    {
        if(hashes.containsKey(userName))
        {
            hashes.replace(userName, hash);
        }
        else
            hashes.put(userName, hash);
    }
    private synchronized void registerSalt(String userName, byte [] salt)
    {
        if(salts.containsKey(userName))
        {
            salts.replace(userName, salt);
        }
        else
            salts.put(userName, salt);
    }

    private byte[] genHash(byte[] salt, String password) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        bStream.write(salt);
        bStream.write(password.getBytes());
        md.update(bStream.toByteArray());
        return md.digest();
    }

    private byte[] genSalt()
    {
        byte[] toRet = new byte[128];
        new SecureRandom().nextBytes(toRet);
        return toRet;
    }


    public boolean equals(Object o)
    {
        if(this == o)
            return true;

        if(o == null || this.getClass() != o.getClass() ) {
            return false;
        }
        else
        {
            Login toTest = (Login) o;
            for(Map.Entry<String, byte[]> entry : toTest.getHashes().entrySet())
            {
                if(!hashes.containsKey(entry.getKey()) || Arrays.equals(entry.getValue(), hashes.get(entry.getKey())))
                    return false;
            }
            for(Map.Entry<String, byte[]> entry : toTest.getSalts().entrySet())
            {
                if(!salts.containsKey(entry.getKey()) || Arrays.equals(entry.getValue(), salts.get(entry.getKey())))
                    return false;
            }
            return true;
        }
    }

    public HashMap<String, Boolean> getLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(HashMap<String, Boolean> loggedIn) {
        this.loggedIn = loggedIn;
    }

    public HashMap<String, byte[]> getHashes() {
        return hashes;
    }

    public HashMap<String, byte[]> getSalts() {
        return salts;
    }

    public HashMap<String, User> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, User> users) {
        this.users = users;
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("Salts-");
        for(Map.Entry<String, byte[]> entry : salts.entrySet())
        {
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            sb.append(";");
        }
        sb.append("\nHashes-");
        for(Map.Entry<String, byte[]> entry : hashes.entrySet())
        {
            sb.append(entry.getKey());
            sb.append(":");
            sb.append(entry.getValue());
            sb.append(";");
        }

        return sb.toString();
    }

    public Login clone(Login l)
    {
        return new Login(l);
    }

    public boolean loggedIntoServer(String user, int id)
    {
        boolean toRet = false;

        if(loggedIn.get(user) && loggedInServerID.get(user) == id)
            toRet = true;


        return toRet;
    }

    public HashMap<String, Integer> getLoggedInServerID() {
        return loggedInServerID;
    }

    public void setLoggedInServerID(HashMap<String, Integer> loggedInServerID) {
        this.loggedInServerID = loggedInServerID;
    }
}
