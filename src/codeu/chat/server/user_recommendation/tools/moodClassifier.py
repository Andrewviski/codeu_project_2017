import sys
import csv

from operator import itemgetter

import pandas as pd

from sklearn import metrics

from sklearn.feature_extraction.text import CountVectorizer

from sklearn.model_selection import train_test_split
from sklearn.naive_bayes import MultinomialNB
from sklearn.linear_model import LogisticRegression

def analyze_sentiment(sentence_in):

    # open training data csv
    training_data = pd.read_csv("twitter_12_sentiments.csv")
    training_data['sentiment_num'] = training_data.sentiment.map({'empty': 0, 'sadness': 1, 'hate': 2,
                                                                  'anger': 3, 'neutral': 4, 'happiness': 5,
                                                                  'surprise': 6, 'boredom': 7, 'relief': 8,
                                                                  'enthusiasm': 9, 'love': 10, 'fun': 11})

    X = training_data.content
    y = training_data.sentiment_num

    # vectorize the training dataset
    vect = CountVectorizer()
    X_dtm = vect.fit_transform(X)

    # vectorize the sentence input for prediction
    sentence = []
    sentence.append(sentence_in)
    sentence_dtm = vect.transform(sentence)
    sentence_dtm.toarray()

    # Evaluate the data with the naive bayes model
    nb = MultinomialNB()
    nb.fit(X_dtm, y)
    y_pred_class = nb.predict(sentence_dtm)

    # translate and return prediction as a string
    translator_dict = {0: 'empty', 1: 'sadness', 2: 'hate', 3: 'anger', 4: 'neutral', 5: 'happiness',
                        6: 'surprise', 7: 'boredom', 8: 'relief', 9: 'enthusiasm', 10: 'love', 11: 'fun'}

    print(translator_dict[int(y_pred_class)])

    return translator_dict[int(y_pred_class)]

def main():
    sentence = sys.argv[1]
    for num in range(2,len(sys.argv)):
        sentence = sentence + " " + sys.argv[num]
    analyze_sentiment(sentence)

if __name__ == "__main__":
    main()
