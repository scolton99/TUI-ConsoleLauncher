package ohi.andre.consolelauncher.commands;

import androidx.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.Param;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.tuils.Tuils;

public class CommandInvocation {

    private final Command command;
    public Object[] mArgs;
    public int nArgs;

    public int indexNotFound = -1;

    public CommandInvocation(Command command) {
        this.command = command;
    }

    private static String findName(String input) {
        int space = input.indexOf(Tuils.SPACE);

        if (space == -1) {
            return input;
        } else {
            return input.substring(0, space);
        }
    }

    /**
     * Do something?
     * @param pack
     * @param paramCommand
     * @param input
     * @return
     */
    private static CommandTuils.ArgInfo param(MainPack pack, ParamCommand paramCommand, String input) {
        if (StringUtils.isBlank(input))
            return null;

        int indexOfFirstSpace = input.indexOf(Tuils.SPACE);
        if (indexOfFirstSpace == -1)
            indexOfFirstSpace = input.length();

        String param = input.substring(0, indexOfFirstSpace).trim();
        if (!param.startsWith("-"))
            param = "-".concat(param);

        AbstractMap.SimpleEntry<Boolean, Param> sm = paramCommand.getParam(pack, param);

        // The subcommand to be run
        @Nullable Param p = sm.getValue();

        // Was this the default?
        boolean df = sm.getKey();

        return new CommandTuils.ArgInfo(p, df ? input : input.substring(indexOfFirstSpace), p != null, p != null ? 1 : 0);
    }

    public static CommandInvocation parse(String input, ExecutePack pack) {
        String name = findName(input);
        if (!Tuils.isAlpha(name))
            return null;

        Command command = pack.commandGroup.getCommandByName(name);
        if (command == null)
            return null;

        CommandInvocation invocation = new CommandInvocation(command);

        input = input.substring(name.length());
        input = input.trim();

        List<Object> args = new ArrayList<>();
        int nArgs = 0;
        int[] types;

        try {
            if(command instanceof ParamCommand) {
                // TODO: Can we safely cast this to MainPack?
                CommandTuils.ArgInfo arg = param((MainPack) pack, (ParamCommand) command, input);
                if(arg == null || !arg.found) {
                    // We didn't find the subcommand
                    invocation.indexNotFound = 0;
                    args.add(input);
                    invocation.nArgs = 1;
                    invocation.mArgs = args.toArray(new Object[args.size()]);
                    return invocation;
                }

                input = arg.residualString;
                Param p = (Param) arg.arg;
                types = p.args();

                nArgs++;
                args.add(p);
            } else {
                types = command.argType();
            }

            if (types != null) {
                for (int count = 0; count < types.length; count++) {
                    if (input == null) break;

                    input = input.trim();
                    if(input.isEmpty()) {
                        break;
                    }

                    CommandTuils.ArgInfo arg = CommandTuils.getArg(pack, input, types[count]);
                    if(arg == null) {
                        return null;
                    }

                    if (!arg.found) {
                        invocation.indexNotFound = command instanceof ParamCommand ? count + 1 : count;
                        args.add(input);
                        invocation.mArgs = args.toArray(new Object[args.size()]);
                        invocation.nArgs = nArgs;
                        return invocation;
                    }

                    nArgs += arg.n;
                    args.add(arg.arg);
                    input = arg.residualString;
                }
            }
        } catch (Exception e) {
            Tuils.log(e);
        }

        invocation.mArgs = args.toArray(new Object[args.size()]);
        invocation.nArgs = nArgs;

        return invocation;
    }

    public String exec(ExecutePack info) throws Exception {
        info.set(mArgs);

        if(command instanceof ParamCommand) {
            if(indexNotFound == 0) {
                return info.context.getString(R.string.output_invalid_param) + Tuils.SPACE + mArgs[0];
            }

            ParamCommand pCmd = (ParamCommand) command;
            Param param = (Param) mArgs[0];

            int[] args = param.args();
//            if(args == null || mArgs[0] instanceof String) {
//                if(((String) mArgs[0]).length() == 0) return cmd.onNotArgEnough(info, 0);
//                else return resources.getString(R.string.output_invalid_param) + Tuils.SPACE + mArgs[0];
//            }

            if(indexNotFound != -1) {
                return param.onArgNotFound(info, indexNotFound);
            }

            if(pCmd.defaultParamReference() != null) {
                if(args.length > nArgs) {
                    return param.onNotArgEnough(info, nArgs);
                }
            } else {
                if(args.length + 1 > nArgs) {
                    return param.onNotArgEnough(info, nArgs);
                }
            }
        } else if(indexNotFound != -1) {
            return command.onArgNotFound(info, indexNotFound);
        }
        else {
            int[] args = command.argType();
            if (nArgs < args.length || (mArgs == null && args.length > 0)) {
                return command.onNotArgEnough(info, nArgs);
            }
        }

        return command.exec(info);
    }

    public int nextArg() {
        boolean useParamArgs = command instanceof ParamCommand && mArgs != null && mArgs.length >= 1;

        int[] args;
        if (useParamArgs) {
            if(!(mArgs[0] instanceof Param)) args = null;
            else args = ((Param) mArgs[0]).args();
        } else {
            args = command.argType();
        }

        if (args == null || args.length == 0) {
            return 0;
        }

        try {
            return args[useParamArgs ? nArgs - 1 : nArgs];
        } catch (ArrayIndexOutOfBoundsException e) {
            return 0;
        }
    }

    public Command getCommand() {
        return command;
    }
}
