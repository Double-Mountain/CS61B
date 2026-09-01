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
            case "commit":
                // handle the 'commit [message]' command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.commit(args[1]);
                break;
            case "rm":
                // handel the 'rm [file name]' command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.rm(args[1]);
                break;
            case "log":
                // handel the 'log' command
                if (args.length != 1) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.log();
                break;
            case "global-log":
                // handel the 'global-log' command
                if (args.length != 1) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.globalLog();
                break;
            case "find":
                // handel the 'find [commit message]' command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.find(args[1]);
                break;
            case "status":
                // handel the 'status' command
                if (args.length != 1) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.status();
                break;
            case "checkout":
                // handle the 'checkout' command
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                if (args.length == 3 && args[1].equals("--")) {
                    Repository.checkoutFile(args[2]);
                } else if (args.length == 4 && args[2].equals("--")) {
                    Repository.checkoutCommitFile(args[1], args[3]);
                } else if (args.length == 2) {
                    Repository.checkoutBranch(args[1]);
                } else {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                break;
            case "branch":
                // handel the 'branch [branch name]' command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.branch(args[1]);
                break;
            case "rm-branch":
                // handel the 'rm-branch [branch name]' command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.rmBranch(args[1]);
                break;
            case "reset":
                // handel the 'reset [commit id]' command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.reset(args[1]);
                break;
            case "merge":
                // handel the 'merge [branch name]' command
                if (args.length != 2) {
                    System.err.println(OPERANDS);
                    System.exit(0);
                }
                if (!Repository.GITLET_DIR.exists()) {
                    System.err.println(NO_DIRECTORY);
                    System.exit(0);
                }
                Repository.merge(args[1]);
                break;
            default:
                System.err.println("No command with that name exists.");
        }
    }
}
