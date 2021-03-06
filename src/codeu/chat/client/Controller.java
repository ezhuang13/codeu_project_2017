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

package codeu.chat.client;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.Thread;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.*;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

import codeu.chat.authentication.AuthenticationCode;

public class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public final PublicKey publicKey;
  private final PrivateKey privateKey;

  private PublicKey serverPublicKey;

  public Controller(ConnectionSource source, KeyPair keyPair, PublicKey serverKey) {
    this.source = source;
    this.publicKey = keyPair.getPublic();
    this.privateKey = keyPair.getPrivate();
    this.serverPublicKey = serverKey;
  }

  @Override
  public Message newMessage(Uuid author, Uuid token, Uuid conversation, String body) {

    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Encryptor.SERIALIZER.write(connection.out(), publicKey);
      Uuid.SERIALIZER.write(connection.out(), author);
      Uuid.SERIALIZER.write(connection.out(), token);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      EncryptedSerializers.STRING.write(connection.out(), body, serverPublicKey);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = EncryptedSerializers.nullable(Message.ENCRYPTED_SERIALIZER).read(connection.in(), privateKey);
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public int newUser(String username, String password) {

    int response = AuthenticationCode.UNKNOWN;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_USER_REQUEST);
      EncryptedSerializers.STRING.write(connection.out(), username, serverPublicKey);
      EncryptedSerializers.STRING.write(connection.out(), password, serverPublicKey);
      LOG.info("newUser: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_USER_RESPONSE) {
        response = Serializers.INTEGER.read(connection.in());
        LOG.info("newUser: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  @Override
  public User login(String username, String password) {

    User user = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.LOGIN_REQUEST);
      Encryptor.SERIALIZER.write(connection.out(), publicKey);
      EncryptedSerializers.STRING.write(connection.out(), username, serverPublicKey);
      EncryptedSerializers.STRING.write(connection.out(), password, serverPublicKey);
      LOG.info("login: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.LOGIN_RESPONSE) {
        user = EncryptedSerializers.nullable(User.ENCRYPTED_SERIALIZER).read(connection.in(), privateKey);
        Uuid token = Serializers.nullable(Uuid.SERIALIZER).read(connection.in());
        user.token = token;
        LOG.info("login: Response completed.");
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return user;

  }

  @Override
  public Conversation newConversation(String title, Uuid owner, Uuid token)  {

    Conversation response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_CONVERSATION_REQUEST);
      Encryptor.SERIALIZER.write(connection.out(), publicKey);
      EncryptedSerializers.STRING.write(connection.out(), title, serverPublicKey);
      Uuid.SERIALIZER.write(connection.out(), owner);
      Uuid.SERIALIZER.write(connection.out(), token);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = EncryptedSerializers.nullable(Conversation.ENCRYPTED_SERIALIZER).read(connection.in(), privateKey);
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }
}
