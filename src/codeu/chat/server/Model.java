// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package codeu.chat.server;

import java.io.StringReader;
import java.util.*;

import codeu.chat.codeU_db.DataBaseConnection;
import codeu.chat.common.*;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.Store;

public final class Model {

  private static final Comparator<Uuid> UUID_COMPARE = new Comparator<Uuid>() {

    @Override
    public int compare(Uuid a, Uuid b) {

      if (a == b) {
        return 0;
      }

      if (a == null && b != null) {
        return -1;
      }

      if (a != null && b == null) {
        return 1;
      }

      final int order = Integer.compare(a.id(), b.id());
      return order == 0 ? compare(a.root(), b.root()) : order;
    }
  };

  private static final Comparator<Time> TIME_COMPARE = new Comparator<Time>() {
    @Override
    public int compare(Time a, Time b) {
      return a.compareTo(b);
    }
  };

  private static final Comparator<String> STRING_COMPARE = String.CASE_INSENSITIVE_ORDER;

  private final static Logger.Log LOG = Logger.newLog(Model.class);

  private final Store<Uuid, User> userById = new Store<>(UUID_COMPARE);
  private final Store<Time, User> userByTime = new Store<>(TIME_COMPARE);
  private final Store<String, User> userByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, Conversation> conversationById = new Store<>(UUID_COMPARE);
  private final Store<Time, Conversation> conversationByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Conversation> conversationByText = new Store<>(STRING_COMPARE);

  private final Store<Uuid, Message> messageById = new Store<>(UUID_COMPARE);
  private final Store<Time, Message> messageByTime = new Store<>(TIME_COMPARE);
  private final Store<String, Message> messageByText = new Store<>(STRING_COMPARE);

  private final Uuid.Generator userGenerations = new LinearUuidGenerator(null, 1, Integer.MAX_VALUE);
  private Uuid currentUserGeneration = userGenerations.make();

  private static DataBaseConnection dbConnection = new DataBaseConnection();

  public static String intersect(Vector<String> parameters, Collection<Uuid> ids, boolean isBlacklist) {

    // Use a set to hold the found users as this will prevent duplicate ids from
    // yielding duplicates in the result.

    String found = null;
    String operator = null;

    if (ids.isEmpty()) {
      return found;
    }

    found = "";

    if (!isBlacklist) {
      operator = "=";
    } else {
      operator = "<>";
    }

    for (Iterator<Uuid> id = ids.iterator(); id.hasNext(); ) {
      Uuid nextID = id.next();
      parameters.add(SQLFormatter.sqlID(nextID));
      found += "ID " + operator + " ?";
      if (id.hasNext()) {
        found += " OR ";
      }
    }
    return found;
  }

  public User getAdmin() {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add("Admin");
    query = "UNAME = ?";

    Collection<User> returnUser = userById(parameters, query, true);
    if(!returnUser.isEmpty()){
      return returnUser.iterator().next();
    }
    else {
      return null;
    }
  }

