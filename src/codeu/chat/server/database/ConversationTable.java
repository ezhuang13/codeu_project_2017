package codeu.chat.server.database;

import java.sql.SQLException;

import codeu.chat.database.Database;
import codeu.chat.database.Table;

/**
 * @description Table for storing conversation data
 */
public class ConversationTable extends Table<ConversationSchema> {

  public ConversationTable(Database database) throws SQLException {
    super(new ConversationSchema(), database, "conversations");
  }

}