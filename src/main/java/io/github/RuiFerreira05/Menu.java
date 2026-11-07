package io.github.RuiFerreira05;

import io.github.RuiFerreira05.annotations.MenuConfig;

import java.util.List;

/**
 * com.serverofglass.Menu representation.
 * <p>
 * Must be extended and annotated with {@link MenuConfig MenuConfig} to implement a menu in the CLI.
 * <p>
 * In {@link Cli}, this class serves as the main reference to the implemented Menus.
 *
 * @see Cli
 * @see MenuConfig
 */
public class Menu {

    /**
     * Pointer back to the associated {@link Cli}.
     * Initialized by the CLI itself during reflection.
     */
    private Cli cli;

    /**
     * Display a string in the terminal.
     * <p>
     * Recommended over {@code System.in.println()} for future extensions to its implementation, even though right now
     * they do the same thing.
     *
     * @param str The string to display
     */
    protected void display(String str) {
        cli.display(str);
    }

    /**
     * Display a string in the terminal, with support for string formatting.
     * <p>
     * Does the same as {@link #display}, but formatting the string first.
     *
     * @param str The string to print, with support for formatting
     * @param objects The objects to insert in the string
     *
     * @see display
     */
    protected void displayf(String str, Object... objects) {
        cli.display(String.format(str, objects));
    }

    /**
     * Move to another menu
     *
     * @param menu The menu class to move to
     */
    protected void moveTo(Class<? extends Menu> menu) {
        cli.moveTo(menu);
    }

    /**
     * @param menu An instance of a menu
     *
     * @see #moveTo(Class)
     */
    protected void moveTo(Menu menu) {
        cli.moveTo(menu);
    }

    /**
     *  Move back in the stack.
     *  <p>
     *  Corresponds to pressing "go back" in a submenu.
     *  This method will do nothing if there are no other Menus to move to in the stack
     */
    protected void moveBack() {
        cli.moveBack();
    }

    /**
     * @param destroy If the menu instance should not be stored in the {@link Cli CLI's} object history array,
     *                in case {@link Cli#errorHistory(boolean) errorHistory} in the CLI is set to true.
     *                Does nothing if false.
     *
     * @see #moveback
     * @see Cli#errorHistory(boolean)
     */
    protected void moveback(boolean destroy) {
        cli.moveBack(destroy);
    }

    /**
     * Stops the CLI
     */
    protected void stopCli() {
        cli.stop();
    }

    /**
     * Method called by the CLI itself at instantiation, should not be meddled with.
     * It's not necessary to call this method if instantiation is done manually
     *
     * @param cli The associated CLI
     */
    public void setCli(Cli cli) {
        this.cli = cli;
    }

    // Context

    /**
     * @return {@code true} if the last command raised an exception, {@code false} otherwise.
     * The error can be inspected by checking {@link #errorHistory()}
     */
    protected boolean lastCommandErrored() {
        return cli.isLastCommandErrored();
    }

    /**
     * @return A list of all errors thrown by the CLI during runtime. Works in tandem with {@link #lastCommandErrored()}
     */
    protected List<Throwable> errorHistory() {
        return cli.getErrorHistory();
    }
}
