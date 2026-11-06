import annotations.Header;
import annotations.MenuConfig;
import annotations.MenuOption;

void main() {
    Cli cli = new Cli(MainMenu.class);

    cli.start();
}

@MenuConfig(
//        title = "Super Duper Main Class",
        prompt = "$"
)
static class MainMenu extends Menu {

    @Header(
            showTitle = true
    )
    private void description() {
        display("Welcome to the Main Menu");
    }

    @MenuOption
    private void sayHello() {
        display("Hello World");
    }

    @MenuOption(name="Go to menu 2")
    private void goMenu2() {
        display("Moving to Menu 2");
        moveTo(Menu2.class);
    }

    @MenuOption
    private void shutdown() {
        display("Shutting down");
        stopCli();
    }
}

@MenuConfig
static class Menu2 extends Menu {

    @Header
    void description() {
        display("Welcome to Menu 2");
    }

    @MenuOption
    private void goBack() {
        display("Moving Back");
        moveBack();
    }

}