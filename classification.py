import re
import ssl
import nltk
import pickle
import argparse
import numpy as np
import tensorflow as tf
from collections import Counter
from nltk.corpus import stopwords
from nltk.stem import PorterStemmer
from nltk.stem import SnowballStemmer
from nltk.stem import WordNetLemmatizer
from tensorflow.keras.preprocessing.text import Tokenizer
from tensorflow.keras.preprocessing.sequence import pad_sequences


class Model:
    def __init__(self, text):
        self.text = text

    def download_dependencies(self):
        pass

    def process_text(self):
        pass

    def predict(self):
        pass


class BiLstm(Model):
    def __init__(self, text):
        super().__init__(text)
        self.model = tf.keras.models.load_model('models/Bi-Lstm/model')
        with open('models/Bi-Lstm/tokenizer.pickle', 'rb') as f:
            self.tokenizer = pickle.load(f)

    def download_dependencies(self):
        try:
            _create_unverified_https_context = ssl._create_unverified_context
        except AttributeError:
            pass
        else:
            ssl._create_default_https_context = _create_unverified_https_context

        try:
            nltk.data.find('stopwords')
        except LookupError:
            nltk.download('stopwords')

        try:
            nltk.data.find('wordnet')
        except LookupError:
            nltk.download('wordnet')

    def process_text(self):
        review = re.sub('[^a-zA-Z]', ' ', self.text)
        review = review.lower()
        review = review.split()
        review = [word for word in review
                  if word not in stopwords.words('english')]
        review = ' '.join(review)
        corpus = [review]
        sequence = self.tokenizer.texts_to_sequences(corpus)
        max_len = max([len(x) for x in sequence])
        self.processed_text = np.array(pad_sequences(sequence, padding='post', maxlen=max_len))

    def predict(self):
        if self.processed_text.any():
            prediction = self.model.predict_classes(self.processed_text)
            if prediction[0] == 0:
                return "false"
            elif prediction[0] == 1:
                return "true"
            elif prediction[0] == 2:
                return "partially false"
            elif prediction[0] == 3:
                return "other"


class Sentiment(Model):
    def __init__(self, text):
        super().__init__(text)
        self.model = tf.keras.models.load_model('models/SentimentAnalysis/model')
        with open('models/SentimentAnalysis/tokenizer.pickle', 'rb') as f:
            self.tokenizer = pickle.load(f)
        self.TEXT_CLEANING_RE = "@\S+|https?:\S+|http?:\S|[^A-Za-z0-9]+"

    def download_dependencies(self):
        try:
            nltk.data.find('stopwords')
        except LookupError:
            nltk.download('stopwords')

    def process_text(self):
        self.text = re.sub(self.TEXT_CLEANING_RE, ' ', str(text).lower()).strip()
        tokens = []
        stop_words = stopwords.words('english')
        stemmer = SnowballStemmer('english')
        for token in text.split():
            if token not in stop_words:
                tokens.append(stemmer.stem(token))
        self.text = " ".join(tokens)
        self.data = pad_sequences(self.tokenizer.texts_to_sequences(self.text), maxlen=700)

    def predict(self):
        if self.data.any():
            prediction = self.model.predict_classes(self.data)
            if prediction[0] == 0:
                return "partially false"
            elif prediction[0] == 1:
                return "false"
            elif prediction[0] == 2:
                return "true"
            elif prediction[0] == 3:
                return "other"


def parse_args():
    parser = argparse.ArgumentParser(description='Fake News Classification')
    parser.add_argument('text', metavar='text', type=str, nargs='+', help='Text to be classified')
    args = parser.parse_args()
    return ' '.join(args.text)


if __name__ == '__main__':
    text = parse_args()

    # Bi LSTM
    bilstm = BiLstm(text)
    bilstm.download_dependencies()
    bilstm.process_text()
    prediction_bilstm = bilstm.predict()

    # Sentiment Analysis
    sentiment = Sentiment(text)
    sentiment.download_dependencies()
    sentiment.process_text()
    prediction_sentiment = sentiment.predict()

    c = Counter([prediction_bilstm, prediction_sentiment])
    value, count = c.most_common()[0]
    if value:
        with open ('scor.txt', 'w') as f:
            f.write(value)
