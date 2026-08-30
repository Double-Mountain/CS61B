package gitlet;

import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

/** Unit tests for the Commit constructors that were added for
 *  repository initialization.
 */
public class CommitTest {

    @Test
    public void testSingleParentConstructor() throws Exception {
        Date time = new Date(1000L);
        Commit commit = new Commit("message", time, "parent1");

        assertEquals("message", field(commit, "message"));
        assertEquals(time, field(commit, "timestamp"));
        assertEquals("parent1", field(commit, "parent"));
        assertNull(field(commit, "secondParent"));
        assertNotNull(field(commit, "tree"));
        assertTrue(((Map<?, ?>) field(commit, "tree")).isEmpty());
    }

    @Test
    public void testTwoParentConstructor() throws Exception {
        Date time = new Date(2000L);
        Commit commit = new Commit("merge", time, "parent1", "parent2");

        assertEquals("merge", field(commit, "message"));
        assertEquals(time, field(commit, "timestamp"));
        assertEquals("parent1", field(commit, "parent"));
        assertEquals("parent2", field(commit, "secondParent"));
    }

    @Test
    public void testGetBlobReturnsNullWhenMissing() {
        Commit commit = new Commit("message", new Date(0), null);
        assertNull(commit.getBlob("wug.txt"));
    }

    @Test
    public void testGetBlobReturnsMappedBlob() throws Exception {
        Commit commit = new Commit("message", new Date(0), null);
        Map<String, String> tree = (Map<String, String>) field(commit, "tree");
        tree.put("wug.txt", "abc123");

        assertEquals("abc123", commit.getBlob("wug.txt"));
    }

    /** Returns the value of the private field named NAME on OBJ. */
    private static Object field(Object obj, String name) throws Exception {
        Field f = obj.getClass().getDeclaredField(name);
        f.setAccessible(true);
        return f.get(obj);
    }
}
