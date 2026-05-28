package tester;
import static org.junit.Assert.*;
import org.junit.Test;
import student.StudentArrayDeque;
import edu.princeton.cs.introcs.StdRandom;

public class TestArrayDequeEC {
   @Test
   /**
    * @source StudentArrayDequeLauncher
    */
   public void testMethod() {
      String message = "";
      StudentArrayDeque<Integer> sad1 = new StudentArrayDeque<>();
      ArrayDequeSolution<Integer> sad2 = new ArrayDequeSolution<>();

        for (int i = 0; i < 100000; i += 1) {
            double numberBetweenZeroAndOne = StdRandom.uniform();

            if (numberBetweenZeroAndOne < 0.25) {
               sad1.addFirst(i);
               sad2.addFirst(i);
               message += "addFirst(" + i + ")\n";
            } else if (numberBetweenZeroAndOne < 0.5) {
               sad1.addLast(i);
               sad2.addLast(i);
               message += "addLast(" + i + ")\n";
            } else if (numberBetweenZeroAndOne < 0.75 && !sad1.isEmpty() && !sad2.isEmpty()) {
               Integer actual = sad1.removeFirst();
               Integer expected = sad2.removeFirst();
               message += "removeFirst()\n";
               assertEquals(message, expected, actual);
            } else if (!sad1.isEmpty() && !sad2.isEmpty()){
               Integer actual = sad1.removeLast();
               Integer expected = sad2.removeLast();
               message += "removeLast()\n";
               assertEquals(message, expected, actual);
            }
        }

        sad1.printDeque();
   } 
}
