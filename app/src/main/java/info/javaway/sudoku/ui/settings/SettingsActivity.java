package info.javaway.sudoku.ui.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import info.javaway.sudoku.BuildConfig;
import info.javaway.sudoku.R;
import info.javaway.sudoku.core.Background;
import info.javaway.sudoku.settings.Links;
import info.javaway.sudoku.settings.Prefs;
import info.javaway.sudoku.ui.mvi.Store;

/**
 * Настройки: два переключателя игры и связь с автором. Экран ничего не решает сам —
 * он показывает, что записано, и записывает то, что переключили.
 */
public class SettingsActivity extends Activity
        implements Store.View<SettingsState>, Store.Effects<SettingsEffect> {

    private final Store<SettingsState, SettingsAction, SettingsEffect> store =
            new Store<>(SettingsState.initial(), new SettingsReducer());

    private Prefs prefs;
    private Switch candidates;
    private Switch sound;
    private View rustore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = new Prefs(this);
        candidates = findViewById(R.id.candidates);
        sound = findViewById(R.id.sound);
        rustore = findViewById(R.id.link_rustore);

        bindViews();
        store.attach(this, this);
        handle(new SettingsEffect.Load());
    }

    @Override
    protected void onDestroy() {
        store.detach();
        super.onDestroy();
    }

    private void bindViews() {
        findViewById(R.id.raccoon).setOnClickListener(
                v -> store.dispatch(new SettingsAction.LinkClicked(Links.RACCOON)));
        findViewById(R.id.link_telegram).setOnClickListener(
                v -> store.dispatch(new SettingsAction.LinkClicked(Links.TELEGRAM)));
        findViewById(R.id.link_max).setOnClickListener(
                v -> store.dispatch(new SettingsAction.LinkClicked(Links.MAX)));
        findViewById(R.id.link_play).setOnClickListener(
                v -> store.dispatch(new SettingsAction.LinkClicked(Links.PLAY_DEVELOPER)));
        rustore.setOnClickListener(
                v -> store.dispatch(new SettingsAction.LinkClicked(Links.RUSTORE_DEVELOPER)));
        findViewById(R.id.link_site).setOnClickListener(
                v -> store.dispatch(new SettingsAction.LinkClicked(Links.SITE)));
        findViewById(R.id.link_rate).setOnClickListener(
                v -> store.dispatch(new SettingsAction.RateClicked()));
        findViewById(R.id.link_write).setOnClickListener(
                v -> store.dispatch(new SettingsAction.WriteClicked()));

        // Версия не состояние экрана: она не меняется, пока приложение не переустановят.
        ((TextView) findViewById(R.id.version)).setText(version());
    }

    @Override
    public void render(SettingsState state) {
        bind(candidates, state.candidates,
                value -> store.dispatch(new SettingsAction.CandidatesToggled(value)));
        bind(sound, state.sound,
                value -> store.dispatch(new SettingsAction.SoundToggled(value)));
        // Пункт без адреса — обманка: нажимать не на что, пока владелец не даст ссылку.
        rustore.setVisibility(Links.RUSTORE_DEVELOPER.isEmpty() ? View.GONE : View.VISIBLE);
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

    private interface Toggle {
        void onToggle(boolean value);
    }

    @Override
    public void handle(SettingsEffect effect) {
        if (effect instanceof SettingsEffect.Load) {
            Background.work(() -> {
                boolean showCandidates = prefs.candidates();
                boolean playSound = prefs.sound();
                Background.main(() -> store.dispatch(
                        new SettingsAction.Loaded(showCandidates, playSound)));
            });
        } else if (effect instanceof SettingsEffect.SaveCandidates) {
            boolean value = ((SettingsEffect.SaveCandidates) effect).value;
            Background.work(() -> prefs.setCandidates(value));
        } else if (effect instanceof SettingsEffect.SaveSound) {
            boolean value = ((SettingsEffect.SaveSound) effect).value;
            Background.work(() -> prefs.setSound(value));
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
