package vinechallenge.statuses;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import vinechallenge.model.Response;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class StatusRetrieval {
    // constants
    private static final String CONSUMER_KEY = "EvDxkfP3xzlZp9Ss5HoTxBFu4";
    private static final String CONSUMER_SECRET = "HoBfu1L2MLlozYhY2Gq99FtF3ZQIWyKVl2hjXEbJIUCAHL4sZ8";
    private static final String ACCESS_TOKEN = "702257299012849669-WTfyYVWAdmUKqRq9zPsWtS5ugMhsjbd";
    private static final String ACCESS_TOKEN_SECRET = "5uLYJnk4pIBoDKaOVC8jrteRc6KAD20jg62Bz7x6B3IuO";
    private static final int HEAD_INDEX = 0; // index of head for a linked list

    // global vars
    private static String currCursor = null;
    private static int currPageNo = 1;
    private static List<Status> statuses;

    private Twitter twitter;

    public StatusRetrieval() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setOAuthConsumerKey(CONSUMER_KEY);
        cb.setOAuthConsumerSecret(CONSUMER_SECRET);
        cb.setOAuthAccessToken(ACCESS_TOKEN);
        cb.setOAuthAccessTokenSecret(ACCESS_TOKEN_SECRET);

        twitter = new TwitterFactory(cb.build()).getInstance();
    }

    /**
     * return the most recent statuses up to count statuses
     */
    public synchronized Response retrieveStatus(String[] userNames, int count, String cursor) {
        List<Status> retList = new LinkedList<Status>();
        String nextCursor = null;
        if (cursor != null) {
            // if there is a cursor in our request, it needs to match a cursor
            // we sent in our last response
            // if there is a cursor in our request, both the cursor and statuses
            // vars must have been set already
            if (currCursor == null || !cursor.equals(currCursor) || statuses == null)
                return null;

            // if our list is empty, we must make another call to get statuses
            if (statuses.size() == 0)
                currPageNo++;
            else {
                for (Status status : statuses) {
                    retList.add(status);
                    statuses.remove(status);
                    count--;

                    // if we've filled up our count, break
                    if (count == 0)
                        break;
                }

                // if we've reached our count, set the nextCursor and return a
                // response
                if (count == 0) {
                    nextCursor = UUID.randomUUID().toString();
                    return new Response(retList, nextCursor);
                }
                // else, count has not been filled but our list is empty so we
                // must retrieve new statuses
                else
                    currPageNo++;
            }
        }

        else {
            currCursor = null;
            currPageNo = 1;
            statuses = new LinkedList<Status>();
        }

        try {
            populateStatuses(userNames, count, currPageNo);
        } catch (TwitterException e) {
            e.printStackTrace();
            return null;
        }

        // if our statuses contain less than our count, than we are done. Else,
        // we must set our next cursor
        if (statuses.size() >= count) {
            nextCursor = UUID.randomUUID().toString();
            for (int i = 0; i < count; i++) {
                retList.add(statuses.remove(HEAD_INDEX));
            }

            currCursor = nextCursor;
        }

        return new Response(retList, nextCursor);
    }

    /**
     * Populates our statuses so statuses.size is at least as large as count if
     * there are enough tweets to return
     * 
     * @throws TwitterException
     */
    private void populateStatuses(String[] userNames, int count, int pageNo) throws TwitterException {
        Paging page = new Paging(pageNo, count);

        for (int i = 0; i < userNames.length; i++) {
            statuses.addAll(twitter.getUserTimeline(userNames[i], page));
        }

        // must sort list if we had more than 1 user
        if (userNames.length > 1)
            sortStatusesByDate(statuses);
    }

    /*
     * Sorts statuses based on posted date descending
     */
    private static void sortStatusesByDate(List<Status> statuses) {
        Collections.<Status> sort(statuses, new Comparator<Status>() {
            public int compare(Status s1, Status s2) {
                return s2.getCreatedAt().compareTo(s1.getCreatedAt());
            }
        });
    }
}
