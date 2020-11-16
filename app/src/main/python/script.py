from tweepy.streaming import StreamListener
import tweepy
from tweepy import OAuthHandler
from tweepy import Stream
from tweepy import Cursor
from tweepy import API
import pandas as pd
from textblob import TextBlob
import numpy as np
import datetime
import time
import re


ACCESS_TOKEN = "327121050-iAZBarTDOjt1giRSgaqtAjhRKetRvL7zigkgjW01"
ACCESS_TOKEN_SECRET = "WFEHD165slkGLC129z3HGQP2EzpKa52BiqS3JTCXLYR6T"
CONSUMER_KEY = "0yznK9lFD1FCvjGI380TZCGye"
CONSUMER_SECRET = "iOoJstfJtwJyZANjlMbKZOGESqigO8gD1ykFRXL4nCUxdmsvfK"

class TwitterClient():
    def __init__(self, twitter_user = None):
        self.auth = TwitterAuthenticator().authenticate_twitter_app()
        self.twitter_client = API(self.auth)
        self.twitter_user = twitter_user


    def get_twitter_client_api(self):
        return self.twitter_client


        # Gets the user that's signed in's timeline tweets.
    def get_user_timeline_tweets(self, num_tweets):
        tweets = []
        for tweet in Cursor(self.twitter_client.user_timeline, id = self.twitter_user).items(num_tweets):
            tweets.append(tweet)
        return tweets

        # Gets the user that's signed in's mentioned tweets (this is the one we're gonna need !!! )
    def get_user_mentions(self,num_tweets):
        tweets = []
        for tweet in Cursor(self.twitter_client.mentions_timeline, id = self.twitter_user).items(num_tweets):
            tweets.append(tweet)
        return tweets

        #Im pretty sure it means someone you follow and they follow you back
    def get_friend_list(self,num_friends):
        friend_list = []
        for friend in Cursor(self.twitter_client.friends, id = self.twitter_user).items(num_friends):
            friend_list.append(friend)
        return friend_list

    def get_home_timeline_tweets(self,num_tweets):
        home_timeline_tweets = []
        for tweet in Cursor(self.twitter_client.home_timeline, id = self.twitter_user).items(num_tweets):
            home_timeline_tweets.append(tweet)
        return home_timeline_tweets

class TweetAnalyzer():

    def clean_tweet(self, tweet):
        return ' '.join(re.sub("(@[A-Za-z0-9]+)|([^0-9A-Za-z \t])|(\w+:\/\/\S+)", " ", tweet).split())

    def analyze_sentiment(self, tweet):
        analysis = TextBlob(self.clean_tweet(tweet))

        if analysis.sentiment.polarity > 0:
            return 1
        elif analysis.sentiment.polarity == 0:
            return 0
        else:
            return -1

    def tweets_to_data_frame(self, tweets):
        df = pd.DataFrame(data=[tweet.text for tweet in tweets],columns = ['tweets'])
        df['id'] = np.arrsay([tweet.id for tweet in tweets])
        df['length'] = np.array([len(tweet.text) for tweet in tweets])
        df['date'] = np.array([tweet.created_at for tweet in tweets])
        df['likes'] = np.array([tweet.favorite_count for tweet in tweets])
        df['retweets'] = np.array([tweet.retweet_count for tweet in tweets])

        return df


    # GET ALL TARGETED TWEETS TOWARDS A USER #

    """
    def get_new_mentions(self):
        file_name = 'Twitter_Mentions.txt'
        get_new_mentions = True

        while get_new_mentions:
            try:
                current_mentions = []
                with open(file_name) as f:
                    for line in f:
                        line = line.strip('\n')
                        current_mentions.append(line)
                with open(file_name,'n') as f:
                    for mention in api.mentions_timeline(count=1):
                        f.write(mention.user.screen_name + ' mentioned you: ' + mention.text + "\n")
                new_mentions = []
                with open(file_name) as f:
                    for line in f:
                        line = line.strip("\n")
                        new_mentions.append(line)
                current_mentions_set = set(current_mentions)
                new_mentions_set = set(new_mentions)
                new_mentions_list = new_mentions_set.difference(current_mentions_set)
                for new_mention in new_mentions_list:
                    print(new_mention)
                    time.sleep(15)
                except tweepy.RateLimitError:
                    print("You've exceeded the rate limit.")
    """

# Authentication using the Twitter API credentials we have for our project

class TwitterAuthenticator():
    def authenticate_twitter_app(self):
        auth = OAuthHandler(CONSUMER_KEY, CONSUMER_SECRET)
        auth.set_access_token(ACCESS_TOKEN, ACCESS_TOKEN_SECRET)
        return auth


# # # # TWITTER STREAMER # # # #
class TwitterStreamer():
    """
    Class for streaming and processing live tweets.
    """
    def __init__(self):
        self.twitter_authenticator = TwitterAuthenticator()

    def stream_tweets(self, fetched_tweets_filename, hash_tag_list):
        # This handles Twitter authentication and the connection to Twitter Streaming API
        listener = TwitterListener(fetched_tweets_filename)
        auth = self.twitter_authenticator.authenticate_twitter_app()
        stream = Stream(auth, listener)
        # This line filter Twitter Streams to capture data by the keywords:
        stream.filter(track=hash_tag_list)


# # # # TWITTER STREAM LISTENER # # # #
class TwitterListener(StreamListener):
    """
    This is a basic listener that just prints received tweets to stdout.
    """
    def __init__(self, fetched_tweets_filename):
        self.fetched_tweets_filename = fetched_tweets_filename

    # on_data takes in the data from the stream listener, then we can do whatever we want with it
    def on_data(self, data):
        try:
            print(data)
            with open(self.fetched_tweets_filename, 'a') as tf:
                tf.write(data)
            return True
        except BaseException as e:
            print("Error on_data %s" % str(e))
        return True


    def on_error(self, status):
        # This is so that you dont flood tweets and get kicked out by twitter (this happens if rate limit occurs)
        if status == 420:
            return False
        print(status)

def check_user_validity(user_name):
    hello = str (user_name)
    tweet_analyzer = TweetAnalyzer()

    twitter_streamer = TwitterStreamer()
    twitter_client = TwitterClient()


    api = twitter_client.get_twitter_client_api()
    x=-1
    try:
        u=tweepy.API(twitter_client.auth).get_user(user_name)
        x=1
    except Exception:
        x=0

    return x



if __name__ == '__main__':

    # Authenticate using config.py and connect to Twitter Streaming API.
    hash_tag_list = ["donald trump", "hillary clinton", "barack obama", "bernie sanders"]
    fetched_tweets_filename = "tweets.txt"


    tweet_analyzer = TweetAnalyzer()

    twitter_streamer = TwitterStreamer()
    twitter_client = TwitterClient()


    api = twitter_client.get_twitter_client_api()
    # tweets = api.user_timeline(screen_name="realDonaldTrump",count=10)

    # df = tweet_analyzer.tweets_to_data_frame(tweets)

    # df['sentiment'] = np.array([tweet_analyzer.analyze_sentiment(tweet) for tweet in df['tweets']])

    # Executing the line below allows you to see the tweet attributes like text, retweet count, etc.
    #print(dir(tweets[0]))

    #print(df.head(5))

    today = datetime.date.today()
    week_ago = today - datetime.timedelta(days=7)


    tweet = api.user_timeline(screen_name="realDonaldTrump",  count = 1)
    for twt in tweet:
        print(pd.to_datetime(twt.created_at))
        print(week_ago)
