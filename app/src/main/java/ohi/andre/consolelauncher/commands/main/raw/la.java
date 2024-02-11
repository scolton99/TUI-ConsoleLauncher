package ohi.andre.consolelauncher.commands.main.raw;

import android.content.ComponentName;
import android.content.Intent;

import ohi.andre.consolelauncher.commands.CommandAbstraction;
import ohi.andre.consolelauncher.commands.ExecutePack;

public class la implements CommandAbstraction {
    @Override
    public String exec(ExecutePack pack) throws Exception {
        String arg = pack.get(String.class, 0);
        String[] pieces = arg.split("/");

        Intent intent;
        if (pieces.length == 0) {
            return "Invalid format";
        } else if (pieces.length == 1) {
            intent = pack.context.getPackageManager().getLaunchIntentForPackage(pieces[0]);
        } else if (pieces.length == 2) {
            ComponentName name = new ComponentName(pieces[0], pieces[1]);
            intent = new Intent();
            intent.setComponent(name);
        } else {
            return "Invalid format";
        }

        pack.context.startActivity(intent);

        return "";
    }

    @Override
    public int[] argType() {
        return new int[] { CommandAbstraction.PLAIN_TEXT };
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public int helpRes() {
        return 0;
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int indexNotFound) {
        return null;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return null;
    }
}
