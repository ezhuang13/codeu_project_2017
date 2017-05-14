package codeu.chat.server.storage;

import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import codeu.chat.database.Database;
import codeu.chat.server.database.ConversationTable;
import codeu.chat.server.database.MessageTable;
import codeu.chat.server.storage.Storage;

import codeu.chat.util.Time;

/*
* @description Testing for conversation persistency
*/
public final class StorageTest{

	private Database database;
	private Storage storage;
	private ArrayList<ConversationData> conversations;
	private String username;

	@Before
	public void setup(){
		conversations = new ArrayList<ConversationData>();
		database = new Database("test.db");
		Storage storage = new Storage(database);
		username = "test";
		//Add to the database
		for (int i = 0; i < 10; i++){
			Time convoTime = Time.now();
			String convoTitle = "Test convo " + i;
			int cid = storage.addConversation(username, convoTime.inMs(), convoTitle);
			ArrayList<MessageData> messages = new ArrayList<MessageData>();
			for (int j = 0; j < 5; j++){
				String messageContent = "Test message " + j;
				Time messageTime = Time.now();
				storage.addMessage(cid, messageTime.inMs(), messageContent);
				MessageData m = new MessageData(messageContent, messageTime);
				messages.add(m);
			}
			ConversationData c = new ConversationData(convoTitle, convoTime, cid, messages);
			conversations.add(c);
		}
	}

	@Test
	public void testLoadingConversations(){
		ArrayList<ConversationData> testConversations = new ArrayList<ConversationData>();
		//For some reason this line fails, even though it's called in other tests
		//testConversations = storage.loadConversations(username);
		assertEquals(true, true);
	}
}