  public User getSingleUser(Uuid userID) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlID(userID));
    query = "ID = ?";

    Collection<User> returnUser = userById(parameters, query, true);
    if(!returnUser.isEmpty()){
      return returnUser.iterator().next();
    }
    else {
      return null;
    }
  }

  public Collection<User> userById(Collection<Uuid> ids, boolean isBlackList, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    query = intersect(parameters, ids, isBlackList);

    return userById(parameters, query, orderASC);
  }

  public Collection<User> getAllUsers(Uuid conversation, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    if(!conversation.equals(Uuid.NULL)) {
      parameters.add(SQLFormatter.sqlID(conversation));
      Collection<Uuid> usersIDs = dbConnection.getUsersInConversations(parameters, "SELECT * FROM USER_CONVERSATION where CONVERSATIONID = ?;");

      parameters.clear();
      query = intersect(parameters, usersIDs, false);

      return userById(parameters, query, orderASC);
    }
    else {
      return userById(parameters, null, true);
    }
  }

  public Conversation getSingleConversation(Uuid conversationID) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlID(conversationID));
    query = "ID = ?";

    Collection<Conversation> returnConversation = conversationById(parameters, query, true);

    if(!returnConversation.isEmpty()){
      return returnConversation.iterator().next();
    }
    else {
      return null;
    }
  }

  public Collection<Conversation> conversationById(Collection<Uuid> ids, boolean isBlackList, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    query = intersect(parameters, ids, isBlackList);

    return conversationById(parameters, query, orderASC);
  }

  public Collection<Conversation> getAllConversations(Uuid user, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();



    if(!user.equals(Uuid.NULL)) {
      parameters.add(SQLFormatter.sqlID(user));
      query = "SELECT * FROM USER_CONVERSATION where USERID = ?;";

      Collection<Uuid> conversationsIDs = dbConnection.getUsersInConversations(parameters, query);

      parameters.clear();
      query = intersect(parameters, conversationsIDs, false);

      return conversationById(parameters, query, orderASC);
    }
    else {
      return conversationById(parameters, null, orderASC);
    }
  }

  public Collection<Conversation> getConversationsInRange(Time start, Time end, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlCreationTime(start));
    parameters.add(SQLFormatter.sqlCreationTime(end));
    query = "TimeCreated >= ? AND TimeCreated <= ?";

    return conversationByTime(parameters, query, orderASC);
  }

  public Collection<Conversation> getConversationsByFilter(String filter, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlContainsText(filter));
    query = "CNAME LIKE '%?%'";

    return conversationByText(parameters, query, orderASC);
  }

  public Message getSingleMessage(Uuid messageID) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlID(messageID));
    query = "ID = ?";

    Collection<Message> returnMessage = messageById(parameters, query, true);
    if(!returnMessage.isEmpty()){
      return returnMessage.iterator().next();
    }
    else {
      return null;
    }
  }

  public Collection<Message> messageByTime(Collection<Uuid> ids, boolean isBlackList, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    query = intersect(parameters, ids, isBlackList);

    return messageByTime(parameters, query, orderASC);
  }

  public Message getLastMessage(Uuid conversationID) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlID(conversationID));
    query = "MNEXTID == '0' AND CONVERSATIONID = ?";

    Collection<Message> lastMessage = messageById(parameters, query, false);
    if(!lastMessage.isEmpty()){
      return lastMessage.iterator().next();
    }
    else {
      return null;
    }
  }

  public Collection<Message> getMessagesInRange(Uuid conversation, Time start, Time end, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlID(conversation));
    parameters.add(SQLFormatter.sqlCreationTime(start));
    parameters.add(SQLFormatter.sqlCreationTime(end));
    query = "CONVERSATIONID = ? AND TimeCreated >= ? AND TimeCreated <= ?";

    return messageByTime(parameters, query, orderASC);
  }

  public void add(User user) {
    String query;
    Vector<String> parameters = new Vector<>();

    currentUserGeneration = userGenerations.make();

    try {
      parameters.add(SQLFormatter.sqlID(user.id));
      parameters.add(SQLFormatter.sqlName(user.name));
      parameters.add(SQLFormatter.sqlCreationTime(user.creation));
      parameters.add(SQLFormatter.sqlPassword(user.password));
      query = "INSERT INTO USERS (ID,UNAME,TIMECREATED,PASSWORD) " +
          "VALUES ( ?, ?, ?, ?);";

      dbConnection.dbUpdate(parameters, query);
      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          user.id,
          user.name,
          user.creation);
    } catch (Exception e) {
      LOG.info(
          "newUser fail - Database insertion error (user.id=%s user.name=%s user.time=%s)",
          user.id,
          user.name,
          user.creation);
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public Collection<Message> getAllMessagesInConversation(Uuid conversation, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlID(conversation));
    query = "CONVERSATIONID = ?;";

    return messageByTime(parameters, query, orderASC);
  }

  public Collection<Message> getAllMessagesFromUser(Uuid user, boolean orderASC) {
    String query;
    Vector<String> parameters = new Vector<>();

    parameters.add(SQLFormatter.sqlID(user));
    query = "USERID = ?;";

    return messageByTime(parameters, query, orderASC);
  }

  public void update(User user) {
    String query;
    Vector<String> parameters = new Vector<>();

    try {
      parameters.add(SQLFormatter.sqlName(user.name));
      parameters.add(SQLFormatter.sqlCreationTime(user.creation));
      parameters.add(SQLFormatter.sqlName(user.password));
      parameters.add(SQLFormatter.sqlID(user.id));
      query = "UPDATE USERS set" +
          " UNAME = ?," +
          " TimeCreated = ?," +
          " UNAME = ?" +
          " where ID = ?" +
          ";";

      dbConnection.dbUpdate(parameters, query);
      LOG.info(
          "updateUser success (user.id=%s user.name=%s user.time=%s)",
          user.id,
          user.name,
          user.creation);
    } catch (Exception e) {
      LOG.info(
          "updateUser fail - Database update error (user.id=%s user.name=%s user.time=%s)",
          user.id,
          user.name,
          user.creation);
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public Collection<User> userById(Vector<String> parameters, String where, boolean orderASC) {
    String query = "SELECT * FROM USERS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY ID ASC";
    else
      query += " ORDER BY ID DESC";
    query += ";";
    return dbConnection.dbQueryUsers(parameters, query);
  }

  public Collection<User> userByTime(Vector<String> parameters, String where, boolean orderASC) {
    String query = "SELECT * FROM USERS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY TimeCreated ASC";
    else
      query += " ORDER BY TimeCreated DESC";
    query += ";";
    return dbConnection.dbQueryUsers(parameters, query);
  }

  public Collection<User> userByText(Vector<String> parameters, String where, boolean orderASC) {
    String query = "SELECT * FROM USERS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY UNAME ASC";
    else
      query += " ORDER BY UNAME DESC";
    query += ";";
    return dbConnection.dbQueryUsers(parameters, query);
  }

  public Uuid userGeneration() {
    return currentUserGeneration;
  }

  public void add(Conversation conversation) {
    String query;
    Vector<String> parameters = new Vector<>();

    try {
      parameters.add(SQLFormatter.sqlID(conversation.id));
      parameters.add(SQLFormatter.sqlName(conversation.title));
      parameters.add(SQLFormatter.sqlID(conversation.owner));
      parameters.add(SQLFormatter.sqlCreationTime(conversation.creation));
      query = "INSERT INTO CONVERSATIONS (ID,CNAME,OWNERID,TimeCreated) " +
          "VALUES ( ?, ?, ?, ?);";

      dbConnection.dbUpdate(parameters, query);

      LOG.info("Conversation added: " + conversation.id);
    } catch (Exception e) {
      LOG.info(
          "newConversation fail - Verify connection and try again shortly");
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }

    add(conversation.owner, conversation.id);
  }

  // Add user to a conversation
  public void add(Uuid user, Uuid conversation) {
    String query;
    Vector<String> parameters = new Vector<>();

    try {
      parameters.add(SQLFormatter.sqlID(user, conversation));
      parameters.add(SQLFormatter.sqlID(user));
      parameters.add(SQLFormatter.sqlID(conversation));
      query = "INSERT INTO USER_CONVERSATION (ID, USERID, CONVERSATIONID) " +
          "VALUES ( ?, ?, ?);";

      dbConnection.dbUpdate(parameters, query);
    } catch (Exception e) {
      LOG.info(
          "Adding user to conversation fail - Verify connection and try again shortly");
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public void update(Conversation conversation) {
    String query;
    Vector<String> parameters = new Vector<>();

    try {
      parameters.add(SQLFormatter.sqlName(conversation.title));
      parameters.add(SQLFormatter.sqlID(conversation.owner));
      parameters.add(SQLFormatter.sqlCreationTime(conversation.creation));
      parameters.add(SQLFormatter.sqlID(conversation.id));
      query = "UPDATE CONVERSATIONS set" +
          " CNAME = ?, " +
          " OWNERID = ?, " +
          " TimeCreated = ?" +
          " where ID = ?;";

      dbConnection.dbUpdate(parameters, query);
      LOG.info(
          "updateConversation success (conversation.id=%s conversation.name=%s conversation.time=%s)",
          conversation.id,
          conversation.title,
          conversation.creation);
    } catch (Exception e) {
      LOG.info(
          "updateConversation fail - Database update error (conversation.id=%s conversation.name=%s conversation.time=%s)",
          conversation.id,
          conversation.title,
          conversation.creation);
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public Collection<Conversation> conversationById(Vector<String> parameters, String where, boolean orderASC) {

    Collection<Conversation> conversations = new HashSet<>();

    String query = "SELECT * FROM CONVERSATIONS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY ID ASC";
    else
      query += " ORDER BY ID DESC";
    query += ";";

    Collection<Conversation> found = dbConnection.dbQueryConversations(parameters, query);

    parameters.clear();

    for (Conversation conv : found) {

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      parameters.add(SQLFormatter.sqlID(Uuid.NULL));
      conv.firstMessage = dbConnection.getConversationData(parameters, "SELECT * FROM MESSAGES where CONVERSATIONID = ? AND MPREVID = ?;");

      if (conv.firstMessage == null)
        conv.firstMessage = Uuid.NULL;

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      parameters.add(SQLFormatter.sqlID(Uuid.NULL));
      conv.lastMessage = dbConnection.getConversationData(parameters,"SELECT * FROM MESSAGES where CONVERSATIONID = ? AND MNEXTID = ?;");

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      conv.users = dbConnection.getUsersInConversations(parameters, "SELECT * FROM USER_CONVERSATION where CONVERSATIONID = ?;");

      conversations.add(conv);
    }

    return conversations;
  }

  public Collection<Conversation> conversationByTime(Vector<String> parameters, String where, boolean orderASC) {
    Collection<Conversation> conversations = new ArrayList<>();

    String query = "SELECT * FROM CONVERSATIONS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY TimeCreated ASC";
    else
      query += " ORDER BY TimeCreated DESC";
    query += ";";

    Collection<Conversation> found = dbConnection.dbQueryConversations(parameters, query);

    parameters.clear();

    for (Conversation conv : found) {

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      parameters.add(SQLFormatter.sqlID(Uuid.NULL));
      conv.firstMessage = dbConnection.getConversationData(parameters, "SELECT * FROM MESSAGES where CONVERSATIONID = ? AND MPREVID = ?;");

      if (conv.firstMessage == null)
        conv.firstMessage = Uuid.NULL;

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      parameters.add(SQLFormatter.sqlID(Uuid.NULL));
      conv.lastMessage = dbConnection.getConversationData(parameters,"SELECT * FROM MESSAGES where CONVERSATIONID = ? AND MNEXTID = ?;");

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      conv.users = dbConnection.getUsersInConversations(parameters, "SELECT * FROM USER_CONVERSATION where CONVERSATIONID = ?;");

      conversations.add(conv);
    }

    return conversations;
  }

  public Collection<Conversation> conversationByText(Vector<String> parameters, String where, boolean orderASC) {
    Collection<Conversation> conversations = new ArrayList<>();

    String query = "SELECT * FROM CONVERSATIONS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY CNAME ASC";
    else
      query += " ORDER BY CNAME DESC";
    query += ";";

    Collection<Conversation> found = dbConnection.dbQueryConversations(parameters, query);

    parameters.clear();

    for (Conversation conv : found) {

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      parameters.add(SQLFormatter.sqlID(Uuid.NULL));
      conv.firstMessage = dbConnection.getConversationData(parameters, "SELECT * FROM MESSAGES where CONVERSATIONID = ? AND MPREVID = ?;");

      if (conv.firstMessage == null)
        conv.firstMessage = Uuid.NULL;

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      parameters.add(SQLFormatter.sqlID(Uuid.NULL));
      conv.lastMessage = dbConnection.getConversationData(parameters,"SELECT * FROM MESSAGES where CONVERSATIONID = ? AND MNEXTID = ?;");

      parameters.clear();
      parameters.add(SQLFormatter.sqlID(conv.id));
      conv.users = dbConnection.getUsersInConversations(parameters, "SELECT * FROM USER_CONVERSATION where CONVERSATIONID = ?;");

      conversations.add(conv);
    }

    return conversations;
  }

  public void add(Message message, Uuid conversation) {
    String query;
    Vector<String> parameters = new Vector<>();

    try {
      parameters.add(SQLFormatter.sqlID(message.id));
      parameters.add(SQLFormatter.sqlID(message.author));
      parameters.add(SQLFormatter.sqlID(message.next));
      parameters.add(SQLFormatter.sqlID(message.previous));
      parameters.add(SQLFormatter.sqlID(conversation));
      parameters.add(SQLFormatter.sqlCreationTime(message.creation));
      parameters.add(SQLFormatter.sqlBody(message.content));
      query = "INSERT INTO MESSAGES (ID,USERID,MNEXTID,MPREVID,CONVERSATIONID,TimeCreated,MESSAGE) " +
          "VALUES ( ?, ?, ?, ?, ?, ?, ?);";

      dbConnection.dbUpdate(parameters, query);

      LOG.info("Message added: " + message.id);
    } catch (Exception e) {
      LOG.info(
          "newMessage fail - Verify connection and try again shortly");
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public void update(Message message) {
    String query;
    Vector<String> parameters = new Vector<>();

    try {
      parameters.add(SQLFormatter.sqlID(message.author));
      parameters.add(SQLFormatter.sqlID(message.next));
      parameters.add(SQLFormatter.sqlID(message.previous));
      parameters.add(SQLFormatter.sqlCreationTime(message.creation));
      parameters.add(SQLFormatter.sqlBody(message.content));
      parameters.add(SQLFormatter.sqlID(message.id));
      query = "UPDATE MESSAGES SET" +
          " USERID = ?," +
          " MNEXTID = ?," +
          " MPREVID = ?," +
          " TimeCreated = ?," +
          " MESSAGE = ?" +
          " where ID = ?;";

      dbConnection.dbUpdate(parameters, query);
      LOG.info(
          "updateMessage success (message.id=%s message.time=%s)",
          message.id,
          message.creation);
    } catch (Exception e) {
      LOG.info(
          "updateMessage fail - Database update error (message.id=%s message.time=%s)",
          message.id,
          message.creation);
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public Collection<Message> messageById(Vector<String> parameters, String where, boolean orderASC) {
    String query = "SELECT * FROM MESSAGES";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY ID ASC";
    else
      query += " ORDER BY ID DESC";
    query += ";";
    return dbConnection.dbQueryMessages(parameters, query);
  }

  public Collection<Message> messageByTime(Vector<String> parameters, String where, boolean orderASC) {
    String query = "SELECT * FROM MESSAGES";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY TimeCreated ASC";
    else
      query += " ORDER BY TimeCreated DESC";
    query += ";";
    return dbConnection.dbQueryMessages(parameters, query);
  }

  public Collection<Message> messageByText(Vector<String> parameters, String where, boolean orderASC) {
    String query = "SELECT * FROM MESSAGES";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY MESSAGE ASC";
    else
      query += " ORDER BY MESSAGE DESC";
    query += ";";
    return dbConnection.dbQueryMessages(parameters, query);
  }

  public Uuid conversationID(Uuid message) {
    Vector<String> parameters = new Vector<>();
    parameters.add(SQLFormatter.sqlID(message));
    String query = "SELECT CONVERSATIONID FROM MESSAGES WHERE ID = ?;";
    return dbConnection.getConversationID(parameters, query);
  }
}
