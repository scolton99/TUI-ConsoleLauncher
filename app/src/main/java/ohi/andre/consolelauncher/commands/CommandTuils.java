package ohi.andre.consolelauncher.commands;

import android.annotation.SuppressLint;
import android.graphics.Color;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.managers.AppsManager;
import ohi.andre.consolelauncher.managers.ContactManager;
import ohi.andre.consolelauncher.managers.FileManager;
import ohi.andre.consolelauncher.managers.FileManager.DirInfo;
import ohi.andre.consolelauncher.managers.HTMLExtractManager;
import ohi.andre.consolelauncher.managers.RssManager;
import ohi.andre.consolelauncher.managers.music.MusicManager2;
import ohi.andre.consolelauncher.managers.notifications.NotificationManager;
import ohi.andre.consolelauncher.managers.xml.XMLPrefsManager;
import ohi.andre.consolelauncher.managers.xml.classes.XMLPrefsSave;
import ohi.andre.consolelauncher.managers.xml.options.Apps;
import ohi.andre.consolelauncher.managers.xml.options.Notifications;
import ohi.andre.consolelauncher.managers.xml.options.Rss;
import ohi.andre.consolelauncher.tuils.Tuils;

@SuppressLint("DefaultLocale")
public class CommandTuils {

    private static final FileManager.SpecificExtensionFileFilter extensionFileFilter = new FileManager.SpecificExtensionFileFilter();
    private static final FileManager.SpecificNameFileFilter nameFileFilter = new FileManager.SpecificNameFileFilter();

    public static List<XMLPrefsSave> xmlPrefsEntrys;
    public static List<String> xmlPrefsFiles;


    //	find args
    public static ArgInfo getArg(ExecutePack info, String input, int type) {
        if (type == Command.FILE && info instanceof MainPack) {
            MainPack pack = (MainPack) info;
            return file(input, pack.currentDirectory);
        } else if (type == Command.CONTACTNUMBER && info instanceof MainPack) {
            MainPack pack = (MainPack) info;
            return contactNumber(input, pack.contacts);
        } else if (type == Command.PLAIN_TEXT) {
            return plainText(input);
        } else if (type == Command.VISIBLE_PACKAGE && info instanceof MainPack) {
            MainPack pack = (MainPack) info;
            return activityName(input, pack.appsManager);
        } else if (type == Command.HIDDEN_PACKAGE && info instanceof MainPack) {
            MainPack pack = (MainPack) info;
            return hiddenPackage(input, pack.appsManager);
        } else if (type == Command.TEXTLIST) {
            return textList(input);
        } else if (type == Command.SONG && info instanceof MainPack) {
            MainPack pack = (MainPack) info;
            if(pack.player == null) return null;

            return song(input, pack.player);
        } else if (type == Command.COMMAND) {
            return command(input, info.commandGroup);
        } else if(type == Command.BOOLEAN) {
            return bln(input);
        } else if(type == Command.COLOR) {
            return color(input);
        } else if(type == Command.CONFIG_ENTRY) {
            return configEntry(input);
        } else if(type == Command.CONFIG_FILE) {
            return configFile(input);
        } else if(type == Command.INT) {
            return integer(input);
        } else if(type == Command.DEFAULT_APP) {
            return defaultApp(input, ((MainPack) info).appsManager);
        } else if(type == Command.ALL_PACKAGES) {
            return allPackages(input, ((MainPack) info).appsManager);
        } else if(type == Command.NO_SPACE_STRING || type == Command.APP_GROUP || type == Command.BOUND_REPLY_APP) {
            return noSpaceString(input);
        } else if(type == Command.APP_INSIDE_GROUP) {
            return activityName(input, ((MainPack) info).appsManager);
        } else if(type == Command.LONG) {
            return numberLong(input);
        } else if(type == Command.DATASTORE_PATH_TYPE) {
            return dataStoreType(input);
        }

        return null;
    }

//	args extractors {

    private static ArgInfo dataStoreType(String input) {
        ArgInfo a = noSpaceString(input);
        if(a.found) {
            String s = (String) a.arg;
            try {
                HTMLExtractManager.StoreableValue.Type.valueOf(s);
                return a;
            } catch (Exception e) {
                return null;
            }
        }

        a.found = false;
        return a;
    }

    private static ArgInfo numberLong(String input) {
        String[] split = input.split(Tuils.SPACE);

        try {
            long l = Long.parseLong(split[0]);

            StringBuilder builder = new StringBuilder();
            for(int c = 1; c < split.length; c++) {
                builder.append(split[c]).append(Tuils.SPACE);
            }
            return new ArgInfo(l, builder.toString().trim(), true, 1);
        } catch (Exception e) {
            return new ArgInfo(null, input, false, 0);
        }
    }

