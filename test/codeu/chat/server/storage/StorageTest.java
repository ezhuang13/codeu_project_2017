package codeu.chat.server.storage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.After;

import codeu.chat.database.Database;
import codeu.chat.database.DBObject;
import codeu.chat.server.database.ConversationSchema;

import codeu.chat.util.Time;

/*
* @description Testing for data persistence
*/
public final class StorageTest{

	private Database database;
	private Storage storage;
	private ArrayList<ConversationData> conversations;
	private String username, username2;

	//Adds multiple conversations and messages to storage and conversations
	private void addToStorage(int numConvos, int numMessages, String name){
		for (int i = 0; i < numConvos; i++){
			Time convoTime = Time.now();
			String convoTitle = "Test convo " + i;
			int cid = storage.addConversation(name, convoTime.inMs(), convoTitle);
			ArrayList<MessageData> messages = new ArrayList<MessageData>();
			for (int j = 0; j < numMessages; j++){
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

	//Resets fields for next tests
	private void reset() throws SQLException{
		storage.messageTable.destroy();
		storage.conversationTable.destroy();
		storage = new Storage(database);
		conversations.clear();
	}

	@Before
	public void setup(){
		database = new Database("test.db");
		username = "test";
		username2 = "test2";
		conversations = new ArrayList<ConversationData>();
		storage = new Storage(database);
	}

	@Test
	public void testAddingData() throws SQLException{
		addToStorage(15, 3, username);
		addToStorage(12, 3, username2);
		List<DBObject<ConversationSchema>> addedConvos = storage.conversationTable.find(ConversationSchema.UNIQUE_ID, username);
		assertEquals(addedConvos.size(), 15);
		assertEquals(conversations.size(), 27);
		reset();
	}

	@Test
	public void testLoadingData() throws SQLException{
		addToStorage(10, 5, username);
		ArrayList<ConversationData> testConversations = storage.loadConversations(username);
		for (int i = 0; i < testConversations.size(); i++){
			assertTrue(testConversations.get(i).isEqual(conversations.get(i)));
		}
		reset();
	}

	@Test
	public void testMultipleUsers() throws SQLException{
		addToStorage(8, 3, username);
		addToStorage(4, 7, username2);
		ArrayList<ConversationData> userConvos = storage.loadConversations(username);
		userConvos.addAll(storage.loadConversations(username2));
		for (int i = 0; i < userConvos.size(); i++){
			assertTrue(userConvos.get(i).isEqual(conversations.get(i)));
		}
		reset();
	}

	@Test
	public void testChronological() throws SQLException{
		addToStorage(3, 11, username);
		ArrayList<ConversationData> basis = storage.loadConversations(username);
		ArrayList<ConversationData> sorted = storage.loadConversations(username);
		Collections.shuffle(sorted);
		Collections.sort(sorted);
		for (int i = 0; i < sorted.size(); i++){
			assertEquals(basis.get(i).getCreation().inMs(), sorted.get(i).getCreation().inMs());
		}
		reset();
	}

	@After
	public void cleanup() throws SQLException{
		storage.messageTable.destroy();
		storage.conversationTable.destroy();
	}
}