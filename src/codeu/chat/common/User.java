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

package codeu.chat.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;

import codeu.chat.util.*;

public final class User {

  public static final Serializer<User> SERIALIZER = new Serializer<User>() {

    @Override
    public void write(OutputStream out, User value) throws IOException {

      Uuid.SERIALIZER.write(out, value.id);
      Serializers.STRING.write(out, value.name);
      Time.SERIALIZER.write(out, value.creation);

    }

    @Override
    public User read(InputStream in) throws IOException {

      return new User(
          Uuid.SERIALIZER.read(in),
          Serializers.STRING.read(in),
          Time.SERIALIZER.read(in)
      );
    }
  };

  public static final EncryptedSerializer<User> ENCRYPTED_SERIALIZER = new EncryptedSerializer<User>() {

    @Override
    public void write(OutputStream out, User value, PublicKey key) throws IOException {

      Uuid.SERIALIZER.write(out, value.id);
      EncryptedSerializers.STRING.write(out, value.name, key);
      Time.SERIALIZER.write(out, value.creation);

    }

    @Override
    public User read(InputStream in, PrivateKey key) throws IOException {

      return new User(
          Uuid.SERIALIZER.read(in),
          EncryptedSerializers.STRING.read(in, key),
          Time.SERIALIZER.read(in)
      );
    }
  };

  public final Uuid id;
  public final String name;
  public final Time creation;
  public Uuid token;

  public User(Uuid id, String name, Time creation) {

    this.id = id;
    this.name = name;
    this.creation = creation;
    this.token = null; // This will be null unless explicitly set.
    // Is not transferred in the serializer for security.

  }

}