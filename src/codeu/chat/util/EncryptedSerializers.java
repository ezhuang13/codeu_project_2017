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

package codeu.chat.util;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Collection;

public final class EncryptedSerializers {

  public static final EncryptedSerializer<byte[]> BYTES = new EncryptedSerializer<byte[]>() {

    @Override
    public void write(OutputStream out, byte[] value, PublicKey publicKey) throws IOException {
      SecretKey key = Encryptor.makeSymmetricKey();
      Serializers.BYTES.write(out, Encryptor.wrap(key, publicKey));
      Serializers.BYTES.write(out, Encryptor.encrypt(value, key));
    }

    @Override
    public byte[] read(InputStream input, PrivateKey privateKey) throws IOException {
      SecretKey key = Encryptor.unwrap(Serializers.BYTES.read(input), privateKey);
      return Encryptor.decrypt(Serializers.BYTES.read(input), key);
    }
  };

  public static final EncryptedSerializer<String> STRING = new EncryptedSerializer<String>() {
    private static final String _ENCODING = "ISO-8859-1";

    @Override
    public void write(OutputStream out, String value, PublicKey publicKey) throws IOException {
      BYTES.write(out, value.getBytes(_ENCODING), publicKey);
    }

    @Override
    public String read(InputStream input, PrivateKey privateKey) throws IOException {
      return new String(BYTES.read(input, privateKey), _ENCODING);
    }
  };

  public static <T> EncryptedSerializer<Collection<T>> collection(final EncryptedSerializer<T> serializer) {

    return new EncryptedSerializer<Collection<T>>() {

      @Override
      public void write(OutputStream out, Collection<T> value, PublicKey key) throws IOException {
        Serializers.INTEGER.write(out, value.size());
        for (final T x : value) {
          serializer.write(out, x, key);
        }
      }

      @Override
      public Collection<T> read(InputStream in, PrivateKey key) throws IOException {
        final int size = Serializers.INTEGER.read(in);
        Collection<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
          list.add(serializer.read(in, key));
        }
        return list;
      }
    };
  }

  public static <T> EncryptedSerializer<T> nullable(final EncryptedSerializer<T> serializer) {

    final int NO_VALUE = 0x00;
    final int YES_VALUE = 0xFF;

    return new EncryptedSerializer<T>() {

      @Override
      public void write(OutputStream out, T value, PublicKey key) throws IOException {
        if (value == null) {
          out.write(NO_VALUE);
        } else {
          out.write(YES_VALUE);
          serializer.write(out, value, key);
        }
      }

      @Override
      public T read(InputStream in, PrivateKey key) throws IOException {
        return in.read() == NO_VALUE ? null : serializer.read(in, key);
      }
    };
  }
}

