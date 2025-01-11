package ohi.andre.consolelauncher.tuils.libsuperuser;

import android.content.Context;
import android.graphics.Color;

import java.io.File;
import java.util.regex.Pattern;

import ohi.andre.consolelauncher.managers.TerminalManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.options.Behavior;
import ohi.andre.consolelauncher.tuils.Tuils;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by francescoandreuzzi on 18/08/2017.
 */

// TODO: Move to recommended https://github.com/topjohnwu/libsu
public class ShellHolder {

    private final Context context;

    public ShellHolder(Context context) {
        this.context = context;
    }

    final Pattern p = Pattern.compile("^\\n");

    public Shell.Interactive build() {
        ShellOpenListener listener = new ShellOpenListener();

        Shell.Interactive interactive = new Shell.Builder()
                .setOnSTDOUTLineListener(line -> {
                    if (!listener.isOpen)
                        return;

                    line = p.matcher(line).replaceAll(Tuils.EMPTY_STRING);
                    Tuils.sendOutput(context, line, TerminalManager.CATEGORY_OUTPUT);
                })
                .setOnSTDERRLineListener(line -> {
                    if (!listener.isOpen)
                        return;

                    line = p.matcher(line).replaceAll(Tuils.EMPTY_STRING);
                    Tuils.sendOutput(Color.RED, context, line, TerminalManager.CATEGORY_OUTPUT);
                })
                .open(listener);
        interactive.addCommand("cd " + XMLPrefsManager.get(File.class, Behavior.home_path));
        return interactive;
    }

    private static class ShellOpenListener implements Shell.OnShellOpenResultListener {
        public boolean isOpen = false;

        @Override
        public void onOpenResult(boolean success, int reason) {
            if (success && reason == Shell.OnShellOpenResultListener.SHELL_RUNNING)
                this.isOpen = true;
        }
    }
}
