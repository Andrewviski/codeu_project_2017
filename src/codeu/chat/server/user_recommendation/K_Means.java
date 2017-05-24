package codeu.chat.server.user_recommendation;

import codeu.chat.common.Message;
import codeu.chat.common.SQLFormatter;
import codeu.chat.common.User;
import codeu.chat.util.Uuid;
import codeu.chat.server.user_recommendation.UserFeatures;
import codeu.chat.server.user_recommendation.Cluster;
import codeu.chat.server.user_recommendation.Mood;
import codeu.chat.server.Model;

import java.io.IOException;
import java.util.*;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

/**
 * Created by strobe on 23/05/17.
 */
public class K_Means{

    Vector<UserFeatures> userVector;
    Vector<Cluster> clusters;

    private final Model model;
    private MaxentTagger tagger;

    K_Means(Model model) throws IOException,
            ClassNotFoundException{

        this.model = model;
        InitializeTagger();
    }

    private String Tagger(String sentence) throws IOException,
            ClassNotFoundException {

        return tagger.tagString(sentence);
    }

    private void InitializeTagger() throws IOException,
            ClassNotFoundException{
        tagger = new MaxentTagger("models/left3words-wsj-0-18.tagger");
    }

    private void InitializeClusters() {

    }

    private void InitializeUserVector() throws IOException,
        ClassNotFoundException{

        Iterator<UserFeatures> iterator;
        Collection<User> users = model.userById(null, null);
        Collection<Message> messages;

        for(User user : users) {
            UserFeatures features = new UserFeatures(user.id);
            features.setInterests(InitializeUserInterests(user.id));
            userVector.add(features);
        }
    }

    private NavigableMap<String, Mood> InitializeUserInterests(Uuid user) throws IOException,
            ClassNotFoundException{

        NavigableMap<String, Mood> interests = new TreeMap<>();

        Collection<Message> messages = model.messageByTime("USERID = " + SQLFormatter.sqlID(user), "ASC");

        for(Message message : messages) {
            Mood mood = new Mood();
            //Assign value with Austin's Algorithm based on message.content
            boolean validWord;
            String taggedMessage = Tagger(message.content);

            for(String word : taggedMessage.split("\\s+")) {
                validWord = false;
                if(word.contains("/NN")) {
                    word.replace("/NN", "");
                    validWord = true;
                }
                else if(word.contains("/NNS")) {
                    word.replace("/NNS", "");
                    validWord = true;
                }
                else if(word.contains("/NNP")) {
                    word.replace("/NNP", "");
                    validWord = true;
                }
                else if(word.contains("/NNPS")) {
                    word.replace("/NNPS", "");
                    validWord =true;
                }

                if(validWord) {
                    if(interests.get(word) == null) {
                        interests.put(word, mood);
                    }
                    else {
                        Mood temp = interests.get(word);
                        temp.add(mood);
                        interests.put(word, mood);
                    }
                }
            }
        }
        return interests;
    }

    public void runClusterer() {

    }
}
