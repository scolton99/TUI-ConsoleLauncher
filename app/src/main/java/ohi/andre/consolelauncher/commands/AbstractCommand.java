package ohi.andre.consolelauncher.commands;

public abstract class AbstractCommand implements Command {
    @Override
    public boolean willWorkOn(int apiVersion) {
        return true;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }
}
