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
import java.lang.reflect.Array;
import java.util.*;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import javax.swing.text.html.HTMLDocument;

/**
 * Created by strobe on 23/05/17.
 */
public class K_Means{

    private Vector<UserFeatures> userVector;
    private Vector<Cluster> clusterVector;
    private Collection<String> allKeyWords;

    private final Model model;
    private MaxentTagger tagger;

    public K_Means(Model model) throws IOException,
            ClassNotFoundException{

        this.model = model;
        //Set the Model File to a correct location after build
        tagger = new MaxentTagger("models/left3words-wsj-0-18.tagger");
    }

    private String Tagger(String sentence) throws IOException,
            ClassNotFoundException {

        return tagger.tagString(sentence);
    }

    private void InitializeClusters() {
        allKeyWords = new HashSet<>();
        Cluster cluster;
        int numUsers = userVector.size();
        int numClusters = (int)(Math.sqrt(numUsers));
        int clustersIdx[] = new int[numUsers];

        //Change for a more efficient way to get all the key words found in messages
        Iterator<UserFeatures> iterator = userVector.iterator();
        while (iterator.hasNext()) {
            allKeyWords.addAll(iterator.next().getInterestsList());
        }

        //Change for a more efficient way to get non repeating random numbers
        for(int i = 0; i < numUsers; i++)
        {
            clustersIdx[i] = i;
        }
        Collections.shuffle(Arrays.asList(clustersIdx));

        //Initialize the clusters with random userVectors
        for(int i = 0; i < numClusters; i++) {
            int idx = clustersIdx[i];
            cluster = new Cluster(i);
            cluster.Initialize(userVector.get(idx));
            clusterVector.add(cluster);
        }
    }

    private void InitializeUserVector() throws IOException,
        ClassNotFoundException{

        userVector = new Vector<>();
        Collection<User> users = model.userById(null, null);

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

    private double distance(UserFeatures user, Cluster cluster) {
        double distance = 0;
        for(String keyWord : allKeyWords) {
            distance += Mood.distance(user.getMood(keyWord), cluster.getMood(keyWord));
        }
        return Math.sqrt(distance);
    }

    private void clusterAssignment() {
        int assignedCluster;
        double minDistance;
        for(UserFeatures user : userVector) {
            assignedCluster = -1;
            minDistance = Double.MAX_VALUE;
            for(Cluster cluster : clusterVector) {
                double newDistance = distance(user, cluster);
                if(newDistance < minDistance) {
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

        for(String keyWord : allKeyWords) {

            for(int i = 0; i < numClusters; i++) {
                sums[i] = new Mood();
                counter[i] = 0;
            }

            for(UserFeatures user : userVector) {
                sums[user.getCluster()].add(user.getMood(keyWord));
                counter[user.getCluster()]++;
            }

            for(int i = 0; i < numClusters; i++) {
                Mood avg = Mood.average(sums[i], counter[i]);
                clusterVector.get(i).updateEntry(keyWord, avg);
            }
        }
    }

    public void runClusterer(int iterations) throws IOException, ClassNotFoundException{
        InitializeUserVector();
        InitializeClusters();

        for(int i = 0; i < iterations; i++) {
            clusterAssignment();
            moveCentroids();
        }

        //Store the Clusters in the Database for access

    }
}
