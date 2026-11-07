import io.github.ruiferreira05.Cli;
import io.github.ruiferreira05.Menu;
import io.github.ruiferreira05.annotations.MenuConfig;
import io.github.ruiferreira05.annotations.Option;

public class Main {
    public static void main(String[] args) {
        Cli cli = new Cli(MainMenu.class).debug(true).printErrors(true);

        cli.start();
    }
}

@MenuConfig
class MainMenu extends Menu {

    @Option
    private void sayHello() {
        display("Hello!");
    }

    @Option
    private void shutdown() {stopCli();}

}