package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// any imports you need here
import java.util.HashMap;
import java.util.Map;
import java.util.Date;

/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author TODO
 */
public class Repository {
    /**
     * add instance variables here.
     *
     * List all instance variables of the Repository class here with a useful
     * comment above them describing what that variable represents and how that
     * variable is used. We've provided two examples for you.
     */

    /** The current working directory. */
    public static final File CWD = new File(System.getProperty("user.dir"));
    /** The .gitlet directory. */
    public static final File GITLET_DIR = join(CWD, ".gitlet");
    public static final File BRANCHES = join(GITLET_DIR, "branches");
    public static final File COMMITS = join(GITLET_DIR, "commits");
    public static final File BLOBS = join(GITLET_DIR, "blobs");

    /* fill in the rest of this class. */
    private static HashMap<String, String> branches;


    /**
     * Initiate the gitlet repository
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.out.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMITS.mkdir();
        BLOBS.mkdir();
        
        branches = new HashMap<String, String>();
        String first = createCommitFile(new Commit("initial commit", new Date(0), null));
        branches.put("master", first);
        moveHead("master");
        saveBranches();
    }

    /**
     * Create a new commit file
     * @param commit the commit object to create
     * @return the SHA-1 of the commit file
     */
    private static String createCommitFile(Commit commit) {
        String name = sha1(serialize(commit));
        File file = join(COMMITS, name);
        writeObject(file, commit);
        return name;
    }

    /**
     * Move the head pointer to a certain branch
     * @param branch the branch that the head pointer move to
     */
    private static void moveHead(String branch) {
        branches.put("HEAD", branches.get(branch));
    }

    /**
     * Save the state of branches
     */
    private static void saveBranches() {
        writeObject(BRANCHES, branches);
    }

    /**
     * Load the state of branches
     */
    private static void loadBranches() {
        branches = readObject(BRANCHES, HashMap.class);
    }

    public static void add() {

    }
}
