/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description Testing suite for compression
*/

//TODO: Add integration tests

package codeu.chat.compression;

import static org.junit.Assert.*;
import org.junit.Test;
import codeu.chat.common.Uuid;
import codeu.chat.common.Uuids;
import codeu.chat.common.Message;

public final class CompressionTest{
	private CompressionEngine compression;

	@Test
	public void testCompression(){
		final String ids = "100.200.300";
		final Uuid next = Uuids.fromString(ids);
		final Uuid cur = next.root();
		final Uuid prev = cur.root();
		assertEquals(next.id(), 300);
	}
}