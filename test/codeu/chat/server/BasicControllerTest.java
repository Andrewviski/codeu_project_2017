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

import codeu.chat.util.Time;
import org.junit.Test;
import org.junit.Before;

import codeu.chat.common.BasicController;
import codeu.chat.common.Conversation;
import codeu.chat.common.Message;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;

import java.io.IOException;

public final class BasicControllerTest {

  private Model model;
  private BasicController controller;

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
  }

  @Test
  public void testAddUser() {

    final User admin = createAdmin();
    final User user = controller.newUser(admin, "user", "password");

    assertFalse(
        "Check that user has a valid reference",
        user == null);

  }

  @Test
  public void testAddConversation() {

    final User admin = createAdmin();
    final User user = controller.newUser(admin, "user", "password");

    assertFalse(
        "Check that user has a valid reference",
        user == null);

    final Conversation conversation = controller.newConversation(
        "conversation",
        user.id);

    assertFalse(
        "Check that conversation has a valid reference",
        conversation == null);
  }

  @Test
  public void testAddUserToConversation() {

    final User admin = createAdmin();
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

    final User user = controller.newUser(admin, "user", "password");

    assertFalse(
        "Check that user has a valid reference",
        user == null);

    final boolean success = controller.addUserToConversation(
        owner.id,
        user.id,
        conversation.id
    );

    assertFalse(
        "Check that conversation and user have a valid reference",
        success == false);
  }

  @Test
  public void testAddMessage() {

    final User admin = createAdmin();
    final User user = controller.newUser(admin, "user", "password");

    assertFalse(
        "Check that user has a valid reference",
        user == null);

    final Conversation conversation = controller.newConversation(
        "conversation",
        user.id);

    assertFalse(
        "Check that conversation has a valid reference",
        conversation == null);

    final Message message = controller.newMessage(
        user.id,
        conversation.id,
        "Hello World");

    assertFalse(
        "Check that the message has a valid reference",
        message == null);
  }
}
