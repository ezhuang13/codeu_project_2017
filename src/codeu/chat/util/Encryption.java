/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description Outlines the interface for the Compression engine
*/

package codeu.chat.util;

public interface Encryption<T>{

    /**
     * @param data The byte array to encrypt
     * @return An encrypted byte array
     */
	byte[] encrypt(T data);

    /**
     * @param data The byte array to decrypt
     * @return The decrypted byte array
     */
	T decrypt(byte[] data);

}