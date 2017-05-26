package codeu.chat.common;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.Date;

import codeu.chat.util.Uuid;
import codeu.chat.util.Time;

/**
 * Created by strobe on 4/04/17.
 */
public class SQLFormatter {
  public static String sqlID(Uuid userID) {
    String sqlID = "NULL";
    if (userID != null) {
      sqlID = userID.toString();
      sqlID = sqlID.replace("[UUID:", "");
      sqlID = sqlID.replace("]", "");
    }

    return sqlID;
  }

  public static String sqlID(Uuid userID1, Uuid userID2) {
    String sqlID1 = "NULL";
    if (userID1 != null) {
      sqlID1 = userID1.toString();
      sqlID1 = sqlID1.replace("[UUID:", "");
      sqlID1 = sqlID1.replace("]", "");
    }

    String sqlID2 = "NULL";
    if (userID2 != null) {
      sqlID2 = userID2.toString();
      sqlID2 = sqlID2.replace("[UUID:", "");
      sqlID2 = sqlID2.replace("]", "");
    }

    String sqlID = sqlID1 + sqlID2;
    return sqlID;
  }

  public static String sqlName(String userName) {
    String sqlName = userName;
    return sqlName;
  }

  public static String sqlCreationTime(Time userTime) {
    Long inMs = userTime.inMs();
    String sqlCreationTime = Long.toString(inMs);
    return sqlCreationTime;
  }

  public static String sqlPassword(String userPassword) {
    String sqlPassword = userPassword;
    return sqlPassword;
  }

  public static String sqlBody(String userBody) {
    String sqlBody = userBody;
    return sqlBody;
  }

  public static String sqlContainsText(String text) {
    String sqlConstainsText = text;
    return sqlConstainsText;
  }

  public static boolean sqlValidConversation(Uuid userID, Uuid conversationID) {
    boolean validConversation = false;

    Connection connection = null;
    Statement stmt = null;

    try {
      Class.forName("org.sqlite.JDBC");
      connection = DriverManager.getConnection("jdbc:sqlite:./bin/codeu/chat/codeU_db/ChatDatabase.db");
      connection.setAutoCommit(false);

      stmt = connection.createStatement();
      ResultSet rs = stmt.executeQuery("SELECT * " +
          "FROM USER_CONVERSATION" +
          "WHERE  USERID = " + sqlID(userID) + "" +
          "AND    CONVERSATIONID = " + sqlID(conversationID) + ";");
      if (rs.next()) {
        validConversation = true;
        System.out.println("Conversation exists and User is a member");
      }
      rs.close();
      stmt.close();
      connection.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    return validConversation;
  }


}