    private static ArgInfo color(String input) {
        input = input.trim();

        int space = input.indexOf(Tuils.SPACE);
        String cl = input.substring(0, space == -1 ? input.length() : space);
        input = space == -1 ? Tuils.EMPTY_STRING : input.substring(space + 1);

        try {
            Color.parseColor(cl);
            return new ArgInfo(cl, input, true, 1);
        } catch (Exception e) {
            return new ArgInfo(null, input, false, 0);
        }
    }

    private static ArgInfo bln(String input) {
        String used, notUsed;
        if(input.contains(Tuils.SPACE)) {
            int space = input.indexOf(Tuils.SPACE);
            used = input.substring(0, space);
            notUsed = input.substring(space + 1);
        } else {
            used = input;
            notUsed = null;
        }

        Object result = Boolean.parseBoolean(used);
        return new ArgInfo(result, notUsed, !used.isEmpty(), !used.isEmpty() ? 1 : 0);
    }

    private static ArgInfo plainText(String input) {
        return new ArgInfo(input, "", true, 1);
    }

    private static ArgInfo textList(String input) {
        if (input == null) {
            return null;
        }

        String[] strings = input.split(Tuils.SPACE + "+");
        List<String> arg = new ArrayList<>(Arrays.asList(strings));

        return new ArgInfo(arg, null, true, arg.size());
    }

    private static ArgInfo noSpaceString(String input) {
        if(input == null) return null;

        int index = input.indexOf(Tuils.SPACE);
        if(index == -1) index = input.length();

        return new ArgInfo(input.substring(0,index), input.length() > index ? input.substring(index + 1) : null, true, 1);
    }

    private static ArgInfo command(String string, CommandGroup active) {
        ArgInfo i = noSpaceString(string);
        string = (String) i.arg;

        Command abstraction = null;
        try {
            abstraction = active.getCommandByName(string);
        } catch (Exception e) {
            Tuils.log(e);
            Tuils.toFile(e);
        }

        return new ArgInfo(abstraction, i.residualString, abstraction != null, 1);
    }

    @SuppressWarnings("unchecked")
    private static ArgInfo file(String input, File cd) {
        input = input.trim();
        if((input.startsWith("\"") || input.startsWith("'")) && (input.substring(1, input.length()).contains("\"") || input.substring(1, input.length()).contains("'"))) {
            String afterFirst = input.substring(1, input.length());

            int endIndex = afterFirst.indexOf("\"");
            if(endIndex == -1) endIndex = afterFirst.indexOf("'");

            if(endIndex != -1) {
                String file = afterFirst.substring(0, endIndex);
                String residual = afterFirst.substring(endIndex + 1, afterFirst.length());

                File f;
                if(afterFirst.startsWith("/")) /*absolute*/ f = new File(file);
                else f = new File(cd, file);

                return new ArgInfo(f, residual, true, 1);
            }
        }

        List<String> strings = (List<String>) CommandTuils.textList(input).arg;

        String toVerify = Tuils.EMPTY_STRING;
        for (int count = 0; count < strings.size(); count++) {
            toVerify = toVerify.concat(strings.get(count));

            DirInfo info = CommandTuils.getFile(toVerify, cd);
            if (info.file != null && info.notFound == null) {
                while (count-- >= 0)
                    strings.remove(0);

                String residual = Tuils.toPlanString(strings, Tuils.SPACE);
                return new ArgInfo(info.file, residual, true, 1);
            }

            toVerify = toVerify.concat(Tuils.SPACE);
        }

        return new ArgInfo(null, input, false, 0);
    }

//    @SuppressWarnings("unchecked")
//    private static ArgInfo fileList(String input, File cd) {
//        List<File> files = new ArrayList<>();
//        List<String> strings = (List<String>) CommandTuils.textList(input).arg;
//
//        String toVerify = Tuils.EMPTY_STRING;
//        for (int count = 0; count < strings.size(); count++) {
//            String s = strings.get(count);
//
//            toVerify = toVerify.concat(s);
//
//            DirInfo dir = CommandTuils.getFile(toVerify, cd);
//            if (dir.notFound == null) {
//                files.add(dir.file);
//
//                toVerify = Tuils.EMPTY_STRING;
//                continue;
//            }
//
//            List<File> tempFiles = CommandTuils.attemptWildcard(dir);
//            if (tempFiles != null) {
//                files.addAll(tempFiles);
//
//                toVerify = Tuils.EMPTY_STRING;
//                continue;
//            }
//
//            toVerify = toVerify.concat(Tuils.SPACE);
//        }
//
//        if (toVerify.length() > 0) return new ArgInfo(null, null, false, 0);
//        return new ArgInfo(files, null, files.size() > 0, files.size());
//    }

    private static DirInfo getFile(String path, File cd) {
        return FileManager.cd(cd, path);
    }

