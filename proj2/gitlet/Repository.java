package gitlet;

import java.io.File;
import static gitlet.Utils.*;

// any imports you need here
import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Locale;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.TreeMap;
import java.util.Map;
import java.util.HashMap;

/** Represents a gitlet repository.
 *  It's a good idea to give a description here of what else this Class
 *  does at a high level.
 *
 *  @author DoubleMountain
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
    public static final File REMOVAL = join(GITLET_DIR, "removal");
    public static final File ADDITION = join(GITLET_DIR, "addition");

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
        HashSet<String> removal = new HashSet<>();
        writeObject(REMOVAL, removal);
        HashMap<String, String> addition = new HashMap<>();
        writeObject(ADDITION, addition);
        String first = saveCommit(new Commit("initial commit", new Date(0), null));
        setBranch("master", first);
        moveHead("master");
    }

    /**
     * Save the commit
     * @param commit the commit object to be saved
     * @return the SHA-1 of the commit file
     */
    private static String saveCommit(Commit commit) {
        String name = sha1(serialize(commit));
        File file = join(COMMITS, name);
        writeObject(file, commit);
        return name;
    }

    /**
     * Load the commit as an object
     * @param name the SHA-1 of the commit, or a unique abbreviation of it
     * @return an commit object, null if none or multiple commits can be found under the given name or the name itself is null
     */
    private static Commit loadCommit(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() != UID_LENGTH) {
            List<String> commits = plainFilenamesWithoutOrder(COMMITS);
            String target = null;
            for (String file : commits) {
                if (file.startsWith(name)) {
                    if (target != null) {
                        return null;
                    }
                    target = file;
                }
            }
            if (target == null) {
                return null;
            }
            name = target;
        }
        File file = join(COMMITS, name);
        if (!file.exists()) {
            return null;
        }
        return readObject(file, Commit.class);
    }

    /**
     * Load the commit that a branch points to as an object, null if the given branch does not exist
     * @param branch the branch name
     * @return an commit object
     */
    private static Commit loadBranchCommit(String branch) {
        return loadCommit(getBranch(branch));
    }

    /**
     * Load the commit that the head pointer points to as an object
     * @return an commit object
     */
    private static Commit loadHeadCommit() {
        return loadBranchCommit(getHead());
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
     * Make a certain branch point to a commit. Create the branch if it does not exist.
     * @param branch the name of the branch
     * @param commit the SHA-1 of the commit
     */
    private static void setBranch(String branch, String commit) {
        writeContents(join(BRANCHES, branch), commit);
    }

    /**
     * Return the commit that a certain branch points to, null if the branch does not exist
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
        if (sha1(readContents(file)).equals(loadHeadCommit().getBlob(name))) {
            unstage(name);
        } else {
            stage(name);
        }
        deleteFromRemoval(name);
    }

    /**
     * Stage a file in CWD if it exists
     * @param name the file name
     * @return whether the file exists
     */
    private static boolean stage(String name) {
        File file = join(CWD, name);
        if (!file.exists()) {
            return false;
        }
        writeContents(join(STAGING, name), readContents(file));
        addToAddition(name);
        return true;
    }
    /**
     * Delete a file in the staging area with a certain name if it exists
     * @param name the name of the file
     * @return whether the file was deleted
     */
    private static boolean unstage(String name) {
        deleteFromAddition(name);
        return join(STAGING, name).delete();
    }

    /**
     * Load addition as a hash map object
     * @return a hash map object
     */
    private static HashMap<String, String> loadAddition() {
        return readObject(ADDITION, HashMap.class);
    }

    /**
     * Save a hash map object as addition
     * @param addition a hash map object
     */
    private static void saveAddition(HashMap<String, String> addition) {
        writeObject(ADDITION, addition);
    }

    /**
     * Add a staged file to addition if it exists
     * @param name the name of the staged file
     * @return whether the file exists in the staging area
     */
    private static boolean addToAddition(String name) {
        File file = join(STAGING, name);
        if (!file.exists()) return false;
        HashMap<String, String> addition = loadAddition();
        addition.put(name, sha1(readContents(file)));
        saveAddition(addition);
        return true;
    }

    /**
     * Delete a file from addition
     * @param name the name of the file to be deleted
     * @return the SHA-1 of the file if it existed in addition, null otherwise
     */
    private static String deleteFromAddition(String name) {
        HashMap<String, String> addition = loadAddition();
        String ret = addition.remove(name);
        saveAddition(addition);
        return ret;
    }

    /**
     * Return a set of file names in staging
     * @return the set of file names
     */
    private static Set<String> getStagedFileSet() {
        return loadAddition().keySet();
    }

    /**
     * Return a staged file as a file object
     * @param name the file name
     * @return a file object
     */
    private static File loadStagedFile(String name) {
        return join(STAGING, name);
    }

    /**
     * Create a blob from a staged file if it exists
     * @param name the name of the staged file
     * @return the SHA-1 of the blob, null if the staged file does not exist
     */
    private static String createBlob(String name) {
        File file = loadStagedFile(name);
        if (!file.exists()) {
            return null;
        }
        byte[] contents = readContents(loadStagedFile(name));
        String blob = sha1(contents);
        writeContents(join(BLOBS, blob), contents);
        return blob;
    }

    /**
     * Commit the staging files.
     * @param message the commit message
     */
    public static void commit(String message) {
        if (message.isBlank()) {
            System.err.println("Please enter a commit message.");
            System.exit(0);
        }
        Set<String> stagedFiles = getStagedFileSet();
        HashSet<String> removal = loadRemoval();
        if (stagedFiles.isEmpty() && removal.isEmpty()) {
            System.err.println("No changes added to the commit.");
            System.exit(0);
        }
        String branch = getHead();
        Commit commit = loadHeadCommit();
        commit.setMetadata(message, new Date());
        commit.setParents(getBranch(branch), null);
        for (String name : stagedFiles) {
            String blob = createBlob(name);
            commit.trackBlob(name, blob);
            unstage(name);
        }
        for (String name : removal) {
            commit.untrackBlob(name);
        }
        clearRemoval();
        String name = saveCommit(commit);
        setBranch(branch, name);
    }

    /**
     * Delete all files from removal
     */
    private static void clearRemoval() {
        saveRemoval(new HashSet<String>());
    }

    /**
     * Delete a file in removal if it existed
     * @param name the file to be deleted
     * @return whether the file existed before deleting
     */
    private static boolean deleteFromRemoval(String name) {
        HashSet<String> removal = loadRemoval();
        boolean flag = removal.remove(name);
        saveRemoval(removal);
        return flag;
    }

    private static void addToRemoval(String name) {
        HashSet<String> removal = loadRemoval();
        removal.add(name);
        saveRemoval(removal);
    }

    /**
     * Load the removal as a hash set object
     * @return the removal as a hash set
     */
    private static HashSet<String> loadRemoval() {
        return readObject(REMOVAL, HashSet.class);
    }

    /**
     * Save the state of the removal
     * @param removal the hash set object to be saved as the removal
     */
    private static void saveRemoval(HashSet<String> removal) {
        writeObject(REMOVAL, removal);
    }

    /**
     * Unstage a file if it is currently staged for addition
     * Remove and untrack the file if it is tracked by the current commit
     * @param name the file name
     */
    public static void rm(String name) {
        boolean staged = unstage(name);
        boolean tracked = (loadHeadCommit().getBlob(name) != null);
        if (!staged && !tracked) {
            System.err.println("No reason to remove the file.");
            System.exit(0);
        }
        if (tracked) {
            restrictedDelete(join(CWD, name));
            addToRemoval(name);
        }
    }

    /**
     * Display the history of the current commit, following the first parents.
     */
    public static void log() {
        String name = getBranch(getHead());
        Commit current = loadCommit(name);
        while (true) {
            printLog(name);
            name = current.getParent();
            if (name == null) {
                break;
            }
            current = loadCommit(name);
        }
    }

    /**
     * Print the log of a certain commit
     * @param name the SHA-1 of the commit to be printed
     */
    private static void printLog(String name) {
        Commit commit = loadCommit(name);
        System.out.println("===");
        System.out.println("commit " + name);
        if (commit.getSecondParent() != null) {
            System.out.println("Merge: " + commit.getParent().substring(0, 7) + " " + commit.getSecondParent().substring(0, 7));
        }
        Date time = commit.getTime();
        System.out.println(String.format(Locale.US, "Date: %ta %tb %td %tH:%tM:%tS %tY %tz", time, time, time, time, time, time, time, time));
        System.out.println(commit.getMessage());
        System.out.println("");
    }

    /**
     * Print the logs of all commits
     */
    public static void globalLog() {
        List<String> commits = getCommitList();
        for (String name : commits) {
            printLog(name);
        }
    }

    /**
     * Return all commit names as a list
     * @return the SHA-1s of all commits
     */
    private static List<String> getCommitList() {
        return plainFilenamesWithoutOrder(COMMITS);
    }

    /**
     * Print out all SHA-1 of commits with the given commit message
     * @param message the commit message to be found
     */
    public static void find(String message) {
        List<String> commits = getCommitList();
        boolean found = false;
        for (String name : commits) {
            Commit current = loadCommit(name);
            if (current.getMessage().equals(message)) {
                found = true;
                System.out.println(name);
            }
        }
        if (!found) {
            System.err.println("Found no commit with that message.");
        }
    }

    /**
     * Print out the status of the current repository
     */
    public static void status() {
        System.out.println("=== Branches ===");
        List<String> branches = getSortedBranchList();
        for (String branch : branches) {
            if (getHead().equals(branch)) {
                System.out.print("*");
            }
            System.out.println(branch);
        }
        System.out.println("");

        System.out.println("=== Staged Files ===");
        List<String> stagedFiles = getSortedStagedFileList();
        for (String file : stagedFiles) {
            System.out.println(file);
        }
        System.out.println("");

        System.out.println("=== Removed Files ===");
        List<String> removedFiles = getSortedRemovalList();
        for (String file : removedFiles) {
            System.out.println(file);
        }
        System.out.println("");
        
        System.out.println("=== Modifications Not Staged For Commit ===");
        Map<String, Boolean> notStagedFiles = getSortedNotStagedMap();
        for (String file : notStagedFiles.keySet()) {
            System.out.print(file);
            if (notStagedFiles.get(file)) {
                System.out.println(" (modified)");
            } else {
                System.out.println(" (deleted)");
            }
        }
        System.out.println("");

        System.out.println("=== Untracked Files ===");
        Set<String> untrackedFiles = getSortedUntrackedSet();
        for (String file : untrackedFiles) {
            System.out.println(file);
        }
        System.out.println("");
    }

    /**
     * Return a sorted list of all names of branches
     * @return a list of strings
     */
    private static List<String> getSortedBranchList() {
        return plainFilenamesIn(BRANCHES);
    }

    /**
     * Return a sorted list of all names of staged files
     * @return a list of strings
     */
    private static List<String> getSortedStagedFileList() {
        List<String> list = new ArrayList<>(getStagedFileSet());
        list.sort(Comparator.naturalOrder());
        return list;
    }

    /**
     * Return a sorted list of all file names in removal
     * @return a list of strings
     */
    private static List<String> getSortedRemovalList() {
        ArrayList<String> list = new ArrayList<>(loadRemoval());
        list.sort(Comparator.naturalOrder());
        return list;
    }

    /**
     * Return a sorted map of all names of files that are modified but not staged
     * Value = true means modified, and Value = false means deleted
     * @return a map from String to Boolean
     */
    private static Map<String, Boolean> getSortedNotStagedMap() {
        Map<String, String> tree = new HashMap<>(loadHeadCommit().getTree());
        Map<String, String> addition = loadAddition();
        Set<String> removal = loadRemoval();
        Set<String> cwd = new HashSet<>(plainFilenamesWithoutOrder(CWD));
        Map<String, Boolean> notStaged = new TreeMap<>();
        for (String file : cwd) {
            if (addition.containsKey(file)) {
                if (!sha1(readContents(join(CWD, file))).equals(addition.get(file))) {
                    notStaged.put(file, true);
                }
            } else if (tree.containsKey(file) && !removal.contains(file)) {
                if (!sha1(readContents(join(CWD, file))).equals(tree.get(file))) {
                    notStaged.put(file, true);
                }
            }
        }
        for (String file : addition.keySet()) {
            if (!cwd.contains(file)) {
                notStaged.put(file, false);
            }
        }
        for (String file : tree.keySet()) {
            if (!removal.contains(file) && !cwd.contains(file)) {
                notStaged.put(file, false);
            }
        }
        return notStaged;
    }

    /**
     * Return a sorted set of all untracked files in cwd
     * @return a sorted set
     */
    private static Set<String> getSortedUntrackedSet() {
        Set<String> tree = loadHeadCommit().getTree().keySet();
        Set<String> addition = getStagedFileSet();
        Set<String> removal = loadRemoval();
        List<String> cwd = plainFilenamesWithoutOrder(CWD);
        Set<String> untracked = new TreeSet<>();
        for (String file : cwd) {
            if (removal.contains(file) || !addition.contains(file) && !tree.contains(file)) {
                untracked.add(file);
            }
        }
        return untracked;
    }

    /**
     * Put the given file of the given commit in the working directory
     * @param commit the name of the commit
     * @param file the name of the file
     */
    public static void checkoutCommitFile(String commit, String file) {
        checkoutCommitFile(loadCommit(commit), file);
    }

    /**
     * Put the given file of the given commit in the working directory
     * @param commit the given commit object
     * @param file the name of the file
     */
    private static void checkoutCommitFile(Commit commit, String file) {
        if (commit == null) {
            System.err.println("No commit with that id exists.");
            System.exit(0);
        }
        String name = commit.getBlob(file);
        if (name == null) {
            System.err.println("File does not exist in that commit.");
            System.exit(0);
        }
        writeContents(join(CWD, file), readContents(loadBlob(name)));
    }

    /**
     * Return a file object of a blob of the given name
     * @param name the SHA-1 of the blob
     * @return a file object
     */
    private static File loadBlob(String name) {
        return join(BLOBS, name);
    }

    /**
     * Put the given file of the current commit in the working directory
     * @param file the name of the file
     */
    public static void checkoutFile(String file) {
        checkoutCommitFile(loadHeadCommit(), file);
    }

    /**
     * Move from the current branch to the given branch and clear the staging area
     * @param branch the name of the given branch
     */
    public static void checkoutBranch(String branch) {
        Commit source = loadBranchCommit(branch);
        if (source == null) {
            System.err.println("No such branch exists.");
            System.exit(0);
        }
        if (branch.equals(getHead())) {
            System.err.println("No need to checkout the current branch.");
            System.exit(0);
        }
        Commit current = loadHeadCommit();
        Set<String> tracked = new HashSet<>(current.getTree().keySet());
        Set<String> blobs = source.getTree().keySet();
        for (String file : blobs) {
            if (!tracked.contains(file) && join(CWD, file).exists()) {
                System.err.println("There is an untracked file in the way; delete it, or add and commit it first.");
                System.exit(0);
            }
        }
        for (String file : tracked) {
            restrictedDelete(join(CWD, file));
        }
        for (String file : blobs) {
            checkoutCommitFile(source, file);
        }
        clearStaging();
        moveHead(branch);
    }

    /**
     * Clear the staging area
     */
    private static void clearStaging() {
        for (String file : getStagedFileSet()) {
            unstage(file);
        }
        clearRemoval();
    }

    public static void branch(String branch) {
        if (getBranch(branch) != null) {
            System.err.println("A branch with that name already exists.");
            System.exit(0);
        }
        setBranch(branch, getBranch(getHead()));
    }
}