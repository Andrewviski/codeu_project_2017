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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import codeu.chat.common.*;
import codeu.chat.util.Logger;
import codeu.chat.util.Time;
import codeu.chat.util.Uuid;
import codeu.chat.util.store.StoreAccessor;

public final class View implements BasicView, LogicalView, SinglesView {

  private final static Logger.Log LOG = Logger.newLog(View.class);

  private final Model model;

  public View(Model model) {
    this.model = model;
  }


  @Override
  public Collection<User> getUsers(Collection<Uuid> ids) {
    return model.userById(ids, false, true);
  }

  @Override
  public Collection<ConversationSummary> getAllConversations() {

    final Collection<ConversationSummary> summaries = new ArrayList<>();

    for (final Conversation conversation : model.getAllConversations(Uuid.NULL,true)) {
      summaries.add(conversation.summary);
    }

    return summaries;

  }

  @Override
  public Collection<Conversation> getConversations(Collection<Uuid> ids) {
    return model.conversationById(ids, false, true);
  }

  @Override
  public Collection<Message> getMessages(Collection<Uuid> ids) {
    return model.messageByTime(ids, false, true);
  }

  @Override
  public Uuid getUserGeneration() {
    return model.userGeneration();
  }

  @Override
  public Collection<User> getUsersExcluding(Collection<Uuid> ids) {
    return model.userById(ids, true, true);
  }

  @Override
  public Collection<Conversation> getConversations(Time start, Time end) {

    final Collection<Conversation> conversations = model.getConversationsInRange(start, end, true);

    return conversations;

  }

  @Override
  public Collection<Conversation> getConversations(String filter) {

    final Collection<Conversation> found = model.getConversationsByFilter(filter, true);

    return found;
  }

  @Override
  public Collection<Message> getMessages(Uuid conversation, Time start, Time end) {

    final Collection<Message> foundMessages = model.getMessagesInRange(conversation, start, end, true);

    return foundMessages;
  }

  @Override
  public Collection<Message> getMessages(Uuid rootMessage, int range) {

    int remaining = Math.abs(range);
    LOG.info("in getMessage: UUID=%s range=%d", rootMessage, range);

    // We want to return the messages in order. If the range was negative
    // the messages would be backwards. Use a linked list as it supports
    // adding at the front and adding at the end.

    final LinkedList<Message> found = new LinkedList<>();

    Uuid conversation = model.conversationID(rootMessage);

    final Collection<Message> allMessages = model.getAllMessagesInConversation(conversation, true);

    // i <= remaining : must be "<=" and not just "<" or else "range = 0" would
    // return nothing and we want it to return just the root because the description
    // is that the function will return "range" around the root. Zero messages
    // around the root means that it should just return the root.

    Message current = null;
    boolean foundRoot = false;
    int countMessages = 0;

    for (Iterator<Message> iterator = allMessages.iterator(); iterator.hasNext() && countMessages <= remaining; ) {
      current = iterator.next();
      if (current.id.equals(rootMessage)) {
        foundRoot = true;
      }
      if (foundRoot) {
        if (range > 0) {
          found.addLast(current);
        } else {
          found.addFirst(current);
        }
      }
    }

    return found;
  }

  @Override
  public User findUser(Uuid id) {
    return model.getSingleUser(id);
  }

  @Override
  public Conversation findConversation(Uuid id) {
    return model.getSingleConversation(id);
  }

  @Override
  public Message findMessage(Uuid id) {
    return model.getSingleMessage(id);
  }
}