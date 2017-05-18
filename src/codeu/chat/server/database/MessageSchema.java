package codeu.chat.server.database;

import codeu.chat.database.Database;
import codeu.chat.database.Schema;

/**
* @description Schema for messages
*/
public class MessageSchema extends Schema{

	//Declare field names
	public static final String UNIQUE_ID = "conversation_id";
	public static final String TIMESTAMP = "time_created";
	public static final String TEXT = "content";
	//Declare field properties
	private static final String PROP_TEXT = "TEXT(255)";
	private static final String PROP_INT = "INT";

	public MessageSchema(){
		addField(UNIQUE_ID, PROP_INT);
		addField(TIMESTAMP, PROP_TEXT);
		addField(TEXT, PROP_TEXT);
	}
}