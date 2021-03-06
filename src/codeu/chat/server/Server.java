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


package codeu.chat.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import java.sql.SQLException;

import codeu.chat.common.Conversation;
import codeu.chat.common.ConversationSummary;
import codeu.chat.common.LinearUuidGenerator;
import codeu.chat.common.Message;
import codeu.chat.common.NetworkCode;
import codeu.chat.common.Relay;
import codeu.chat.common.User;
import codeu.chat.database.Database;
import codeu.chat.util.*;
import codeu.chat.util.connections.Connection;

import codeu.chat.server.authentication.Authentication;
import codeu.chat.server.storage.Storage;

public final class Server {

  private static final Logger.Log LOG = Logger.newLog(Server.class);

  private static final int RELAY_REFRESH_MS = 5000;  // 5 seconds

  private final Timeline timeline = new Timeline();

  private final Uuid id;
  private final byte[] secret;

  private final Model model = new Model();
  private final View view = new View(model);
  private final Controller controller;

  private final Relay relay;
  private Uuid lastSeen = Uuid.NULL;

  private final Database database;

  private final Authentication authentication;

  private final String ASYMMETRIC_ALGORITHM = "RSA";
  private PrivateKey privateKey;
  public PublicKey publicKey;

  private final Storage storage;

  public Server(final Uuid id, final byte[] secret, final Relay relay, final Database database, final KeyPair keyPair) {

    this.id = id;
    this.secret = Arrays.copyOf(secret, secret.length);

    // Set up the authentication manager.
    this.database = database;
    this.authentication = new Authentication(database);
    //Set up storage manager
    this.storage = new Storage(database);

    this.controller = new Controller(id, model, authentication, storage);
    this.relay = relay;

    this.privateKey = keyPair.getPrivate();
    this.publicKey = keyPair.getPublic();

    // Server initialization finished.
    LOG.info("Server initialized.");

    timeline.scheduleNow(new Runnable() {
      @Override
      public void run() {
        try {

          LOG.verbose("Reading update from relay...");

          for (final Relay.Bundle bundle : relay.read(id, secret, lastSeen, 32)) {
            onBundle(bundle);
            lastSeen = bundle.id();
          }

        } catch (Exception ex) {

          LOG.error(ex, "Failed to read update from relay.");

        }

        timeline.scheduleIn(RELAY_REFRESH_MS, this);
      }
    });
  }

  public void handleConnection(final Connection connection) {
    timeline.scheduleNow(new Runnable() {
      @Override
      public void run() {
        try {

          LOG.verbose("Handling connection...");

          final boolean success = onMessage(
              connection.in(),
              connection.out());

          LOG.verbose("Connection handled: %s", success ? "ACCEPTED" : "REJECTED");
        } catch (Exception ex) {

          LOG.error(ex, "Exception while handling connection.");

        }

        try {
          connection.close();
        } catch (Exception ex) {
          LOG.error(ex, "Exception while closing connection.");
        }
      }
    });
  }

