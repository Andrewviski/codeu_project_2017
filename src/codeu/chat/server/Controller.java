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

import codeu.chat.codeU_db.DataBaseConnection;
import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.RandomUuidGenerator;
import codeu.chat.common.RawController;
import codeu.chat.common.User;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;

import javax.swing.plaf.nimbus.State;
import java.sql.*;
import java.util.Date;
import java.text.SimpleDateFormat;

import codeu.chat.common.SQLFormatter;

public final class Controller implements RawController, BasicController {

  private final static Logger.Log LOG = Logger.newLog(Controller.class);

  private final Model model;
  private final DataBaseConnection connection = new DataBaseConnection();
  private final Uuid.Generator uuidGenerator;

  public Controller(Uuid serverId, Model model) {
    this.model = model;
    this.uuidGenerator = new RandomUuidGenerator(serverId, System.currentTimeMillis());
    if (this.model.userByText("UNAME = 'Admin'", null).isEmpty())
      this.model.add(new User(createId(), "Admin", Time.now(), "admin"));
  }

  @Override
  public Message newMessage(Uuid author, Uuid conversation, String body) {
    return newMessage(createId(), author, conversation, body, Time.now());
  }

  @Override
  public User newUser(String name, String password) {
    return newUser(createId(), name, Time.now(), password);
  }

  @Override
  public Conversation newConversation(String title, Uuid owner) {
    return newConversation(createId(), title, owner, Time.now());
  }

  @Override
  public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

    Collection<Message> prevMessages = model.messageByTime("MNEXTID == '0' AND CONVERSATIONID = " + SQLFormatter.sqlID(conversation), "DESC");

    Message message = null;
    Message prevMessage = null;

    if (!prevMessages.isEmpty()) {
      prevMessage = prevMessages.iterator().next();
      prevMessage.next = id;
      model.update(prevMessage);
      message = new Message(id, Uuid.NULL, prevMessage.id, creationTime, author, body);
      model.add(message, conversation);
    } else {
      message = new Message(id, Uuid.NULL, Uuid.NULL, creationTime, author, body);
      model.add(message, conversation);
    }
    return message;
  }

  @Override
  public User newUser(Uuid id, String name, Time creationTime, String password) {

    User user = null;

    if (isIdFree(id)) {

      user = new User(id, name, creationTime, password);
      model.add(user);

      LOG.info(
          "newUser success (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);

    } else {

      LOG.info(
          "newUser fail - id in use (user.id=%s user.name=%s user.time=%s)",
          id,
          name,
          creationTime);
    }

    return user;
  }

  @Override
  public Conversation newConversation(Uuid id, String title, Uuid owner, Time creationTime) {

    Conversation conversation = null;

    if (isIdFree(id)) {
      conversation = new Conversation(id, owner, creationTime, title);
      model.add(conversation);

      LOG.info("Conversation added: " + conversation.id);
    }

    return conversation;
  }

  private Uuid createId() {

    Uuid candidate;
    for (candidate = uuidGenerator.make();
         isIdInUse(candidate);
         candidate = uuidGenerator.make()) {

      // Assuming that "randomUuid" is actually well implemented, this
      // loop should never be needed, but just incase make sure that the
      // Uuid is not actually in use before returning it.

    }

    return candidate;
  }

  private boolean isIdInUse(Uuid id) {
    String findID = "ID = " + SQLFormatter.sqlID(id);
    return !model.messageById(findID, null).isEmpty() ||
        !model.conversationById(findID, null).isEmpty() ||
        !model.userById(findID, null).isEmpty();
  }

  private boolean isIdFree(Uuid id) {
    return !isIdInUse(id);
  }

}
