import annotations.Header;
import annotations.MenuConfig;
import annotations.MenuOption;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class Cli {

    private final Deque<Menu> backStack = new ArrayDeque<>();
    private final Scanner scanner = new Scanner(System.in);
    public int maxTitleLength = 40;
    public boolean active = false;
    private Menu currentMenu;

    Cli(Class<? extends Menu> startingMenu) {
        moveTo(startingMenu);
    }

    void start() {
        active = true;
        while (active) {
            if (currentMenu == null) {
                System.err.println("Something went wrong and current menu is null. Exiting");
                return;
            }

            if (!isValidMenu(currentMenu)) {
                System.out.println("Current Menu does not meet Menu requirements. Exiting");
                return;
            }

            DisplayMenu(currentMenu);
        }
    }

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
        displayTitle(menuClass.getSimpleName(), maxTitleLength, menuConfig);

        // Display headers
        displayHeaders(currentMenu, menuClass);

        // Display and get options
        Method[] options = getAndListOptions(menuClass);

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

        // Execute chosen option
        try {
            options[inputParsed-1].invoke(currentMenu);
        } catch (IllegalAccessException e) {
            // Should not happen because of setAccessible(true)
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            System.err.println("Menu option " + inputParsed + " threw an exception: " + e.getMessage());
        }
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
                System.out.println(parseHeaderName(header));
            }

            // Execute
            try {
                header.invoke(currentMenu);
            } catch (IllegalAccessException e) {
                // Should not happen because of setAccessible(true)
                throw new RuntimeException(e);
            } catch (InvocationTargetException e) {
                System.err.println("Header \"" + header.getName() + "\" threw an exception: " + e);
            }
        }
    }

    private static void displayTitle(String classTitle, int maxLength, MenuConfig menuConfig) {
        if (maxLength <= 0) {
            throw new IllegalArgumentException("maxLength must be greater than 0");
        }

        String rawTitle = (menuConfig != null && menuConfig.title() != null && !menuConfig.title().isBlank())
                ? menuConfig.title()
                : Utils.parseCamelCase(classTitle);

        int margin = (maxLength / 5);
        int maxTitleLength = Math.max(1, maxLength - (margin * 2));
        int indentSpaces = (maxLength - maxTitleLength) / 2;

        StringBuilder wrapped = new StringBuilder();
        for (int i = 0; i < rawTitle.length(); i += maxTitleLength) {
            if (i > 0) {
                wrapped.append(System.lineSeparator());
            }
            wrapped.append(rawTitle, i, Math.min(i + maxTitleLength, rawTitle.length()));
        }

        String formattedTitle = new StringBuilder()
                .repeat('=', maxLength).append(System.lineSeparator())
                .append(wrapped.toString().indent(indentSpaces))
                .repeat('=', maxLength)
                .toString();

        System.out.println(formattedTitle);
    }

    private String parseOptionName(Method option) {
        MenuOption optAnn = option.getAnnotation(MenuOption.class);
        if (optAnn.name().isBlank()) {
            return Utils.parseCamelCase(option.getName(), true);
        } else {
            return optAnn.name();
        }
    }

    private String parseHeaderName(Method header) {
        Header headerAnn = header.getAnnotation(Header.class);
        if (headerAnn.title().isBlank()) {
            return Utils.parseCamelCase(header.getName(), false);
        } else {
            return headerAnn.title();
        }
    }

    void display(String str) {
        System.out.println(str);
    }

    void moveTo(Class<? extends Menu> menu) {
        Menu newMenu;
        try {
            newMenu = menu.getDeclaredConstructor().newInstance();
            newMenu.setCli(this);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            System.err.println("Attempted to move current menu to illegal class: " + menu.getSimpleName());
            System.err.println("Error: " + e);
            return;
        }
        if (currentMenu != null) {
            backStack.push(currentMenu);
        }
        currentMenu = newMenu;
    }

    void moveBack() {
        if (backStack.isEmpty()) {
            return;
        }
        currentMenu = backStack.pop();
    }

    void stop() {
        active = false;
    }
}
