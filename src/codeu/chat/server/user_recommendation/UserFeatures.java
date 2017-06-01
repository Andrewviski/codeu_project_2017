package codeu.chat.server.user_recommendation;

import java.util.Collection;
import java.util.HashSet;
import java.util.NavigableMap;
import java.util.TreeMap;

import codeu.chat.util.Uuid;

/**
 * Created by strobe on 23/05/17.
 */
public class UserFeatures {
  Uuid userID;
  int cluster;
  private NavigableMap<String, Mood> interests;

  public UserFeatures(Uuid userID) {
    this.userID = userID;
    cluster = -1;
    interests = new TreeMap<>();
  }

  public void SetMood(String keyWord, Mood values) {
    if (interests.get(keyWord) == null) {
      interests.put(keyWord, values);
    } else {
      interests.get(keyWord).add(values);
    }
  }

  public Mood getMood(String keyWord) {
    return interests.get(keyWord);
  }

  public void setInterests(NavigableMap<String, Mood> interests) {
    this.interests = interests;
  }

  public NavigableMap<String, Mood> getInterests() {
    return interests;
  }

  public Collection<String> getInterestsList() {
    Collection<String> keyWords = new HashSet<>();
    keyWords.addAll(interests.keySet());

    return keyWords;
  }

  public void setCluster(int cluster) {
    this.cluster = cluster;
  }

  public int getCluster() {
    return cluster;
  }
}
