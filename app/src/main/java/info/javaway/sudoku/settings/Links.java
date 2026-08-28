package info.javaway.sudoku.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import info.javaway.sudoku.BuildConfig;

/**
 * Адреса каналов и магазинов владельца. Не переводятся, поэтому живут в коде, а не в ресурсах.
 * Само приложение в сеть не ходит: всё, что тут есть, отдаётся системе через ACTION_VIEW.
 */
public final class Links {

    public static final String TELEGRAM = "https://t.me/max_simple_apps";
    public static final String MAX = "https://max.ru/channel_max_simple_apps";
    static final String GOOGLE_PLAY = "GOOGLE_PLAY";
    static final String RUSTORE = "RUSTORE";

    private static final String PLAY_DEVELOPER =
            "https://play.google.com/store/apps/dev?id=6023648979127962332";
    public static final String SITE = "https://javaway.info";
    public static final String EMAIL = "max.simple.apps@gmail.com";
    private static final String RACCOON_ID = "info.javaway.raccoon_notes";

    /** Страница разработчика в RuStore. Одна на все приложения владельца. */
    private static final String RUSTORE_DEVELOPER = "https://www.rustore.ru/catalog/developer/a83331c1";

    public static boolean open(Context context, String url) {
        if (url == null || url.isEmpty()) return false;
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public static boolean write(Context context, String subject) {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + EMAIL));
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        try {
            context.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    /** Карточка приложения в магазине, который выбран release-веткой. */
    public static boolean rate(Context context) {
        String id = context.getPackageName();
        return open(context, appUri(BuildConfig.STORE, id))
                || open(context, webUrl(BuildConfig.STORE, id));
    }

    /** Карточка Блокнота Енота в магазине, который выбран release-веткой. */
    public static boolean raccoon(Context context) {
        return open(context, appUri(BuildConfig.STORE, RACCOON_ID))
                || open(context, webUrl(BuildConfig.STORE, RACCOON_ID));
    }

    public static boolean isGooglePlay() {
        return isGooglePlay(BuildConfig.STORE);
    }

    public static String developerUrl() {
        return developerUrl(BuildConfig.STORE);
    }

    static boolean isGooglePlay(String store) {
        requireStore(store);
        return GOOGLE_PLAY.equals(store);
    }

    static String developerUrl(String store) {
        return isGooglePlay(store) ? PLAY_DEVELOPER : RUSTORE_DEVELOPER;
    }

    static String appUri(String store, String id) {
        return isGooglePlay(store)
                ? "market://details?id=" + id
                : "rustore://apps.rustore.ru/app/" + id;
    }

    static String webUrl(String store, String id) {
        return isGooglePlay(store)
                ? "https://play.google.com/store/apps/details?id=" + id
                : "https://apps.rustore.ru/app/" + id;
    }

    private static void requireStore(String store) {
        if (!GOOGLE_PLAY.equals(store) && !RUSTORE.equals(store)) {
            throw new IllegalArgumentException("Unknown release store: " + store);
        }
    }

    private Links() {
    }
}
