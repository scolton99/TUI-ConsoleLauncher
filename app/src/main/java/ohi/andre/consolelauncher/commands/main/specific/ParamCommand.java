package ohi.andre.consolelauncher.commands.main.specific;

import java.util.AbstractMap;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.AbstractCommand;
import ohi.andre.consolelauncher.commands.Command;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.Param;
import ohi.andre.consolelauncher.managers.xml.classes.XMLPrefsSave;
import ohi.andre.consolelauncher.tuils.Tuils;

/**
 * Created by francescoandreuzzi on 01/05/2017.
 */

public abstract class ParamCommand extends AbstractCommand {

//    copy this
    /*
    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {



        static Param get(String p) {
            p = p.toLowerCase();
            Param[] ps = values();
            for (Param p1 : ps)
                if (p.endsWith(p1.label()))
                    return p1;
            return null;
        }

        static String[] labels() {
            Param[] ps = values();
            String[] ss = new String[ps.length];

            for (int count = 0; count < ps.length; count++) {
                ss[count] = ps[count].label();
            }

            return ss;
        }

        @Override
        public String label() {
            return Tuils.MINUS + name();
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
        }

        @Override
        public String onArgNotFound(ExecutePack pack, int index) {
        }
    }

    @Override
    protected Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }
     */

    @Override
    public final int[] argType() {
        return new int[] {Command.PARAM};
    }

    @Override
    public final String exec(ExecutePack pack) throws Exception {
        String o = doThings(pack);
        if(o != null) return o;

        Param param = pack.get(Param.class);
        if(param == null) {
            Object o1 = pack.get(Object.class, 0);

            if(o1 == null || o1.toString().isEmpty()) return pack.context.getString(helpRes());
            else return pack.context.getString(R.string.output_invalid_param) + Tuils.SPACE + o1;
        }
        return param.exec(pack);
    }

    public AbstractMap.SimpleEntry<Boolean, Param> getParam(MainPack pack, String param) {
        // What we're really getting here is a subcommand
        Param p = paramForString(pack, param);

        // This allows a default subcommand to be chosen if the given one doesn't exist
        if(p == null && defaultParamReference() != null)
            return new AbstractMap.SimpleEntry<>(true, paramForString(pack, defaultParam(pack)));

        // Otherwise, return the subcommand (indicate that it is not the default)
        return new AbstractMap.SimpleEntry<>(false, p);
    }

    public String defaultParam(MainPack pack) {
        String def = pack.cmdPrefs.get(defaultParamReference());
        if(!def.startsWith("-")) def = "-" + def;
        return def;
    }

    @Override
    public String onNotArgEnough(ExecutePack pack, int nArgs) {
        return pack.context.getString(helpRes());
    }

    @Override
    public String onArgNotFound(ExecutePack pack, int indexNotFound) {
        Tuils.log("inf", indexNotFound);
        if(indexNotFound == 0) {
            try {
                Tuils.log("last");
                String param = pack.get(String.class, 0);
                return pack.context.getString(R.string.output_invalid_param) + Tuils.SPACE + param;
            } catch (Exception e) {}
        }

        return pack.context.getString(helpRes());
    }

    public abstract String[] params();
    protected abstract Param paramForString(MainPack pack, String param);
    protected abstract String doThings(ExecutePack pack);
    public XMLPrefsSave defaultParamReference() {
        return null;
    }
}
