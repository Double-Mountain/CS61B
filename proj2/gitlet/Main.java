package gitlet;

/** Driver class for Gitlet, a subset of the Git version-control system.
 *  @author DoubleMountain
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND1> <OPERAND2> ... 
     */
    public static void main(String[] args) {
        // what if args is empty?
        if (args.length == 0) {
            throw new GitletException("Please enter a command.");
        }
        String firstArg = args[0];
        switch(firstArg) {
            case "init":
                // handle the `init` command
                if (args.length != 1) throw new GitletException("Incorrect operands.");
                Repository.init();
                break;
            case "add":
                // TODO: handle the `add [filename]` command
                break;
            // FILL THE REST IN
            default:
                throw new GitletException("No command with that name exists.");
        }
    }
}
