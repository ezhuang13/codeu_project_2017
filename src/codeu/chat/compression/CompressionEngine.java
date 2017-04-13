/**
 * @author Eric Zhuang, CodeU Project Group 6
 * @description Converts items into a compressed bytestream in order
 * to minimize bandwidth when sending messages
*/

package codeu.chat.compression;

import codeu.chat.common.Uuids;
import codeu.chat.common.Time;
import codeu.chat.common.Message;
import codeu.chat.util.Serializers;

import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

public final class CompressionEngine {

    /**
     * @param msg The message to be compressed
     * @return A byte array representation of the parameter message
     */
    public static byte[] compressMessage(Message msg) {
        ByteArrayOutputStream msgStream = new ByteArrayOutputStream();
        try{
            Message.SERIALIZER.write(msgStream, msg);
        }catch (IOException e){
            e.printStackTrace();
        }
        byte[] byteMsg = msgStream.toByteArray();
        //Now compress
        return byteMsg;
    }

    /**
     * @pre packet must be a compressed Message
     * @param packet The entry to be decompressed
     * @return A message that represents the compressed packet passed in
     */
    public static Message decompressMessage(byte[] packet) {

        //Decompress packet


        ByteArrayInputStream byteMsg = new ByteArrayInputStream(packet);
        //Must create a filler message in order to satisfy compiler
        Message msg = new Message(Uuids.NULL, Uuids.NULL, Uuids.NULL, Time.now(), Uuids.NULL, "");
        try {
            msg = Message.SERIALIZER.read(byteMsg);
        }catch (IOException e){
            e.printStackTrace();
        }
        return msg;
    }
}