  private boolean onMessage(InputStream in, OutputStream out) throws IOException {

    final int type = Serializers.INTEGER.read(in);

    if (type == NetworkCode.NEW_MESSAGE_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Uuid author = Uuid.SERIALIZER.read(in);
	    final Uuid token = Uuid.SERIALIZER.read(in);
      final Uuid conversation = Uuid.SERIALIZER.read(in);
      final String content = EncryptedSerializers.STRING.read(in, getServerPrivateKey());

      final Message message = controller.newMessage(author, token, conversation, content);

      Serializers.INTEGER.write(out, NetworkCode.NEW_MESSAGE_RESPONSE);
      EncryptedSerializers.nullable(Message.ENCRYPTED_SERIALIZER).write(out, message, clientKey);

      timeline.scheduleNow(createSendToRelayEvent(
          author,
          conversation,
          message.id));

    } else if (type == NetworkCode.NEW_USER_REQUEST) {

      final String username = EncryptedSerializers.STRING.read(in, getServerPrivateKey());
      final String password = EncryptedSerializers.STRING.read(in, getServerPrivateKey());

      final int result = controller.newUser(username, password);

      Serializers.INTEGER.write(out, NetworkCode.NEW_USER_RESPONSE);
      Serializers.INTEGER.write(out, result);

    } else if (type == NetworkCode.LOGIN_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final String username = EncryptedSerializers.STRING.read(in, getServerPrivateKey());
      final String password = EncryptedSerializers.STRING.read(in, getServerPrivateKey());

      final User user = controller.login(username, password);

      Serializers.INTEGER.write(out, NetworkCode.LOGIN_RESPONSE);
      EncryptedSerializers.nullable(User.ENCRYPTED_SERIALIZER).write(out, user, clientKey);
      if (user == null) {
        Serializers.nullable(Uuid.SERIALIZER).write(out, null);
      } else {
        Serializers.nullable(Uuid.SERIALIZER).write(out, user.token);
      }

    } else if (type == NetworkCode.NEW_CONVERSATION_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final String title = EncryptedSerializers.STRING.read(in, getServerPrivateKey());
      final Uuid owner = Uuid.SERIALIZER.read(in);
	    final Uuid token = Uuid.SERIALIZER.read(in);

      final Conversation conversation = controller.newConversation(title, owner, token);

      Serializers.INTEGER.write(out, NetworkCode.NEW_CONVERSATION_RESPONSE);
      EncryptedSerializers.nullable(Conversation.ENCRYPTED_SERIALIZER).write(out, conversation, clientKey);

    } else if (type == NetworkCode.GET_USERS_BY_ID_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<User> users = view.getUsers(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_USERS_BY_ID_RESPONSE);
      EncryptedSerializers.collection(User.ENCRYPTED_SERIALIZER).write(out, users, clientKey);

    } else if (type == NetworkCode.GET_ALL_CONVERSATIONS_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Collection<ConversationSummary> conversations = view.getAllConversations();

      Serializers.INTEGER.write(out, NetworkCode.GET_ALL_CONVERSATIONS_RESPONSE);
      EncryptedSerializers.collection(ConversationSummary.ENCRYPTED_SERIALIZER).write(out, conversations, clientKey);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_ID_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<Conversation> conversations = view.getConversations(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_ID_RESPONSE);
      EncryptedSerializers.collection(Conversation.ENCRYPTED_SERIALIZER).write(out, conversations, clientKey);

    } else if (type == NetworkCode.GET_MESSAGES_BY_ID_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<Message> messages = view.getMessages(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_ID_RESPONSE);
      EncryptedSerializers.collection(Message.ENCRYPTED_SERIALIZER).write(out, messages, clientKey);

    } else if (type == NetworkCode.GET_USER_GENERATION_REQUEST) {

      Serializers.INTEGER.write(out, NetworkCode.GET_USER_GENERATION_RESPONSE);
      Uuid.SERIALIZER.write(out, view.getUserGeneration());

    } else if (type == NetworkCode.GET_USERS_EXCLUDING_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Collection<Uuid> ids = Serializers.collection(Uuid.SERIALIZER).read(in);

      final Collection<User> users = view.getUsersExcluding(ids);

      Serializers.INTEGER.write(out, NetworkCode.GET_USERS_EXCLUDING_RESPONSE);
      EncryptedSerializers.collection(User.ENCRYPTED_SERIALIZER).write(out, users, clientKey);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_TIME_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Time startTime = Time.SERIALIZER.read(in);
      final Time endTime = Time.SERIALIZER.read(in);

      final Collection<Conversation> conversations = view.getConversations(startTime, endTime);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_TIME_RESPONSE);
      EncryptedSerializers.collection(Conversation.ENCRYPTED_SERIALIZER).write(out, conversations, clientKey);

    } else if (type == NetworkCode.GET_CONVERSATIONS_BY_TITLE_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final String filter = Serializers.STRING.read(in);

      final Collection<Conversation> conversations = view.getConversations(filter);

      Serializers.INTEGER.write(out, NetworkCode.GET_CONVERSATIONS_BY_TITLE_RESPONSE);
      EncryptedSerializers.collection(Conversation.ENCRYPTED_SERIALIZER).write(out, conversations, clientKey);

    } else if (type == NetworkCode.GET_MESSAGES_BY_TIME_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Uuid conversation = Uuid.SERIALIZER.read(in);
      final Time startTime = Time.SERIALIZER.read(in);
      final Time endTime = Time.SERIALIZER.read(in);

      final Collection<Message> messages = view.getMessages(conversation, startTime, endTime);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_TIME_RESPONSE);
      EncryptedSerializers.collection(Message.ENCRYPTED_SERIALIZER).write(out, messages, clientKey);

    } else if (type == NetworkCode.GET_MESSAGES_BY_RANGE_REQUEST) {

      final PublicKey clientKey = Encryptor.SERIALIZER.read(in);
      final Uuid rootMessage = Uuid.SERIALIZER.read(in);
      final int range = Serializers.INTEGER.read(in);

      final Collection<Message> messages = view.getMessages(rootMessage, range);

      Serializers.INTEGER.write(out, NetworkCode.GET_MESSAGES_BY_RANGE_RESPONSE);
      EncryptedSerializers.collection(Message.ENCRYPTED_SERIALIZER).write(out, messages, clientKey);

    } else if (type == NetworkCode.SERVER_PUBLIC_KEY_REQUEST) {
      
      Serializers.INTEGER.write(out, NetworkCode.SERVER_PUBLIC_KEY_RESPONSE);
      Encryptor.SERIALIZER.write(out, getServerPublicKey());

    } else {

      // In the case that the message was not handled make a dummy message with
      // the type "NO_MESSAGE" so that the client still gets something.

      Serializers.INTEGER.write(out, NetworkCode.NO_MESSAGE);

    }

    return true;
  }

  private void onBundle(Relay.Bundle bundle) {

    final Relay.Bundle.Component relayUser = bundle.user();
    final Relay.Bundle.Component relayConversation = bundle.conversation();
    final Relay.Bundle.Component relayMessage = bundle.user();

    User user = model.userById().first(relayUser.id());

    if (user == null) {
      // Invalid user.
      LOG.error("Invalid user received from relay.");
      return;
    }

    Conversation conversation = model.conversationById().first(relayConversation.id());

    if (conversation == null) {

      // As the relay does not tell us who made the conversation - the first person who
      // has a message in the conversation will get ownership over this server's copy
      // of the conversation.
      conversation = controller.newConversation(relayConversation.id(),
                                                relayConversation.text(),
                                                user.id,
                                                relayConversation.time());
    }

    Message message = model.messageById().first(relayMessage.id());

    if (message == null) {
      message = controller.newMessage(relayMessage.id(),
                                      user.id,
                                      conversation.id,
                                      relayMessage.text(),
                                      relayMessage.time());
    }
  }

  private Runnable createSendToRelayEvent(final Uuid userId,
                                          final Uuid conversationId,
                                          final Uuid messageId) {
    return new Runnable() {
      @Override
      public void run() {
        final User user = view.findUser(userId);
        final Conversation conversation = view.findConversation(conversationId);
        final Message message = view.findMessage(messageId);
        relay.write(id,
                    secret,
                    relay.pack(user.id, user.name, user.creation),
                    relay.pack(conversation.id, conversation.title, conversation.creation),
                    relay.pack(message.id, message.content, message.creation));
      }
    };
  }

  public PublicKey getServerPublicKey() {return publicKey;}
  private PrivateKey getServerPrivateKey() {return privateKey;}
}
