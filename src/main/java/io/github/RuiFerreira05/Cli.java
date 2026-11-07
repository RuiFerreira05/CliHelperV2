package io.github.RuiFerreira05;

import io.github.RuiFerreira05.annotations.Header;
import io.github.RuiFerreira05.annotations.MenuConfig;
import io.github.RuiFerreira05.annotations.Option;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * The main class of the package, responsible for holding the CLI loop and performing reflection on the menus
 */
public class Cli {

    /// The Backstack of the CLI, holds the objects that were previously on screen but moved out of.
    private final Deque<Menu> backStack = new ArrayDeque<>();

    /// Holds previously instantiated menus, as a way of keeping state without destroying them.
    /// As it might increase size beyond acceptable it can be turned off with {@link #menuHistory(boolean)}
    private final Map<Class<? extends Menu>, Menu> menuHistory = new HashMap<>();

    /// Holds all Throwables encountered by the CLI while executing the options declared in the custom menus.
    /// Is exposed to menus via {@link Menu#errorHistory()}
    private final List<Throwable> errorHistory = new ArrayList<>();

    /// This method serves as a helper to check if the last option executed returned an error,
    /// works in tandem with {@link #errorHistory}.
    /// Is exposed to menus via {@link Menu#lastCommandErrored()}
    private boolean lastCommandErrored = false;

    private final Scanner scanner = new Scanner(System.in);
    private boolean menuHistoryEnabled = true;
    private boolean debugEnabled = false;
    private boolean printErrorsEnabled = true;
    private boolean errorHistoryEnabled = false;

    /// If true, cli loop is running, can be stopped with {@link Menu#stopCli()} which calls {@link #stop()} internally
    public boolean active = false;
    private Menu currentMenu;

    // Constructors

    /**
     * Instantiate a CLI. Can be started with {@link #start()}.
     * If
     *
     * @param startingMenu A Menu compatible class which will be the first to be displayed.
     *                     Must extend {@link Menu} and be annotated with {@link MenuConfig}
     */
    Cli(Class<? extends Menu> startingMenu) {
        moveTo(startingMenu);
    }

    /**
     * Instantiate a CLI. Can be started with {@link #start()}.
     * <p>
     * Using {@link #Cli(Class)} is preferred if {@code startingMenu} contains no custom constructor.
     *
     * @param startingMenu An instance of a menu compatible class.
     *                     Must extend {@link Menu} and be annotated with {@link MenuConfig}
     */
    Cli(Menu startingMenu) {moveTo(startingMenu);}

    // Actions

    /**
     * Starts the CLI loop. Hangs.
     * CLI loop can be stopped with {@link #stop()} or {@link Menu#stopCli()} if inside a custom menu
     */
    void start() {
        active = true;
        while (active) {
            if (currentMenu == null) {
                if (debugEnabled) {
                    System.err.println("Something went wrong and current menu is null. Exiting");
                }
                return;
            }

            if (!isValidMenu(currentMenu)) {
                if (debugEnabled) {
                    System.err.println("Current com.serverofglass.Menu does not meet menu requirements. Exiting");
                }
                return;
            }

            DisplayMenu(currentMenu);
        }
    }

    void display(String str) {
        System.out.println(str);
    }

    void moveTo(Class<? extends Menu> menu) {
        Menu newMenu;
        if (menuHistoryEnabled && menuHistory.containsKey(menu)) {
            newMenu = menuHistory.get(menu);
        } else {
            try {
                newMenu = menu.getDeclaredConstructor().newInstance();
                newMenu.setCli(this);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                registerError(e.getCause(), "Attempted to move current menu (\"" + menu.getSimpleName() + "\") to illegal class");
                return;
            }
        }
        if (currentMenu != null) {
            backStack.push(currentMenu);
        }
        currentMenu = newMenu;
    }

    void moveTo(Menu menu) {
        if (currentMenu != null) {
            backStack.push(currentMenu);
        }
        // Might be redundant, but is necessary for completely new classes
        menu.setCli(this);
        currentMenu = menu;
    }

    void moveBack(boolean destroy) {
        if (backStack.isEmpty()) {
            return;
        }
        if (!destroy) {
            menuHistory.put(currentMenu.getClass(), currentMenu);
        }
        currentMenu = backStack.pop();
    }

    void moveBack() {
        moveBack(false);
    }

    void stop() {
        active = false;
    }

    // Private helper methods

    private boolean isValidMenu(Menu currentMenu) {
        Class<? extends Menu> menuClass = currentMenu.getClass();

        if (!menuClass.isAnnotationPresent(MenuConfig.class)) {
            return false;
        }

        if (Arrays.stream(menuClass.getDeclaredMethods()).noneMatch(method -> method.isAnnotationPresent(Option.class))) {
            return false;
        }

        return true;
    }

