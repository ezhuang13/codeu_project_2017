/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description Converts items into a compressed bytestream in order
 * to minimize bandwidth when sending messages
*/ 

package codeu.chat.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.DataFormatException;  
import java.util.zip.Deflater;  
import java.util.zip.Inflater;

public final class Compressions{

	public static final Compression<byte[]> BYTES = new Compression<byte[]>(){

		/**
     	* @param data The bytes that are to be compressed
     	* @return A smaller byte array that can be decompressed back into itself
     	*/
		@Override
		public byte[] compress (byte[] data){

			//TODO: Experiment with setting level of deflater for improved performance
	        Deflater deflater = new Deflater();
	        deflater.setInput(data);

	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

	        //Is this needed?
	        deflater.finish();

	        byte[] buffer = new byte[1024];
	        while (!deflater.finished()) {  
	            int count = deflater.deflate(buffer);
	            outputStream.write(buffer, 0, count);   
	        }
	        byte[] output = outputStream.toByteArray();

	        deflater.end();

	        //TODO: Create logging for % compression
	        //System.out.println("Original: " + data.length + " bytes");  
	        //System.out.println("Compressed: " + output.length + " bytes");  
	        return output;  

		}
    	/**
     	* @param data The bytes that are to be decompressed
     	* @return A larger byte array that represents the original data
    	 */
		@Override
		public byte[] decompress(byte[] data){

	        Inflater inflater = new Inflater();
	        inflater.setInput(data);

	        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);  
	        byte[] buffer = new byte[1024];  
	        while (!inflater.finished()) {
	            int count = 0;
	            try{
	                count = inflater.inflate(buffer);
	            }
	            catch (DataFormatException e){
	                e.printStackTrace();
	            } 
	            outputStream.write(buffer, 0, count);  
	        }
	        byte[] output = outputStream.toByteArray();

	        inflater.end();

        	return output;
        }
    };
}