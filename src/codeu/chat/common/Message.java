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

import codeu.chat.util.Serializer;
import codeu.chat.util.Serializers;

import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.compression.CompressionEngine;


public final class Message {

  public static final Serializer<Message> SERIALIZER = new Serializer<Message>() {

    /**
    * @description Now sends to ostream a message represented as a compressed byte[]
    */
    @Override
    public void write(OutputStream out, Message value) throws IOException {
      
      byte[] message = CompressionEngine.compressMessage(value);
      Serializers.BYTES.write(out, message);

    }

    /**
    * @description Deserializes compressed byte[], and then decompressed to original message
    */
    @Override
    public Message read(InputStream in) throws IOException {

      byte[] message = Serializers.BYTES.read(in);
      return CompressionEngine.decompressMessage(message);

    }
  };

  public final Uuid id;
  public final Uuid previous;
  public final Time creation;
  public final Uuid author;
  public final String content;
  public Uuid next;

  public Message(Uuid id, Uuid next, Uuid previous, Time creation, Uuid author, String content) {

    this.id = id;
    this.next = next;
    this.previous = previous;
    this.creation = creation;
    this.author = author;
    this.content = content;

  }

  /**
  * @param a, b The messages that are compared to each other
  * @return true if the fields of the messages are identical, otherwise false
  */
  public static boolean equals(Message a, Message b){
    //Only check the next field of Uuids, because this performs a deep check and
    //we assume cur and prev are linked
    return a.content.equals(b.content) && a.creation.compareTo(b.creation) == 0
    && Uuid.equals(a.author, b.author) && Uuid.equals(a.next, b.next);
  }


  /**
  * @brief Formerly the overriden Serializer write
  */
  public static void toStream(OutputStream out, Message value) throws IOException {

    Uuid.SERIALIZER.write(out, value.id);
    Uuid.SERIALIZER.write(out, value.next);
    Uuid.SERIALIZER.write(out, value.previous);
    Time.SERIALIZER.write(out, value.creation);
    Uuid.SERIALIZER.write(out, value.author);
    Serializers.STRING.write(out, value.content);

  }

  /**
  * @brief Formerly the overriden Serializer read
  */
  public static Message fromStream(InputStream in) throws IOException {

    return new Message(
        Uuid.SERIALIZER.read(in),
        Uuid.SERIALIZER.read(in),
        Uuid.SERIALIZER.read(in),
        Time.SERIALIZER.read(in),
        Uuid.SERIALIZER.read(in),
        Serializers.STRING.read(in)
    );
  }
}
