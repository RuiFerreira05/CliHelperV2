import io.github.ruiferreira05.Cli;
import io.github.ruiferreira05.exceptions.FormCancelledException;
import io.github.ruiferreira05.Menu;
import io.github.ruiferreira05.annotations.form.Form;
import io.github.ruiferreira05.annotations.form.FormProperty;
import io.github.ruiferreira05.annotations.menu.MenuConfig;
import io.github.ruiferreira05.annotations.menu.Option;

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
    private void form() {
        try {
            FormMenu response = displayForm(FormMenu.class);
        } catch (FormCancelledException e) {
            display("Ok");
        }
    }

    @Option
    private void shutdown() {stopCli();}
}

@Form(
        cancelKeyword = "/cancel"
)
record FormMenu(
   @FormProperty(
           prompt = "Your name:",
           acceptEmpty = false
   ) String username,
   @FormProperty int age,
   @FormProperty String password
) { }