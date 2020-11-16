from tweepy.streaming import StreamListener
import tweepy
from tweepy import OAuthHandler
from tweepy import Stream
from tweepy import Cursor
from tweepy import API
from bs4 import BeautifulSoup
import pandas as pd
import requests
from functools import reduce
from textblob import TextBlob
import numpy as np
from datetime import datetime
import datetime
import time
from regexp import re


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
            return analysis.sentiment.polarity
        elif analysis.sentiment.polarity == 0:
            return analysis.sentiment.polarity
        else:
            return analysis.sentiment.polarity

    def tweets_to_data_frame(self, tweets):
        df = pd.DataFrame(data=[tweet.text for tweet in tweets],columns = ['tweets'])
        df['id'] = np.array([tweet.id for tweet in tweets])
        df['length'] = np.array([len(tweet.text) for tweet in tweets])
        df['date'] = np.array([tweet.created_at for tweet in tweets])
        df['likes'] = np.array([tweet.favorite_count for tweet in tweets])
        df['retweets'] = np.array([tweet.retweet_count for tweet in tweets])
        df['racial_insults_count'] = 0
        df['gender_insults_count'] = 0
        df['personal_insults_count'] = 0

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


#Pass in a string of the name of the user, and it will either return their id and screen name, or an error
def check_user_validity(user_name):

    try:
        u=tweepy.API(twitter_client.auth).get_user(user_name)
        return True
    except Exception:
        return False

def initialize_tweepy_functions():
    tweet_analyzer = TweetAnalyzer()
    twitter_streamer = TwitterStreamer()
    twitter_client = TwitterClient()
    api = twitter_client.get_twitter_client_api()    

# This creates a col that takes into account the sentiment and the number of insults that are in the tweets and makes 
# a new cumulative score
def label_tweet_bully_score (row):
    if row['sentiment'] < 0 :
        return row['sentiment'] - row['racial_insults_count'] * 0.05 - row['gender_insults_count'] * 0.025 - row['personal_insults_count'] * 0.025
    return 0


