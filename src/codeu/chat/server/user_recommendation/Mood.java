package codeu.chat.server.user_recommendation;

import codeu.chat.server.Model;

/**
 * Created by strobe on 23/05/17.
 */
public class Mood {

    private double moods[] = new double[12];

    Mood() {
        for(int i = 0; i < 12; i++) {
            moods[i] = 0;
        }
    }

    Mood(int mood) {
        for(int i = 0; i < 12; i++) {
            moods[i] = 0;
        }

        moods[mood] = 1;
    }

    public double getMood(int mood) {
        return moods[mood];
    }

    public void add(Mood mood) {
        for(int i = 0; i < 12; i++) {
            moods[i] += mood.getMood(i);
        }
    }
}
