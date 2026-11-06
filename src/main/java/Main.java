import annotations.Header;
import annotations.MenuConfig;
import annotations.MenuOption;

void main() {
    Cli cli = new Cli(MainMenu.class).menuHistory(false);

    cli.start();
}

@MenuConfig(
//        title = "Super Duper Main Class",
//        prompt = "$"
)
static class MainMenu extends Menu {

    private final TestClass testClass = new TestClass();

    @Header(
            showTitle = true
    )
    private void description() {
        display("Welcome to the Main Menu");
    }

    @Header
    private void counter() {
        displayf("Counter is at %d", testClass.counter);
    }

    @MenuOption
    private void sayHello() {
        display("Hello World");
    }

    @MenuOption
    private void incrementCounter() {
        testClass.counter++;
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

static class TestClass {
    public int counter = 0;
}

@MenuConfig
static class Menu2 extends Menu {

    private int counter = 0;

    @Header
    void counter() {
        displayf("Counter at %d", counter);
    }

    @MenuOption
    private void incrementCounter(){
        counter++;
    }

    @MenuOption
    private void goBack() {
        display("Moving Back");
        moveBack();
    }

}