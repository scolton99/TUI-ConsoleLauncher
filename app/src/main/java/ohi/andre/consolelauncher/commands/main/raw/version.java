package ohi.andre.consolelauncher.commands.main.raw;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import ohi.andre.consolelauncher.commands.AbstractCommand;
import ohi.andre.consolelauncher.commands.ExecutePack;

public class version extends AbstractCommand {
    @Override
    public String exec(ExecutePack pack) throws Exception {
        PackageManager pm = pack.context.getPackageManager();
        PackageInfo info;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            info = pm.getPackageInfo(pack.context.getPackageName(), PackageManager.PackageInfoFlags.of(0));
        } else {
            info = pm.getPackageInfo(pack.context.getPackageName(), 0);
        }

        return info.versionName + " : " + info.getLongVersionCode();
    }

    @Override
    public int[] argType() {
        return new int[0];
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
        return "";
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return "";
    }
}
