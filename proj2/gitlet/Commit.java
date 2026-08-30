package gitlet;

// any imports you need here
import java.util.TreeMap;
import java.util.Map;
import java.io.Serializable;
import java.util.Date; // You'll likely use this in this class

/** Represents a gitlet commit object.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author DoubleMountain
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
    private Map<String, String> tree = new TreeMap<>();
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
    
    /**
     * Return the blob that a file name refers to, null if it does not exist.
     * @param name the file name
     * @return the SHA-1 of the blob
     */
    public String getBlob(String name) {
        return tree.get(name);
    }

    /**
     * Set the metadata of the commit
     * @param message the message of the commit
     * @param timestamp the time stamp of the commit
     */
    public void setMetadata(String message, Date timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    /**
     * Set the parents of the commit
     * @param parent1 the SHA-1 of the main parent of the commit
     * @param parent2 the SHA-1 of the second parent of the commit
     */
    public void setParents(String parent1, String parent2) {
        parent = parent1;
        secondParent = parent2;
    }

    /**
     * Track a certain blob with a certain name
     * @param name the name of the file
     * @param blob the SHA-1 of the blob to be tracked
     */
    public void trackBlob(String name, String blob) {
        tree.put(name, blob);
    }

    /**
     * Untrack the blob with a certain file name
     * @param name the name of the file
     * @return the blob that was tracked by the name before, null if nothing was tracked.
     */
    public String untrackBlob(String name) {
        return tree.remove(name);
    }
}
