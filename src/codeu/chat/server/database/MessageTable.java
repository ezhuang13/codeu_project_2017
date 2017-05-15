package codeu.chat.server.database;

import java.sql.SQLException;

import codeu.chat.database.Database;
import codeu.chat.database.Table;

/**
 * @description Table for storing message data
 */
public class MessageTable extends Table<MessageSchema> {

  public MessageTable(Database database) throws SQLException {
    super(new MessageSchema(), database, "messages");
  }

}