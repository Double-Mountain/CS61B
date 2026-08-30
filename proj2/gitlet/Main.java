package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author DoubleMountain
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static final String OPERANDS = "Incorrect operands.";
    public static final String NO_DIRECTORY = "Not in an initialized Gitlet directory.";
    public static void main(String[] args) {
        // what if args is empty?
        if (args.length == 0) {
            System.err.println("Please enter a command.");
            System.exit(0);
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // handle the `init` command
                if (args.length != 1) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                Repository.init();
                break;
            case "add":
                // handle the `add [filename]` command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.add(args[1]);
                break;
            // FILL THE REST IN
            default:
                System.err.println("No command with that name exists.");
                System.exit(0);
        }
    }
}
