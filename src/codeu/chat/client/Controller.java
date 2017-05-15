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
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.User;
import codeu.chat.util.Encryptor;
import codeu.chat.util.Logger;
import codeu.chat.util.Serializers;
import codeu.chat.util.Uuid;
import codeu.chat.util.connections.Connection;
import codeu.chat.util.connections.ConnectionSource;

import codeu.chat.authentication.AuthenticationCode;

public class Controller implements BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final ConnectionSource source;

  public Controller(ConnectionSource source) {
    this.source = source;
  }

  @Override
  public Message newMessage(Uuid author, Uuid token, Uuid conversation, String body) {

    Message response = null;

    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.NEW_MESSAGE_REQUEST);
      Uuid.SERIALIZER.write(connection.out(), author);
      Uuid.SERIALIZER.write(connection.out(), token);
      Uuid.SERIALIZER.write(connection.out(), conversation);
      Serializers.writeStringEnc(connection.out(), body, getServerPublicKey());

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_MESSAGE_RESPONSE) {
        response = Serializers.nullable(Message.SERIALIZER).read(connection.in());
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
      Serializers.writeStringEnc(connection.out(), username, getServerPublicKey());
      Serializers.writeStringEnc(connection.out(), password, getServerPublicKey()
      );
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
      Serializers.writeStringEnc(connection.out(), username, getServerPublicKey());
      Serializers.writeStringEnc(connection.out(), password, getServerPublicKey());
      LOG.info("login: Request completed.");

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.LOGIN_RESPONSE) {
        user = Serializers.nullable(User.SERIALIZER).read(connection.in());
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
      Serializers.writeStringEnc(connection.out(), title, getServerPublicKey());
      Uuid.SERIALIZER.write(connection.out(), owner);
      Uuid.SERIALIZER.write(connection.out(), token);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.NEW_CONVERSATION_RESPONSE) {
        response = Serializers.nullable(Conversation.SERIALIZER).read(connection.in());
      } else {
        LOG.error("Response from server failed.");
      }
    } catch (Exception ex) {
      System.out.println("ERROR: Exception during call on server. Check log for details.");
      LOG.error(ex, "Exception during call on server.");
    }

    return response;
  }

  public PublicKey getServerPublicKey() {
    try (final Connection connection = source.connect()) {

      Serializers.INTEGER.write(connection.out(), NetworkCode.GET_SERVER_PUBLIC_KEY);

      if (Serializers.INTEGER.read(connection.in()) == NetworkCode.GET_SERVER_PUBLIC_KEY) {
        KeyFactory keyFactory = KeyFactory.getInstance(Serializers.STRING.read(connection.in()));
        return keyFactory.generatePublic(new X509EncodedKeySpec(Serializers.BYTES.read(connection.in())));
      } else {
        LOG.error("Response from client setup failed.");
      }

    } catch (Exception ex) {
      System.out.println("ERROR: Exception during client setup. Check log for details.");
      LOG.error(ex, "Exception during client setup.");
    }

    return null;
  }
}