    private void DisplayMenu(Menu currentMenu) {
        // Get object class
        Class<? extends Menu> menuClass = currentMenu.getClass();

        // Get menu config
        MenuConfig menuConfig = menuClass.getAnnotation(MenuConfig.class);

        // Display title
        displayTitle(menuClass.getSimpleName(), menuConfig);

        // Display headers
        displayHeaders(currentMenu, menuClass);

        // Display and get options
        Method[] options = getAndListOptions(menuClass);

        System.out.println();

        // Collect user input
        System.out.print(menuConfig.prompt().trim() + " ");
        String input = scanner.nextLine();
        int inputParsed;
        try {
            inputParsed = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid Input");
            return;
        }

        // Validate input
        if (inputParsed <= 0 || options.length < inputParsed) {
            System.out.println("Option chosen not within valid options (" + inputParsed + " not in 0.." + options.length);
            return;
        }

        System.out.println();

        // Execute chosen option
        lastCommandErrored = false;

        Method chosenMethod = options[inputParsed-1];
        try {
            chosenMethod.invoke(currentMenu);
        } catch (IllegalAccessException e) {
            // Should not happen because of setAccessible(true)
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            if (chosenMethod.getAnnotation(Option.class).throwErrors()) {
                throw new RuntimeException(e);
            }
            registerError(e.getCause(), "com.serverofglass.Menu option n " + inputParsed + " threw an exception: " + e.getCause());
        }

        System.out.println();
    }

    private Method[] getAndListOptions(Class<? extends Menu> menuClass) {
        Method[] options = Arrays.stream(menuClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Option.class))
                .toArray(Method[]::new);
        options = Utils.reverseArray(options);

        // List options and set accessible
        for (int i = 0; i < options.length; i++) {
            options[i].setAccessible(true);
            System.out.println(i+1 + ". " + parseOptionName(options[i]));
        }
        return options;
    }

    private void displayHeaders(Menu currentMenu, Class<? extends Menu> menuClass) {
        Method[] headers = Arrays.stream(menuClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(Header.class))
                .toArray(Method[]::new);
        headers = Utils.reverseArray(headers);

        for (Method header : headers) {
            header.setAccessible(true);
            Header annotation = header.getAnnotation(Header.class);

            // Title
            if (annotation.showTitle()) {
                String title = annotation.title();
                if (title.isEmpty()) {
                    title = Utils.parseCamelCase(header.getName(), true) + ":";
                }
                System.out.println(title);
            }

            // Execute
            try {
                header.invoke(currentMenu);
            } catch (IllegalAccessException e) {
                // Should not happen because of setAccessible(true)
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                registerError(e.getCause(), "Header \"" + header.getName() + "\" threw an exception: " + e.getCause());
            }
        }
    }

    private static void displayTitle(String classTitle, MenuConfig menuConfig) {
        String title = menuConfig.title();
        if (title.isEmpty()) {
            title = Utils.parseCamelCase(classTitle).toUpperCase();
        }
        System.out.println(title);
        System.out.println();
    }

    private String parseOptionName(Method option) {
        Option optAnn = option.getAnnotation(Option.class);
        if (optAnn.name().isEmpty()) {
            return Utils.parseCamelCase(option.getName(), true);
        } else {
            return optAnn.name();
        }
    }

    private void registerError(Throwable e, String message) {
        lastCommandErrored = true;
        if (printErrorsEnabled) {
            System.err.println(message);
        }
        if (errorHistoryEnabled) {
            errorHistory.add(e);
        }
    }

    // Config

    /**
     * if {@code true}, enables the menu history functionality, which allows menus popped out of the stack to keep
     * their state.
     * <p>
     * Should not be confused with the backstack.
     * <p>
     * Defaults to {@code true}.
     *
     * @param enabled if {@code true}, is enabled
     * @return the same instance of the CLI
     */
    Cli menuHistory(boolean enabled) {
        this.menuHistoryEnabled = enabled;
        return this;
    }

    /**
     * if {@code true}, enables debug mode on the menu
     * <p>
     * Defaults to {@code false}.
     *
     * @param enabled if {@code true}, is enabled
     * @return the same instance of the CLI
     */
    Cli debug(boolean enabled) {
        this.debugEnabled = enabled;
        return this;
    }

    /**
     * if {@code true}, prints a helpful error message to the console, does not throw the error.
     * <p>
     * Defaults to {@code true}.
     *
     * @param enabled if {@code true}, is enabled
     * @return the same instance of the CLI
     */
    Cli printErrors(boolean enabled) {
        this.printErrorsEnabled = enabled;
        return this;
    }

    /**
     * if {@code true}, enables error history in the CLI which allows checking which errors were thrown by
     * executing an option. Useful in tandem with {@link #lastCommandErrored}
     * <p>
     * Defaults to {@code false}.
     *
     * @param enabled if {@code true}, is enabled
     * @return the same instance of the CLI
     */
    Cli errorHistory(boolean enabled) {
        this.errorHistoryEnabled = enabled;
        return this;
    }

    // Getters/Setters

    /**
     * @return {@code true} if the last command raised an exception, {@code false} otherwise.
     */
    boolean isLastCommandErrored() {
        return lastCommandErrored;
    }

    /**
     * @return A list of all errors thrown by the CLI during runtime.
     */
    List<Throwable> getErrorHistory() {
        return errorHistory;
    }
}
