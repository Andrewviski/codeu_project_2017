package codeu.chat.server.user_recommendation;

import java.util.Collection;
import java.util.NavigableMap;

/**
 * Created by strobe on 23/05/17.
 */
public class Cluster {
    private NavigableMap<String, Mood> interests;

    Cluster(Collection<String > keyWords) {
        for(String keyWord : keyWords) {
            interests.put(keyWord, null);
        }
    }

    public void Initialize(UserFeatures baseUser) {
        for(NavigableMap.Entry<String, Mood> entry : baseUser.getInterests().entrySet()) {
            interests.put(entry.getKey(), entry.getValue());
        }
    }

    public void updateEntry(String keyWord, Mood value) {
        interests.put(keyWord, value);
    }
}
