package deque;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class ArrayDequeRandomTest {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> a = new AListNoResizing<>();
        ArrayDeque<Integer> b = new ArrayDeque<>();
        a.addLast(4);
        a.addLast(5);
        a.addLast(6);
        b.addLast(4);
        b.addLast(5);
        b.addLast(6);
        assertEquals(a.removeLast(), b.removeLast());
        assertEquals(a.removeLast(), b.removeLast());
        assertEquals(a.removeLast(), b.removeLast());
    }
    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> correct = new AListNoResizing<>();
        ArrayDeque<Integer> broken = new ArrayDeque<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                correct.addLast(randVal);
                broken.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = correct.size();
                int bsize = broken.size();
                assertEquals(size, bsize);
            } else if (operationNumber == 2) {
                int last = 0;
                int blast = 0;
                if (correct.size() > 0) {
                    last = correct.getLast();
                }
                if (broken.size() > 0) {
                    blast = broken.get(broken.size() - 1);
                }
                assertEquals(last, blast);
            } else if (operationNumber == 3) {
                int last = 0;
                int blast = 0;
                if (correct.size() > 0) {
                    last = correct.removeLast();
                }
                if (broken.size() > 0) {
                    blast = broken.removeLast();
                }
                assertEquals(last, blast);
            }
        }
    }
}
