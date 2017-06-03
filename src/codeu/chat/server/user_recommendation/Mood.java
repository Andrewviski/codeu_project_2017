package codeu.chat.server.user_recommendation;

import codeu.chat.server.Model;

/**
 * Created by strobe on 23/05/17.
 */
public class Mood {

  private double moods[] = new double[12];

  public Mood() {
    for (int i = 0; i < 12; i++) {
      moods[i] = 0;
    }
  }

  public Mood(double[] mood) {
    for (int i = 0; i < 12; i++) {
      moods[i] = mood[i];
    }
  }

  public void setMood(int mood, double value) {
    this.moods[mood] = value;
  }

  public double getMood(int mood) {
    return moods[mood];
  }

  public double[] getMoods() {
    return moods;
  }

  public void add(Mood mood) {
    if(mood == null)
      return;

    for (int i = 0; i < 12; i++) {
      moods[i] += mood.getMood(i);
    }
  }

  public static Mood average(Mood mood, int users) {

    Mood avg = new Mood();
    double newMoods[] = new double[12];

    if (users == 0) {
      return avg;
    }

    for (int i = 0; i < 12; i++) {
      avg.setMood(i, mood.getMood(i) / users);
    }

    return avg;
  }

  public static double distance(Mood mood1, Mood mood2) {
    double dist = 0;
    if (mood1 == null) {
      mood1 = new Mood();
    }

    if (mood2 == null) {
      mood2 = new Mood();
    }

    for (int i = 0; i < 12; i++) {
      dist += ((mood1.getMood(i) - mood2.getMood(i)) * (mood1.getMood(i) - mood2.getMood(i)));
    }
    return dist;
  }

  public void printMood() {
    if (moods == null)
    {
      System.out.println("Word Not Included");
    }
    for (int i = 0; i < 12; i++) {
      System.out.println(i + ": " + moods[i]);
    }
  }
}
