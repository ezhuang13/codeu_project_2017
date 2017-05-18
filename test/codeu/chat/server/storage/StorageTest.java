package codeu.chat.server.storage;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import codeu.chat.database.Database;
import codeu.chat.database.DBObject;
import codeu.chat.server.database.ConversationSchema;
import codeu.chat.server.database.MessageSchema;


import codeu.chat.util.Time;

/*
* @description Testing for data persistence
*/
public final class StorageTest{

	private Database database;
	private Storage storage;
	private static final String USERNAME = "test";
	private static final String USERNAME2 = "test2";

	//Adds multiple conversations and messages to storage
	//Returns an ArrayList containing the expected values based on what was added
	private ArrayList<ConversationData> addToStorage(String name){
		ArrayList<ConversationData> expected = new ArrayList<ConversationData>();
		for (int i = 0; i < 10; i++){
			Time convoTime = Time.now();
			String convoTitle = "Test convo " + i;
			int cid = storage.addConversation(name, convoTime.inMs(), convoTitle);
			ArrayList<MessageData> messages = new ArrayList<MessageData>();
			for (int j = 0; j < 5; j++){
				String messageContent = "Test message " + j;
				Time messageTime = Time.now();
				storage.addMessage(cid, messageTime.inMs(), messageContent);
				MessageData m = new MessageData(messageContent, messageTime);
				messages.add(m);
			}
			ConversationData c = new ConversationData(convoTitle, convoTime, cid, messages);
			expected.add(c);
		}
		return expected;
	}

	//Create the storage manager, clear the database, and then reinitialize it
	@Before
	public void setup() throws SQLException{
		database = new Database("test.db");
		storage = new Storage(database);
		storage.messageTable.destroy();
		storage.conversationTable.destroy();
		storage = new Storage(database);
	}

	@Test
	public void testAddingData() throws SQLException{
		//Checks that conversations are added as expected
		ArrayList<ConversationData> expected = addToStorage(USERNAME);
		List<DBObject<ConversationSchema>> actualConvos = storage.conversationTable.find(ConversationSchema.UNIQUE_ID, USERNAME);
		for (int i = 0; i < 10; i++){
			ConversationData expectedData = expected.get(i);
			DBObject<ConversationSchema> actualData = actualConvos.get(i);
			assertTrue(expectedData.getTitle().equals(actualData.get(ConversationSchema.TEXT)));
			assertEquals(expectedData.getCreation().inMs(), Long.parseLong(actualData.get(ConversationSchema.TIMESTAMP)));
			//Checks that messages are added as expected
			ArrayList<MessageData> expectedMsgs = expectedData.getMessages();
			List<DBObject<MessageSchema>> actualMsgs = storage.messageTable.find(MessageSchema.UNIQUE_ID, Integer.toString(expectedData.getId()));
			for (int j = 0; j < 5; j++) {
				MessageData expectedMsg = expectedMsgs.get(j);
				DBObject<MessageSchema> actualMsg = actualMsgs.get(j);
				assertTrue(expectedMsg.getContent().equals(actualMsg.get(MessageSchema.TEXT)));
				assertEquals(expectedMsg.getCreation().inMs(), Long.parseLong(actualMsg.get(MessageSchema.TIMESTAMP)));
			}
		}
	}

	@Test
	public void testLoadingData(){
		ArrayList<ConversationData> expected = addToStorage(USERNAME);
		ArrayList<ConversationData> actual = storage.loadConversations(USERNAME);
		for (int i = 0; i < 10; i++){
			assertTrue(actual.get(i).isEqual(expected.get(i)));
		}
	}

	@Test
	public void testMultipleUsers(){
		ArrayList<ConversationData> expected = addToStorage(USERNAME);
		expected.addAll(addToStorage(USERNAME2));
		ArrayList<ConversationData> actual = storage.loadConversations(USERNAME);
		actual.addAll(storage.loadConversations(USERNAME2));
		for (int i = 0; i < 20; i++){
			assertTrue(actual.get(i).isEqual(expected.get(i)));
		}
	}

	@Test
	public void testChronological(){
		addToStorage(USERNAME);
		ArrayList<ConversationData> sorted = storage.loadConversations(USERNAME);
		for (int i = 0; i < 9; i++){
			ConversationData cur = sorted.get(i);
			ConversationData next = sorted.get(i + 1);
			assertTrue(cur.getCreation().inMs() < next.getCreation().inMs());
			for (int j = 0; j < 4; j++) {
				assertTrue(cur.getMessages().get(j).getCreation().inMs() < next.getMessages().get(j).getCreation().inMs());
			}
		}
	}
}