def getMetrics(twitterUserName):
    retweet_filter = '-filter:retweets'
    q = twitterUserName + retweet_filter
    tweetsPerQry = 100
    sinceId = None
    max_id = -1
    maxTweets = 50


    racial_insults = ["Abo", "Abbo", "Boong", "bong", "bung", "Coon", "Gin", "Lubra", "Af", "Ape", "Béni-oui-oui", "Bluegum",
                "Boogie", "Buck", "Burrhead", "Burr-head", "Burrhead", "Colored", "Coon", "Crow", "Eggplant", "Fuzzies", "Fuzzy-Wuzzy", "Golliwogg",
                "Jigaboo", "jiggabo,jijjiboo,zigabo", "jig,jigg,jiggy,jigga", "JimCrow", "JimFish", "Junglebunny", "Kaffir,kaffer,kafir,kaffre",
                "Macaca,macaque", "Mammy", "Monkey", "Mosshead", "Munt", "Nig-nog", "Nigger", "niggar", "niggur,niger", "nigor", "nigre(Caribbean)",
                "nigar,nigga", "niggah", "nig", "nigguh", "Niglet", "nigglet", "Nigra", "negra", "niggra", "nigrah", "nigruh", "Pickaninny", "Porchmonkey",
                "Powderburn", "Quashie", "Sambo", "SmokedIrishman", "Sooty", "Spade", "Spook", "Tarbaby", "Teapot", "Thicklips,bootlips", "Celestial",
                "Charlie", "Chinaman", "Chink", "Coolie", "Gook", "Jap", "Nip", "Oriental", "Yellow,Yellowman,orYellowwoman", "American-BornConfusedDesi,orABCD", 
                "Brownie", "Chee-chee", "Chinki", "Currymuncher", "Madrasi", "Malaun", "Paki", "Dink", "Flip", "Gugus", "Huan-a", "Jakun", "Cameljockey", 
                "Hajji,Hadji,Haji", "Sandnigger", "Towelhead", "Raghead", "Beaner", "Brownie", "Cholo", "Greaseball", "Greaser", "Spic,spick,spik,spig,orspigotty", 
                "Sudaca", "Tacohead", "Tonk", "Veneco", "Wetback", "Angmo", "Barang", "Bule", "Charlie", "Coonassorcoon-ass", "Cracker", "Farang",
                "Gammon", "Gringo", "Gubba", "Gweilo,gwailo,kwailo", "Honky", "Haole", "Hunky", "Bohunk", "Mangiacake", "cake", "Medigan", "Amedigan", 
                "Ofay", "Arkie", "Okie", "Peckerwood", "Whitey", "Chocko", "Dago", "Greaseball,Greaser", "Kanake", "Métèque", "Wog", "Brownie", "Chug", 
                "Eskimo,EskimoPie", "Indian", "PrairieNigger", "Redskin", "Squaw", "TimberNigger", "Wagonburner", "Yanacona", "Boonga", "boong", "bunga",
                "boonie", "Brownie", "Hori", "Kanaka", "Merkin", "Yankee,Yank", "SeppoandSeptic", "Buckra,Bakra", "Bumpkin,CountryBumpkin,HillbillyBumpkin",
                "Cracker", "Goodol boy", "Hick", "Hillbilly", "Honky,honkey,honkie", "Peckerwood,wood", "Redneck", "TrailerTrash", "WhiteTrash", "Whitey",
                "Curepí", "Argie", "Limey", "Pom,Pommy", "Pirata", "Jock", "Scotch", "Teuchter", "Sheepshagger", "Cubiche", "Gusano", "Boches", "Chleuh",
                "Hermans,Herms", "TheHun", "Huns", "Jerry,Gerry", "Kraut", "Marmeladinger", "Mof", "Nazi", "Piefke", "Bog-trotterorBogIrish", "Mick", "Paddy", 
                "Prod", "Taig", "Snout", "Continentale", "Dago", "Eyetie", "Ginzo", "Goombah", "Greaseball,Greaser", "Guido", "Guinea", "Polentone", "Terrone",
                "Wog", "Wop", "Sardegnolo,sardignòlo,sardignuolo,sardagnòlo", "Sheepshagger", "Kapo", "Kike,kyke", "Shylock", "Yid,zhyd", "Lebo,Lebbo",
                "Wog", "FYROMian", "Bulgaroskopian", "Macedonist", "Pseudomacedonian,pseudo-Macedonian", "Skopjan", "Skopjian,Skopiana", "Skopianika", 
                "ChinaSwede", "Chukhna", "Polack,Polak,Pollack,Pollock,Polock", "Pshek", "Mazurik", "Russki,Russkie", "Moskal", "Japies,Yarpies", "Coon", 
                "Mulatto", "UncleTom", "UncleRuckus", "Oreo", "AuntJemima", "AuntJane", "AuntMary", "AuntSally", "AuntThomasina", "Afro-Saxon", "Ann,MissAnn",
                "Wigger", "Wigga","wegro" , "RhinelandBastard", "Mulatto", "Zambo", "Lobos", "Mulatto", "Apple", "American-BornConfusedDesi,orABCD", "Banana", "Coconut"]

    gender_insults= ["bitch", "cunt", "whore", "slut", "hoe", "bastard", "sissy", "girlie man", "pansy", "sodomite", "brownie",
                 "janus", "bugger", "pillow biter", "fudgepacker", "fag", "rug muncher", "marimacho", "homo", "faggot", "quean",
                 "wonk", "sod", "jocker", "flamer","ponce", "steamer", "poove", "lesbo", "flata", "dyke",
                 "AC/DC", "switch hitter", "lug", "fauxbians", "tranny", "shim", "HeShe", "transtrender", "cuntboy",
                 "hefemale", "shemale", "dickgirl", "ladyboy", "chicks with dicks"]

    personal_insults= ["dick", "piece of shit", "fuck", "fucker", "motherfucker", "dog", "animal", "impolite", "disrespectful", "trash",
                   "tramp", "snitch", "ugly", "stupid", "dumb", "crazy", "insane", "mental", "addict", "jackass",
                   "jerk", "thug", "killer", "murderer", "rapist", "thief", "pedophile", "sex offender", "hate", "freak",
                   "bully", "hysterical", "imbecile", "idiot", "junkie", "lame", "loser", "maniac", "moron", "moronic",
                   "midget", "narcissist", "nutter", "nut", "psycho", "psychopath", "tard", "retard", "window licker", "Sociopath",
                   "Scatterbrained","abnormal","subnormal","creep","crappy","crap","donkey","dumbass","asshole","ass",
                   "butthead","buttkisser","asskisser","butt","ball licker","dick licker","dick rider","hooker","no manners","cocksucker"]


    # Initializations
    tweet_analyzer = TweetAnalyzer()
    twitter_streamer = TwitterStreamer()
    twitter_client = TwitterClient()
    api = twitter_client.get_twitter_client_api()
    searchQuery=twitterUserName

    #Authorization
    auth = tweepy.AppAuthHandler(CONSUMER_KEY,CONSUMER_SECRET)
    auth.secure = True
    api2 = tweepy.API(auth, wait_on_rate_limit=True, wait_on_rate_limit_notify=True)

    # Get tweets that mention our user, and filter out retweets so we dont have duplicates
    new_tweets = api2.search(q=q, count = tweetsPerQry, since_id = sinceId)


    df = tweet_analyzer.tweets_to_data_frame(new_tweets)

    racial_insults_lower = []
    for insult in racial_insults:
        racial_insults_lower.append(insult.lower())


    df['sentiment'] = np.array([tweet_analyzer.analyze_sentiment(tweet) for tweet in df['tweets']])

    # Update the column that shows how many insults were targeted in the tweet.
    for idx in df.index:
        for insult in racial_insults_lower:
            if insult in df['tweets'].loc[idx]:
                df['racial_insults_count'].loc[idx] = df['racial_insults_count'].loc[idx] + 1

        for insult in gender_insults:
            if insult in df['tweets'].loc[idx]:
                df['gender_insults_count'].loc[idx] = df['gender_insults_count'].loc[idx] + 1

        for insult in personal_insults:
            if insult in df['tweets'].loc[idx]:
                df['personal_insults_count'].loc[idx] = df['personal_insults_count'].loc[idx] + 1       

    for idx in df.index:
        df['date'].loc[idx] = df['date'].loc[idx].date()

    # We need three more columns, racially_targeted_bool, gender_targeted_bool, personally_targeted_bool
    df['racially_targeted_bool'] = df['racial_insults_count'] > 0
    df['gender_targeted_bool'] = df['gender_insults_count'] > 0
    df['personally_targeted_bool'] = df['personal_insults_count'] > 0

    today = datetime.date.today()
    week_ago = today - datetime.timedelta(days=7)
    month_ago = today - datetime.timedelta(days=30)

    
    df['date'] = pd.to_datetime(df['date'])

    df['tweet_bully_score'] = 0

    # Add the new column to the old dataframe
    df['tweet_bully_score'] = df.apply (lambda row: label_tweet_bully_score(row), axis=1)

    week_ago = pd.to_datetime(week_ago)
    month_ago = pd.to_datetime(month_ago)

    weekly_mask = (df['date'] > week_ago)
    monthly_mask =(df['date'] > month_ago)

    weekly_tweets = df.loc[weekly_mask]
    monthly_tweets = df.loc[monthly_mask]

    # For some reason, it's adding the tweets the user himself makes. Here we're only adding the tweets the user is mentioned in
    # By other people
    weekly_tweets["containsMention"]= weekly_tweets["tweets"].str.lower().str.find('@' +  searchQuery)
    monthly_tweets["containsMention"]= monthly_tweets["tweets"].str.lower().str.find('@' + searchQuery)

    #The ones with 0 are the ones where the user is mentinoned, as opposed to the ones with -1
    mentioned_weekly_tweets = weekly_tweets.loc[weekly_tweets['containsMention'] == 0]
    mentioned_monthly_tweets = monthly_tweets.loc[monthly_tweets['containsMention'] == 0]

    print(mentioned_monthly_tweets)
    print(mentioned_weekly_tweets)

    #Get the aggregate bully score from all our data to send over to android studio
    weekly_bully_score = (mentioned_weekly_tweets["tweet_bully_score"] ).mean()
    print(weekly_bully_score)
    monthly_bully_score = (mentioned_monthly_tweets["tweet_bully_score"] ).mean()
    print(monthly_bully_score)
    monthly_bully_score=monthly_bully_score+0.0
    weekly_bully_score=weekly_bully_score+0.0

    # Next, I need to count how many racially targeted, gender targeted, etc. tweets went to our user
    # Each week
    weeklyRacialTweetsCount = (mentioned_weekly_tweets.racially_targeted_bool).sum()
    weeklyGenderTweetsCount = (mentioned_weekly_tweets.gender_targeted_bool).sum()
    weeklyPersonalTweetsCount = (mentioned_weekly_tweets.personally_targeted_bool).sum()

    # Each month
    monthlyRacialTweetsCount = (mentioned_monthly_tweets.racially_targeted_bool).sum()
    monthlyGenderTweetsCount = (mentioned_monthly_tweets.gender_targeted_bool).sum()
    monthlyPersonalTweetsCount = (mentioned_monthly_tweets.personally_targeted_bool).sum()
    numbers = [weekly_bully_score, monthly_bully_score, weeklyRacialTweetsCount, weeklyGenderTweetsCount, weeklyPersonalTweetsCount, monthlyRacialTweetsCount, monthlyGenderTweetsCount, monthlyPersonalTweetsCount]
    return numbers



