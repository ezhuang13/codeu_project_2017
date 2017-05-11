package codeu.chat.server.database;

import codeu.chat.database.Database;
import codeu.chat.database.Schema;

/**
* @description Schema for conversations
*/
public class ConversationSchema extends Schema{

	public ConversationSchema(){
		addField("id", "INT8");
		addField("title", "TEXT(255)");
		addField("owner", "TEXT(255)");
	}
}