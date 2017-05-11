/**
 * SharedEncryptor.java
 * Represents user security data to be attached to each account.
 * Uses AES encryption.
 */

package codeu.chat.util;

import javax.crypto.*;
import java.security.*;
import java.io.UnsupportedEncodingException;

public class Encryptor {
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final String _ENCODING = "ISO-8859-1";

    /**
     * Encryptor
     * Initializes a key to prepare for encryption.
     */
    public Encryptor(boolean shared) {
        try {
            if (shared) {
                KeyGenerator keyMaker = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
                keyMaker.init(128);

                SecretKey key = keyMaker.generateKey();
            } else {
                KeyPairGenerator keyMaker = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
                keyMaker.initialize(2048);

                KeyPair keypair = keyMaker.generateKeyPair();
                PrivateKey privateKey = keypair.getPrivate();
                PublicKey publicKey = keypair.getPublic();
            }
        }
        catch (NoSuchAlgorithmException nsae) {}
    }

    /**
     * encrypt
     * Takes in a byte array and encrypts it into an encrypted byte array.
     * Default version.
     * @param input byte array to encrypt
     * @return encrypted byte array
     */
    public static byte[] encrypt(byte[] input, java.security.Key key) {
        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(input);
        }
        catch (InvalidKeyException iee) {System.out.print("\"INVALIDKEY\"");}
        catch (IllegalBlockSizeException ibse) {System.out.print("\"ILLEGALBLOCKSIZE\"");}
        catch (BadPaddingException bpe) {System.out.print("\"BADPADDING\"");}
        catch (GeneralSecurityException gse) {}

        System.out.println(" on decryption.");
        return null;
    }

    public static String encrypt(String input, java.security.Key key) {
        try {
            return new String(encrypt(input.getBytes(_ENCODING), key), _ENCODING);
        }
        catch (UnsupportedEncodingException uee) {System.out.print("\"UNSUPPORTEDENCODING\"");}

        System.out.println(" on decryption.");
        return null;
    }

    /**
     * decrypt
     * Decrypts a previously encrypted byte array. Default version
     * @param input encrypted byte array
     * @return the plaintext yielded from the byte array
     */
    public byte[] decrypt(byte[] input, java.security.Key key) {
        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(input);
        }
        catch (InvalidKeyException iee) {System.out.print("\"INVALIDKEY\"");}
        catch (IllegalBlockSizeException ibse) {System.out.print("\"ILLEGALBLOCKSIZE\"");}
        catch (BadPaddingException bpe) {System.out.print("\"BADPADDING\"");}
        catch (GeneralSecurityException gse) {}

        System.out.println(" on decryption.");
        return null;
    }

    public String decrypt(String input, java.security.Key key) {
        try {
            return new String(decrypt(input.getBytes(_ENCODING), key), _ENCODING);
        }
        catch (UnsupportedEncodingException uee) {System.out.print("\"UNSUPPORTEDENCODING\"");}

        System.out.println(" on decryption.");
        return null;
    }
}