if __name__ == '__main__':

    tweet_analyzer = TweetAnalyzer()
    twitter_streamer = TwitterStreamer()
    twitter_client = TwitterClient()
    api = twitter_client.get_twitter_client_api()    


    auth = tweepy.AppAuthHandler(CONSUMER_KEY,CONSUMER_SECRET)
    auth.secure = True
    api2 = tweepy.API(auth, wait_on_rate_limit=True, wait_on_rate_limit_notify=True)

    searchQuery = 'ffsabdu'

    retweet_filter = '-filter:retweets'

    q = searchQuery + retweet_filter

    tweetsPerQry = 100
    sinceId = None
    max_id = -1
    maxTweets = 30

    tweetCount = 0

    # Get tweets that mention our user, and filter out retweets so we dont have duplicates
    new_tweets = api2.search(q=q, count = tweetsPerQry, since_id = sinceId)

    headers = {
    'Access-Control-Allow-Origin': '*',
    'Access-Control-Allow-Methods': 'GET',
    'Access-Control-Allow-Headers': 'Content-Type',
    'Access-Control-Max-Age': '3600',
    'User-Agent': 'Mozilla/5.0 (X11; Ubuntu; Linux x86_64; rv:52.0) Gecko/20100101 Firefox/52.0'
    }

    url = "http://www.owlapps.net/owlapps_apps/articles?id=8308199&lang=en"
    req = requests.get(url, headers)
    soup = BeautifulSoup(req.content, 'html.parser')

    # racialInsultList = []
    # for dt in soup.findAll('dt'):
    #     racialInsultList.append(dt.get_text().replace(" ", "").split("/"))
    

    # # You get a list of lists, so here, youre just flattening out the list
    # racialInsultList = reduce(lambda x,y :x+y ,racialInsultList)

    # #Split synonymous insults into separate ones, so we can identify them later on
    # for insult in racialInsultList:
    #     if type(insult) == "<class 'list'>":
    #         racialInsultList.remove(insult)

    # for insult in racialInsultList:
    #     insult = insult.split(",")



    getMetrics('abduffs',racial_insults, gender_insults, personal_insults)

    # #Executing the line below allows you to see the tweet attributes like text, retweet count, etc.
    # #print(dir(tweets[0]))