package info.javaway.sudoku.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;

import info.javaway.sudoku.settings.Prefs;
import info.javaway.sudoku.settings.Theme;

/**
 * Экран, который знает про выбранную тему. Общий предок всех трёх экранов: тему выбирают
 * один раз на приложение, и «где-то ночь, а где-то день» — это дефект, а не свобода.
 *
 * AndroidX в проекте нет, поэтому и {@code AppCompatDelegate.setDefaultNightMode} нет тоже.
 * Ночные ресурсы выбираются полем {@code uiMode} в конфигурации, а конфигурацию экрана можно
 * подменить своей: {@link #attachBaseContext} отдаёт наверх контекст, в котором ночной бит
 * поставлен настройкой, а не системой. Дальше всё идёт обычным путём — разметка, тема и
 * {@code getResources().getColor()} читают ту же подменённую конфигурацию.
 *
 * Подмена делается копией нынешней конфигурации, а не пустой: в пустой сброшены и масштаб
 * шрифта, и плотность, и локаль — экран поехал бы вместе с темой.
 */
public abstract class ThemedActivity extends Activity {

    /** Тема, с которой этот экран построен. Сравнивается с настройкой при каждом показе. */
    private Theme applied = Theme.SYSTEM;

    @Override
    protected void attachBaseContext(Context base) {
        applied = new Prefs(base).theme();
        super.attachBaseContext(night(base, applied));
    }

    /**
     * Тему меняют на экране настроек, а этот экран всё это время лежит в стопке под ним:
     * система его не пересоздаёт, потому что с её точки зрения ничего не менялось.
     * Поэтому сверяемся сами и пересоздаёмся, если настройка разошлась с построенным.
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (new Prefs(this).theme() != applied) restart();
    }

    /**
     * Перестроить экран на нынешней теме.
     *
     * Именно перезапуск, а не {@code recreate()}: при пересоздании платформа отдаёт новому
     * экземпляру старое окно вместе с уже построенным decor, а он собран на прежнем контексте
     * и прежней конфигурации. На экране от этого остаётся пустой чёрный прямоугольник без
     * бара. Своя же копия экрана получает окно с нуля.
     *
     * Анимация погашена с обеих сторон: человек менял тему, а не уходил с экрана, и переход
     * должен выглядеть перекраской, а не сменой страницы.
     */
    protected void restart() {
        Intent intent = getIntent();
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private static Context night(Context base, Theme theme) {
        if (theme == Theme.SYSTEM) return base;
        Configuration config = new Configuration(base.getResources().getConfiguration());
        config.uiMode = (config.uiMode & ~Configuration.UI_MODE_NIGHT_MASK)
                | (theme == Theme.DARK
                ? Configuration.UI_MODE_NIGHT_YES : Configuration.UI_MODE_NIGHT_NO);
        return base.createConfigurationContext(config);
    }
}
