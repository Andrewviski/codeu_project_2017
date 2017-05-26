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

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;

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

  public static String intersect(Collection<Uuid> ids, boolean isBlacklist) {

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
      found += "ID " + operator + " " + SQLFormatter.sqlID(nextID);
      if (id.hasNext()) {
        found += " OR ";
      }
    }
    return found;
  }

  public User getAdmin() {
    Collection<User> returnUser = userById("UNAME = 'Admin'", true);
    if(!returnUser.isEmpty()){
      return returnUser.iterator().next();
    }
    else {
      return null;
    }
  }

  public User getSingleUser(Uuid userID) {
    Collection<User> returnUser = userById("ID = " + SQLFormatter.sqlID(userID), true);
    if(!returnUser.isEmpty()){
      return returnUser.iterator().next();
    }
    else {
      return null;
    }
  }



  public Collection<User> getAllUsers(Uuid conversation, boolean orderASC) {
    if(!conversation.equals(Uuid.NULL)) {
      Collection<Uuid> usersIDs = dbConnection.getUsersInConversations("SELECT * FROM USER_CONVERSATION where CONVERSATIONID = " + SQLFormatter.sqlID(conversation) + ";");
      return userById(intersect(usersIDs, false), true);
    }
    else {
      return userById(null, true);
    }
  }

  public Conversation getSingleConversation(Uuid conversationID) {
    Collection<Conversation> returnConversation = conversationById("ID = " + SQLFormatter.sqlID(conversationID), true);
    if(!returnConversation.isEmpty()){
      return returnConversation.iterator().next();
    }
    else {
      return null;
    }
  }

  public Collection<Conversation> getAllConversations(Uuid user, boolean orderASC) {
    if(!user.equals(Uuid.NULL)) {
      Collection<Uuid> conversationsIDs = dbConnection.getUsersInConversations("SELECT * FROM USER_CONVERSATION where USERID = " + SQLFormatter.sqlID(user) + ";");
      return conversationById(intersect(conversationsIDs, false), orderASC);
    }
    else {
      return conversationById(null, orderASC);
    }
  }

  public Collection<Conversation> getConversationsInRange(Time start, Time end, boolean orderASC) {
    return conversationByTime("TimeCreated >= " + SQLFormatter.sqlCreationTime(start) + " AND TimeCreated <= " + SQLFormatter.sqlCreationTime(end), orderASC);
  }

  public Collection<Conversation> getConversationsByFilter(String filter, boolean orderASC) {
    return conversationByText("CNAME LIKE " + SQLFormatter.sqlContainsText(filter), orderASC);
  }

  public Message getSingleMessage(Uuid messageID) {
    Collection<Message> returnMessage = messageById("ID = " + SQLFormatter.sqlID(messageID), true);
    if(!returnMessage.isEmpty()){
      return returnMessage.iterator().next();
    }
    else {
      return null;
    }
  }

  public Message getLastMessage(Uuid conversationID) {
    Collection<Message> lastMessage = messageById("MNEXTID == '0' AND CONVERSATIONID = " + SQLFormatter.sqlID(conversationID), false);
    if(!lastMessage.isEmpty()){
      return lastMessage.iterator().next();
    }
    else {
      return null;
    }
  }

  public Collection<Message> getMessagesInRange(Uuid conversation, Time start, Time end, boolean orderASC) {
    return messageByTime("CONVERSATIONID = " + SQLFormatter.sqlID(conversation) + " AND TimeCreated >= " + SQLFormatter.sqlCreationTime(start) + " AND TimeCreated <= " + SQLFormatter.sqlCreationTime(end), orderASC);
  }

  public void add(User user) {

    currentUserGeneration = userGenerations.make();

    try {
      dbConnection.dbUpdate("INSERT INTO USERS (ID,UNAME,TIMECREATED,PASSWORD) " +
          "VALUES (" + SQLFormatter.sqlID(user.id) + ", " + SQLFormatter.sqlName(user.name) + ", " +
          SQLFormatter.sqlCreationTime(user.creation) + ", " + SQLFormatter.sqlPassword(user.password) + ");");
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
    return messageByTime("CONVERSATIONID = " + SQLFormatter.sqlID(conversation), orderASC);
  }

  public Collection<Message> getAllMessagesFromUser(Uuid user, boolean orderASC) {
    return messageByTime("USERID = " + SQLFormatter.sqlID(user), orderASC);
  }

  public void update(User user) {
    try {
      dbConnection.dbUpdate("UPDATE USERS set" +
          " UNAME = " + SQLFormatter.sqlName(user.name) + ", " +
          " TimeCreated = " + SQLFormatter.sqlCreationTime(user.creation) + ", " +
          " UNAME = " + SQLFormatter.sqlName(user.password) +
          " where ID = " + SQLFormatter.sqlID(user.id) +
          ";");
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

  public Collection<User> userById(String where, boolean orderASC) {
    String query = "SELECT * FROM USERS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY ID ASC";
    else
      query += " ORDER BY ID DESC";
    query += ";";
    return dbConnection.dbQueryUsers(query);
  }

  public Collection<User> userByTime(String where, boolean orderASC) {
    String query = "SELECT * FROM USERS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY TimeCreated ASC";
    else
      query += " ORDER BY TimeCreated DESC";
    query += ";";
    return dbConnection.dbQueryUsers(query);
  }

  public Collection<User> userByText(String where, boolean orderASC) {
    String query = "SELECT * FROM USERS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY UNAME ASC";
    else
      query += " ORDER BY UNAME DESC";
    query += ";";
    return dbConnection.dbQueryUsers(query);
  }

  public Uuid userGeneration() {
    return currentUserGeneration;
  }

  public void add(Conversation conversation) {

    try {
      dbConnection.dbUpdate("INSERT INTO CONVERSATIONS (ID,CNAME,OWNERID,TimeCreated) " +
          "VALUES (" + SQLFormatter.sqlID(conversation.id) + ", " + SQLFormatter.sqlName(conversation.title) + ", " +
          SQLFormatter.sqlID(conversation.owner) + ", " + SQLFormatter.sqlCreationTime(conversation.creation) + ");");

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
    try {
      dbConnection.dbUpdate("INSERT INTO USER_CONVERSATION (ID, USERID, CONVERSATIONID) " +
          "VALUES (" + SQLFormatter.sqlID(user, conversation) + ", " + SQLFormatter.sqlID(user) + ", " + SQLFormatter.sqlID(conversation) + ");");
    } catch (Exception e) {
      LOG.info(
          "Adding user to conversation fail - Verify connection and try again shortly");
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public void update(Conversation conversation) {
    try {
      dbConnection.dbUpdate("UPDATE CONVERSATIONS set" +
          " CNAME = " + SQLFormatter.sqlName(conversation.title) + ", " +
          " OWNERID = " + SQLFormatter.sqlID(conversation.owner) + ", " +
          " TimeCreated = " + SQLFormatter.sqlCreationTime(conversation.creation) +
          " where ID = " + SQLFormatter.sqlID(conversation.id) +
          ";");
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

  public Collection<Conversation> conversationById(String where, boolean orderASC) {

    Collection<Conversation> conversations = new HashSet<>();

    String query = "SELECT * FROM CONVERSATIONS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY ID ASC";
    else
      query += " ORDER BY ID DESC";
    query += ";";

    Collection<Conversation> found = dbConnection.dbQueryConversations(query);

    for (Conversation conv : found) {
      conv.firstMessage = dbConnection.getConversationData("SELECT * FROM MESSAGES where CONVERSATIONID = " + SQLFormatter.sqlID(conv.id) + " AND MPREVID = " + SQLFormatter.sqlID(Uuid.NULL) + ";");
      if (conv.firstMessage == null)
        conv.firstMessage = Uuid.NULL;
      conv.lastMessage = dbConnection.getConversationData("SELECT * FROM MESSAGES where CONVERSATIONID = " + SQLFormatter.sqlID(conv.id) + " AND MNEXTID = " + SQLFormatter.sqlID(Uuid.NULL) + ";");
      conv.users = dbConnection.getUsersInConversations("SELECT * FROM USER_CONVERSATION where CONVERSATIONID = " + SQLFormatter.sqlID(conv.id) + ";");

      conversations.add(conv);
    }

    return conversations;
  }

  public Collection<Conversation> conversationByTime(String where, boolean orderASC) {
    String query = "SELECT * FROM CONVERSATIONS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY TimeCreated ASC";
    else
      query += " ORDER BY TimeCreated DESC";
    query += ";";
    return dbConnection.dbQueryConversations(query);
  }

  public Collection<Conversation> conversationByText(String where, boolean orderASC) {
    String query = "SELECT * FROM CONVERSATIONS";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY CNAME ASC";
    else
      query += " ORDER BY CNAME DESC";
    query += ";";
    return dbConnection.dbQueryConversations(query);
  }

  public void add(Message message, Uuid conversation) {
    try {
      dbConnection.dbUpdate("INSERT INTO MESSAGES (ID,USERID,MNEXTID,MPREVID,CONVERSATIONID,TimeCreated,MESSAGE) " +
          "VALUES (" + SQLFormatter.sqlID(message.id) + ", " +
          SQLFormatter.sqlID(message.author) + ", " +
          SQLFormatter.sqlID(message.next) + ", " +
          SQLFormatter.sqlID(message.previous) + ", " +
          SQLFormatter.sqlID(conversation) + ", " +
          SQLFormatter.sqlCreationTime(message.creation) + ", " +
          SQLFormatter.sqlBody(message.content) + ");");

      LOG.info("Message added: " + message.id);
    } catch (Exception e) {
      LOG.info(
          "newMessage fail - Verify connection and try again shortly");
      System.err.println(e.getClass().getName() + ": " + e.getMessage());
      System.exit(0);
    }
  }

  public void update(Message message) {
    try {
      dbConnection.dbUpdate("UPDATE MESSAGES SET" +
          " USERID = " + SQLFormatter.sqlID(message.author) + "," +
          " MNEXTID = " + SQLFormatter.sqlID(message.next) + "," +
          " MPREVID = " + SQLFormatter.sqlID(message.previous) + "," +
          " TimeCreated = " + SQLFormatter.sqlCreationTime(message.creation) + "," +
          " MESSAGE = " + SQLFormatter.sqlBody(message.content) +
          " where ID = " + SQLFormatter.sqlID(message.id) +
          ";");
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

  public Collection<Message> messageById(String where, boolean orderASC) {
    String query = "SELECT * FROM MESSAGES";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY ID ASC";
    else
      query += " ORDER BY ID DESC";
    query += ";";
    return dbConnection.dbQueryMessages(query);
  }

  public Collection<Message> messageByTime(String where, boolean orderASC) {
    String query = "SELECT * FROM MESSAGES";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY TimeCreated ASC";
    else
      query += " ORDER BY TimeCreated DESC";
    query += ";";
    return dbConnection.dbQueryMessages(query);
  }

  public Collection<Message> messageByText(String where, boolean orderASC) {
    String query = "SELECT * FROM MESSAGES";
    if (where != null)
      query += " where " + where;
    if (orderASC)
      query += " ORDER BY MESSAGE ASC";
    else
      query += " ORDER BY MESSAGE DESC";
    query += ";";
    return dbConnection.dbQueryMessages(query);
  }

  public Uuid conversationID(Uuid message) {
    String query = "SELECT CONVERSATIONID FROM MESSAGES WHERE ID = " + SQLFormatter.sqlID(message) + ";";
    return dbConnection.getConversationID(query);
  }
}
