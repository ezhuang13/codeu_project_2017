package codeu.chat.server.storage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

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

/*
* @description Manager for conversation and message storage
*/
public final class Storage{

	private static final Logger.Log LOG = Logger.newLog(Storage.class);

	private final Database database;

	private ConversationTable conversationTable;
	private MessageTable messageTable;

	/*
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

	/*
	* @brief Adds a conversation to the database
	* @param uid The owner of the conversation
	* @param time The time of creation
	* @param title The title of the conversation
	*/
	public int addConversation(String uid, long time, String title){
		try{
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("username", uid);
			fields.put("time_created", Long.toString(time));
			fields.put("title", title);
			conversationTable.create(fields);
			return 0;
		}
		catch(SQLException e){
			LOG.error(e, "Failed to add conversation");
			return 1;
		}
	}

	/*
	* @brief Adds a message to the database
	* @param cid The conversation the message belongs in
	* @param time The time of creation
	* @param content The contents of the message
	*/
	public int addMessage(int cid, long time, String content){
		try{
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("conversation_id", Integer.toString(cid));
			fields.put("time_created", Long.toString(time));
			fields.put("content", content);
			messageTable.create(fields);
			return 0;
		}
		catch(SQLException e){
			LOG.error(e, "Failed to add message");
			return 1;
		}
	}

	/*
	* @brief Loads all messages associated with a user into the database
	* @param cid The conversation the message belongs in
	* @param time The time of creation
	* @param content The contents of the message
	*/
	public ArrayList<ConversationData> loadConversations(String username){
		ArrayList<ConversationData> conversationData = new ArrayList<ConversationData>();

		try{
			//Compiles all conversations that match the given username
			List<DBObject<ConversationSchema>> conversationList = new ArrayList<DBObject<ConversationSchema>>();
			conversationList = conversationTable.find("username", username);

			//Iterates through conversation DBObjects and extracts data
			for (DBObject<ConversationSchema> c: conversationList){
				String title = c.get("title");
				Time time = Time.fromMs(Long.parseLong(c.get("time_created")));
				ConversationData convo = new ConversationData(title, time);
				conversationData.add(convo);
			}
		}
		catch(SQLException e){
			LOG.error(e, "Failed to load past conversations of user");
		}
		return conversationData;
	}

	public ArrayList<MessageData> loadMessages(){
		ArrayList<MessageData> messageData = new ArrayList<MessageData>();
		return messageData;
	}

}

/*
* @description metadata for a conversation
*/
class ConversationData{
	public String title;
	public Time creation;

	public ConversationData(String title, Time creation){
		this.title = title;
		this.creation = creation;
	}
}

/*
* @description metadata for a message
*/
class MessageData{

}