/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description Converts items into a compressed bytestream in order
 * to minimize bandwidth when sending messages
*/

package codeu.chat.compression;

import codeu.chat.util.Uuid;
import codeu.chat.util.Time;
import codeu.chat.common.Message;
import codeu.chat.util.Serializers;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.zip.DataFormatException;  
import java.util.zip.Deflater;  
import java.util.zip.Inflater;

public final class CompressionEngine {

    /**
     * @param msg The message to be compressed
     * @return A byte array representation of the parameter message
     */
    public static byte[] compressMessage(Message msg) {
        ByteArrayOutputStream msgStream = new ByteArrayOutputStream();
        try{
            Message.toStream(msgStream, msg);
        }catch (IOException e){
            e.printStackTrace();
        }
        byte[] byteMsg = msgStream.toByteArray();

        return compress(byteMsg);
    }

    /**
     * @pre packet must be a compressed Message
     * @param packet The entry to be decompressed
     * @return A message that represents the compressed packet passed in
     */
    public static Message decompressMessage(byte[] packet) {

        packet = decompress(packet);

        ByteArrayInputStream byteMsg = new ByteArrayInputStream(packet);
        //Must create a filler message in order to satisfy compiler
        Message msg = new Message(Uuid.NULL, Uuid.NULL, Uuid.NULL, Time.now(), Uuid.NULL, "");
        try {
            msg = Message.fromStream(byteMsg);
        }catch (IOException e){
            e.printStackTrace();
        }
        return msg;
    }

    /**
     * @param data The bytes that are to be compressed
     * @return A smaller byte array that can be decompressed back into itself
     */
    private static byte[] compress(byte[] data) {
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
    private static byte[] decompress(byte[] data){

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
}
