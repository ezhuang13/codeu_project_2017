/**
 * Encryptor.java
 * Provides various encryption-related methods.
 */

package codeu.chat.util;

import javax.crypto.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.*;

public class Encryptor {
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final String ASYMMETRIC_ALGORITHM = "RSA";

    public static SecretKey makeSymmetricKey() {
        KeyGenerator keyMaker;

        try {
            keyMaker = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
        } catch(NoSuchAlgorithmException nsae) {return null;}

        keyMaker.init(128);
        return keyMaker.generateKey();
    }

    public static KeyPair makeAsymmetricKeyPair() {
        KeyPairGenerator keyMaker;

        try {
            keyMaker = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
        } catch(NoSuchAlgorithmException nsae) {return null;}

        keyMaker.initialize(2048);
        return keyMaker.generateKeyPair();
    }

    public static byte[] wrap(SecretKey keyToWrap, PublicKey publicKey) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        }
        catch(GeneralSecurityException gse) {return null;}

        try {
            cipher.init(Cipher.WRAP_MODE, publicKey);
        }
        catch (InvalidKeyException iee) {System.out.println("INVALID public key."); return null;}

        try {
            return cipher.wrap(keyToWrap);
        }
        catch (InvalidKeyException iee) {System.out.println("INVALID symmetric key.");}
        catch (IllegalBlockSizeException ibse) {System.out.println("ILLEGAL BLOCK SIZE on wrapping.");}

        return null;
    }

    public static SecretKey unwrap(byte[] keyToUnwrap, PrivateKey privateKey) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
        }
        catch(GeneralSecurityException gse) {return null;}

        try {
            cipher.init(Cipher.UNWRAP_MODE, privateKey);
        }
        catch (InvalidKeyException iee) {System.out.println("INVALID public key."); return null;}

        try {
            return (SecretKey) cipher.unwrap(keyToUnwrap, SYMMETRIC_ALGORITHM, Cipher.SECRET_KEY);
        }
        catch (InvalidKeyException iee) {System.out.println("INVALID symmetric key.");}
        catch (GeneralSecurityException gse) {}

        return null;
    }

    public static void writeKey(OutputStream out, SecretKey key, PublicKey publicKey) throws IOException {
        byte[] value = wrap(key, publicKey);
        Serializers.INTEGER.write(out, value.length);
        out.write(value);
    }

    public static SecretKey readKey(InputStream input, PrivateKey privateKey) throws IOException {
        final int length = Serializers.INTEGER.read(input);
        final byte[] value = new byte[length];

        for (int i = 0; i < length; i++) {
            value[i] = (byte) input.read();
        }

        return unwrap(value, privateKey);
    }

    /**
     * encrypt
     * Takes in a byte array and encrypts it into an encrypted byte array.
     * @param input byte array to encrypt
     * @return encrypted byte array
     */
    public static byte[] encrypt(byte[] input, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return cipher.doFinal(input);
        }
        catch (InvalidKeyException iee) {System.out.print("INVALID KEY");}
        catch (IllegalBlockSizeException ibse) {System.out.print("ILLEGAL BLOCK SIZE");}
        catch (BadPaddingException bpe) {System.out.print("BAD PADDING");}
        catch (GeneralSecurityException gse) {}

        System.out.println(" on decryption.");
        return null;
    }

    /**
     * decrypt
     * Decrypts a previously encrypted byte array.
     * @param input encrypted byte array
     * @return the plaintext yielded from the byte array
     */
    public static byte[] decrypt(byte[] input, SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(input);
        }
        catch (InvalidKeyException iee) {System.out.print("INVALID KEY");}
        catch (IllegalBlockSizeException ibse) {System.out.print("ILLEGAL BLOCK SIZE");}
        catch (BadPaddingException bpe) {System.out.print("BAD PADDING");}
        catch (GeneralSecurityException gse) {}

        System.out.println(" on decryption.");
        return null;
    }
}

