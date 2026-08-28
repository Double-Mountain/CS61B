package gitlet;

import org.junit.Test;
import static org.junit.Assert.*;

/** Simple JUnit 4 tests for the provided Utils helpers.
 *  These are intentionally independent of the not-yet-implemented
 *  Repository/Main logic, so they should pass out of the box.
 */
public class UtilsTest {

    @Test
    public void testJoin() {
        assertEquals("foo/bar/baz", Utils.join("foo", "bar", "baz").toString());
    }

    @Test
    public void testSha1KnownValue() {
        // SHA-1 of "abc" from FIPS 180-1 test vector.
        assertEquals(
                "a9993e364706816aba3e25717850c26c9cd0d89d",
                Utils.sha1("abc"));
    }
}
