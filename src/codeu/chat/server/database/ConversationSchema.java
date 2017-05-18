package codeu.chat.server.database;

import codeu.chat.database.Database;
import codeu.chat.database.Schema;

/**
* @description Schema for conversations
*/
public class ConversationSchema extends Schema{

	//Declare field names
	public static final String UNIQUE_ID = "username";
	public static final String TIMESTAMP = "time_created";
	public static final String TEXT = "title";
	//Declare field properties
	private static final String PROP_TEXT = "TEXT(255)";

	public ConversationSchema(){
		addField(UNIQUE_ID, PROP_TEXT);
		addField(TIMESTAMP, PROP_TEXT);
		addField(TEXT, PROP_TEXT);
	}
}