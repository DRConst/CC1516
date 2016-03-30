package Commons;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Created by drcon on 30/03/2016.
 */
public class RSAEncryptionHelper {
    Cipher encryptCipher, decryptCipher;
    PublicKey publicKey;
    PrivateKey privateKey;



    public RSAEncryptionHelper(String pubKey, String privKey) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(pubKey.getBytes());
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(privKey.getBytes());

        publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
        privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);


        encryptCipher = Cipher.getInstance("RSA");
        decryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
    }

    public String encryptString(String target) throws BadPaddingException, IllegalBlockSizeException {
        return new String(encryptCipher.doFinal(target.getBytes()));
    }

    public String decryptString(String target) throws BadPaddingException, IllegalBlockSizeException {
        return new String(decryptCipher.doFinal(target.getBytes()));
    }
}
