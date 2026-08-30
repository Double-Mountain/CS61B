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
    public static final File STAGING = join(GITLET_DIR, "staging");
    public static final File HEAD = join(GITLET_DIR, "head");

    /* fill in the rest of this class. */

    /**
     * Initiate the gitlet repository
     */
    public static void init() {
        if (GITLET_DIR.exists()) {
            System.err.println("A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        GITLET_DIR.mkdir();
        COMMITS.mkdir();
        BLOBS.mkdir();
        STAGING.mkdir();
        BRANCHES.mkdir();
        String first = createCommitFile(new Commit("initial commit", new Date(0), null));
        pointBranch("master", first);
        moveHead("master");
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
        writeContents(HEAD, branch);
    }

    /**
     * Return the branch name that the head pointer points to
     * @return the branch name
     */
    private static String getHead() {
        return readContentsAsString(HEAD);
    }

    /**
     * Point a certain branch to a commit. Create the branch if it does not exist.
     * @param branch the name of the branch
     * @param commit the SHA-1 of the commit
     */
    private static void pointBranch(String branch, String commit) {
        writeContents(join(BRANCHES, branch), commit);
    }

    /**
     * Return the commit that a certain branch points to
     * @param branch the name of the branch
     * @return the SHA-1 of the commit
     */
    private static String getBranch(String branch) {
        File file = join(BRANCHES, branch);
        if (!file.exists()) {
            return null;
        }
        return readContentsAsString(file);
    }

    /**
     * Add a certain file to the staging area
     * Delete it from the staging area if it exists there and is identical to the current commit
     * @param name the name of the file
     */
    public static void add(String name) {
        File file = join(CWD, name);
        if (!file.exists()) {
            System.err.println("File does not exist.");
            System.exit(0);
        }
        if (sha1(readContents(file)).equals(
            readObject(join(COMMITS, getBranch(getHead())), Commit.class).getBlob(name))) {
            deleteStaging(name);
        }
        else {
            writeContents(join(STAGING, name), readContents(file));
        }
    }

    /**
     * Delete a file with a certain name if it exists
     * @param name the name of the file
     * @return whether the file was deleted
     */
    private static boolean deleteStaging(String name) {
        return restrictedDelete(join(STAGING, name));
    }
}
