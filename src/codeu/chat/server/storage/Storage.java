package codeu.chat.server.storage;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Logger;

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
	public int addConversation(int uid, String time, String title){
		try{
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("user_id", Integer.toString(uid));
			fields.put("time_created", time);
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
	public int addMessage(int cid, String time, String content){
		try{
			Map<String, String> fields = new HashMap<String, String>();
			fields.put("conversation_id", Integer.toString(cid));
			fields.put("time_created", time);
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
	//Transfers all conversations and messages to the server
	//Arraylist of convos
	public Arraylist<Conversation> loadConversations(String username){

	}

	public Arraylist<Message> loadMessages(){

	}
	*/
}