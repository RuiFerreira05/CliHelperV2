import annotations.Header;
import annotations.MenuConfig;
import annotations.MenuOption;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Cli {

    private final Deque<Menu> backStack = new ArrayDeque<>();
    private final Map<Class<? extends Menu>, Menu> objectHistory = new HashMap<>();
    private final List<Throwable> errorHistory = new ArrayList<>();
    private boolean lastCommandErrored = false;

    private final Scanner scanner = new Scanner(System.in);
    private boolean menuHistoryEnabled = true;
    private boolean debugEnabled = false;
    private boolean printErrorsEnabled = true;
    private boolean errorHistoryEnabled = false;

    public boolean active = false;
    private Menu currentMenu;

    // Constructors

    Cli(Class<? extends Menu> startingMenu) {
        moveTo(startingMenu);
    }

    Cli(Menu startingMenu) {moveTo(startingMenu);}

    // Actions

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
                    System.err.println("Current Menu does not meet menu requirements. Exiting");
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
        if (menuHistoryEnabled && objectHistory.containsKey(menu)) {
            newMenu = objectHistory.get(menu);
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
            objectHistory.put(currentMenu.getClass(), currentMenu);
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

        if (Arrays.stream(menuClass.getDeclaredMethods()).noneMatch(method -> method.isAnnotationPresent(MenuOption.class))) {
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
            if (chosenMethod.getAnnotation(MenuOption.class).throwErrors()) {
                throw new RuntimeException(e);
            }
            registerError(e.getCause(), "Menu option n " + inputParsed + " threw an exception: " + e.getCause());
        }

        System.out.println();
    }

    private Method[] getAndListOptions(Class<? extends Menu> menuClass) {
        Method[] options = Arrays.stream(menuClass.getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(MenuOption.class))
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
                if (title.isBlank()) {
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
        if (title.isBlank()) {
            title = Utils.parseCamelCase(classTitle).toUpperCase();
        }
        System.out.println(title);
        System.out.println();
    }

    private String parseOptionName(Method option) {
        MenuOption optAnn = option.getAnnotation(MenuOption.class);
        if (optAnn.name().isBlank()) {
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

    Cli menuHistory(boolean enabled) {
        this.menuHistoryEnabled = enabled;
        return this;
    }

    Cli debug(boolean enabled) {
        this.debugEnabled = enabled;
        return this;
    }

    Cli printErrors(boolean enabled) {
        this.printErrorsEnabled = enabled;
        return this;
    }

    Cli errorHistory(boolean enabled) {
        this.errorHistoryEnabled = enabled;
        return this;
    }

    // Getters/Setters

    boolean isLastCommandErrored() {
        return lastCommandErrored;
    }

    List<Throwable> getErrorHistory() {
        return errorHistory;
    }
}
