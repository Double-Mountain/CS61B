package gitlet;

// any imports you need here
import java.util.HashMap;
import java.util.Map;
import java.io.Serializable;
import java.util.Date; // You'll likely use this in this class

/** Represents a gitlet commit object.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Commit implements Serializable {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Commit class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided one example for `message`.
     */

    /** The message of this Commit. */
    private String message;

    /* fill in the rest of this class. */
    private Date timestamp;
    private Map<String, String> tree = new HashMap<>();
    private String parent = null;
    private String secondParent = null;
    
    /**
     * Construct a commit with only one parent.
     * @param message log message
     * @param timestamp timestamp in Date class
     * @param parent SHA-1 string of the parent commit
     */
    public Commit(String message, Date timestamp, String parent) {
        this.message = message;
        this.timestamp = timestamp;
        this.parent = parent;
    }

    /**
     * Construct a commit with two parents.
     * @param message log message
     * @param timestamp time stamp in Date class
     * @param parent SHA-1 string of the parent commit
     * @param secondParent SHA-1 string of the other parent commit
     */
    public Commit(String message, Date timestamp, String parent, String secondParent) {
        this(message, timestamp, parent);
        this.secondParent = secondParent;
    }
}
