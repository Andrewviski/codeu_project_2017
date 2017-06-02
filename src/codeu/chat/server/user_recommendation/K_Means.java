package codeu.chat.server.user_recommendation;

import codeu.chat.common.Message;
import codeu.chat.common.SQLFormatter;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;
import codeu.chat.server.user_recommendation.UserFeatures;
import codeu.chat.server.user_recommendation.Cluster;
import codeu.chat.server.user_recommendation.Mood;
import codeu.chat.server.user_recommendation.moodClassifier;
import codeu.chat.server.Model;

import java.io.*;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

import codeu.chat.util.connections.Connection;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSSample;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.tokenize.WhitespaceTokenizer;

/**
 * Created by strobe on 23/05/17.
 */
public class K_Means {

  private Vector<UserFeatures> userVector;
  private Vector<Cluster> clusterVector;
  private Collection<String> allKeyWords;

  private final Model model;
  private moodClassifier classifier;

  private boolean isModelTrained;

  public K_Means(Model model) {

    this.model = model;
    this.classifier = new moodClassifier();
    this.isModelTrained = false;
  }

  private String tagger(String sentence) throws Exception{
    String taggedSentence = null;
    InputStream inputStream = new FileInputStream("./bin/codeu/chat/server/user_recommendation/tools/en-pos-maxent.bin");
    POSModel posModel = new POSModel(inputStream);

    POSTaggerME tagger = new POSTaggerME(posModel);

    WhitespaceTokenizer whitespaceTokenizer = WhitespaceTokenizer.INSTANCE;
    String[] tokens = whitespaceTokenizer.tokenize(sentence);

    String[] tags = tagger.tag(tokens);

    POSSample sample = new POSSample(tokens, tags);

    taggedSentence = sample.toString();

    return taggedSentence;
  }

  private Set<Integer> randomIndexes(int requiredIDX, int possibleUsers) {
    Random rng = new Random();
    Set<Integer> generated = new HashSet<>();
    while (generated.size() < requiredIDX)
    {
      Integer next = rng.nextInt(possibleUsers);
      generated.add(next);
    }

    return generated;
  }

  private void initializeClusters() {
    clusterVector = new Vector<>();
    allKeyWords = new HashSet<>();
    Cluster cluster;
    int numUsers = userVector.size();
    int counter = 0;
    int numClusters = (int) (Math.sqrt(numUsers));

    //Change for a more efficient way to get all the key words found in messages
    Iterator<UserFeatures> iterator = userVector.iterator();
    while (iterator.hasNext()) {
      allKeyWords.addAll(iterator.next().getInterestsList());
    }

    //Initialize the clusters with random userVectors
    for (int idx : randomIndexes(numClusters, numUsers)) {
      cluster = new Cluster(counter);
      cluster.initialize(userVector.get(idx));
      clusterVector.add(cluster);
      counter++;
    }
  }

  private void initializeUserVector() {
    Collection<Uuid> adminID = new ArrayList<>();
    adminID.add(model.getAdmin().id);

    userVector = new Vector<>();
    Collection<User> users = model.userById(adminID, true);

    for (User user : users) {
      UserFeatures features = new UserFeatures(user.id);
      features.setInterests(initializeUserInterests(user.id));
      userVector.add(features);
    }
  }

  private NavigableMap<String, Mood> initializeUserInterests(Uuid user) {

    NavigableMap<String, Mood> interests = new TreeMap<>();

    Collection<Message> messages = model.getUserMessages(user);

    for (Message message : messages) {
      //Assign value with Austin's Algorithm based on message.content
      Mood mood = new Mood(classifier.classifyTweet(message.content.split("\\s+")));
      boolean validWord;
      try {
        String taggedMessage = tagger(message.content);
        for (String word : taggedMessage.split("\\s+")) {
          validWord = false;
          if (word.contains("_NNS")) {
            word = word.replace("_NNS", "");
            validWord = true;
          } else if (word.contains("_NNP")) {
            word = word.replace("_NNP", "");
            validWord = true;
          } else if (word.contains("_NN")) {
            word = word.replace("_NN", "");
            validWord = true;
          }

          if (validWord) {
            if (interests.get(word) == null) {
              interests.put(word, mood);
            } else {
              Mood temp = interests.get(word);
              temp.add(mood);
              interests.put(word, temp);
            }
          }
        }
      } catch (Exception e) {
        System.out.println("Error Running the tagger");
      }
    }
    return interests;
  }

  private double distance(UserFeatures user, Cluster cluster) {
    double distance = 0;
    for (String keyWord : allKeyWords) {
      distance += Mood.distance(user.getMood(keyWord), cluster.getMood(keyWord));
    }
    return Math.sqrt(distance);
  }

  private void clusterAssignment() {
    int assignedCluster;
    double minDistance;
    for (UserFeatures user : userVector) {
      assignedCluster = -1;
      minDistance = Double.MAX_VALUE;
      for (Cluster cluster : clusterVector) {
        double newDistance = distance(user, cluster);
        if (newDistance < minDistance) {
          minDistance = newDistance;
          assignedCluster = cluster.getCluster();
        }
      }
      user.setCluster(assignedCluster);
    }
  }

  private void moveCentroids() {
    int numClusters = clusterVector.size();
    Mood sums[] = new Mood[numClusters];
    int counter[] = new int[numClusters];

    for (String keyWord : allKeyWords) {

      for (int i = 0; i < numClusters; i++) {
        sums[i] = new Mood();
        counter[i] = 0;
      }

      for (UserFeatures user : userVector) {
        sums[user.getCluster()].add(user.getMood(keyWord));
        counter[user.getCluster()] = counter[user.getCluster()] + 1;
      }

      for (int i = 0; i < numClusters; i++) {
        Mood avg = Mood.average(sums[i], counter[i]);
        clusterVector.get(i).setEntry(keyWord, avg);
      }
    }
  }

  public boolean runClusterer(int iterations) {
    System.out.println("Running Clusterer");
    if(!isModelTrained) {
      System.out.println("Training Model");
      classifier.trainModel();
      isModelTrained = true;
    }
    initializeUserVector();
    initializeClusters();

    for (int i = 0; i < iterations; i++) {
      clusterAssignment();
      moveCentroids();
    }

    for(UserFeatures userFeatures : userVector) {
      model.assignUserToCluster(userFeatures.userID, userFeatures.getCluster());
    }

    return true;
  }
}
