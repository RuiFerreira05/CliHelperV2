import java.util.List;

public class Menu {

    private Cli cli;

    protected void display(String str) {
        cli.display(str);
    }

    protected void displayf(String str, Object... objects) {
        cli.display(String.format(str, objects));
    }

    protected void moveTo(Class<? extends Menu> menu) {
        cli.moveTo(menu);
    }

    protected void moveBack() {
        cli.moveBack();
    }

    protected void stopCli() {
        cli.stop();
    }

    public void setCli(Cli cli) {
        this.cli = cli;
    }

    // Context
    protected boolean lastCommandErrored() {
        return cli.isLastCommandErrored();
    }

    protected List<Throwable> errorHistory() {
        return cli.getErrorHistory();
    }
}
