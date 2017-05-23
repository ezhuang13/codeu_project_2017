// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import static org.junit.Assert.*;

import codeu.chat.common.Secret;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.util.Encryptor;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.KeyPair;

public final class EncryptorTest {
  private static final String _ENCODING = "ISO-8859-1";
  private static final String SYMMETRIC_ALGORITHM = "AES";
  private static final String ASYMMETRIC_ALGORITHM = "RSA";

  @Test
  public void testEncrypt() throws GeneralSecurityException, UnsupportedEncodingException {
    String plaintext = "We are gonna ace this project!";
    SecretKey key = Encryptor.makeSymmetricKey();

    String text1 = new String(Encryptor.encrypt(plaintext.getBytes(_ENCODING), key), _ENCODING);

    Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
    cipher.init(Cipher.ENCRYPT_MODE, key);
    String text2 = new String(cipher.doFinal(plaintext.getBytes(_ENCODING)), _ENCODING);

    assertTrue(text1 == text2);
  }

  @Test
  public void testDecrypt() throws GeneralSecurityException, UnsupportedEncodingException {
    String plaintext = "We are gonna ace this project!";
    SecretKey key = Encryptor.makeSymmetricKey();
    byte[] ciphertext = Encryptor.encrypt(plaintext.getBytes(_ENCODING), key);

    String text1 = new String(Encryptor.decrypt(ciphertext, key), _ENCODING);

    Cipher cipher = Cipher.getInstance(SYMMETRIC_ALGORITHM);
    cipher.init(Cipher.DECRYPT_MODE, key);
    String text2 = new String(cipher.doFinal(ciphertext), _ENCODING);

    assertTrue(plaintext == text1 && text1 == text2);
  }

  @Test
  public void testWrap() throws GeneralSecurityException {
    SecretKey key = Encryptor.makeSymmetricKey();
    KeyPair keyPair = Encryptor.makeAsymmetricKeyPair();
    byte[] wrapped1 = Encryptor.wrap(key, keyPair.getPublic());

    Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
    cipher.init(Cipher.WRAP_MODE, keyPair.getPublic());
    byte[] wrapped2 = cipher.wrap(key.getEncoded());

    assertTrue(wrapped1 == wrapped2);
  }

  @Test
  public void testUnwrap() throws GeneralSecurityException {
    SecretKey key = Encryptor.makeSymmetricKey();
    KeyPair keyPair = Encryptor.makeAsymmetricKeyPair();
    byte[] wrapped = Encryptor.wrap(key, keyPair.getPublic());

    SecretKey key1 = Encryptor.unwrap(wrapped, keyPair.getPrivate());

    Cipher cipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
    cipher.init(Cipher.UNWRAP_MODE, keyPair.getPublic());
    SecretKey key2 = (SecretKey) cipher.unwrap(wrapped, SYMMETRIC_ALGORITHM, Cipher.SECRET_KEY);

    assertTrue(key1.equals(key2));
  }
}
