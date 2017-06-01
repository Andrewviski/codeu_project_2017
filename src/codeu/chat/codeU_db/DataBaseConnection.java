package codeu.chat.codeU_db;

import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.*;

import java.sql.*;
import java.sql.Time;
import java.util.*;

public final class DataBaseConnection {

  private Connection c = null;

  public DataBaseConnection() {
    c = null;
  }

  private void open() {
    if (c != null) {
      System.out.println("ERROR: already connected to the Database");
    } else {
      try {
        Class.forName("org.sqlite.JDBC");
        c = DriverManager.getConnection("jdbc:sqlite:./bin/codeu/chat/codeU_db/ChatDatabase.db");
        c.setAutoCommit(false);
        System.out.println("Opened database successfully");
      } catch (Exception e) {
        System.err.println(e.getClass().getName() + ": " + e.getMessage());
        System.exit(0);
      }
    }
  }

  private void close() {
    try {
      c.close();
      c = null;
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public boolean dbUpdate(Vector<String> parameters, String str) {
    open();
    boolean status = true;
    int parCounter = 1;

    try {
      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      stmt.executeUpdate();
      stmt.close();
      c.commit();
    } catch (Exception e) {
      System.out.println("Error adding element to database");
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      status = false;
      System.exit(0);
    }
    close();
    return status;
  }

  public Collection<User> dbQueryUsers(Vector<String> parameters, String str) {

    final Collection<User> found = new ArrayList<>();
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Uuid userID = Uuid.parse(rs.getString("ID"));
        String userName = rs.getString("UNAME");
        codeu.chat.util.Time creationTime = codeu.chat.util.Time.fromMs(rs.getLong("TimeCreated"));
        String userPassword = rs.getString("PASSWORD");

        User user = new User(userID, userName, creationTime, userPassword);
        found.add(user);
      }

      rs.close();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public Collection<Conversation> dbQueryConversations(Vector<String> parameters, String str) {

    final Collection<Conversation> found = new ArrayList<>();
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Uuid conversationID = Uuid.parse(rs.getString("ID"));
        String conversationName = rs.getString("CNAME");
        codeu.chat.util.Time creationTime = codeu.chat.util.Time.fromMs(rs.getLong("TimeCreated"));
        Uuid ownerID = Uuid.parse(rs.getString("OWNERID"));

        Conversation conversation = new Conversation(conversationID, ownerID, creationTime, conversationName);
        found.add(conversation);
      }

      rs.close();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public Uuid getConversationData(Vector<String> parameters, String str) {

    Uuid found = null;
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        if (rs.getString("ID") == null)
          found = Uuid.NULL;
        else
          found = Uuid.parse(rs.getString("ID"));
      }

      rs.close();
      stmt.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public Uuid getConversationID(Vector<String> parameters, String str) {

    Uuid found = null;
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        found = Uuid.parse(rs.getString("CONVERSATIONID"));
      }

      rs.close();
      stmt.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public Collection<Uuid> getUsersInConversations(Vector<String> parameters, String str) {

    final Collection<Uuid> found = new ArrayList<>();
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Uuid userID = Uuid.parse(rs.getString("USERID"));
        found.add(userID);
      }

      rs.close();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public Collection<Uuid> getConversationsOfUser(Vector<String> parameters, String str) {

    final Collection<Uuid> found = new ArrayList<>();
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Uuid conversationID = Uuid.parse(rs.getString("CONVERSATIONID"));
        found.add(conversationID);
      }

      rs.close();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public Collection<Message> dbQueryMessages(Vector<String> parameters, String str) {

    final Collection<Message> found = new ArrayList<>();
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Uuid messageID = Uuid.parse(rs.getString("ID"));
        String next = rs.getString("MNEXTID");
        Uuid nextMessageID = Uuid.NULL;
        if (next != null)
          nextMessageID = Uuid.parse(next);
        String prev = rs.getString("MPREVID");
        Uuid prevMessageID = Uuid.NULL;
        if (prev != null)
          prevMessageID = Uuid.parse(prev);
        codeu.chat.util.Time creationTime = codeu.chat.util.Time.fromMs(rs.getLong("TimeCreated"));
        Uuid authorID = Uuid.parse(rs.getString("USERID"));
        String content = rs.getString("MESSAGE");

        Message message = new Message(messageID, nextMessageID, prevMessageID, creationTime, authorID, content);
        found.add(message);
      }

      rs.close();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public int getUserCluster(Vector<String> parameters, String str) {

    int userCluster = -1;
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      if (rs.next()) {
        userCluster = rs.getInt("CLUSTER");
      }

      rs.close();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return userCluster;
  }

  public Collection<Uuid> getUsersInCluster(Vector<String> parameters, String str) {

    final Collection<Uuid> found = new ArrayList<>();
    int parCounter = 1;

    open();
    try {

      PreparedStatement stmt = c.prepareStatement(str);

      for(String parameter : parameters) {
        stmt.setString(parCounter, parameter);
        parCounter++;
      }

      ResultSet rs = stmt.executeQuery();

      while (rs.next()) {
        Uuid userID = Uuid.parse(rs.getString("ID"));
        found.add(userID);
      }

      rs.close();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    close();
    return found;
  }

  public Connection getConnection() {
    return c;
  }

  public void createTables() {
    c = null;
    PreparedStatement stmt = null;
    open();
    try {
      String sql = "CREATE TABLE USERS " +
          "(ID            VARCHAR(16) PRIMARY KEY NOT NULL," +
          " UNAME         CHAR(25)    UNIQUE      NOT NULL, " +
          " TimeCreated   BIGINT               NOT NULL, " +
          " PASSWORD      TEXT                    NOT NULL)";
      stmt = c.prepareStatement(sql);
      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Table <USERS> created successfully");

    try {
      String sql = "CREATE TABLE CONVERSATIONS " +
          "(ID            VARCHAR(16) PRIMARY KEY NOT NULL, " +
          " CNAME         CHAR(25)                NOT NULL, " +
          " OWNERID       VARCHAR(16)             NOT NULL, " +
          " TimeCreated   BIGINT               NOT NULL, " +
          " FOREIGN KEY(OWNERID) REFERENCES USERS(ID))";
      stmt = c.prepareStatement(sql);
      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Table <CONVERSATIONS> created successfully");

    try {
      String sql = "CREATE TABLE USER_CONVERSATION " +
          "(ID                VARCHAR(32) PRIMARY KEY NOT NULL, " +
          " USERID            VARCHAR(16)             NOT NULL, " +
          " CONVERSATIONID    VARCHAR(16)             NOT NULL, " +
          " FOREIGN KEY(USERID)         REFERENCES USERS(ID), " +
          " FOREIGN KEY(CONVERSATIONID) REFERENCES CONVERSATIONS(ID))";
      stmt = c.prepareStatement(sql);
      stmt.executeUpdate();
      stmt.close();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Table <USER_CONVERSATION> created successfully");

    try {
      String sql = "CREATE TABLE MESSAGES " +
          "(ID                VARCHAR(16) PRIMARY KEY NOT NULL, " +
          " USERID            VARCHAR(16)             NOT NULL, " +
          " MNEXTID           VARCHAR(16),                      " +
          " MPREVID           VARCHAR(16),                      " +
          " CONVERSATIONID    VARCHAR(16)             NOT NULL, " +
          " TimeCreated       BIGINT                  NOT NULL, " +
          " MESSAGE           TEXT                    NOT NULL, " +
          " FOREIGN KEY(USERID)         REFERENCES USERS(ID), " +
          " FOREIGN KEY(MNEXTID)        REFERENCES MESSAGES(ID), " +
          " FOREIGN KEY(MPREVID)        REFERENCES MESSAGES(ID), " +
          " FOREIGN KEY(CONVERSATIONID) REFERENCES CONVERSATIONS(ID))";
      stmt = c.prepareStatement(sql);
      stmt.executeUpdate();
      stmt.close();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Table <MESSAGES> created successfully");

    try {
      String sql = "CREATE TABLE USER_CLUSTER " +
          "(ID                VARCHAR(16) PRIMARY KEY NOT NULL, " +
          " CLUSTER           INT                     NOT NULL) ";
      stmt = c.prepareStatement(sql);
      stmt.executeUpdate();
      stmt.close();
      c.commit();

    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    System.out.println("Table <USER_CLUSTER> created successfully");
    close();
  }

  public void dropTables() {
    open();
    Statement stmt = null;
    //drop tables before creating them
    try {
      stmt = c.createStatement();
      String sql = "DROP TABLE USERS";
      stmt.executeUpdate(sql);
      stmt.close();

      stmt = c.createStatement();
      sql = "DROP TABLE CONVERSATIONS";
      stmt.executeUpdate(sql);
      stmt.close();

      stmt = c.createStatement();
      sql = "DROP TABLE USER_CONVERSATION";
      stmt.executeUpdate(sql);
      stmt.close();

      stmt = c.createStatement();
      sql = "DROP TABLE MESSAGES";
      stmt.executeUpdate(sql);
      stmt.close();

      stmt = c.createStatement();
      sql = "DROP TABLE USER_CLUSTER";
      stmt.executeUpdate(sql);
      stmt.close();

      c.commit();
    } catch (Exception e) {
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
    close();
  }

}
