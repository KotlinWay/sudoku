package info.javaway.sudoku.settings;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Адреса каналов и магазинов владельца. Не переводятся, поэтому живут в коде, а не в ресурсах.
 * Само приложение в сеть не ходит: всё, что тут есть, отдаётся системе через ACTION_VIEW.
 */
public final class Links {

    public static final String TELEGRAM = "https://t.me/max_simple_apps";
    public static final String MAX = "https://max.ru/channel_max_simple_apps";
    public static final String PLAY_DEVELOPER =
            "https://play.google.com/store/apps/dev?id=6023648979127962332";
    public static final String SITE = "https://javaway.info";
    public static final String EMAIL = "max.simple.apps@gmail.com";
    private static final String RACCOON_ID = "info.javaway.raccoon_notes";

    /** Страница разработчика в RuStore. Одна на все приложения владельца. */
    public static final String RUSTORE_DEVELOPER = "https://www.rustore.ru/catalog/developer/a83331c1";

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

    /**
     * Карточка приложения в том магазине, откуда оно поставлено. Аудитория RuStore часто
     * живёт без Google Play, и ссылка на Play для неё мертва.
     */
    public static boolean rate(Context context) {
        String installer = installerOf(context);
        String id = context.getPackageName();
        if (installer != null && installer.contains("rustore")) {
            return open(context, "rustore://apps.rustore.ru/app/" + id)
                    || open(context, "https://apps.rustore.ru/app/" + id);
        }
        return open(context, "market://details?id=" + id)
                || open(context, "https://play.google.com/store/apps/details?id=" + id);
    }

    /**
     * Карточка Блокнота Енота в том магазине, откуда стоит само приложение. Тот же довод,
     * что у rate(): аудитория RuStore часто без Google Play, и ссылка на Play для неё мертва.
     */
    public static boolean raccoon(Context context) {
        String installer = installerOf(context);
        if (installer != null && installer.contains("rustore")) {
            return open(context, "rustore://apps.rustore.ru/app/" + RACCOON_ID)
                    || open(context, "https://apps.rustore.ru/app/" + RACCOON_ID);
        }
        return open(context, "market://details?id=" + RACCOON_ID)
                || open(context, "https://play.google.com/store/apps/details?id=" + RACCOON_ID);
    }

    private static String installerOf(Context context) {
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                return context.getPackageManager()
                        .getInstallSourceInfo(context.getPackageName()).getInstallingPackageName();
            }
            return context.getPackageManager().getInstallerPackageName(context.getPackageName());
        } catch (Exception e) {
            return null;
        }
    }

    private Links() {
    }
}
