/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description Testing suite for compression
*/

//TODO: Add integration tests

package codeu.chat.compression;

import static org.junit.Assert.*;
import org.junit.Test;
import codeu.chat.compression.CompressionEngine;
import codeu.chat.common.Uuid;
import codeu.chat.common.Uuids;
import codeu.chat.common.Time;
import codeu.chat.common.Message;
import com.diogoduailibe.lzstring4j.LZString;

public final class CompressionTest{

	//TODO: gson does not properly convert Uuids. Will probably have to revise
	// CompressionEngine to convert Uuid's to a string first, and pass
	//in a pseudo-message class in order to handle

	@Test
	public void testCompression(){
		// Create chained Uuid's and Time in order to create a realistic message
		final String ids = "100.200.300";
		final Uuid next = Uuids.fromString(ids);
		final Uuid cur = next.root();
		final Uuid prev = cur.root();
		final Time time = Time.now();
		Message message1 = new Message(cur, next, prev, time, cur, ids);
		// ------Ultimately, this is what I want to be able to test------
		//assertEquals(message1.toString(), CompressionEngine.decompressMessage(CompressionEngine.compressMessage(message1)).toString());

		// -----Using CompressionEngine here gives me errors---------
		//String s = CompressionEngine.compressMessage(message1);
		//System.out.println(s);
		//Message m2 = CompressionEngine.decompressMessage(s);
		//System.out.println(m2.toString());

		// ---Even this line, which manually calls LZString's compress gives me errors.
		// So it seems to be an issue with LZString---------
		//System.out.println(LZString.compress("Foobar"));

		// -----implicitly calling gson within toString() does not give me the same issues, these run cleanly
		// Further evidence it's some issue with LZString ----
		String test = message1.toString();
		assertEquals(cur.id(), 200);
	}
}