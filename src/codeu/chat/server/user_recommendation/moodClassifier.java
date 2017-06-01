package codeu.chat.server.user_recommendation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.doccat.*;
import opennlp.tools.util.*;

/**
 * Created by strobe on 31/05/17.
 */
public class moodClassifier {

  DoccatModel model;

  public moodClassifier() {
    model = null;
  }

  public void trainModel() {
    InputStreamFactory tweets = null;

    try {
      tweets = new MarkableFileInputStreamFactory(new File("./bin/codeu/chat/server/user_recommendation/tools/twitter_12_sentiments.txt"));
      ObjectStream<String> lineStream = new PlainTextByLineStream(tweets, "UTF-8");
      ObjectStream<DocumentSample> sampleStream = new DocumentSampleStream(lineStream);

      TrainingParameters trainingParameters = new TrainingParameters();
      trainingParameters.put("CUTOFF_PARAM", 2);
      trainingParameters.put("ITERATUIBS_PARAM", 30);

      DoccatFactory doccatFactory = new DoccatFactory();

      model = DocumentCategorizerME.train("en", sampleStream, trainingParameters, doccatFactory);

    } catch (IOException ex) {
      System.out.println("Exception:");
      ex.printStackTrace();
    }
  }

  public double[] classifyTweet(String[] sentence) {
    DocumentCategorizerME categorizer = new DocumentCategorizerME(model);
    double[] result = categorizer.categorize(sentence);
    System.out.println("RESULT: " + categorizer.getBestCategory(result));

    return result;
  }
}
