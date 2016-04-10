package vinechallenge.model;

import java.util.LinkedList;
import java.util.List;

import vinechallenge.utils.TweetParser;
import twitter4j.Status;

/**
 * Response is a object that holds the JSON response for the java servlet
 */

public class Response {
    private String next_cursor;
    private List<StatusResponse> statuses;
    private TweetParser helper;

    public Response(List<Status> twitterStatuses, String cursor) {
        helper = new TweetParser();
        statuses = new LinkedList<StatusResponse>();

        if (cursor != null)
            next_cursor = cursor;
        for (Status nextStatus : twitterStatuses) {
            StatusResponse statusResponse = new StatusResponse(helper.convertStatusToHtml(nextStatus),
                    nextStatus.getUser().getScreenName());
            statuses.add(statusResponse);
        }
    }

    public String getNext_cursor() {
        return next_cursor;
    }

    public List<StatusResponse> getStatuses() {
        return statuses;
    }

    /**
     * Status Response is an object that holds what we need to include in our
     * response per status
     */
    private static class StatusResponse {
        private String status;
        private String user;

        public StatusResponse(String status, String user) {
            this.status = status;
            this.user = user;
        }

        public String getStatus() {
            return status;
        }

        public String getUser() {
            return user;
        }
    }
}
