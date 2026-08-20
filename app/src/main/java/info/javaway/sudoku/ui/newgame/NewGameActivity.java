package info.javaway.sudoku.ui.newgame;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import info.javaway.sudoku.R;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.GameMode;
import info.javaway.sudoku.settings.Prefs;
import info.javaway.sudoku.ui.Labels;
import info.javaway.sudoku.ui.ThemedActivity;
import info.javaway.sudoku.ui.mvi.Store;

/**
 * Выбор уровня для новой партии. Отдельным экраном, а не вкладками над доской: решение
 * принимают раз в партию, а место над доской нужно каждую секунду.
 *
 * Экран ничего не начинает сам — он отдаёт выбор обратно игре и закрывается.
 */
public class NewGameActivity extends ThemedActivity
        implements Store.View<NewGameState>, Store.Effects<NewGameEffect> {

    /** Начатая партия, которую сотрёт выбор. Отсутствие уровня означает «стирать нечего». */
    public static final String EXTRA_CURRENT_LEVEL = "current_level";
    public static final String EXTRA_CURRENT_MODE = "current_mode";
    public static final String EXTRA_CURRENT_SECONDS = "current_seconds";

    public static final String EXTRA_LEVEL = "level";
    public static final String EXTRA_MODE = "mode";

    private Store<NewGameState, NewGameAction, NewGameEffect> store;
    private Prefs prefs;

    private TextView current;
    private Switch relaxed;
    private View[] rows;
    private final CompoundButton.OnCheckedChangeListener relaxedListener = (button, checked) ->
            store.dispatch(new NewGameAction.ModeToggled(
                    checked ? GameMode.RELAXED : GameMode.STANDARD));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        current = findViewById(R.id.current);
        relaxed = findViewById(R.id.relaxed);
        rows = new View[]{
                findViewById(R.id.level_easy), findViewById(R.id.level_medium),
                findViewById(R.id.level_hard), findViewById(R.id.level_expert)
        };

        Intent intent = getIntent();
        String level = intent.getStringExtra(EXTRA_CURRENT_LEVEL);
        Difficulty currentLevel = level == null ? null : Difficulty.byName(level, Difficulty.MEDIUM);
        prefs = new Prefs(this);
        store = new Store<>(NewGameState.of(
                currentLevel,
                currentLevel == null ? null : GameMode.byName(
                        intent.getStringExtra(EXTRA_CURRENT_MODE), GameMode.STANDARD),
                intent.getIntExtra(EXTRA_CURRENT_SECONDS, 0), prefs.gameMode()), new NewGameReducer());

        bindViews();
        store.attach(this, this);
    }

    @Override
    protected void onDestroy() {
        store.detach();
        super.onDestroy();
    }

    private void bindViews() {
        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            Difficulty level = levels[i];
            rows[i].setOnClickListener(v -> store.dispatch(new NewGameAction.LevelPicked(level)));
        }
    }

    @Override
    public void render(NewGameState state) {
        relaxed.setOnCheckedChangeListener(null);
        relaxed.setChecked(state.mode == GameMode.RELAXED);
        relaxed.setOnCheckedChangeListener(relaxedListener);

        current.setVisibility(state.hasCurrentGame() ? View.VISIBLE : View.GONE);
        if (state.hasCurrentGame()) {
            int warning = state.currentMode == GameMode.RELAXED
                    ? R.string.current_game_relaxed : R.string.current_game;
            current.setText(getString(warning, Labels.level(this, state.currentLevel),
                    Labels.time(this, state.currentSeconds)));
        }

        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            fill(rows[i], Labels.level(this, levels[i]), Labels.note(this, levels[i]));
        }
    }

    /** Строка целиком — одна цель для TalkBack: название и подпись читаются вместе. */
    private void fill(View row, String name, String note) {
        ((TextView) row.findViewById(R.id.level_name)).setText(name);
        ((TextView) row.findViewById(R.id.level_note)).setText(note);
        row.setContentDescription(name + System.lineSeparator() + note);
    }

    @Override
    public void handle(NewGameEffect effect) {
        if (effect instanceof NewGameEffect.RememberMode) {
            prefs.setGameMode(((NewGameEffect.RememberMode) effect).mode);
        } else if (effect instanceof NewGameEffect.Start) {
            Intent result = new Intent();
            result.putExtra(EXTRA_LEVEL, ((NewGameEffect.Start) effect).level.name());
            result.putExtra(EXTRA_MODE, ((NewGameEffect.Start) effect).mode.name());
            setResult(RESULT_OK, result);
            finish();
        }
    }
}
