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
import java.util.Queue;
import java.util.ArrayDeque;

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
    private static File gitletDir = join(CWD, ".gitlet");

    /**
     * Return the branches file object
     * @return a file object
     */
    private static File branches() {
        return join(gitletDir, "branches");
    }

    /**
     * Return the commits file object
     * @return a file object
     */
    private static File commits() {
        return commitsFrom(gitletDir); 
    }
    
    /**
     * Return the blobs file object
     * @return a file object
     */
    private static File blobs() {
        return blobsFrom(gitletDir);
    }

    /**
     * Return the staging file object
     * @return a file object
     */
    private static File staging() {
        return join(gitletDir, "staging");
    }

    /**
     * Return the head file object
     * @return a file object
     */
    private static File head() {
        return join(gitletDir, "head");
    }

    /**
     * Return the removal file object
     * @return a file object
     */
    private static File removal() {
        return join(gitletDir, "removal");
    }

    /**
     * Return the addition file object
     * @return a file object
     */
    private static File addition() {
        return join(gitletDir, "addition");
    }

    /**
     * Return the remotes file object
     * @return a file object
     */
    private static File remotes() {
        return join(gitletDir, "remotes");
    }

    /* fill in the rest of this class. */

    /**
     * Change the gitlet directory to the given directory
     * @param directory the given directory file object
     */
    private static void changeDirectory(File directory) {
        gitletDir = directory;
    }

    /**
     * Initiate the gitlet repository
     */
    public static void init() {
        if (gitletDir.exists()) {
            System.err.println(
                "A Gitlet version-control system already exists in the current directory.");
            System.exit(0);
        }
        gitletDir.mkdir();
        commits().mkdir();
        blobs().mkdir();
        staging().mkdir();
        HashMap<String, File> remotes = new HashMap();
        saveRemotes(remotes);
        HashSet<String> removal = new HashSet<>();
        saveRemoval(removal);
        HashMap<String, String> addition = new HashMap<>();
        saveAddition(addition);
        HashMap<String, String> branches = new HashMap<>();
        saveBranches(branches);
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
        File file = join(commits(), name);
        writeObject(file, commit);
        return name;
    }

    /**
     * Load the commit as an object
     * @param name the SHA-1 of the commit, or a unique abbreviation of it
     * @return an commit object, 
        null if none or multiple commits can be found under the given name 
        or the name itself is null
     */
    private static Commit loadCommit(String name) {
        name = fullCommitName(name);
        if (name == null) {
            return null;
        }
        File file = join(commits(), name);
        return readObject(file, Commit.class);
    }

    /**
     * Get a full and existed commit name out of a given name
     * @param name the given commit name
     * @return the full commit name, 
        null if none or multiple commits can be found under the given name 
        or the name itself is null
     */
    private static String fullCommitName(String name) {
        if (name == null) {
            return null;
        }
        if (name.length() == UID_LENGTH) {
            if (!join(commits(), name).exists()) {
                name = null;
            }
        } else {
            List<String> commits = getCommitList();
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
        return name;
    }

    /**
     * Load the commit that a branch points to as an object, 
     * null if the given branch does not exist
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
        writeContents(head(), branch);
    }

    /**
     * Return the branch name that the head pointer points to
     * @return the branch name
     */
    private static String getHead() {
        return readContentsAsString(head());
    }

    /**
     * Load branches as a hash map
     * @return a hash map object
     */
    private static HashMap<String, String> loadBranches() {
        return readObject(branches(), HashMap.class);
    }

    /**
     * Save a hash map as branches
     * @param branches a hash map object
     */
    private static void saveBranches(HashMap<String, String> branches) {
        writeObject(branches(), branches);
    }

    /**
     * Make a certain branch point to a commit. Create the branch if it does not exist.
     * @param branch the name of the branch
     * @param commit the SHA-1 of the commit
     */
    private static void setBranch(String branch, String commit) {
        HashMap<String, String> branches = loadBranches();
        branches.put(branch, commit);
        saveBranches(branches);
    }

    /**
     * Return the commit that a certain branch points to, null if the branch does not exist
     * @param branch the name of the branch
     * @return the SHA-1 of the commit
     */
    private static String getBranch(String branch) {
        return loadBranches().get(branch);
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
        writeContents(join(staging(), name), readContents(file));
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
        return join(staging(), name).delete();
    }

    /**
     * Load addition as a hash map object
     * @return a hash map object
     */
    private static HashMap<String, String> loadAddition() {
        return readObject(addition(), HashMap.class);
    }

    /**
     * Save a hash map object as addition
     * @param addition a hash map object
     */
    private static void saveAddition(HashMap<String, String> addition) {
        writeObject(addition(), addition);
    }

    /**
     * Add a staged file to addition if it exists
     * @param name the name of the staged file
     * @return whether the file exists in the staging area
     */
    private static boolean addToAddition(String name) {
        File file = join(staging(), name);
        if (!file.exists()) {
            return false;
        }
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
        return join(staging(), name);
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
        writeContents(join(blobs(), blob), contents);
        return blob;
    }

    /**
     * Commit the staging files.
     * @param message the commit message
     */
    public static void commit(String message) {
        setBranch(getHead(), saveCommit(commitAsObject(message)));
    }

    /**
     * Commit the staging files without saving and return a Commit object
     * @param message the commit message
     * @return a commit object
     */
    private static Commit commitAsObject(String message) {
        if (message.isBlank()) {
            System.err.println("Please enter a commit message.");
            System.exit(0);
        }
        if (stagingIsEmpty()) {
            System.err.println("No changes added to the commit.");
            System.exit(0);
        }
        Set<String> stagedFiles = getStagedFileSet();
        HashSet<String> removal = loadRemoval();
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
        return commit;
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
        return readObject(removal(), HashSet.class);
    }

    /**
     * Save the state of the removal
     * @param removal the hash set object to be saved as the removal
     */
    private static void saveRemoval(HashSet<String> removal) {
        writeObject(removal(), removal);
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
            System.out.println("Merge: " + commit.getParent().substring(0, 7) + " "
                + commit.getSecondParent().substring(0, 7));
        }
        Date time = commit.getTime();
        System.out.println(String.format(Locale.US, "Date: %ta %tb %td %tH:%tM:%tS %tY %tz", 
            time, time, time, time, time, time, time, time));
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
        return plainFilenamesWithoutOrder(commits());
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
        List<String> branches = new ArrayList<>(loadBranches().keySet());
        branches.sort(Comparator.naturalOrder());
        return branches;
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
        return join(blobs(), name);
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
        Commit commit = loadBranchCommit(branch);
        if (commit == null) {
            System.err.println("No such branch exists.");
            System.exit(0);
        }
        if (branch.equals(getHead())) {
            System.err.println("No need to checkout the current branch.");
            System.exit(0);
        }
        moveToCommit(commit);
        moveHead(branch);
    }

    /**
     * Move from the current branch to the given commit without moving any branch or head pointer
     * Clear the staging area
     * @param commit the commit object to move to
     */
    private static void moveToCommit(Commit commit) {
        untrackedOverwrittenCheck(commit);
        Set<String> tracked = loadHeadCommit().getTree().keySet();
        Set<String> blobs = commit.getTree().keySet();
        for (String file : tracked) {
            restrictedDelete(join(CWD, file));
        }
        for (String file : blobs) {
            checkoutCommitFile(commit, file);
        }
        clearStaging();
    }

    /**
     * Exit if there is an untracked file will be overwritten by the commit
     * @param commit the commit object that might overwrite an untracked file
     */
    private static void untrackedOverwrittenCheck(Commit commit) {
        Set<String> tracked = loadHeadCommit().getTree().keySet();
        Set<String> blobs = commit.getTree().keySet();
        for (String file : blobs) {
            if (!tracked.contains(file) && join(CWD, file).exists()) {
                System.err.println("There is an untracked file in the way; "
                    + "delete it, or add and commit it first.");
                System.exit(0);
            }
        }
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

    /**
     * Create a new branch with the given name
     * @param branch the name of the branch
     */
    public static void branch(String branch) {
        if (getBranch(branch) != null) {
            System.err.println("A branch with that name already exists.");
            System.exit(0);
        }
        setBranch(branch, getBranch(getHead()));
    }

    /**
     * Remove a branch
     * @param branch the name of the branch
     */
    public static void rmBranch(String branch) {
        if (branch.equals(getHead())) {
            System.err.println("Cannot remove the current branch.");
            System.exit(0);
        }
        HashMap<String, String> branches = loadBranches();
        if (branches.remove(branch) == null) {
            System.err.println("A branch with that name does not exist.");
            System.exit(0);
        }
        saveBranches(branches);
    }

    /**
     * Checkout a given commit and move the current branch head to it
     * @param commit the SHA-1 of the given commit
     */
    public static void reset(String commit) {
        commit = fullCommitName(commit);
        if (commit == null) {
            System.err.println("No commit with that id exists.");
            System.exit(0);
        }
        moveToCommit(loadCommit(commit));
        setBranch(getHead(), commit);
    }

    /**
     * Merge files from a given branch to the current branch
     * @param branch
     */
    public static void merge(String branch) {
        if (!stagingIsEmpty()) {
            System.err.println("You have uncommitted changes.");
            System.exit(0);
        }
        String name = getBranch(branch);
        if (name == null) {
            System.err.println("A branch with that name does not exist.");
            System.exit(0);
        }
        if (branch.equals(getHead())) {
            System.err.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
        Commit commit = loadCommit(name);
        untrackedOverwrittenCheck(commit);

        String head = getBranch(getHead());
        String split = getSplitPoint(name);
        if (split.equals(name)) {
            System.out.println("Given branch is an ancestor of the current branch.");
            return;
        }
        if (split.equals(head)) {
            reset(name);
            System.out.println("Current branch fast-forwarded.");
            return;
        }

        Commit current = loadCommit(head);
        Commit ancestor = loadCommit(split);
        Map<String, String> commitTree = new HashMap<>(commit.getTree());
        Map<String, String> ancestorTree = new HashMap<>(ancestor.getTree());
        Map<String, String> currentTree = current.getTree();
        Map<String, String> changes = new HashMap<>();
        for (String file : commitTree.keySet()) {
            if (!commitTree.get(file).equals(ancestorTree.get(file))) {
                changes.put(file, commitTree.get(file));
            }
        }
        for (String file : ancestorTree.keySet()) {
            if (!commitTree.containsKey(file)) {
                changes.put(file, null);
            }
        }
        boolean conflicted = false;
        for (String file : changes.keySet()) {
            if (changes.get(file) == null) {
                if (currentTree.containsKey(file)) {
                    if (currentTree.get(file).equals(ancestorTree.get(file))) {
                        restrictedDelete(join(CWD, file));
                        addToRemoval(file);
                    } else {
                        writeConflict(current, commit, file);
                        conflicted = true;
                        stage(file);
                    }
                }
            } else if (!changes.get(file).equals(currentTree.get(file))) {
                if (!currentTree.containsKey(file) && !ancestorTree.containsKey(file)
                    || currentTree.containsKey(file)
                        && currentTree.get(file).equals(ancestorTree.get(file))) {
                    writeContents(join(CWD, file), readContents(loadBlob(changes.get(file))));
                    stage(file);
                } else {
                    writeConflict(current, commit, file);
                    conflicted = true;
                    stage(file);
                }
            }
        }
        Commit result = commitAsObject("Merged " + branch + " into " + getHead() + ".");
        result.setParents(head, name);
        setBranch(getHead(), saveCommit(result));
        if (conflicted) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /**
     * Return whether the staging area is empty
     * @return a boolean value
     */
    private static boolean stagingIsEmpty() {
        return loadAddition().isEmpty() && loadRemoval().isEmpty();
    }

    /**
     * Return the latest common ancestor of the current commit and the given commit
     * @param commit the SHA-1 of the given commit
     * @return the SHA-1 of the latest common ancestor
     */
    private static String getSplitPoint(String commit) {
        Queue<String> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.offer(getBranch(getHead()));
        queue.offer(commit);
        String current;
        while (true) {
            current = queue.poll();
            if (visited.contains(current)) {
                break;
            }
            visited.add(current);
            Commit tmp = loadCommit(current);
            String parent = tmp.getParent();
            if (parent != null) {
                queue.offer(parent);
            }
            parent = tmp.getSecondParent();
            if (parent != null) {
                queue.offer(parent);
            }
        }
        return current;
    }

    /**
     * Write conflict changes of the same file into the file in CWD
     * @param current the current commit object
     * @param commit the given commit object
     * @param file the file name
     */
    private static void writeConflict(Commit current, Commit commit, String file) {
        byte[] currentContent = new byte[0];
        byte[] commitContent = new byte[0];
        String currentBlob = current.getBlob(file);
        String commitBlob = commit.getBlob(file);
        if (currentBlob != null) {
            currentContent = readContents(loadBlob(currentBlob));
        }
        if (commitBlob != null) {
            commitContent = readContents(loadBlob(commitBlob));
        }
        writeContents(join(CWD, file), "<<<<<<< HEAD\n", currentContent, "=======\n",
            commitContent, ">>>>>>>\n");
    }

    /**
     * Load the remotes as a hash map
     * @return a hash map object
     */
    private static HashMap<String, File> loadRemotes() {
        return readObject(remotes(), HashMap.class);
    }

    /**
     * Save a hash map to remotes
     * @param remotes a hash map object
     */
    private static void saveRemotes(HashMap<String, File> remotes) {
        writeObject(remotes(), remotes);
    }

    /**
     * Add a remote directory
     * @param name the name of the remote directory
     * @param directory the path of the remote directory
     */
    public static void addRemote(String name, String path) {
        HashMap<String, File> remotes = loadRemotes();
        if (remotes.get(name) != null) {
            System.err.println("A remote with that name already exists.");
            System.exit(0);
        }
        remotes.put(name, new File(path.replace("/", File.separator)));
        saveRemotes(remotes);
    }

    /**
     * Return a remote directory as a file object, return null if it does not exist
     * @return a file object
     */
    private static File getRemote(String name) {
        return loadRemotes().get(name);
    }

    /**
     * Remove a remote directory
     * @param name the name of the remote directory
     */
    public static void rmRemote(String name) {
        HashMap<String, File> remotes = loadRemotes();
        if (remotes.get(name) == null) {
            System.err.println("A remote with that name does not exist.");
            System.exit(0);
        }
        remotes.remove(name);
        saveRemotes(remotes);
    }

    public static void push(String remote, String branch) {
        checkRemoteExists(remote);
        changeToRemote(remote);
        String commit = getBranch(branch);
        backToLocal();
        if (commit != null && (loadCommit(commit) == null
            || !getSplitPoint(commit).equals(commit))) {
            System.err.println("Please pull down remote changes before pushing.");
            System.exit(0);
        }
        String latest = getBranch(getHead());
        transport(gitletDir, getRemote(remote), latest);
        changeToRemote(remote);
        setBranch(branch, latest);
        moveHead(branch);
    }

    /**
     * Change back to the local .gitlet directory
     */
    private static void backToLocal() {
        changeDirectory(join(CWD, ".gitlet"));
    }

    /**
     * Change to the remote directory with the given name
     * @param name the name of the remote directory
     */
    private static void changeToRemote(String name) {
        backToLocal();
        changeDirectory(getRemote(name));
    }

    /**
     * Exit if the given remote directory does not exist
     * @param name the name of the remote directory
     */
    private static void checkRemoteExists(String name) {
        File remote = getRemote(name);
        if (remote == null || !remote.exists()) {
            System.err.println("Remote directory not found.");
            System.exit(0);
        }
    }

    /**
     * Transport the full history of a commit from src to dst
     * @param src the source directory
     * @param dst the destination directory
     * @param commit the SHA-1 of the commit
     */
    private static void transport(File src, File dst, String commit) {
        Queue<String> queue = new ArrayDeque<>();
        if (join(commitsFrom(dst), commit).exists()) {
            return;
        }
        queue.offer(commit);
        while (!queue.isEmpty()) {
            String name = queue.poll();
            Commit current = readObject(join(commitsFrom(src), name), Commit.class);
            String parent = current.getParent();
            if (parent != null && !join(commitsFrom(dst), parent).exists()) {
                queue.offer(parent);
            }
            parent = current.getSecondParent();
            if (parent != null && !join(commitsFrom(dst), parent).exists()) {
                queue.offer(parent);
            }
            writeObject(join(commitsFrom(dst), name), current);
            for (String blob : current.getTree().values()) {
                if (!join(blobsFrom(dst), blob).exists()) {
                    writeContents(join(blobsFrom(dst), blob),
                        readContents(join(blobsFrom(src), blob)));
                }
            }
        }
    }

    /**
     * Return the commits file object from the given directory
     * @param directory the file object of the given directory
     */
    private static File commitsFrom(File directory) {
        return join(directory, "commits");
    }

    /**
     * Return the blobs file object from the given directory
     * @param directory the blobs object of the given directory
     * @return
     */
    private static File blobsFrom(File directory) {
        return join(directory, "blobs");
    }

    /**
     * Bring down the given branch from the given remote
     * @param remote the given remote
     * @param branch the given branch
     */
    public static void fetch(String remote, String branch) {
        checkRemoteExists(remote);
        changeToRemote(remote);
        if (getBranch(branch) == null) {
            System.err.println("That remote does not have that branch.");
            System.exit(0);
        }
        String commit = getBranch(branch);
        backToLocal();
        transport(getRemote(remote), gitletDir, commit);
        setBranch(remote + "/" + branch, commit);
    }

    /**
     * Fetch a remote branch and merge it to the current branch
     * @param remote the given remote
     * @param branch the given branch
     */
    public static void pull(String remote, String branch) {
        fetch(remote, branch);
        merge(remote + "/" + branch);
    }

    /**
     * Return the .gitlet director as a file object
     * @return a file object
     */
    public static File getGitletDir() {
        return gitletDir;
    }
}
