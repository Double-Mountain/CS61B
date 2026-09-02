package gitlet;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/** Unit tests for Repository.init() and the helpers it exercises. */
public class RepositoryTest {

    @Before
    public void cleanUp() {
        deleteRecursively(Repository.gitlet_dir);
        deleteRecursively(Utils.join(Repository.CWD, "wug.txt"));
        deleteRecursively(Utils.join(Repository.CWD, "notwug.txt"));
        deleteRecursively(Utils.join(Repository.CWD, "a.txt"));
        deleteRecursively(Utils.join(Repository.CWD, "b.txt"));
        deleteRecursively(Utils.join(Repository.CWD, "c.txt"));
        deleteRecursively(Utils.join(Repository.CWD, "d.txt"));
    }

    @Test
    public void testInitCreatesDirectoryStructure() {
        Repository.init();

        assertTrue(Repository.gitlet_dir.isDirectory());
        assertTrue(Utils.join(Repository.gitlet_dir, "commits").isDirectory());
        assertTrue(Utils.join(Repository.gitlet_dir, "blobs").isDirectory());
        assertTrue(Utils.join(Repository.gitlet_dir, "staging").isDirectory());
        assertTrue(Utils.join(Repository.gitlet_dir, "branches").isFile());
        assertTrue(Utils.join(Repository.gitlet_dir, "head").isFile());
        assertTrue(Utils.join(Repository.gitlet_dir, "removal").isFile());
        assertTrue(Utils.join(Repository.gitlet_dir, "addition").isFile());
        assertTrue(Utils.join(Repository.gitlet_dir, "remotes").isFile());
    }

    @Test
    public void testInitCreatesInitialCommit() {
        Repository.init();

        String expected = Utils.sha1(
                Utils.serialize(new Commit("initial commit", new Date(0), null)));
        File commitFile = Utils.join(Utils.join(Repository.gitlet_dir, "commits"), expected);
        assertTrue(commitFile.isFile());

        Commit commit = Utils.readObject(commitFile, Commit.class);
        assertNotNull(commit);
    }

    @Test
    public void testInitPointsMasterAtInitialCommit() {
        Repository.init();

        String expected = Utils.sha1(
                Utils.serialize(new Commit("initial commit", new Date(0), null)));

        assertEquals(expected, branchCommit("master"));
    }

    @Test
    public void testInitMovesHeadToMaster() {
        Repository.init();

        assertEquals("master", Utils.readContentsAsString(Utils.join(Repository.gitlet_dir, "head")));
    }

    @Test
    public void testAddStagesFile() {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");

        Repository.add("wug.txt");

        File staged = Utils.join(Utils.join(Repository.gitlet_dir, "staging"), "wug.txt");
        assertTrue(staged.isFile());
        assertEquals("hello", Utils.readContentsAsString(staged));
        assertEquals(Utils.sha1("hello"), loadAddition().get("wug.txt"));
    }

    @Test
    public void testAddStagesNewestContent() {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "version 1");
        Repository.add("wug.txt");

        Utils.writeContents(wug, "version 2");
        Repository.add("wug.txt");

