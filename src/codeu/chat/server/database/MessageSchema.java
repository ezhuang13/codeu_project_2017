package codeu.chat.server.database;

import codeu.chat.database.Database;
import codeu.chat.database.Schema;

/**
* @description Schema for messages
*/
public class MessageSchema extends Schema{

	public MessageSchema(){
		addField("user_id", "INT");
		addField("conversation_id", "INT");
		addField("time_created", "TEXT(255)");
		addField("content", "TEXT(255)");
	}
}