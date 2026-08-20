package info.javaway.sudoku.ui.game;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Random;

import info.javaway.sudoku.R;
import info.javaway.sudoku.core.Background;
import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Cells;
import info.javaway.sudoku.game.Day;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.Generator;
import info.javaway.sudoku.game.GameMode;
import info.javaway.sudoku.game.Rng;
import info.javaway.sudoku.settings.Prefs;
import info.javaway.sudoku.stats.Outcome;
import info.javaway.sudoku.stats.Stats;
import info.javaway.sudoku.stats.StatsStore;
import info.javaway.sudoku.ui.Labels;
import info.javaway.sudoku.ui.ThemedActivity;
import info.javaway.sudoku.ui.mvi.Store;
import info.javaway.sudoku.ui.newgame.NewGameActivity;
import info.javaway.sudoku.ui.settings.SettingsActivity;

/**
 * Экран игры. Делает ровно две вещи: превращает жесты в действия и рисует состояние.
 * Правила игры сюда не заглядывают — они целиком в {@link GameReducer}.
 *
 * На экране осталось только то, без чего не сделать ход: счётчики, доска и клавиатура.
 * Инструменты живут значками в баре, выбор уровня — на отдельном экране: и то и другое
 * стоило доске высоты, а доске нужна вся, какую экран может дать.
 */
