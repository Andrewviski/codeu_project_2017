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

import static org.junit.Assert.*;

import codeu.chat.server.user_recommendation.K_Means;
import codeu.chat.util.Time;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

import java.io.IOException;

public final class user_recommendationTest {

  private Model model;
  private BasicController controller;
  private K_Means clusterer;

  private int iterations = 10;
  private User admin;

  private User createAdmin() {
    User admin = null;

    try {
      admin = new User(Uuid.parse("100.0000000000"), "Admin", Time.fromMs(0), "admin");
    } catch (IOException ex) {    }

    return admin;
  }

  @Before
  public void doBefore() {
    model = new Model();
    controller = new Controller(Uuid.NULL, model);
    clusterer = new K_Means(model);
    admin = createAdmin();
  }

  @Test
  public void testRunClusterer() {

    final User owner = controller.newUser(admin, "owner", "password");

    assertFalse(
        "Check that owner has a valid reference",
        owner == null);

    final Conversation conversation = controller.newConversation(
        "conversation",
        owner.id);

    assertFalse(
        "Check that conversation has a valid reference",
        conversation == null);

    final User user1 = controller.newUser(admin, "user1", "password");

    assertFalse(
        "Check that user has a valid reference",
        user1 == null);

    final User user2 = controller.newUser(admin, "user2", "password");

    assertFalse(
        "Check that user has a valid reference",
        user2 == null);

    final User user3 = controller.newUser(admin, "user3", "password");

    assertFalse(
        "Check that user has a valid reference",
        user3 == null);

    final User user4 = controller.newUser(admin, "user4", "password");

    assertFalse(
        "Check that user has a valid reference",
        user4 == null);

    final boolean success1 = controller.addUserToConversation(
        owner.id,
        user1.id,
        conversation.id
    );

    assertFalse(
        "Check that conversation and user have a valid reference",
        success1 == false);

    final boolean success2 = controller.addUserToConversation(
        owner.id,
        user2.id,
        conversation.id
    );

    assertFalse(
        "Check that conversation and user have a valid reference",
        success2 == false);

    final boolean success3 = controller.addUserToConversation(
        owner.id,
        user3.id,
        conversation.id
    );

    assertFalse(
        "Check that conversation and user have a valid reference",
        success3 == false);

    final boolean success4 = controller.addUserToConversation(
        owner.id,
        user4.id,
        conversation.id
    );

    assertFalse(
        "Check that conversation and user have a valid reference",
        success4 == false);

    final Message message1 = controller.newMessage(
        user1.id,
        conversation.id,
        "I love Hello World");

    assertFalse(
        "Check that the message has a valid reference",
        message1 == null);

    final Message message2 = controller.newMessage(
        user2.id,
        conversation.id,
        "I love Hello World");

    assertFalse(
        "Check that the message has a valid reference",
        message2 == null);

    final Message message3 = controller.newMessage(
        user3.id,
        conversation.id,
        "I hate Hello World");

    assertFalse(
        "Check that the message has a valid reference",
        message3 == null);

    final Message message4 = controller.newMessage(
        user4.id,
        conversation.id,
        "I hate Hello World");

    assertFalse(
        "Check that the message has a valid reference",
        message4 == null);

    final boolean success = clusterer.runClusterer(iterations, admin.id);

    assertFalse(
        "Check that cluster was executed successfully",
        success == false);

  }
}
