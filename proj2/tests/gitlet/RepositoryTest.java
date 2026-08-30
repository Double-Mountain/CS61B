package gitlet;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Date;

/** Unit tests for Repository.init() and the helpers it exercises. */
public class RepositoryTest {

    @Before
    public void cleanUp() {
        deleteRecursively(Repository.GITLET_DIR);
        deleteRecursively(Utils.join(Repository.CWD, "wug.txt"));
        deleteRecursively(Utils.join(Repository.CWD, "notwug.txt"));
    }

    @Test
    public void testInitCreatesDirectoryStructure() {
        Repository.init();

        assertTrue(Repository.GITLET_DIR.isDirectory());
        assertTrue(Repository.COMMITS.isDirectory());
        assertTrue(Repository.BLOBS.isDirectory());
        assertTrue(Repository.STAGING.isDirectory());
        assertTrue(Repository.BRANCHES.isDirectory());
        assertTrue(Repository.HEAD.isFile());
    }

    @Test
    public void testInitCreatesInitialCommit() {
        Repository.init();

        String expected = Utils.sha1(
                Utils.serialize(new Commit("initial commit", new Date(0), null)));
        File commitFile = Utils.join(Repository.COMMITS, expected);
        assertTrue(commitFile.isFile());

        Commit commit = Utils.readObject(commitFile, Commit.class);
        assertNotNull(commit);
    }

    @Test
    public void testInitPointsMasterAtInitialCommit() {
        Repository.init();

        String expected = Utils.sha1(
                Utils.serialize(new Commit("initial commit", new Date(0), null)));
        File master = Utils.join(Repository.BRANCHES, "master");

        assertTrue(master.isFile());
        assertEquals(expected, Utils.readContentsAsString(master));
    }

    @Test
    public void testInitMovesHeadToMaster() {
        Repository.init();

        assertEquals("master", Utils.readContentsAsString(Repository.HEAD));
    }

    @Test
    public void testAddStagesFile() {
        Repository.init();
        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");

        Repository.add("wug.txt");

        File staged = Utils.join(Repository.STAGING, "wug.txt");
        assertTrue(staged.isFile());
        assertEquals("hello", Utils.readContentsAsString(staged));
    }

    @Test
    public void testAddStagesNewestContent() {
        Repository.init();
        File wug = Utils.join(Repository.CWD, "wug.txt");
        Utils.writeContents(wug, "version 1");
        Repository.add("wug.txt");

        Utils.writeContents(wug, "version 2");
        Repository.add("wug.txt");

        File staged = Utils.join(Repository.STAGING, "wug.txt");
        assertEquals("version 2", Utils.readContentsAsString(staged));
    }

    @Test
    public void testCommitCreatesCommitAndClearsStaging() throws Exception {
        Repository.init();
        String initial = Utils.readContentsAsString(
                Utils.join(Repository.BRANCHES, "master"));

        Utils.writeContents(Utils.join(Repository.CWD, "wug.txt"), "hello");
        Repository.add("wug.txt");
        Repository.commit("first commit");

        String blob = Utils.sha1("hello");
        assertTrue(Utils.join(Repository.BLOBS, blob).isFile());
        assertTrue(Utils.plainFilenamesIn(Repository.STAGING).isEmpty());

        String newCommitId = Utils.readContentsAsString(
                Utils.join(Repository.BRANCHES, "master"));
        assertFalse(initial.equals(newCommitId));

        Commit commit = Utils.readObject(
                Utils.join(Repository.COMMITS, newCommitId), Commit.class);
        assertEquals("first commit", field(commit, "message"));
        assertEquals(initial, field(commit, "parent"));
        assertEquals(blob, commit.getBlob("wug.txt"));
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
