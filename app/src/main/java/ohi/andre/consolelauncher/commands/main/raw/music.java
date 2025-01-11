package ohi.andre.consolelauncher.commands.main.raw;

import android.util.Log;

import ohi.andre.consolelauncher.R;
import ohi.andre.consolelauncher.commands.AbstractCommand;
import ohi.andre.consolelauncher.commands.Command;
import ohi.andre.consolelauncher.commands.ExecutePack;
import ohi.andre.consolelauncher.commands.main.MainPack;
import ohi.andre.consolelauncher.commands.main.specific.ParamCommand;
import ohi.andre.consolelauncher.managers.music.MusicManager2;
import ohi.andre.consolelauncher.managers.music.Song;
import ohi.andre.consolelauncher.tuils.Tuils;
import eu.chainfire.libsuperuser.Shell;

public class music extends ParamCommand {

    private enum Param implements ohi.andre.consolelauncher.commands.main.Param {
        next {

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) {
                    execute("NEXT");
                    return null;
                }

                String title = ((MainPack) pack).player.playNext();
                if(title != null) return pack.context.getString(R.string.output_playing) + Tuils.SPACE + title;
                return null;
            }
        },
        previous {

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) {
                    execute("PREVIOUS");
                    return null;
                }

                String title = ((MainPack) pack).player.playPrev();
                if(title != null) return pack.context.getString(R.string.output_playing) + Tuils.SPACE + title;
                return null;
            }
        },
        ls {

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) return pack.context.getString(R.string.output_music_disabled);

                return ((MainPack) pack).player.lsSongs();
            }
        },
        play {

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) {
                    execute("PLAY_PAUSE");
                    return null;
                }

                ((MainPack) pack).player.play();
                return pack.context.getString(R.string.output_playing) + Tuils.SPACE + "TITLE NOT IMPLEMENTED";
            }
        },
        stop {

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) {
                    execute("CLOSE");
                    return null;
                }

                ((MainPack) pack).player.stop();
                return null;
            }
        },
        select {
            @Override
            public int[] args() {
                return new int[] {Command.SONG};
            }

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) return pack.context.getString(R.string.output_music_disabled);

                String s = pack.getString();
                ((MainPack) pack).player.select(s);
                return null;
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int indexNotFound) {
                return pack.context.getString(R.string.output_song_not_found);
            }
        },
        info {

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) return pack.context.getString(R.string.output_music_disabled);

                StringBuilder builder = new StringBuilder();

                MusicManager2 m = ((MainPack) pack).player;
                Song song = m.get(m.getSongIndex());
                if(song == null) return pack.context.getString(R.string.output_song_not_found);

                builder.append("Name: ").append(song.getTitle()).append(Tuils.NEWLINE);
                if(song.getID() == -1) builder.append("Path: ").append(song.getPath()).append(Tuils.NEWLINE);
                builder.append(Tuils.NEWLINE);

                int curS = m.getCurrentPosition() / 1000;
                int curMin = 0;
                if(curS >= 60) {
                    curMin = curS / 60;
                    curS = curS % 60;
                }

                int s = m.getDuration() / 1000;
                int min = 0;
                if(s >= 60) {
                    min = s / 60;
                    s = s % 60;
                }

                builder.append(curMin > 0 ? curMin + "." + curS : curS + "s").append(" of ").append(min > 0 ? min + "." + s : s + "s").append(" (").append(Tuils.percentage(m.getCurrentPosition(), m.getDuration())).append("%)");
                return builder.toString();
            }
        },
        seekto {
            @Override
            public int[] args() {
                return new int[] {Command.INT};
            }

            @Override
            public String exec(ExecutePack pack) {
                if(((MainPack) pack).player == null) return pack.context.getString(R.string.output_music_disabled);

                ((MainPack) pack).player.seekTo(pack.getInt() * 1000);
                return null;
            }

            @Override
            public String onArgNotFound(ExecutePack pack, int indexNotFound) {
                return pack.context.getString(R.string.invalid_integer);
            }
        };

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
        public String onArgNotFound(ExecutePack pack, int indexNotFound) {
            return null;
        }

        @Override
        public String onNotArgEnough(ExecutePack pack, int n) {
            return pack.context.getString(R.string.help_music);
        }

        @Override
        public int[] args() {
            return new int[0];
        }
    }

    private static void execute(String code) {
        try {
            Shell.Pool.SH.run("input keyevent KEYCODE_MEDIA_" + code);
        } catch (Shell.ShellDiedException e) {
            Log.e(Tuils.LOG_TAG, "Error in music command", e);
        }
    }

    @Override
    protected ohi.andre.consolelauncher.commands.main.Param paramForString(MainPack pack, String param) {
        return Param.get(param);
    }

    @Override
    public int priority() {
        return 4;
    }

    @Override
    public int helpRes() {
        return R.string.help_music;
    }

    @Override
    public String[] params() {
        return Param.labels();
    }

    @Override
    protected String doThings(ExecutePack pack) {
        return null;
    }
}
