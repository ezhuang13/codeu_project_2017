package codeu.chat.server.storage;

import java.sql.SQLException;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import codeu.chat.database.Database;
import codeu.chat.server.database.ConversationSchema;

/*
* @description Testing for conversation persistency
*/
public final class ConversationTest{

	private Database database;
	private ConversationSchema conversationSchema;

	@Before
	public void setup(){
		database = new Database("test.db");
		conversationSchema = new ConversationSchema();
	}

	@Test
	public void testFiller(){
		assertEquals(true, true);
	}
}