public class GameActivity extends ThemedActivity
        implements Store.View<GameState>, Store.Effects<GameEffect> {

    private static final long TICK_MILLIS = 1000;

    private static final int NEW_GAME = 1;

    /** Прозрачность значка недоступного действия. Платформа гасит подпись, но не картинку. */
    private static final int DISABLED_ALPHA = 77;
    private static final int ENABLED_ALPHA = 255;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Random random = new Random();

    private Store<GameState, GameAction, GameEffect> store;
    private GameSave save;
    private StatsStore statsStore;
    private Prefs prefs;
    private Responder responder;
    private Day today;

    private BoardView board;
    private ConfettiView confetti;
    private LinearLayout hearts;
    private GameMode renderedLivesMode;
    private TextView hints;
    private TextView timer;
    /** Подпись уровня. Только в альбоме: в портрете уровень назван заголовком бара. */
    private TextView level;
    private TextView generating;
    private View panel;
    private TextView panelTitle;
    private TextView panelText;
    private TextView panelNote;
    private Button panelPrimary;
    private Button panelSecondary;
    private Button[] keys;
    private View erase;

    /** Полоса инструментов слева. Заведена только в альбоме, где бар спрятан. */
    private ImageButton toolUndo;
    private ImageButton toolPause;

    private boolean landscape;
    private boolean keepScreenOn;
    private int visibleModalCount;

    private Menu menu;

    private Object backCallback;

    private final Runnable tick = new Runnable() {
        @Override
        public void run() {
            store.dispatch(new GameAction.Ticked());
            handler.postDelayed(this, TICK_MILLIS);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        prefs = new Prefs(this);
        save = new GameSave(getFilesDir());
        statsStore = new StatsStore(this);
        responder = new Responder(this, prefs.sound());
        today = Day.now();

        setupBar();
        bindViews();

        // Чтение сохранения на главном потоке намеренно: это один небольшой файл, а рисовать
        // пустую доску, которая через мгновение сменится вчерашней партией, — хуже.
        GameState restored = save.load(prefs.candidates());
        store = new Store<>(restored != null
                ? restored
                : GameState.generating(
                        Difficulty.EASY, prefs.gameMode(), prefs.candidates()),
                new GameReducer());
        store.attach(this, this);
        if (restored == null) generate(Difficulty.EASY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        today = Day.now();
        responder.setEnabled(prefs.sound());
        store.dispatch(new GameAction.CandidatesChanged(prefs.candidates()));
        keepScreenOn = prefs.keepScreenOn();
        renderScreenPolicy(store.state());
        // Часы тикают всегда, пока экран виден: тик вне игры редьюсер просто не примет.
        handler.postDelayed(tick, TICK_MILLIS);
    }

    @Override
    protected void onPause() {
        keepScreenOn = false;
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        handler.removeCallbacks(tick);
        // Единственное место, где партия ложится на диск. Система обещает вызвать onPause
        // перед тем, как убить процесс, поэтому чаще незачем.
        save.save(store.state());
        super.onPause();
    }

    /**
     * Поворот экрана переживается без пересоздания (см. configChanges в манифесте), но разметку
     * система сама не перечитает — а она у альбома своя. Раздуваем её здесь и рисуем нынешнее
     * состояние: партия всё это время лежит в сторе, диск не при чём.
     */
    @Override
    public void onConfigurationChanged(Configuration config) {
        super.onConfigurationChanged(config);
        setContentView(R.layout.activity_game);
        setupBar();
        bindViews();
        render(store.state());
    }

    @Override
    protected void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        responder.release();
        store.detach();
        super.onDestroy();
    }

    /**
     * В альбоме бар спрятан. Лежачим он забирал бы 56dp высоты, а высота в альбоме — это
     * сторона доски: доска упирается в неё, а не в ширину. Инструменты стоят там полосой
     * слева, и отнимает она уже ширину, которой в альбоме избыток.
     */
    private void setupBar() {
        landscape = getResources().getConfiguration().orientation
                == Configuration.ORIENTATION_LANDSCAPE;

        ActionBar bar = getActionBar();
        if (bar == null) return;
        if (landscape) {
            bar.hide();
        } else {
            bar.show();
        }
    }

    private void bindViews() {
        board = findViewById(R.id.board);
        confetti = findViewById(R.id.confetti);
        hearts = findViewById(R.id.hearts);
        renderedLivesMode = null;
        hints = findViewById(R.id.hints);
        timer = findViewById(R.id.timer);
        level = findViewById(R.id.level);
        generating = findViewById(R.id.generating);
        panel = findViewById(R.id.panel);
        panelTitle = findViewById(R.id.panel_title);
        panelText = findViewById(R.id.panel_text);
        panelNote = findViewById(R.id.panel_note);
        panelPrimary = findViewById(R.id.panel_primary);
        panelSecondary = findViewById(R.id.panel_secondary);
        erase = findViewById(R.id.key_erase);

        keys = new Button[]{
                findViewById(R.id.key_1), findViewById(R.id.key_2), findViewById(R.id.key_3),
                findViewById(R.id.key_4), findViewById(R.id.key_5), findViewById(R.id.key_6),
                findViewById(R.id.key_7), findViewById(R.id.key_8), findViewById(R.id.key_9)
        };

        board.setOnCellTap(cell -> store.dispatch(new GameAction.CellTapped(cell)));

        for (int i = 0; i < keys.length; i++) {
            int value = i + 1;
            keys[i].setText(String.valueOf(value));
            keys[i].setContentDescription(getString(R.string.key_desc, value));
            keys[i].setOnClickListener(v -> store.dispatch(new GameAction.DigitTapped(value)));
        }
        erase.setOnClickListener(v -> store.dispatch(new GameAction.EraseTapped()));

        // Счётчик подсказок он же кнопка. Слушатель ставится обеим ориентациям: плашка
        // есть и в портрете, и в альбоме, а лампочки в баре и в полосе больше нет.
        hints.setOnClickListener(v -> store.dispatch(new GameAction.HintTapped()));
        expandHintTouch();

        if (!landscape) return;
        toolUndo = findViewById(R.id.tool_undo);
        toolPause = findViewById(R.id.tool_pause);

        toolUndo.setOnClickListener(v -> store.dispatch(new GameAction.UndoTapped()));
        toolPause.setOnClickListener(v -> store.dispatch(new GameAction.PauseToggled()));
        findViewById(R.id.tool_more).setOnClickListener(this::showMore);
    }

    /**
     * Даёт счётчику подсказок зону нажатия 48dp, не увеличивая саму плашку.
     *
     * Плашка ростом 32dp, и поднимать её нельзя: она стоит в ряду над доской, а ряд —
     * над клавишами. Поднять плашку значит поднять ряд, и тогда в портрете доска теряет
     * 12dp стороны, а в альбоме на 16dp худеют клавиши и уходят под тот же норматив,
     * ради которого всё затевалось. Замерено на эмуляторе, а не предположено.
     *
     * Недостающие точки берутся у отступов вокруг ряда: сверху это внутренний отступ
     * страницы, снизу — зазор до доски. Кликать там всё равно не по чему, поэтому зона
     * ничего не отнимает у соседей. Делегат вешается на родителя ряда, а не на сам ряд:
     * прямоугольник выходит за границы ряда, а до делегата доходят только те нажатия,
     * которые получила вьюха, на которой он стоит.
     *
     * Пересчитывается при каждой перекладке: ширина плашки меняется вместе с числом,
     * а в альбоме ряд ещё и переезжает вслед за длиной названия уровня.
     */
    private void expandHintTouch() {
        View row = findViewById(R.id.counters);
        View host = (View) row.getParent();
        int min = getResources().getDimensionPixelSize(R.dimen.touch_target);
        row.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or, ob) -> {
            Rect rect = new Rect();
            hints.getHitRect(rect);
            rect.offset(row.getLeft(), row.getTop());
            grow(rect, min);
            host.setTouchDelegate(new TouchDelegate(rect, hints));
        });
    }

    /** Раздувает прямоугольник до заданной стороны, оставляя его центр на месте. */
    private static void grow(Rect rect, int min) {
        int dx = Math.max(0, min - rect.width());
        int dy = Math.max(0, min - rect.height());
        rect.left -= dx / 2;
        rect.right += dx - dx / 2;
        rect.top -= dy / 2;
        rect.bottom += dy - dy / 2;
    }

    /* ── Отрисовка ────────────────────────────────────────────────────────── */

    @Override
    public void render(GameState state) {
        board.show(state);
        generating.setVisibility(
                state.phase == GameState.Phase.GENERATING ? View.VISIBLE : View.GONE);
        // Уровень назван в заголовке: вкладок над доской больше нет, а знать, во что играешь,
        // надо. Место в баре всё равно пустовало. В альбоме бара нет, и уровень подписан
        // строкой над клавишами — заголовок всё равно ставим, его показывают «недавние».
        String name = Labels.level(this, state.level);
        setTitle(name);
        if (level != null) level.setText(name);

        renderStatus(state);
        renderKeys(state);
        renderTools(state);
        renderPanel(state);
        syncBackCallback(state);
        renderScreenPolicy(state);
    }

    private void renderScreenPolicy(GameState state) {
        int flag = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        boolean current = (getWindow().getAttributes().flags & flag) != 0;
        boolean modalVisible = visibleModalCount > 0;
        if (!ScreenPolicy.shouldChange(current, keepScreenOn, state.phase, modalVisible)) return;
        if (ScreenPolicy.keepOn(keepScreenOn, state.phase, modalVisible)) {
            getWindow().addFlags(flag);
        } else {
            getWindow().clearFlags(flag);
        }
    }

    private void onModalVisibilityChanged(boolean visible) {
        visibleModalCount += visible ? 1 : -1;
        renderScreenPolicy(store.state());
    }

    /** Жизни рисуются конечными сердцами или сердцем с бесконечностью, смотря по режиму. */
    private void renderStatus(GameState state) {
        if (renderedLivesMode != state.mode) {
            hearts.removeAllViews();
            if (state.mode == GameMode.RELAXED) {
                ImageView heart = new ImageView(this);
                heart.setImageResource(R.drawable.ic_heart);
                heart.setColorFilter(color(R.color.heart));
                heart.setContentDescription(null);
                heart.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                int side = getResources().getDimensionPixelSize(R.dimen.space_l);
                LinearLayout.LayoutParams heartParams =
                        new LinearLayout.LayoutParams(side, side);
                heartParams.setMarginEnd(
                        getResources().getDimensionPixelSize(R.dimen.space_xs));
                hearts.addView(heart, heartParams);

                TextView infinity = new TextView(this, null, 0, R.style.Text_Body);
                infinity.setText(R.string.infinity);
                infinity.setTextColor(color(R.color.heart));
                infinity.setContentDescription(null);
                infinity.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                hearts.addView(infinity);
            }
            renderedLivesMode = state.mode;
        }

        int limit = state.mistakeLimit();
        if (state.mode == GameMode.STANDARD) {
            while (hearts.getChildCount() > limit) {
                hearts.removeViewAt(hearts.getChildCount() - 1);
            }
            while (hearts.getChildCount() < limit) {
                ImageView heart = new ImageView(this);
                heart.setImageResource(R.drawable.ic_heart);
                heart.setContentDescription(null);
                heart.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
                int side = getResources().getDimensionPixelSize(R.dimen.space_l);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(side, side);
                params.setMarginEnd(getResources().getDimensionPixelSize(R.dimen.space_xs));
                hearts.addView(heart, params);
            }
            int left = limit - state.mistakes;
            for (int i = 0; i < limit; i++) {
                ((ImageView) hearts.getChildAt(i)).setColorFilter(
                        color(i < left ? R.color.heart : R.color.heart_lost));
            }
            hearts.setContentDescription(
                    getString(R.string.lives_desc, Math.max(0, left), limit));
        } else {
            hearts.setContentDescription(getString(R.string.relaxed_lives_desc));
        }

        hints.setText(String.valueOf(state.hintsLeft));
        // Плашка теперь кнопка, поэтому описание говорит и что будет, и сколько осталось:
        // роль «кнопка» TalkBack добавит сам.
        hints.setContentDescription(getString(R.string.hint_button_desc, state.hintsLeft));

        String time = Labels.time(this, state.seconds);
        timer.setText(time);
        timer.setContentDescription(getString(R.string.time_desc, time));
    }

    private void renderKeys(GameState state) {
        for (int i = 0; i < keys.length; i++) {
            keys[i].setEnabled(state.accepts());
            keys[i].setActivated(state.board.isComplete(i + 1));
        }
        erase.setEnabled(state.accepts());
    }

    /**
     * Инструменты. Что доступно и каким значком показано — считается здесь один раз, а куда
     * это лечь, решает ориентация: в портрете инструменты живут пунктами бара, в альбоме —
     * кнопками полосы слева. Условия общие: разведи их по двум местам, и правило «подсказка
     * недоступна, когда подсказок нет» разойдётся между ориентациями при первой же правке.
     */
    private void renderTools(GameState state) {
        boolean paused = state.phase == GameState.Phase.PAUSED;
        boolean undo = state.accepts() && state.history.canUndo();
        boolean hint = state.accepts() && state.hintsLeft > 0;
        boolean pause = state.accepts() || paused;

        int pauseIcon = paused ? R.drawable.ic_play : R.drawable.ic_pause;
        int pauseTitle = paused ? R.string.resume : R.string.tool_pause;

        // Подсказка не зависит от ориентации: её кнопка это счётчик над доской, и он один
        // на обе разметки. Гашение цифры и лампочки берёт на себя `color/badge_hint`.
        hints.setEnabled(hint);

        if (landscape) {
            // «Ещё» доступно всегда: под ним статистика и настройки, нужные и после партии.
            tool(toolUndo, undo, R.drawable.ic_undo, R.string.tool_undo);
            tool(toolPause, pause, pauseIcon, pauseTitle);
            return;
        }

        // Меню появляется позже первой отрисовки, поэтому его здесь может ещё не быть:
        // onCreateOptionsMenu нарисует всё заново, когда оно придёт.
        if (menu == null) return;

        enable(menu.findItem(R.id.menu_undo), undo);

        MenuItem pauseItem = menu.findItem(R.id.menu_pause);
        enable(pauseItem, pause);
        pauseItem.setIcon(pauseIcon);
        pauseItem.setTitle(pauseTitle);
        alpha(pauseItem, pause);
    }

    private void tool(ImageButton button, boolean enabled, int icon, int description) {
        button.setEnabled(enabled);
        button.setImageResource(icon);
        // Платформа гасит недоступную кнопку, но не картинку в ней — так же, как и в меню.
        button.setImageAlpha(enabled ? ENABLED_ALPHA : DISABLED_ALPHA);
        button.setContentDescription(getString(description));
    }

    private void enable(MenuItem item, boolean enabled) {
        item.setEnabled(enabled);
        alpha(item, enabled);
    }

    private void alpha(MenuItem item, boolean enabled) {
        Drawable icon = item.getIcon();
        if (icon != null) icon.mutate().setAlpha(enabled ? ENABLED_ALPHA : DISABLED_ALPHA);
    }

    /**
     * Переполнение полосы. В портрете этот список платформа рисует сама под тремя точками
     * бара, а в альбоме бара нет — собираем то же меню, пряча пункты, которые уже стоят
     * в полосе значками.
     */
    private void showMore(View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.inflate(R.menu.game);

        Menu items = popup.getMenu();
        items.findItem(R.id.menu_undo).setVisible(false);
        items.findItem(R.id.menu_pause).setVisible(false);

        popup.setOnMenuItemClickListener(this::onOptionsItemSelected);
        popup.show();
    }

    /**
     * Панель одна на паузу, победу и поражение: у всех трёх состав одинаков — заголовок,
     * объяснение, приписка и одна-две кнопки. Разными их делает только текст.
     */
    private void renderPanel(GameState state) {
        panel.setVisibility(state.panel ? View.VISIBLE : View.GONE);
        if (!state.panel) return;

        if (state.phase == GameState.Phase.PAUSED) {
            fillPanel(R.string.paused_title, getString(R.string.paused_text), null,
                    getString(R.string.resume),
                    v -> store.dispatch(new GameAction.PauseToggled()), null);
            return;
        }
        if (state.phase == GameState.Phase.LOST) {
            fillPanel(R.string.lost_title, getString(R.string.lost_text), null,
                    getString(R.string.try_again),
                    v -> store.dispatch(new GameAction.NewGame(state.level, state.mode)), null);
            return;
        }

        String summary = getString(R.string.won_summary,
                getString(R.string.won_time, Labels.time(this, state.seconds)),
                getResources().getQuantityString(
                        R.plurals.won_hints, state.hintsUsed, state.hintsUsed),
                Labels.level(this, state.level));
        String note = state.record
                ? getString(R.string.won_record)
                : state.best != Stats.NO_TIME
                ? getString(R.string.won_best, Labels.time(this, state.best))
                : getString(R.string.won_no_best);
        fillPanel(R.string.won_title, summary, note, getString(R.string.new_game),
                v -> store.dispatch(new GameAction.NewGame(state.level, state.mode)),
                v -> store.dispatch(new GameAction.PanelDismissed()));
    }

    private void fillPanel(int title, String text, String note, String primary,
                           View.OnClickListener onPrimary, View.OnClickListener onSecondary) {
        panelTitle.setText(title);
        panelText.setText(text);
        panelNote.setText(note);
        panelNote.setVisibility(note == null ? View.GONE : View.VISIBLE);
        panelPrimary.setText(primary);
        panelPrimary.setOnClickListener(onPrimary);
        panelSecondary.setVisibility(onSecondary == null ? View.GONE : View.VISIBLE);
        panelSecondary.setText(R.string.view_board);
        panelSecondary.setOnClickListener(onSecondary);
    }

    /* ── Эффекты ──────────────────────────────────────────────────────────── */

    @Override
    public void handle(GameEffect effect) {
        if (effect instanceof GameEffect.Generate) {
            generate(((GameEffect.Generate) effect).level);
        } else if (effect instanceof GameEffect.Respond) {
            responder.respond(board, ((GameEffect.Respond) effect).feedback);
        } else if (effect instanceof GameEffect.Animate) {
            GameEffect.Animate animate = (GameEffect.Animate) effect;
            board.play(animate.animation, animate.cell);
        } else if (effect instanceof GameEffect.Say) {
            say((GameEffect.Say) effect);
        } else if (effect instanceof GameEffect.Celebrate) {
            confetti.launch();
        } else if (effect instanceof GameEffect.RecordWin) {
            recordWin((GameEffect.RecordWin) effect);
        } else if (effect instanceof GameEffect.RecordLoss) {
            Background.work(() -> statsStore.save(statsStore.load().afterLoss()));
        }
    }

    /**
     * Задача считается в фоне: перебор с возвратом да проверка на единственность решения
     * занимают доли секунды, но это как раз тот размер, на котором главный поток начинает
     * подрагивать.
     */
    private void generate(Difficulty level) {
        confetti.stop();
        int seed = random.nextInt();
        Background.work(() -> {
            Board made = Generator.board(level, new Rng(seed));
            Background.main(() -> store.dispatch(new GameAction.Generated(made)));
        });
    }

    private void say(GameEffect.Say say) {
        Toast.makeText(this, say.message == GameEffect.Message.NO_HINTS
                ? R.string.no_hints : R.string.board_full, Toast.LENGTH_SHORT).show();
    }

    private void recordWin(GameEffect.RecordWin win) {
        Background.work(() -> {
            Outcome outcome = statsStore.load()
                    .afterWin(win.level, win.seconds, win.hints, win.recordEligible, today);
            statsStore.save(outcome.stats);
            Background.main(() -> store.dispatch(
                    new GameAction.WinRecorded(outcome.best, outcome.record)));
        });
    }

    /* ── Меню и «назад» ───────────────────────────────────────────────────── */

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game, menu);
        this.menu = menu;
        // Значки в меню общие на всё приложение: без mutate() прозрачность недоступного
        // действия расползлась бы на другие экраны.
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            if (item.getIcon() != null) item.setIcon(item.getIcon().mutate());
        }
        renderTools(store.state());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_undo) {
            store.dispatch(new GameAction.UndoTapped());
        } else if (id == R.id.menu_pause) {
            store.dispatch(new GameAction.PauseToggled());
        } else if (id == R.id.menu_new) {
            openNewGame();
        } else if (id == R.id.menu_stats) {
            StatsDialog.show(this, statsStore, () -> render(store.state()),
                    this::onModalVisibilityChanged);
        } else if (id == R.id.menu_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        } else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    /**
     * Экран новой игры получает начатую партию, чтобы показать, что именно исчезнет.
     * Законченную и ещё не начатую не передаём: терять там нечего, и предупреждение соврало бы.
     */
    private void openNewGame() {
        GameState state = store.state();
        Intent intent = new Intent(this, NewGameActivity.class);
        boolean losing = state.accepts() || state.phase == GameState.Phase.PAUSED;
        if (losing && state.history.isStarted()) {
            intent.putExtra(NewGameActivity.EXTRA_CURRENT_LEVEL, state.level.name());
            intent.putExtra(NewGameActivity.EXTRA_CURRENT_MODE, state.mode.name());
            intent.putExtra(NewGameActivity.EXTRA_CURRENT_SECONDS, state.seconds);
        }
        startActivityForResult(intent, NEW_GAME);
    }

    @Override
    protected void onActivityResult(int request, int code, Intent data) {
        super.onActivityResult(request, code, data);
        if (request != NEW_GAME || code != RESULT_OK || data == null) return;
        store.dispatch(new GameAction.NewGame(
                Difficulty.byName(data.getStringExtra(NewGameActivity.EXTRA_LEVEL),
                        Difficulty.MEDIUM),
                GameMode.byName(data.getStringExtra(NewGameActivity.EXTRA_MODE),
                        GameMode.STANDARD)));
    }

    /**
     * «Назад» при открытой панели закрывает панель, а не приложение: человек нажимает её,
     * чтобы вернуться к доске.
     */
    private boolean handleBack() {
        GameState state = store.state();
        if (!state.panel) return false;
        store.dispatch(state.phase == GameState.Phase.PAUSED
                ? new GameAction.PauseToggled() : new GameAction.PanelDismissed());
        return true;
    }

    /**
     * Колбэк вешается и снимается по состоянию: «пустой» держать нельзя, иначе система
     * перестанет проигрывать предиктивный выход из приложения.
     */
    private void syncBackCallback(GameState state) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return;
        if (state.panel && backCallback == null) {
            android.window.OnBackInvokedCallback callback = this::handleBack;
            backCallback = callback;
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    android.window.OnBackInvokedDispatcher.PRIORITY_DEFAULT, callback);
        } else if (!state.panel && backCallback != null) {
            getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(
                    (android.window.OnBackInvokedCallback) backCallback);
            backCallback = null;
        }
    }

    /** До Android 13 система зовёт этот метод, а не колбэк выше. */
    @Override
    public void onBackPressed() {
        if (!handleBack()) super.onBackPressed();
    }

    /**
     * Внешняя клавиатура и эмулятор: цифры вписывают, стрелки водят выделение по доске.
     * Не украшение — на этом же проверяется игра при разработке.
     */
    @Override
    public boolean onKeyDown(int code, KeyEvent event) {
        if (code >= KeyEvent.KEYCODE_1 && code <= KeyEvent.KEYCODE_9) {
            store.dispatch(new GameAction.DigitTapped(code - KeyEvent.KEYCODE_1 + 1));
            return true;
        }
        if (code == KeyEvent.KEYCODE_DEL || code == KeyEvent.KEYCODE_FORWARD_DEL
                || code == KeyEvent.KEYCODE_0) {
            store.dispatch(new GameAction.EraseTapped());
            return true;
        }
        int step = arrowStep(code);
        if (step != 0) {
            move(step, code == KeyEvent.KEYCODE_DPAD_LEFT || code == KeyEvent.KEYCODE_DPAD_RIGHT);
            return true;
        }
        return super.onKeyDown(code, event);
    }

    private int arrowStep(int code) {
        switch (code) {
            case KeyEvent.KEYCODE_DPAD_UP: return -Cells.SIDE;
            case KeyEvent.KEYCODE_DPAD_DOWN: return Cells.SIDE;
            case KeyEvent.KEYCODE_DPAD_LEFT: return -1;
            case KeyEvent.KEYCODE_DPAD_RIGHT: return 1;
            default: return 0;
        }
    }

    /** По горизонтали выделение не перескакивает на соседнюю строку: край строки — это край. */
    private void move(int step, boolean horizontal) {
        GameState state = store.state();
        int from = state.selected < 0 ? 0 : state.selected;
        int to = from + step;
        if (to < 0 || to >= Cells.COUNT) return;
        if (horizontal && Cells.row(to) != Cells.row(from)) return;
        store.dispatch(new GameAction.CellTapped(to));
    }

    private int color(int id) {
        return getResources().getColor(id, getTheme());
    }
}
