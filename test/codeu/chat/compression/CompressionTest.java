/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description Testing suite for compression
*/

package codeu.chat.compression;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import codeu.chat.common.Time;
import codeu.chat.common.Uuid;
import codeu.chat.common.Uuids;
import codeu.chat.common.Message;

public final class CompressionTest{

	//Question: would it be better to create an array of randomized messages, and
	//test copies of each one?
	private Message testMsg;

	@Before
	public void setupTestMessage(){
		// Create chained Uuid's and Time in order to create a realistic message
		final String authString = "50";
		final String ids = "100.200.300";
		// FIX: Time.now() no longer working
		// final Time time = Time.now();
		final Time time = Time.fromMs(12223456);
		Uuid author = Uuids.fromString(authString);
		Uuid next = Uuids.fromString(ids);
		Uuid id = next.root();
		Uuid prev = id.root();
		testMsg = new Message(id, next, prev, time, author, "I am a test message!\naAbB319@*!^&[]{}~ ZCXv");
	}

	@Test
	public void testMessageCompression(){
		Message copy = Message.COMPRESSION.decompress(Message.COMPRESSION.compress(testMsg));
		assertTrue(Message.equals(testMsg, copy));
	}

	@Test
	public void testMessageReadWrite(){
		try{
            ByteArrayOutputStream output = new ByteArrayOutputStream();
			Message.SERIALIZER.write(output, testMsg);
			ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
			assertTrue(Message.equals(testMsg, Message.SERIALIZER.read(input)));
        }catch (IOException e){
            e.printStackTrace();
        }
	}
}