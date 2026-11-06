public class Menu {

    private Cli cli;

    protected void display(String str) {
        cli.display(str);
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
}
