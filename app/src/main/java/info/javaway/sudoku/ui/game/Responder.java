package info.javaway.sudoku.ui.game;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Handler;
import android.os.Looper;
import android.view.HapticFeedbackConstants;
import android.view.View;

import java.util.EnumMap;
import java.util.Map;

/**
 * Отклик на действие: короткий звук и отдача в палец.
 *
 * Звук собирается из синуса прямо в память — семь тонов по десятой доле секунды весят меньше
 * килобайта каждый, и это дешевле, чем класть в APK семь файлов. Заодно не нужен ни один
 * лишний ресурс и ни одна зависимость.
 *
 * Отдача идёт через {@link View#performHapticFeedback}: у него, в отличие от Vibrator,
 * нет разрешения в манифесте, а приложение обещало обходиться без разрешений вовсе.
 */
public final class Responder {

    private static final int RATE = 22050;

    /** Тон отклика: частота в герцах, длительность в миллисекундах и громкость. */
    private enum Tone {
        SELECT(480, 60, 0.30f),
        PLACE(620, 70, 0.35f),
        NOTE(420, 60, 0.28f),
        ERASE(300, 60, 0.28f),
        WRONG(160, 220, 0.45f),
        HINT(700, 100, 0.40f);

        final int hertz;
        final int millis;
        final float gain;

        Tone(int hertz, int millis, float gain) {
            this.hertz = hertz;
            this.millis = millis;
            this.gain = gain;
        }
    }

    /** Победный проигрыш: до — ми — соль — до — ми через равные промежутки. */
    private static final int[] FANFARE = {523, 659, 784, 1046, 1318};
    private static final int FANFARE_STEP = 130;
    private static final int FANFARE_LENGTH = 320;

    private final Context context;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Map<Tone, AudioTrack> tracks = new EnumMap<>(Tone.class);

    private AudioTrack fanfare;
    private boolean enabled;

    public Responder(Context context, boolean enabled) {
        this.context = context;
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void respond(View view, GameEffect.Feedback feedback) {
        haptic(view, feedback);
        if (feedback == GameEffect.Feedback.WIN) {
            playFanfare();
        } else {
            play(toneOf(feedback));
        }
    }

    /**
     * Отдача есть не на всё. На выбор клетки и на стирание её нет нарочно: по доске водят
     * пальцем постоянно, и телефон, дрожащий на каждое касание, быстро выключают целиком.
     */
    private void haptic(View view, GameEffect.Feedback feedback) {
        switch (feedback) {
            case PLACE:
            case NOTE:
            case HINT:
                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
                break;
            case WRONG:
            case WIN:
                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                break;
            default:
                break;
        }
    }

    private Tone toneOf(GameEffect.Feedback feedback) {
        switch (feedback) {
            case PLACE: return Tone.PLACE;
            case NOTE: return Tone.NOTE;
            case ERASE: return Tone.ERASE;
            case WRONG: return Tone.WRONG;
            case HINT: return Tone.HINT;
            default: return Tone.SELECT;
        }
    }

    private void playFanfare() {
        for (int step = 0; step < FANFARE.length; step++) {
            int hertz = FANFARE[step];
            handler.postDelayed(() -> {
                fanfare = replace(fanfare, hertz, FANFARE_LENGTH, 0.40f);
                start(fanfare);
            }, (long) step * FANFARE_STEP);
        }
    }

    private void play(Tone tone) {
        AudioTrack track = tracks.get(tone);
        if (track == null) {
            track = build(tone.hertz, tone.millis, tone.gain);
            tracks.put(tone, track);
        }
        start(track);
    }

    /**
     * Одна и та же дорожка играется много раз: у неподвижного буфера для этого есть
     * {@link AudioTrack#reloadStaticData()} — он возвращает курсор в начало, и заново
     * собирать звук не нужно.
     */
    private void start(AudioTrack track) {
        if (track == null || !audible()) return;
        try {
            if (track.getPlayState() != AudioTrack.PLAYSTATE_STOPPED) track.stop();
            track.reloadStaticData();
            track.play();
        } catch (IllegalStateException ignored) {
            // дорожку успели освободить между проверкой и запуском — звук не главное
        }
    }

    private AudioTrack replace(AudioTrack previous, int hertz, int millis, float gain) {
        if (previous != null) previous.release();
        return build(hertz, millis, gain);
    }

    /** Тишина, когда звук выключен в настройках или телефон переведён в беззвучный режим. */
    private boolean audible() {
        if (!enabled) return false;
        AudioManager audio = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return audio == null || audio.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    private AudioTrack build(int hertz, int millis, float gain) {
        byte[] samples = samples(hertz, millis, gain);
        AudioTrack track = new AudioTrack.Builder()
                .setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build())
                .setAudioFormat(new AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(RATE)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build())
                .setBufferSizeInBytes(samples.length)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build();
        track.write(samples, 0, samples.length);
        return track;
    }

    /**
     * Синус с затуханием. Затухание обязательно: тон, оборванный на полуволне, щёлкает,
     * и щелчок слышен отчётливее самого тона.
     */
    private static byte[] samples(int hertz, int millis, float gain) {
        int count = RATE * millis / 1000;
        byte[] pcm = new byte[count * 2];
        double step = 2 * Math.PI * hertz / RATE;
        for (int i = 0; i < count; i++) {
            double fade = Math.exp(-5.0 * i / count);
            short value = (short) (Math.sin(step * i) * fade * gain * Short.MAX_VALUE);
            pcm[i * 2] = (byte) (value & 0xFF);
            pcm[i * 2 + 1] = (byte) ((value >> 8) & 0xFF);
        }
        return pcm;
    }

    /** Экран уходит — дорожки тоже: их держит звуковая подсистема, а не сборщик мусора. */
    public void release() {
        handler.removeCallbacksAndMessages(null);
        for (AudioTrack track : tracks.values()) {
            track.release();
        }
        tracks.clear();
        if (fanfare != null) {
            fanfare.release();
            fanfare = null;
        }
    }
}
