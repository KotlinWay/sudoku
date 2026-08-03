package info.javaway.sudoku.ui.settings;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import info.javaway.sudoku.BuildConfig;
import info.javaway.sudoku.R;
import info.javaway.sudoku.core.Background;
import info.javaway.sudoku.settings.Links;
import info.javaway.sudoku.settings.Prefs;
import info.javaway.sudoku.settings.Theme;
import info.javaway.sudoku.ui.ThemedActivity;
import info.javaway.sudoku.ui.mvi.Store;

/**
 * Настройки: три настройки и связь с автором. Экран ничего не решает сам — он показывает,
 * что записано, и записывает то, что переключили.
 */
public class SettingsActivity extends ThemedActivity
        implements Store.View<SettingsState>, Store.Effects<SettingsEffect> {

    /**
     * Собирается в onCreate, а не полем: состояние сразу настоящее, а прочитать настройки
     * в инициализаторе поля нельзя — базового контекста у экрана к тому моменту ещё нет.
     */
    private Store<SettingsState, SettingsAction, SettingsEffect> store;

    private Prefs prefs;
    private Switch candidates;
    private Switch sound;
    private RadioGroup theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new Prefs(this);
        candidates = findViewById(R.id.candidates);
        sound = findViewById(R.id.sound);
        theme = findViewById(R.id.theme);

        // Чтение с диска на главном потоке, и это дешевле, чем кажется: файл настроек уже
        // открыт и разобран — его прочитал ThemedActivity в attachBaseContext, когда выбирал
        // тему для этого же экрана. Фоновое чтение здесь покупало не отзывчивость, а лишнюю
        // отрисовку: экран успевал показать значения по умолчанию, а через кадр переставить
        // их на настоящие, и человек видел, как переключатели сами перещёлкиваются.
        store = new Store<>(
                SettingsState.of(prefs.candidates(), prefs.sound(), prefs.theme()),
                new SettingsReducer());

        bindViews();
        store.attach(this, this);
    }

    @Override
    protected void onDestroy() {
        store.detach();
        super.onDestroy();
    }

    private void bindViews() {
        findViewById(R.id.raccoon).setOnClickListener(
                v -> store.dispatch(new SettingsAction.LinkClicked(Links.RACCOON)));

        // Версия не состояние экрана: она не меняется, пока приложение не переустановят.
        ((TextView) findViewById(R.id.version)).setText(version());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_telegram) {
            store.dispatch(new SettingsAction.LinkClicked(Links.TELEGRAM));
        } else if (id == R.id.menu_max) {
            store.dispatch(new SettingsAction.LinkClicked(Links.MAX));
        } else if (id == R.id.menu_play) {
            store.dispatch(new SettingsAction.LinkClicked(Links.PLAY_DEVELOPER));
        } else if (id == R.id.menu_rustore) {
            store.dispatch(new SettingsAction.LinkClicked(Links.RUSTORE_DEVELOPER));
        } else if (id == R.id.menu_site) {
            store.dispatch(new SettingsAction.LinkClicked(Links.SITE));
        } else if (id == R.id.menu_rate) {
            store.dispatch(new SettingsAction.RateClicked());
        } else if (id == R.id.menu_write) {
            store.dispatch(new SettingsAction.WriteClicked());
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void render(SettingsState state) {
        bind(candidates, state.candidates,
                value -> store.dispatch(new SettingsAction.CandidatesToggled(value)));
        bind(sound, state.sound,
                value -> store.dispatch(new SettingsAction.SoundToggled(value)));
        bindTheme(state.theme);
    }

    /**
     * Слушатель снимается перед установкой значения: иначе программная установка вызовет
     * обработчик, тот пошлёт действие, действие вызовет отрисовку — и так по кругу.
     */
    private void bind(Switch view, boolean value, Toggle toggle) {
        view.setOnCheckedChangeListener(null);
        view.setChecked(value);
        view.setOnCheckedChangeListener((button, checked) -> toggle.onToggle(checked));
    }

    /** То же самое для выбора темы: сначала отметить нужный кружок, потом слушать. */
    private void bindTheme(Theme value) {
        theme.setOnCheckedChangeListener(null);
        theme.check(buttonOf(value));
        theme.setOnCheckedChangeListener(
                (group, id) -> store.dispatch(new SettingsAction.ThemePicked(themeOf(id))));
    }

    private static int buttonOf(Theme value) {
        switch (value) {
            case LIGHT: return R.id.theme_light;
            case DARK: return R.id.theme_dark;
            default: return R.id.theme_system;
        }
    }

    private static Theme themeOf(int id) {
        if (id == R.id.theme_light) return Theme.LIGHT;
        if (id == R.id.theme_dark) return Theme.DARK;
        return Theme.SYSTEM;
    }

    private interface Toggle {
        void onToggle(boolean value);
    }

    @Override
    public void handle(SettingsEffect effect) {
        if (effect instanceof SettingsEffect.SaveCandidates) {
            boolean value = ((SettingsEffect.SaveCandidates) effect).value;
            Background.work(() -> prefs.setCandidates(value));
        } else if (effect instanceof SettingsEffect.SaveSound) {
            boolean value = ((SettingsEffect.SaveSound) effect).value;
            Background.work(() -> prefs.setSound(value));
        } else if (effect instanceof SettingsEffect.SaveTheme) {
            // Единственная настройка, которую пишем на главном потоке: сразу за записью экран
            // пересобирается и читает её заново — фоновая запись могла бы к тому моменту
            // не случиться, и человек увидел бы прежние цвета.
            prefs.setTheme(((SettingsEffect.SaveTheme) effect).value);
            restart();
        } else if (effect instanceof SettingsEffect.OpenLink) {
            if (!Links.open(this, ((SettingsEffect.OpenLink) effect).url)) failed();
        } else if (effect instanceof SettingsEffect.Rate) {
            if (!Links.rate(this)) failed();
        } else if (effect instanceof SettingsEffect.Write) {
            if (!Links.write(this, version())) failed();
        }
    }

    private void failed() {
        Toast.makeText(this, R.string.link_failed, Toast.LENGTH_LONG).show();
    }

    private String version() {
        return getString(R.string.version, getString(R.string.app_name), BuildConfig.VERSION_NAME);
    }
}