        File staged = Utils.join(Utils.join(Repository.gitlet_dir, "staging"), "wug.txt");
        assertEquals("version 2", Utils.readContentsAsString(staged));
        assertEquals(Utils.sha1("version 2"), loadAddition().get("wug.txt"));
    }

    @Test
    public void testCommitCreatesCommitAndClearsStaging() throws Exception {
        Repository.init();
        String initial = branchCommit("master");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");
        Repository.add("wug.txt");
        Repository.commit("first commit");

        String blob = Utils.sha1("hello");
        assertTrue(Utils.join(Utils.join(Repository.gitlet_dir, "blobs"), blob).isFile());
        assertTrue(Utils.plainFilenamesIn(Utils.join(Repository.gitlet_dir, "staging")).isEmpty());
        assertTrue(loadAddition().isEmpty());

        String newCommitId = branchCommit("master");
        assertFalse(initial.equals(newCommitId));

        Commit commit = Utils.readObject(
                Utils.join(Utils.join(Repository.gitlet_dir, "commits"), newCommitId), Commit.class);
        assertEquals("first commit", field(commit, "message"));
        assertEquals(initial, field(commit, "parent"));
        assertEquals(blob, commit.getBlob("wug.txt"));
    }

    @Test
    public void testRmUnstagesStagedFile() {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "hello");
        Repository.add("wug.txt");
        assertTrue(Utils.join(Utils.join(Repository.gitlet_dir, "staging"), "wug.txt").isFile());

        Repository.rm("wug.txt");

        assertTrue(wug.isFile());
        assertFalse(Utils.join(Utils.join(Repository.gitlet_dir, "staging"), "wug.txt").exists());
        assertTrue(loadRemoval().isEmpty());
        assertTrue(loadAddition().isEmpty());
    }

    @Test
    public void testRmTrackedFileStagesRemovalAndDeletesWorkingFile() {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");
        Repository.add("wug.txt");
        Repository.commit("added wug");

        Repository.rm("wug.txt");

        assertFalse(Utils.join(Repository.CWD, "wug.txt").exists());
        assertTrue(loadRemoval().contains("wug.txt"));
    }

    @Test
    public void testAddCancelsRemoval() {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");
        Repository.add("wug.txt");
        Repository.commit("added wug");
        Repository.rm("wug.txt");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "new content");
        Repository.add("wug.txt");

        assertFalse(loadRemoval().contains("wug.txt"));
        assertTrue(Utils.join(Utils.join(Repository.gitlet_dir, "staging"), "wug.txt").isFile());
    }

    @Test
    public void testCommitUntracksRemovedFile() throws Exception {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");
        Repository.add("wug.txt");
        Repository.commit("added wug");
        Repository.rm("wug.txt");
        Repository.commit("removed wug");

        String newCommitId = branchCommit("master");
        Commit commit = Utils.readObject(
                Utils.join(Utils.join(Repository.gitlet_dir, "commits"), newCommitId), Commit.class);
        assertNull(commit.getBlob("wug.txt"));
        assertTrue(loadRemoval().isEmpty());
    }

    @Test
    public void testLogPrintsCommitsNewestFirst() throws Exception {
        Repository.init();
        String initial = branchCommit("master");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v1");
        Repository.add("wug.txt");
        Repository.commit("first message");
        String first = branchCommit("master");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v2");
        Repository.add("wug.txt");
        Repository.commit("second message");
        String second = branchCommit("master");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer));
        try {
            Repository.log();
        } finally {
            System.setOut(original);
        }
        String output = buffer.toString();

        assertTrue(output.contains("commit " + second));
        assertTrue(output.contains("commit " + first));
        assertTrue(output.contains("commit " + initial));
        assertTrue(output.contains("Date: "));
        assertTrue(output.indexOf("second message") < output.indexOf("first message"));
        assertTrue(output.indexOf("first message") < output.indexOf("initial commit"));
    }

    @Test
    public void testGlobalLogPrintsAllCommits() throws Exception {
        Repository.init();
        String initial = branchCommit("master");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v1");
        Repository.add("wug.txt");
        Repository.commit("first message");
        String first = branchCommit("master");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v2");
        Repository.add("wug.txt");
        Repository.commit("second message");
        String second = branchCommit("master");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer));
        try {
            Repository.globalLog();
        } finally {
            System.setOut(original);
        }
        String output = buffer.toString();

        assertTrue(output.contains("commit " + initial));
        assertTrue(output.contains("commit " + first));
        assertTrue(output.contains("commit " + second));
        assertTrue(output.contains("initial commit"));
        assertTrue(output.contains("first message"));
        assertTrue(output.contains("second message"));
        assertEquals(3, output.split("===", -1).length - 1);
    }

    @Test
    public void testFindPrintsMatchingCommitIds() throws Exception {
        Repository.init();

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v1");
        Repository.add("wug.txt");
        Repository.commit("first message");
        String first = branchCommit("master");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v2");
        Repository.add("wug.txt");
        Repository.commit("second message");
        String second = branchCommit("master");

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v3");
        Repository.add("wug.txt");
        Repository.commit("first message");
        String third = branchCommit("master");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer));
        try {
            Repository.find("first message");
        } finally {
            System.setOut(original);
        }
        String output = buffer.toString();

        assertTrue(output.contains(first));
        assertTrue(output.contains(third));
        assertFalse(output.contains(second));
        assertEquals(2, output.trim().split("\n").length);
    }

    @Test
    public void testStatusShowsBranchesAndStagedFiles() throws Exception {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");
        Repository.add("wug.txt");

        String output = captureStatus();

        assertTrue(output.contains("=== Branches ==="));
        assertTrue(output.contains("*master"));
        assertTrue(output.contains("=== Staged Files ==="));
        assertTrue(output.contains("wug.txt"));
        assertTrue(output.contains("=== Modifications Not Staged For Commit ==="));
    }

    @Test
    public void testStatusShowsModifiedAndDeletedFiles() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "hello");
        Repository.add("wug.txt");
        Repository.commit("added wug");

        Utils.writeContents(wug, "changed");
        String modified = captureStatus();
        assertTrue(modified.contains("wug.txt (modified)"));

        wug.delete();
        String deleted = captureStatus();
        assertTrue(deleted.contains("wug.txt (deleted)"));
    }

    @Test
    public void testStatusListsBranchesInSortedOrder() throws Exception {
        Repository.init();
        Repository.branch("zeta");
        Repository.branch("alpha");

        String output = captureStatus();

        assertTrue(output.indexOf("alpha") < output.indexOf("master"));
        assertTrue(output.indexOf("master") < output.indexOf("zeta"));
    }

    @Test
    public void testStatusListsStagedAndUntrackedFilesInSortedOrder() throws Exception {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "c.txt"), "v1");
        Utils.writeContents(Utils.join(Repository.CWD, "a.txt"), "v1");
        Utils.writeContents(Utils.join(Repository.CWD, "b.txt"), "v2");
        Utils.writeContents(Utils.join(Repository.CWD, "d.txt"), "v2");
        Repository.add("a.txt");
        Repository.add("c.txt");

        String output = captureStatus();

        assertTrue(output.indexOf("a.txt") < output.indexOf("c.txt"));
        assertTrue(output.indexOf("b.txt") < output.indexOf("d.txt"));
    }

    @Test
    public void testStatusListsModifiedAndRemovedFilesInSortedOrder() throws Exception {
        Repository.init();
        File a = Utils.join(Repository.CWD, "a.txt");
        File c = Utils.join(Repository.CWD, "c.txt");
        Utils.writeContents(a, "v1");
        Utils.writeContents(c, "v1");
        Repository.add("a.txt");
        Repository.add("c.txt");
        Repository.commit("added a and c");

        Utils.writeContents(a, "changed");
        Utils.writeContents(c, "changed");
        String modified = captureStatus();
        assertTrue(modified.indexOf("a.txt (modified)") < modified.indexOf("c.txt (modified)"));

        Repository.rm("a.txt");
        Repository.rm("c.txt");
        String removed = captureStatus();
        assertTrue(removed.indexOf("a.txt") < removed.indexOf("c.txt"));
    }

    @Test
    public void testCheckoutFileRestoresCommittedVersion() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1");
        Repository.add("wug.txt");
        Repository.commit("added wug");
        Utils.writeContents(wug, "v2");

        Repository.checkoutFile("wug.txt");

        assertEquals("v1", Utils.readContentsAsString(wug));
    }

    @Test
    public void testCheckoutCommitFileRestoresOldVersion() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1");
        Repository.add("wug.txt");
        Repository.commit("first");
        String first = branchCommit("master");
        Utils.writeContents(wug, "v2");
        Repository.add("wug.txt");
        Repository.commit("second");

        Repository.checkoutCommitFile(first, "wug.txt");

        assertEquals("v1", Utils.readContentsAsString(wug));
    }

    @Test
    public void testCheckoutCommitFileAcceptsAbbreviation() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1");
        Repository.add("wug.txt");
        Repository.commit("first");
        String first = branchCommit("master");
        Utils.writeContents(wug, "v2");
        Repository.add("wug.txt");
        Repository.commit("second");

        Repository.checkoutCommitFile(first.substring(0, 16), "wug.txt");

        assertEquals("v1", Utils.readContentsAsString(wug));
    }

    @Test
    public void testBranchPointsAtHeadCommit() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1");
        Repository.add("wug.txt");
        Repository.commit("v1");
        String head = branchCommit("master");

        Repository.branch("feature");

        assertTrue(loadBranchesMap().containsKey("feature"));
        assertEquals(head, branchCommit("feature"));
    }

    @Test
    public void testRmBranchDeletesBranch() throws Exception {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "v1");
        Repository.add("wug.txt");
        Repository.commit("v1");
        Repository.branch("feature");
        assertTrue(loadBranchesMap().containsKey("feature"));

        Repository.rmBranch("feature");

        assertFalse(loadBranchesMap().containsKey("feature"));
    }

    @Test
    public void testCheckoutBranchSwitchesHeadAndFiles() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1");
        Repository.add("wug.txt");
        Repository.commit("v1");
        Repository.branch("other");

        Utils.writeContents(wug, "v2");
        Repository.add("wug.txt");
        Repository.commit("v2");

        Utils.writeContents(wug, "v3");
        Repository.add("wug.txt");

        Repository.checkoutBranch("other");

        assertEquals("other", Utils.readContentsAsString(Utils.join(Repository.gitlet_dir, "head")));
        assertEquals("v1", Utils.readContentsAsString(wug));
        assertTrue(loadAddition().isEmpty());
        assertTrue(loadRemoval().isEmpty());
        assertTrue(Utils.plainFilenamesIn(Utils.join(Repository.gitlet_dir, "staging")).isEmpty());
    }

    @Test
    public void testResetMovesBranchToOldCommit() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1");
        Repository.add("wug.txt");
        Repository.commit("v1");
        String first = branchCommit("master");
        Utils.writeContents(wug, "v2");
        Repository.add("wug.txt");
        Repository.commit("v2");

        Utils.writeContents(wug, "v3");
        Repository.add("wug.txt");

        Repository.reset(first);

        assertEquals(first, branchCommit("master"));
        assertEquals("v1", Utils.readContentsAsString(wug));
        assertTrue(loadAddition().isEmpty());
        assertTrue(loadRemoval().isEmpty());
        assertTrue(Utils.plainFilenamesIn(Utils.join(Repository.gitlet_dir, "staging")).isEmpty());
    }

    @Test
    public void testResetAcceptsAbbreviation() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1");
        Repository.add("wug.txt");
        Repository.commit("v1");
        String first = branchCommit("master");
        Utils.writeContents(wug, "v2");
        Repository.add("wug.txt");
        Repository.commit("v2");

        Repository.reset(first.substring(0, 16));

        assertEquals(first, branchCommit("master"));
        assertEquals("v1", Utils.readContentsAsString(wug));
    }

    @Test
    public void testMergeFastForward() throws Exception {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "v1\n");
        Repository.add("wug.txt");
        Repository.commit("v1");
        Repository.branch("feature");
        Repository.checkoutBranch("feature");

        Utils.writeContents(wug, "v2\n");
        Repository.add("wug.txt");
        Repository.commit("v2");
        String feature = branchCommit("feature");
        Repository.checkoutBranch("master");

        String output = captureMerge("feature");

        assertEquals(feature, branchCommit("master"));
        assertEquals("v2\n", Utils.readContentsAsString(wug));
        assertTrue(output.contains("Current branch fast-forwarded."));
    }

    @Test
    public void testMergeCreatesMergeCommit() throws Exception {
        Repository.init();
        File a = Utils.join(Repository.CWD, "a.txt");
        Utils.writeContents(a, "a1\n");
        Repository.add("a.txt");
        Repository.commit("base");
        Repository.branch("feature");
        Repository.checkoutBranch("feature");

        File b = Utils.join(Repository.CWD, "b.txt");
        Utils.writeContents(b, "b1\n");
        Repository.add("b.txt");
        Repository.commit("feature b");
        String feature = branchCommit("feature");
        Repository.checkoutBranch("master");

        File c = Utils.join(Repository.CWD, "c.txt");
        Utils.writeContents(c, "c1\n");
        Repository.add("c.txt");
        Repository.commit("master c");
        String master = branchCommit("master");

        String output = captureMerge("feature");
        String mergedId = branchCommit("master");
        Commit merged = Utils.readObject(
                Utils.join(Utils.join(Repository.gitlet_dir, "commits"), mergedId), Commit.class);

        assertEquals(master, merged.getParent());
        assertEquals(feature, merged.getSecondParent());
        assertEquals("Merged feature into master.", merged.getMessage());
        assertEquals(Utils.sha1("a1\n"), merged.getBlob("a.txt"));
        assertEquals(Utils.sha1("b1\n"), merged.getBlob("b.txt"));
        assertEquals(Utils.sha1("c1\n"), merged.getBlob("c.txt"));
        assertEquals("b1\n", Utils.readContentsAsString(b));
        assertFalse(output.contains("Encountered a merge conflict."));
    }

    @Test
    public void testMergeConflict() throws Exception {
        Repository.init();
        File a = Utils.join(Repository.CWD, "a.txt");
        Utils.writeContents(a, "base\n");
        Repository.add("a.txt");
        Repository.commit("base");
        Repository.branch("feature");
        Repository.checkoutBranch("feature");

        Utils.writeContents(a, "feature\n");
        Repository.add("a.txt");
        Repository.commit("feature");
        String feature = branchCommit("feature");
        Repository.checkoutBranch("master");

        Utils.writeContents(a, "master\n");
        Repository.add("a.txt");
        Repository.commit("master");
        String master = branchCommit("master");

        String output = captureMerge("feature");
        String mergedId = branchCommit("master");
        Commit merged = Utils.readObject(
                Utils.join(Utils.join(Repository.gitlet_dir, "commits"), mergedId), Commit.class);

        assertEquals(master, merged.getParent());
        assertEquals(feature, merged.getSecondParent());
        assertTrue(output.contains("Encountered a merge conflict."));
        assertEquals("<<<<<<< HEAD\nmaster\n=======\nfeature\n>>>>>>>\n",
                Utils.readContentsAsString(a));
    }

    @Test
    public void testAddRemoteStoresRemote() throws Exception {
        Repository.init();
        String path = Utils.join(Repository.CWD, "remote-repo", ".gitlet").getPath();

        Repository.addRemote("origin", path);

        assertTrue(loadRemotesMap().containsKey("origin"));
        assertEquals(new File(path), loadRemotesMap().get("origin"));
    }

    @Test
    public void testRmRemoteDeletesRemote() throws Exception {
        Repository.init();
        String path = Utils.join(Repository.CWD, "remote-repo", ".gitlet").getPath();
        Repository.addRemote("origin", path);
        assertTrue(loadRemotesMap().containsKey("origin"));

        Repository.rmRemote("origin");

        assertFalse(loadRemotesMap().containsKey("origin"));
    }

    private static String captureMerge(String branch) throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer));
        try {
            Repository.merge(branch);
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    private static String captureStatus() throws Exception {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream original = System.out;
        System.setOut(new PrintStream(buffer));
        try {
            Repository.status();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }

    private static HashSet<String> loadRemoval() {
        return Utils.readObject(Utils.join(Repository.gitlet_dir, "removal"), HashSet.class);
    }

    private static HashMap<String, String> loadAddition() {
        return Utils.readObject(Utils.join(Repository.gitlet_dir, "addition"), HashMap.class);
    }

    private static HashMap<String, String> loadBranchesMap() {
        return Utils.readObject(Utils.join(Repository.gitlet_dir, "branches"), HashMap.class);
    }

    private static HashMap<String, File> loadRemotesMap() {
        return Utils.readObject(Utils.join(Repository.gitlet_dir, "remotes"), HashMap.class);
    }

    private static String branchCommit(String branch) {
        return loadBranchesMap().get(branch);
    }

    private static Object field(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }

    private static void deleteRecursively(File file) {
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteRecursively(child);
                }
            }
        }
        file.delete();
    }
}
