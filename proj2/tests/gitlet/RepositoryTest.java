package gitlet;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import java.io.File;
import java.util.Date;
import java.util.HashMap;

/** Unit tests for Repository.init() and the helpers it exercises. */
public class RepositoryTest {

    @Before
    public void cleanUp() {
        deleteRecursively(Repository.GITLET_DIR);
    }

    @Test
    public void testInitCreatesDirectoryStructure() {
        Repository.init();

        assertTrue(Repository.GITLET_DIR.isDirectory());
        assertTrue(Repository.COMMITS.isDirectory());
        assertTrue(Repository.BLOBS.isDirectory());
        assertTrue(Repository.BRANCHES.isFile());
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
    public void testInitSetsMasterAndHead() {
        Repository.init();

        String expected = Utils.sha1(
                Utils.serialize(new Commit("initial commit", new Date(0), null)));
        HashMap<String, String> branches =
                Utils.readObject(Repository.BRANCHES, HashMap.class);

        assertEquals(expected, branches.get("master"));
        assertEquals(expected, branches.get("HEAD"));
    }

    @Test
    public void testInitTwiceThrows() {
        Repository.init();

        try {
            Repository.init();
            fail("Expected GitletException");
        } catch (GitletException excp) {
            assertEquals("A Gitlet version-control system already exists in the current directory.",
                    excp.getMessage());
        }
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
