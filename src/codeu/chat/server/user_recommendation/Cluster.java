package codeu.chat.server.user_recommendation;

import java.util.Collection;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * Created by strobe on 23/05/17.
 */
public class Cluster {
  private NavigableMap<String, Mood> interests;
  private int cluster;

  public Cluster(int cluster) {
    this.cluster = cluster;
    interests = new TreeMap<>();
  }

  public Cluster(int cluster, Collection<String> keyWords) {
    this.cluster = cluster;
    interests = new TreeMap<>();
    for (String keyWord : keyWords) {
      interests.put(keyWord, null);
    }
  }

  public void initialize(UserFeatures baseUser) {
    for (NavigableMap.Entry<String, Mood> entry : baseUser.getInterests().entrySet()) {
      interests.put(entry.getKey(), entry.getValue());
    }
  }

  public Mood getMood(String keyWord) {
    return interests.get(keyWord);
  }

  public void setEntry(String keyWord, Mood value) {
    interests.put(keyWord, value);
  }

  public int getCluster() {
    return cluster;
  }
}
