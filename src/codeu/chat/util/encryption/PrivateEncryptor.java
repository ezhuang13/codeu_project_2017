/**
 * PrivateEncryptor.java
 * Represents user security data to be attached to each account.
 * Uses AES encryption.
 */

package codeu.chat.util.encryption;

import javax.crypto.Cipher;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

public class PrivateEncryptor {
    /**
     * PrivateEncryptor
     * Initializes a key and cipher to prepare for asymmetric-key encryption.
     * @throws Exception allows for KeyGenerator exceptions.
     */
    public PrivateEncryptor() throws Exception {
        KeyPairGenerator keyMaker = KeyPairGenerator.getInstance("RSA");
        keyMaker.initialize(2048);

        KeyPair keypair = keyMaker.generateKeyPair();
        privateKey = keypair.getPrivate();
        publicKey = keypair.getPublic();

        cipher = Cipher.getInstance("RSA");
    }

    /**
     * encrypt
     * Takes in a byte array and encrypts it into an encrypted byte array.
     * @param input byte array to encrypt
     * @param keyToEncrypt key used for encryption
     * @return encrypted byte array
     * @throws Exception
     */
    public byte[] encrypt(byte[] input, PublicKey keyToEncrypt) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, keyToEncrypt);
        return cipher.doFinal(input);
    }

    /**
     * encrypt
     * Takes in a byte array and encrypts it into an encrypted byte array.
     * Default version.
     * @param input byte array to encrypt
     * @return encrypted byte array
     * @throws Exception
     */
    public byte[] encrypt(byte[] input) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
        return cipher.doFinal(input);
    }

    /**
     * decrypt
     * Decrypts a previously encrypted byte array.
     * @param input encrypted byte array
     * @param keyToDecrypt key to use for decryption
     * @return the plaintext yielded from the byte array
     * @throws Exception
     */
    public byte[] decrypt(byte[] input, PrivateKey keyToDecrypt) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, keyToDecrypt);
        return cipher.doFinal(input);
    }

    /**
     * decrypt
     * Decrypts a previously encrypted byte array. Default version
     * @param input encrypted byte array
     * @return the plaintext yielded from the byte array
     * @throws Exception
     */
    public byte[] decrypt(byte[] input) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(input);
    }

    /**
     * getKey
     * Returns the key.
     * @return the key that can encrypt
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }

    // RSA keys
    public PublicKey publicKey;
    private PrivateKey privateKey;
    private Cipher cipher;
}
