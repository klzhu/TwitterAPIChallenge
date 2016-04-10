package vinechallenge.utils;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.twitter.Extractor.Entity;

import twitter4j.ExtendedMediaEntity;
import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

/**
 * This class contains methods for parsing a tweet into HTML form
 */

public class TweetParser {
    private static final String HASHTAG_BASE_URL = "https://twitter.com/hashtag/";
    private static final String MENTION_BASE_URL = "https://twitter.com/";

    /*
     * returns a tweet in it's html form
     */
    public String convertStatusToHtml(Status status) {
        List<Entity> entities = extractEntities(status);
        return htmlText(status.getText(), entities);
    }

    /*
     * given entities and a tweet, converts the tweet to the HTML form
     */
    private String htmlText(String text, List<Entity> entities) {
        StringBuilder builder = new StringBuilder(text.length() * 2);
        int beginIndex = 0;

        for (Entity entity : entities) {
            builder.append(text.subSequence(beginIndex, entity.getStart()));
            Entity.Type type = entity.getType();

            switch (type) {
            case URL:
                convertURLTag(entity, builder);
                break;
            case HASHTAG:
                convertHashTag(entity, builder);
                break;
            case MENTION:
                convertMentions(entity, builder);
                break;
            default:
                break;
            }
            beginIndex = entity.getEnd();
        }
        builder.append(text.subSequence(beginIndex, text.length()));

        return builder.toString();
    }

    /*
     * convert hashtags to their html return form:
     */
    private static void convertHashTag(Entity entity, StringBuilder sb) {
        sb.append("<a ");
        sb.append("href=\"").append(escapeHTML(HASHTAG_BASE_URL + entity.getValue()));
        sb.append("\">").append(escapeHTML('#' + entity.getValue())).append("</a>");
    }

    /*
     * convert mentions to their html return form
     */
    private static void convertMentions(Entity entity, StringBuilder sb) {
        sb.append("<a ");
        sb.append("href=\"").append(escapeHTML(MENTION_BASE_URL + entity.getValue()));
        sb.append("\">").append(escapeHTML('@' + entity.getValue())).append("</a>");
    }

    /*
     * convert URLs to their html return form
     */
    private static void convertURLTag(Entity entity, StringBuilder sb) {
        sb.append("<a ");
        sb.append("href=\"").append(escapeHTML(entity.getExpandedURL()));
        sb.append("\">").append(escapeHTML(entity.getDisplayURL())).append("</a>");
    }

    /*
     * escapes characters to their HTML form
     */
    private static CharSequence escapeHTML(CharSequence text) {
        StringBuilder builder = new StringBuilder(text.length() * 2);
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
            case '&':
                builder.append("&amp;");
                break;
            case '>':
                builder.append("&gt;");
                break;
            case '<':
                builder.append("&lt;");
                break;
            case '"':
                builder.append("&quot;");
                break;
            case '\'':
                builder.append("&#39;");
                break;
            default:
                builder.append(c);
                break;
            }
        }
        return builder;
    }

    /*
     * adds Hashtag, mention, URL, media, and extended media entities for a
     * given status to a list
     */
    private static List<Entity> extractEntities(Status status) {
        List<Entity> entities = new LinkedList<Entity>();
        HashtagEntity[] tags = status.getHashtagEntities();
        UserMentionEntity[] mentions = status.getUserMentionEntities();
        URLEntity[] urls = status.getURLEntities();
        MediaEntity[] mediaLinks = status.getMediaEntities();
        ExtendedMediaEntity[] extendedMediaLinks = status.getExtendedMediaEntities();

        // add entities to list
        if (tags != null) {
            for (HashtagEntity tag : tags) {
                Entity entity = new Entity(tag.getStart(), tag.getEnd(), tag.getText(), Entity.Type.HASHTAG);
                entities.add(entity);
            }
        }

        if (mentions != null) {
            for (UserMentionEntity mention : mentions) {
                Entity entity = new Entity(mention.getStart(), mention.getEnd(), mention.getText(),
                        Entity.Type.MENTION);
                entities.add(entity);
            }
        }

        if (urls != null) {
            for (URLEntity url : urls) {
                Entity entity = new Entity(url.getStart(), url.getEnd(), url.getURL(), Entity.Type.URL);
                entity.setDisplayURL(url.getDisplayURL());
                entity.setExpandedURL(url.getExpandedURL());
                entities.add(entity);
            }
        }

        if (mediaLinks != null) {
            for (MediaEntity mediaLink : mediaLinks) {
                Entity entity = new Entity(mediaLink.getStart(), mediaLink.getEnd(), mediaLink.getURL(),
                        Entity.Type.URL);
                entity.setDisplayURL(mediaLink.getDisplayURL());
                entity.setExpandedURL(mediaLink.getMediaURL());
                entities.add(entity);
            }
        }

        if (extendedMediaLinks != null) {
            for (MediaEntity eMediaLink : extendedMediaLinks) {
                Entity entity = new Entity(eMediaLink.getStart(), eMediaLink.getEnd(), eMediaLink.getURL(),
                        Entity.Type.URL);
                entity.setDisplayURL(eMediaLink.getDisplayURL());
                entity.setExpandedURL(eMediaLink.getMediaURL());
                entities.add(entity);
            }
        }

        sortAndRemoveOverlappingEntities(entities);

        return entities;

    }

    /*
     * Sorts the entities based on the starting position in the tweet, and
     * removes any overlapping entities Code taken from twitter-text's
     * Extractor.java from github
     */
    private static void sortAndRemoveOverlappingEntities(List<Entity> entities) {
        // sort by index
        Collections.<Entity> sort(entities, new Comparator<Entity>() {
            public int compare(Entity e1, Entity e2) {
                return e1.getStart() - e2.getStart();
            }
        });

        // Remove overlapping entities.
        // Two entities overlap only when one is URL and the other is
        // hashtag/mention
        // which is a part of the URL. When it happens, we choose URL over
        // hashtag/mention
        // by selecting the one with smaller start index.
        if (!entities.isEmpty()) {
            Iterator<Entity> it = entities.iterator();
            Entity prev = it.next();
            while (it.hasNext()) {
                Entity cur = it.next();
                if (prev.getEnd() > cur.getStart()) {
                    it.remove();
                } else {
                    prev = cur;
                }
            }
        }
    }
}
