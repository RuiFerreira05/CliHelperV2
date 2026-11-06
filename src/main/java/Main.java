import annotations.Header;
import annotations.MenuConfig;
import annotations.MenuOption;

public class Main {

    public static void main(String[] args) {
        Cli cli = new Cli(MainMenu.class)
                .menuHistory(true)
                .debug(true)
                .printErrors(true)
                .errorHistory(true);

        cli.start();
    }

}

@MenuConfig
class MainMenu extends Menu {

    @Header
    void header() {
        if (!errorHistory().isEmpty()) {
            displayf("Last Error: %s", errorHistory().getLast());
        } else {
            display("No errors yet");
        }
        System.out.println();
    }

    @MenuOption()
    void test() throws Exception {
        throw new Exception("test");
    }

}