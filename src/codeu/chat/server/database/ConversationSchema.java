package codeu.chat.server.database;

import codeu.chat.database.Database;
import codeu.chat.database.Schema;

/**
* @description Schema for conversations
*/
public class ConversationSchema extends Schema{

	public ConversationSchema(){
		addField("username", "TEXT(255)");
		addField("time_created", "TEXT(255)");
		addField("title", "TEXT(255)");
	}
}