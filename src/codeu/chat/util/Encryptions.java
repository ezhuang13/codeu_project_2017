/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description A specific compression implementation for byte[]
*/ 

package codeu.chat.util;

import javax.crypto.*;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.PublicKey;

// Specialized only for byte sequences.
public final class Encryptions {
	private static final String SYMMETRIC_ALGORITHM = "AES";

	public static byte[] wrap(SecretKey keyToWrap, PublicKey publicKey) {
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			cipher.init(Cipher.WRAP_MODE, publicKey);
			return cipher.wrap(keyToWrap);
		}
		catch (InvalidKeyException iee) {System.out.print("INVALID KEY");}
		catch (IllegalBlockSizeException ibse) {System.out.print("ILLEGAL BLOCK SIZE");}
		catch (GeneralSecurityException gse) {}

		System.out.println(" on wrapping.");
		return null;
	}

	public static SecretKey unwrap(byte[] keyToUnwrap, PrivateKey privateKey) {
		try {
			Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
			cipher.init(Cipher.UNWRAP_MODE, privateKey);
			return (SecretKey) cipher.unwrap(keyToUnwrap, SYMMETRIC_ALGORITHM, Cipher.SECRET_KEY);
		}
		catch (InvalidKeyException iee) {System.out.print("INVALID KEY");}
		catch (NoSuchPaddingException nspe) {System.out.print("NO SUCH PADDING");}
		catch (GeneralSecurityException gse) {}

		System.out.println(" on unwrapping.");
		return null;
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