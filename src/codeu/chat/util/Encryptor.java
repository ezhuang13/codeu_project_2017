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
import java.security.spec.X509EncodedKeySpec;

public class Encryptor {
  private static final Logger.Log LOG = Logger.newLog(Encryptor.class);

  private static final String SYMMETRIC_ALGORITHM = "AES";
  private static final String ASYMMETRIC_ALGORITHM = "RSA";

  /**
   * makeSymmetricKey
   * Generates a SecretKey to use for encrypting any single exchange of data.
   * @return a valid SecretKey
   */
  public static SecretKey makeSymmetricKey() {
    KeyGenerator keyMaker;

    try {
      keyMaker = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
    } catch(NoSuchAlgorithmException nsae) {
      System.out.println("Incorrect symmetric algorithm provided.");
      System.exit(1);
      return null;
    }

    keyMaker.init(128);
    return keyMaker.generateKey();
  }

  /**
   * makeAsymmetricKeyPair
   * Generates a KeyPair to use for encrypting a SecretKey.
   * @return a KeyPair of a PublicKey and PrivateKey
   */
  public static KeyPair makeAsymmetricKeyPair() {
    KeyPairGenerator keyMaker;

    try {
      keyMaker = KeyPairGenerator.getInstance(ASYMMETRIC_ALGORITHM);
    } catch(NoSuchAlgorithmException nsae) {
      System.out.println("Incorrect asymmetric algorithm provided.");
      System.exit(1);
      return null;
    }

    keyMaker.initialize(2048);
    return keyMaker.generateKeyPair();
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
    catch (InvalidKeyException iee) {
      System.out.println("INVALID symmetric key.");
      iee.printStackTrace();
      LOG.info("INVALID symmetric key.");
      System.exit(1);
    }
    catch (IllegalBlockSizeException ibse) {
      System.out.print("ILLEGAL BLOCK SIZE");
      ibse.printStackTrace();
      LOG.info("ILLEGAL BLOCK SIZE.");
      System.exit(1);
    }
    catch (BadPaddingException bpe) {
      System.out.print("BAD PADDING");
      bpe.printStackTrace();
      LOG.info("BAD PADDING.");
      System.exit(1);
    }
    catch (GeneralSecurityException gse) {
      System.out.println("Incorrect symmetric algorithm provided.");
      System.exit(1);
      return null;
    }

    System.out.println(" on encryption.");
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
    catch (IllegalBlockSizeException ibse) {
      System.out.println("ILLEGAL BLOCK SIZE on wrapping.");
      ibse.printStackTrace();
      LOG.info("ILLEGAL BLOCK SIZE on wrapping.");
      System.exit(1);
    }
    catch (GeneralSecurityException gse) {
      System.out.println("INVALID symmetric key.");
      gse.printStackTrace();
      LOG.info("INVALID symmetric key.");
      System.exit(1);
    }

    System.out.println(" on decryption.");
    return null;
  }

  /**
   * wrap
   * "Wraps" a SecretKey with a PublicKey; basically, this encrypts the SecretKey
   * with the given PublicKey.
   * @param keyToWrap the SecretKey to be wrapped
   * @param publicKey the PublicKey to wrap with
   * @return a byte array of the encrypted SecretKey
   */
  public static byte[] wrap(SecretKey keyToWrap, PublicKey publicKey) {
    Cipher cipher;
    try {
      cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
    }
    catch(GeneralSecurityException gse) {return null;}

    try {
      cipher.init(Cipher.WRAP_MODE, publicKey);
    }
    catch (InvalidKeyException iee) {
      System.out.println("INVALID public key.");
      iee.printStackTrace();
      LOG.info("INVALID public key.");
      System.exit(1);
      return null;
    }

    try {
      return cipher.wrap(keyToWrap);
    }
    catch (InvalidKeyException iee) {
      System.out.println("INVALID symmetric key.");
      iee.printStackTrace();
      LOG.info("INVALID symmetric key.");
      System.exit(1);
    }
    catch (IllegalBlockSizeException ibse) {
      System.out.println("ILLEGAL BLOCK SIZE on wrapping.");
      ibse.printStackTrace();
      LOG.info("ILLEGAL BLOCK SIZE on wrapping.");
      System.exit(1);
    }

    return null;
  }

  /**
   * unwrap
   * "Unwraps" a wrapped SecretKey given the PrivateKey corresponding to the public key
   * with which the SecretKey was originally encrypted. Essentially, this decrypts the
   * SecretKey.
   * @param keyToUnwrap the byte array representing the encrypted SecretKey
   * @param privateKey the PrivateKey to unwrap with
   * @return the unwrapped and useable SecretKey
   */
  public static SecretKey unwrap(byte[] keyToUnwrap, PrivateKey privateKey) {
    Cipher cipher;
    try {
      cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
    }
    catch(GeneralSecurityException gse) {
      System.out.println("Incorrect asymmetric algorithm provided.");
      System.exit(1);
      return null;
    }
    try {
      cipher.init(Cipher.UNWRAP_MODE, privateKey);
    }
    catch (InvalidKeyException iee) {
      System.out.println("INVALID public key.");
      iee.printStackTrace();
      LOG.info("INVALID public key.");
      System.exit(1);
      return null;
    }

    try {
      return (SecretKey) cipher.unwrap(keyToUnwrap, SYMMETRIC_ALGORITHM, Cipher.SECRET_KEY);
    }
    catch (InvalidKeyException iee) {
      System.out.println("INVALID symmetric key.");
      iee.printStackTrace();
      LOG.info("INVALID symmetric key.");
      System.exit(1);
    }
    catch (NoSuchAlgorithmException nsae) {
      System.out.println("Incorrect symmetric algorithm provided.");
      System.exit(1);
      return null;
    }

    return null;
  }

  /**
   * Allows for the PublicKey to be easily exchanged.
   */
  public static final Serializer<PublicKey> SERIALIZER = new Serializer<PublicKey>() {

    @Override
    public void write(OutputStream out, PublicKey value) throws IOException {
      Serializers.STRING.write(out, ASYMMETRIC_ALGORITHM);
      Serializers.BYTES.write(out, value.getEncoded());
    }

    @Override
    public PublicKey read(InputStream in) throws IOException {
      try {
        KeyFactory keyFactory = KeyFactory.getInstance(Serializers.STRING.read(in));
        byte[] keyBytes = Serializers.BYTES.read(in);
        return keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));
      }
      catch (GeneralSecurityException gse) {
        System.out.println("INVALID key.");
        gse.printStackTrace();
        LOG.info("Key transferred incorrectly.");
        System.exit(1);
      }

      return null;
    }
  };
}