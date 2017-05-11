/**
 * SharedEncryptor.java
 * Represents user security data to be attached to each account.
 * Uses AES encryption.
 */

package codeu.chat.util.encryption;

import javax.crypto.*;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;

public class SharedEncryptor {
    private final String _ALGORITHM = "AES";
    private final String _ENCODING = "ISO-8859-1";

    // AES key
    private SecretKey key;
    private Cipher cipher;

    /**
     * SharedEncryptor
     * Initializes a key and cipher to prepare for symmetric-key encryption.
     */
    public SharedEncryptor() {
        try {
            KeyGenerator keyMaker = KeyGenerator.getInstance(_ALGORITHM);
            keyMaker.init(128);

            key = keyMaker.generateKey();
            cipher = Cipher.getInstance(_ALGORITHM);
        }
        catch (GeneralSecurityException gse) {}
    }

    /**
     * encrypt
     * Takes in a byte array and encrypts it into an encrypted byte array.
     * Default version.
     * @param input byte array to encrypt
     * @return encrypted byte array
     */
    public byte[] encrypt(byte[] input) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            return cipher.doFinal(input);
        }
        catch (GeneralSecurityException gse) {return null;}
    }

    public String encrypt(String input) {
        try {
            cipher.init(Cipher.ENCRYPT_MODE, getKey());
            return new String(cipher.doFinal(input.getBytes(_ENCODING)), _ENCODING);
        }
        catch (UnsupportedEncodingException uee) {System.out.print("\"UNSUPPORTEDENCODING\"");}
        catch (InvalidKeyException iee) {System.out.print("\"INVALIDKEY\"");}
        catch (IllegalBlockSizeException ibse) {System.out.print("\"ILLEGALBLOCKSIZE\"");}
        catch (BadPaddingException bpe) {System.out.print("\"BADPADDING\"");}

        System.out.println(" on encryption.");
        return null;
    }

    /**
     * decrypt
     * Decrypts a previously encrypted byte array. Default version
     * @param input encrypted byte array
     * @return the plaintext yielded from the byte array
     */
    public byte[] decrypt(byte[] input) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            return cipher.doFinal(input);
        }
        catch (GeneralSecurityException gse) {return null;}
    }

    public String decrypt(String input) {
        try {
            cipher.init(Cipher.DECRYPT_MODE, getKey());
            return new String(cipher.doFinal(input.getBytes(_ENCODING)), _ENCODING);
        }
        catch (UnsupportedEncodingException uee) {System.out.print("\"UNSUPPORTEDENCODING\"");}
        catch (InvalidKeyException iee) {System.out.print("\"INVALIDKEY\"");}
        catch (IllegalBlockSizeException ibse) {System.out.print("\"ILLEGALBLOCKSIZE\"");}
        catch (BadPaddingException bpe) {System.out.print("\"BADPADDING\"");}

        System.out.println(" on decryption.");
        return null;
    }

    /**
     * getKey
     * Returns the key.
     * @return the key that can encrypt/decrypt
     */
    public SecretKey getKey() {
        return key;
    }
}
