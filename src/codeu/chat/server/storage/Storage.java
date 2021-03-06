package codeu.chat.server.storage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;

import codeu.chat.database.Database;
import codeu.chat.database.DBObject;
import codeu.chat.server.database.ConversationTable;
import codeu.chat.server.database.ConversationSchema;
import codeu.chat.server.database.MessageTable;
import codeu.chat.server.database.MessageSchema;

import codeu.chat.server.storage.ConversationData;
import codeu.chat.server.storage.MessageData;

/**
* @description Manager for conversation and message storage
*/
public final class Storage{

	private static final Logger.Log LOG = Logger.newLog(Storage.class);

	private final Database database;

	public ConversationTable conversationTable;
	public MessageTable messageTable;

	/**
	* @brief Creates the storage manager
	* @param database The server database
	*/
	public Storage(Database database){
		this.database = database;

		try {
			conversationTable = new ConversationTable(database);
			messageTable = new MessageTable(database);
		}
		catch(SQLException e){
			LOG.error(e, "Failed to initialize conversation and message tables");
			System.exit(1);
		}
	}

	/**
	* @brief Adds a conversation to the database
	* @param username The username of the owner of the conversation
	* @param time The time of creation in ms
	* @param title The title of the conversation
	* @return The id of the added conversation
	*/
	public int addConversation(String username, long time, String title){
		try{
			Map<String, String> fields = new HashMap<String, String>();
			fields.put(ConversationSchema.UNIQUE_ID, username);
			fields.put(ConversationSchema.TIMESTAMP, Long.toString(time));
			fields.put(ConversationSchema.TEXT, title);
			//return the id field of the added conversation
			LOG.info("Conversation added");
			return conversationTable.create(fields);
		}
		catch(SQLException e){
			LOG.error(e, "Failed to add conversation");
			return -1;
		}
	}

	/**
	* @brief Adds a message to the database
	* @param cid The conversation the message belongs in
	* @param time The time of creation in ms
	* @param content The contents of the message
	*/
	public void addMessage(int cid, long time, String content){
		try{
			Map<String, String> fields = new HashMap<String, String>();
			fields.put(MessageSchema.UNIQUE_ID, Integer.toString(cid));
			fields.put(MessageSchema.TIMESTAMP, Long.toString(time));
			fields.put(MessageSchema.TEXT, content);
			messageTable.create(fields);
			LOG.info("Messsage added");
		}
		catch(SQLException e){
			LOG.error(e, "Failed to add message");
		}
	}

	/**
	* @brief Loads all conversations stored in the database that are associated with the user
	* @param username The username whose conversations will be returned
	* @return An ArrayList of conversation data, where each conversation also has its
	* message data. Both are sorted in ascending order by time of creation
	*/
	public ArrayList<ConversationData> loadConversations(String username){
		ArrayList<ConversationData> conversationData = new ArrayList<ConversationData>();

		try{
			//Queries all conversations that match the given username
			List<DBObject<ConversationSchema>> conversationList =
			conversationTable.find(ConversationSchema.UNIQUE_ID, username);

			//Iterates through conversation DBObjects and extracts data
			for (DBObject<ConversationSchema> c: conversationList){
				String title = c.get(ConversationSchema.TEXT);
				Time time = Time.fromMs(Long.parseLong(c.get(ConversationSchema.TIMESTAMP)));
				String cid = c.get("_id");
				ArrayList<MessageData> messages = loadMessages(cid);
				ConversationData convo = new ConversationData(title, time, Integer.parseInt(cid), messages);
				conversationData.add(convo);
			}
		}
		catch(SQLException e){
			LOG.error(e, "Failed to load past conversations of user");
		}
		
		//Sort the conversations by Time in ascending order
		Collections.sort(conversationData);
		return conversationData;
	}

	/**
	* @brief Loads all messages stored in the database that are associated with the conversation
	* @param cid The conversation whose messages will be returned
	* @return An ArrayList of message data associated with the given conversation,
	* sorted in ascending order by time of creation
	*/
	private ArrayList<MessageData> loadMessages(String cid){
		ArrayList<MessageData> messageData = new ArrayList<MessageData>();

		try{
			//Queries all messages that match the given conversation id
			List<DBObject<MessageSchema>> messageList =
			messageTable.find(MessageSchema.UNIQUE_ID, cid);

			//Iterates through message DBObjects and extracts data
			for (DBObject<MessageSchema> m: messageList){
				String content = m.get(MessageSchema.TEXT);
				Time time = Time.fromMs(Long.parseLong(m.get(MessageSchema.TIMESTAMP)));
				MessageData message = new MessageData(content, time);
				messageData.add(message);
			}
		}
		catch(SQLException e){
			LOG.error(e, "Failed to lost past message of user");
		}

		//Sort the messages by Time in ascending order
		Collections.sort(messageData);
		return messageData;
	}
}