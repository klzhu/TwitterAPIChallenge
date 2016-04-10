The program takes in a HTTP GET request and returns the latest statuses from the users in the 
request in order from most recent statuses to least recent.

I allow the parameters for the GET request to be case insensitive. I also assume that if a user 
issues a request gets is returned a next_cursor, and then issues a new request 
(with new params without the cursor param set), we are starting a new series of requests. 
See comments and design discussion below for full design limitations and assumptions.

To run:
I assume that the user running the code has java installed. If not, please install java 1.8.
To run the program, please go into the target folder and change the extension of 
vinechallenge-app-0.0.1-SNAPSHOT-jar-with-dependencies from .jar.x to .jar.
Then, return to the vine-api-servlet folder and execute run.sh. 

Once run.sh has been ran, go to a browser and you can start entering requests. Requests will look 
like: localhost:8080/statuses?screen_names=user&count=3

To stop the application, hit Ctrl C.

Summary: 
For this challenge, I used Java and Apache Tomcat to create a Java Servlet to 
handle HTTP Get requests. I used Maven to embed tomcat into my java application.

In my design, I use the following libraries:
1. Jackson - For mapping java objects to JSON
2. Twitter4j - For accessing twitter API calls
3. Twitter-text - For their entity object class

Design and implementation:
I used the provided tokens for authentication.
My program consists of 4 packages.

1. src\main\java\com\vinechallenge\servlet\vine_api_servlet\ServletDriver.java 
This class creates an is our programs driver and creates an instance of tomcat.

2. src\main\java\com\vinechallenge\servlet\vine_api_servlet\VineChallengeServlet.java 
This class is the main program that takes in a HTTP Get request.
This code checks our request and ensures that our required params are in our request and that our
params are of the correct form. It also checks for invalid parameters. Finally, it contains a method
that creates a ObjectMapper for mapping a java object to JSON for our response.

3. src\main\java\vinechallenge\model\Response.java
 This class contains the response java object that we map to JSON. 

4. src\main\java\vinechallenge\statuses\StatusRetrieval.java - 
This class is responsible for retrieving the statuses and returning a 
response object to the caller. It creates a list of statuses from multiple users by using the 
twitter search API. 
Finally, this class also has a global hashmap which is used for handling the next_cursor option.

5. src\main\java\vinechallenge\utils\TweetParser.java
This class is based off of twitter text's implementation. However, Twitter text had a lot of extra 
stuff that I felt wasn't needed, and didn't have the exact formatting to meet this assignments 
specs. So I used the entity clas from Twitter Text and based my methods off of theirs, 
while making the necessary changes to meet the required specs.

How the program runs:
ServletDriver creates a new instance of tomcat and our VineChallengeServlet
 -> VineChallengeServlet gets a HTTP Request 
 -> calls StatusRetrieval.java to get the statuses back
 -> calls TweetParser to get the statuses into the correct html formatting for the return
 -> Creates a Response object to return.

Testing:
Due to the relatively small size of the project and methods, I did not utilize unit and 
integration testing. Had this project needed to scale up and be maintained, I would have used an 
integration and unit test suite to test my program.
Some cases I tested for manually:
- Submitting a request where count is less than the number of statuses returned and thus the
next_cursor had to be used
- Submitting a request using the cursor returned from the response
- Submitting a requst where the count was greater than the number of statuses returned
- Submitting a request to retrieve statuses from multiple users
- Submitting a request and getting a cursor back. Then removing screen names from request 
while using cursor.
- Submitting a request with missing parameters, unknown parameters or invalid values for the 
parameters.

Comments / Noteworthy Design decisions:
1. There were two designs I considered in doing this problem. The first was to use the Twitter 
Search API, which did not retrieve tweets older than a week. The second design was to call 
getUserTimeline for each user in the request and concatenate the statuses together and then sort 
them. In this way,I would not be limited to statuses from only the past week. Because the specs say 
we should return statuses as though we were looking at a twitter account that was following these 
users, and this is not limited to only statuses in the past week when you use Twitter.com, I went 
with the second design.

2. The second design uses more space and twitter API calls than the first design because the first 
design can just return a list of sorted tweets from all users you want in one call.
The second design however must call getUserTimeLine for each user and then sort the list. 
In the second design (the design I implemented), I use a global pageNo, list of statuses, and 
cursor. The global list of statuses holds all the statuses that have been returned. If count is 
less than the number of statuses in this list, the next call will first take from this list 
before making another call to retrieve more statuses. 

3. Everytime a request comes in with a null cursor, I assume this is the start of a new series of 
requests and I reset my global variables. When I send a null next_cursor, this indicates that we 
have reached the last batch of tweets.

Had I needed to support a user's ability to submit multiple requests and user the next_cursor for 
all of them, I would have done something similar to the following:
Created a global hashmap where the key is the cursor, the value is another hashmap where the key is 
the userId, and the value is the tweetId of the last tweet retrieved for that user. When a request
with a cursor comes in, we retrieve the last status of the users in that request from our hashmap 
and make another API call to retrieve statuses older than those statuses in our hashmap for each 
respective user. Every method that accesses this global hashmap will be locked so only one thread 
can access and change the hashmap at a time.

4. In order to implement the next_cursor, I utilized global variables. Because each Tomcat request 
is executed on its own thread, my methods that touch my global variables are synchronization safe. 

I also assume we have enough memory to hold all statuses retrieved in memory 
(In reality, if the user request has a huge number of screen names, we may run out of memory to hold
 all the statuses).

5. I limit the user to a count of 100. This is to try to prevent very large counts in the request 
that can lead to problems (holding too many statuses in memory at once, going past Twitter API's 
limit for a page for status retrieval, etc...). I do not limit the number of screen names a user can
enter for a request however. I assume a count of 0 is not supported.

6. For simplicity, I assume that when users add a cursor to their request, they don't change the 
screen_names in the request. If they did, there could be an issue where the user removes 
screen_name1 from their request but the global statuses list still contains statuses from 
screen_name1. These would appear in the response. In order to solve this problem, I would have 
kept an tweetId for the last tweet that was included in the last batch per user and make a request 
to get tweets after this tweetId in every subsequent request.

7. This design only remembers one cursor value, so the user can only enter the next_cursor from the 
latest response in their next request. This would not work if we wanted users to be able to page 
back and forth or if we had a previous cursor value. I would have done this differently if we wanted
 to allow the user to page forward and backward.