    private static List<File> attemptWildcard(DirInfo dir) {
        List<File> files;

        FileManager.WildcardInfo info = FileManager.wildcard(dir.notFound);
        if(info == null) {
            return null;
        }

        File cd = dir.file;
        if (!cd.isDirectory()) {
            return null;
        }

        if (info.allExtensions && info.allNames) {
            files = Arrays.asList(cd.listFiles());
        } else if(info.allNames) {
            extensionFileFilter.setExtension(info.extension);
            files = Arrays.asList(cd.listFiles(extensionFileFilter));
        } else if(info.allExtensions) {
            nameFileFilter.setName(info.name);
            files = Arrays.asList(cd.listFiles(nameFileFilter));
        } else {
            return null;
        }

        if (!files.isEmpty()) {
            return files;
        } else {
            return null;
        }
    }

    private static ArgInfo activityName(String input, AppsManager apps) {
        AppsManager.LaunchInfo info = apps.findLaunchInfoWithLabel(input, AppsManager.SHOWN_APPS);
        return new ArgInfo(info, null, info != null, info != null ? 1 : 0);
    }

    private static ArgInfo hiddenPackage(String input, AppsManager apps) {
        AppsManager.LaunchInfo info = apps.findLaunchInfoWithLabel(input, AppsManager.HIDDEN_APPS);
        return new ArgInfo(info, null, info != null, info != null ? 1 : 0);
    }

    private static ArgInfo allPackages(String input, AppsManager apps) {
        AppsManager.LaunchInfo info = apps.findLaunchInfoWithLabel(input, AppsManager.SHOWN_APPS);
        if(info == null) {
            info = apps.findLaunchInfoWithLabel(input, AppsManager.HIDDEN_APPS);
        }

        return new ArgInfo(info, null, info != null, info != null ? 1 : 0);
    }

    private static ArgInfo defaultApp(String input, AppsManager apps) {
        AppsManager.LaunchInfo info = apps.findLaunchInfoWithLabel(input, AppsManager.SHOWN_APPS);
        if(info == null) {
            return new ArgInfo(input, null, true, 1);
        } else {
            return new ArgInfo(info, null, true, 1);
        }
    }

    private static ArgInfo contactNumber(String input, ContactManager contacts) {
        String number;

        if (Tuils.isPhoneNumber(input))
            number = input;
        else
            number = contacts.findNumber(input);

        return new ArgInfo(number, null, number != null, 1);
    }

    private static ArgInfo song(String input, MusicManager2 music) {
        return new ArgInfo(input, null, true, 1);
    }

    private static ArgInfo configEntry(String input) {
        int index = input.indexOf(Tuils.SPACE);

        if(xmlPrefsEntrys == null) {
            xmlPrefsEntrys = new ArrayList<>();

            for(XMLPrefsManager.XMLPrefsRoot element : XMLPrefsManager.XMLPrefsRoot.values()) {
                xmlPrefsEntrys.addAll(element.enums);
            }
            Collections.addAll(xmlPrefsEntrys, Apps.values());
            Collections.addAll(xmlPrefsEntrys, Notifications.values());
            Collections.addAll(xmlPrefsEntrys, Rss.values());
        }

        String candidate = index == -1 ? input : input.substring(0,index);
        for(XMLPrefsSave xs : xmlPrefsEntrys) {
            if(xs.label().equals(candidate)) {
                return new ArgInfo(xs, index == -1 ? null : input.substring(index + 1,input.length()), true, 1);
            }
        }
        return new ArgInfo(null, input, false, 0);
    }

    private static ArgInfo configFile(String input) {
        if(xmlPrefsFiles == null) {
            xmlPrefsFiles = new ArrayList<>();
            for(XMLPrefsManager.XMLPrefsRoot element : XMLPrefsManager.XMLPrefsRoot.values())
                xmlPrefsFiles.add(element.path);
            xmlPrefsFiles.add(AppsManager.PATH);
            xmlPrefsFiles.add(NotificationManager.PATH);
            xmlPrefsFiles.add(RssManager.PATH);
        }

        for(String xs : xmlPrefsFiles) {
            if(xs.equalsIgnoreCase(input)) return new ArgInfo(xs, null, true, 1);
        }
        return new ArgInfo(null, input, false, 0);
    }

    private static ArgInfo integer(String input) {
        int n;
        String s;

        int index = input.indexOf(Tuils.SPACE);
        if(index == -1) s = input;
        else s = input.substring(0, index);

        try {
            n = Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return new ArgInfo(null, input, false, 0);
        }

        return new ArgInfo(n, index == -1 ? null : input.substring(index + 1), true, 1);
    }

    public static boolean isSuRequest(String input) {
        return input.equals("su");
    }

    public static boolean isSuCommand(String input) {
        return input.startsWith("su ");
    }

    public static class ArgInfo {
        public final Object arg;
        public final String residualString;
        public final int n;
        public boolean found;

        public ArgInfo(Object arg, String residualString, boolean found, int nFound) {
            this.arg = arg;
            this.residualString = residualString;
            this.found = found;
            this.n = nFound;
        }
    }

}
