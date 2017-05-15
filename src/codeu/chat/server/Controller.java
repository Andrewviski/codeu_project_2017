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
        newUser("Admin", "admin");
        if (this.model.userByText().at("Admin") == null)
            this.model.add(newUser(createId(), "Admin", Time.now(), "admin"));
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

    private void executeUpdate(Connection connection, String sql) {
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(sql);
            stmt.close();
            connection.commit();
        } catch (Exception e) {
            System.out.println("Error adding message to conversation");
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }

    @Override
    public Message newMessage(Uuid id, Uuid author, Uuid conversation, String body, Time creationTime) {

        final User foundUser = model.userById().first(author);
        final Conversation foundConversation = model.conversationById().first(conversation);

        Collection<Message> prevMessages = model.messageById("MNEXTID IS NULL AND CONVERSATIONID = " +SQLFormatter.sqlID(conversation), null);

        Message message = null;
        Message prevMessage = null;
        Message updatePrevMessage = null;

        String prevMsgID = "";

        if(!prevMessages.isEmpty()) {
            prevMessage = prevMessages.iterator().next();
            updatePrevMessage = new Message(prevMessage.id, id, prevMessage.previous, prevMessage.creation, prevMessage.author, prevMessage.content);
            prevMsgID = prevMessage.id.toString();
        }

        if (!prevMsgID.equals("")) {
            model.update(updatePrevMessage);
            message = new Message(id, prevMessage.id, null, creationTime, author, body);
            model.add(message, conversation);
        }
        else {
            System.out.println("Expected Path");
            message = new Message(id, null, null, creationTime, author, body);
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
        System.out.println("Not Looping");
        for (candidate = uuidGenerator.make();
             isIdInUse(candidate);
             candidate = uuidGenerator.make()) {

            System.out.println(candidate.toString());

            // Assuming that "randomUuid" is actually well implemented, this
            // loop should never be needed, but just incase make sure that the
            // Uuid is not actually in use before returning it.

        }

        return candidate;
    }

    private boolean isIdInUse(Uuid id) {
        System.out.println("isIdInUse");
        System.out.println(!model.messageById("ID = " + SQLFormatter.sqlID(id), null).isEmpty() ||
                model.conversationById().first(id) != null ||
                model.userById().first(id) != null);
        return !model.messageById("ID = " + SQLFormatter.sqlID(id), null).isEmpty() ||
                model.conversationById().first(id) != null ||
                model.userById().first(id) != null;
    }

    private boolean isIdFree(Uuid id) {
        System.out.println("isIdFree");
        return !isIdInUse(id);
    }

}
