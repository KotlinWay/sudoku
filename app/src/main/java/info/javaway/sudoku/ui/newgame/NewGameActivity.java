package info.javaway.sudoku.ui.newgame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import info.javaway.sudoku.R;
import info.javaway.sudoku.core.Background;
import info.javaway.sudoku.game.Day;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.stats.StatsStore;
import info.javaway.sudoku.ui.Labels;
import info.javaway.sudoku.ui.mvi.Store;

/**
 * Выбор уровня для новой партии. Отдельным экраном, а не вкладками над доской: решение
 * принимают раз в партию, а место над доской нужно каждую секунду.
 *
 * Экран ничего не начинает сам — он отдаёт выбор обратно игре и закрывается.
 */
public class NewGameActivity extends Activity
        implements Store.View<NewGameState>, Store.Effects<NewGameEffect> {

    /** Начатая партия, которую сотрёт выбор. Отсутствие уровня означает «стирать нечего». */
    public static final String EXTRA_CURRENT_LEVEL = "current_level";
    public static final String EXTRA_CURRENT_DAILY = "current_daily";
    public static final String EXTRA_CURRENT_SECONDS = "current_seconds";

    public static final String EXTRA_LEVEL = "level";
    public static final String EXTRA_DAILY = "daily";

    private Store<NewGameState, NewGameAction, NewGameEffect> store;
    private StatsStore statsStore;
    private Day today;

    private TextView current;
    private View[] rows;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_game);

        statsStore = new StatsStore(this);
        today = Day.now();

        current = findViewById(R.id.current);
        rows = new View[]{
                findViewById(R.id.level_easy), findViewById(R.id.level_medium),
                findViewById(R.id.level_hard), findViewById(R.id.level_expert),
                findViewById(R.id.level_daily)
        };

        Intent intent = getIntent();
        String level = intent.getStringExtra(EXTRA_CURRENT_LEVEL);
        // Номер показываем коротким: полный номер дня от эпохи пятизначный и ничего не говорит.
        store = new Store<>(NewGameState.of(
                today.label(), today.level(),
                level == null ? null : Difficulty.byName(level, Difficulty.MEDIUM),
                intent.getBooleanExtra(EXTRA_CURRENT_DAILY, false),
                intent.getIntExtra(EXTRA_CURRENT_SECONDS, 0)), new NewGameReducer());

        bindViews();
        store.attach(this, this);
        handle(new NewGameEffect.Load());
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
            rows[i].setOnClickListener(
                    v -> store.dispatch(new NewGameAction.LevelPicked(level, false)));
        }
        // Уровень задачи дня определит редьюсер по дате: здесь важно только «это задача дня».
        rows[levels.length].setOnClickListener(
                v -> store.dispatch(new NewGameAction.LevelPicked(null, true)));
    }

    @Override
    public void render(NewGameState state) {
        current.setVisibility(state.hasCurrentGame() ? View.VISIBLE : View.GONE);
        if (state.hasCurrentGame()) {
            current.setText(getString(R.string.current_game,
                    Labels.level(this, state.currentLevel, state.currentDaily),
                    Labels.time(this, state.currentSeconds)));
        }

        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            fill(rows[i], Labels.level(this, levels[i], false), Labels.note(this, levels[i]));
        }
        fill(rows[levels.length], Labels.level(this, state.dailyLevel, true),
                state.dailySolved
                        ? getString(R.string.daily_note_solved, state.dailyNumber)
                        : getString(R.string.daily_note, state.dailyNumber,
                        Labels.note(this, state.dailyLevel)));
    }

    /** Строка целиком — одна цель для TalkBack: название и подпись читаются вместе. */
    private void fill(View row, String name, String note) {
        ((TextView) row.findViewById(R.id.level_name)).setText(name);
        ((TextView) row.findViewById(R.id.level_note)).setText(note);
        row.setContentDescription(name + System.lineSeparator() + note);
    }

    @Override
    public void handle(NewGameEffect effect) {
        if (effect instanceof NewGameEffect.Load) {
            Background.work(() -> {
                boolean solved = statsStore.load().solvedDailyOn(today);
                Background.main(() -> store.dispatch(new NewGameAction.Loaded(solved)));
            });
            return;
        }
        if (effect instanceof NewGameEffect.Start) {
            NewGameEffect.Start start = (NewGameEffect.Start) effect;
            Intent result = new Intent();
            result.putExtra(EXTRA_LEVEL, start.level.name());
            result.putExtra(EXTRA_DAILY, start.daily);
            setResult(RESULT_OK, result);
            finish();
        }
    }
}
