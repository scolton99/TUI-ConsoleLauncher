package ohi.andre.consolelauncher.commands;

import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ohi.andre.consolelauncher.commands.main.raw.*;
import ohi.andre.consolelauncher.tuils.Tuils;

public class CommandGroup {
    public final static CommandGroup MAIN;
    public final static CommandGroup TUIXT;

    private final Set<Command> commands = new HashSet<>();

    private CommandGroup() {}

    public void register(Class<? extends Command> clazz) {
        try {
            Constructor<? extends Command> constructor = clazz.getConstructor();
            Command command = constructor.newInstance();

            if (!command.willWorkOn(Build.VERSION.SDK_INT)) {
                Log.i(Tuils.LOG_TAG, String.format("Command %1$s will not work on version %2$d -- skipping", command.getName(), Build.VERSION.SDK_INT));
                return;
            }

            commands.add(constructor.newInstance());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
            Log.w(Tuils.LOG_TAG, e);
        }
    }

    public Command getCommandByName(@NonNull String name) {
        return this.commands.stream().filter(command -> name.equals(command.getName())).findAny().orElse(null);
    }

    public List<Command> getCommandsByPriority() {
        List<Command> commandsByPriority = new ArrayList<>(this.commands);
        commandsByPriority.sort((c1, c2) -> c2.priority() - c1.priority());
        return commandsByPriority;
    }

    private List<Command> getCommandsAlphabetical() {
        List<Command> commandsAlphabetical = new ArrayList<>(this.commands);
        commandsAlphabetical.sort(Comparator.comparing(Command::getName));
        return commandsAlphabetical;
    }

    public List<String> getCommandNamesAlphabetical() {
        return getCommandsAlphabetical().stream().map(Command::getName).collect(Collectors.toList());
    }

    static {
        MAIN = new CommandGroup();
        TUIXT = new CommandGroup();

        MAIN.register(alias.class);
        MAIN.register(apps.class);
        MAIN.register(beep.class);
        MAIN.register(bluetooth.class);
        MAIN.register(brightness.class);
        MAIN.register(calc.class);
        MAIN.register(call.class);
        MAIN.register(changelog.class);
        MAIN.register(clear.class);
        MAIN.register(cntcts.class);
        MAIN.register(config.class);
        MAIN.register(ctrlc.class);
        MAIN.register(data.class);
        MAIN.register(devutils.class);
        MAIN.register(donate.class);
        MAIN.register(exit.class);
        MAIN.register(flash.class);
        MAIN.register(help.class);
        MAIN.register(htmlextract.class);
        MAIN.register(la.class);
        MAIN.register(location.class);
        MAIN.register(music.class);
        MAIN.register(notes.class);
        MAIN.register(notifications.class);
        MAIN.register(open.class);
        MAIN.register(rate.class);
        MAIN.register(refresh.class);
        MAIN.register(regex.class);
        MAIN.register(reply.class);
        MAIN.register(restart.class);
        MAIN.register(rss.class);
        MAIN.register(search.class);
        MAIN.register(share.class);
        MAIN.register(shellcommands.class);
        MAIN.register(shortcut.class);
        MAIN.register(status.class);
        MAIN.register(theme.class);
        MAIN.register(time.class);
        MAIN.register(tui.class);
        MAIN.register(tuiweather.class);
        MAIN.register(tuixt.class);
        MAIN.register(tutorial.class);
        MAIN.register(uninstall.class);
        MAIN.register(vibrate.class);
        MAIN.register(volume.class);
        MAIN.register(wifi.class);

        TUIXT.register(ohi.andre.consolelauncher.commands.tuixt.raw.exit.class);
        TUIXT.register(ohi.andre.consolelauncher.commands.tuixt.raw.help.class);
        TUIXT.register(ohi.andre.consolelauncher.commands.tuixt.raw.save.class);
    }
}
