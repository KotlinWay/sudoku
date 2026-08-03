package info.javaway.sudoku.ui.game;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import info.javaway.sudoku.game.Board;
import info.javaway.sudoku.game.Cells;
import info.javaway.sudoku.game.Difficulty;
import info.javaway.sudoku.game.Move;
import info.javaway.sudoku.stats.Stats;

/**
 * Начатая партия на диске. Один файл, двоичный: числами тут всё, и складывать 81 клетку
 * в текст значило бы писать разбор строк там, где хватает {@code writeInt}.
 *
 * История ходов сохраняется вместе с доской. Иначе «отменить» после возврата в приложение
 * молчит, а человек в этот момент как раз и хочет отменить то, на чём вчера остановился.
 */
public final class GameSave {

    private static final String NAME = "game.bin";

    /**
     * Метка формата. Другая версия — не ошибка, а причина начать партию заново.
     *
     * Версия 2: из формата ушли стек отменённых ходов и состояние «после» у каждого хода —
     * и то и другое читал только «вернуть», а его в игре больше нет.
     * Версия 3: ушёл признак «это задача дня» — самого режима в игре больше нет.
     */
    private static final int VERSION = 3;

    private final File file;

    public GameSave(File directory) {
        this.file = new File(directory, NAME);
    }

    /**
     * @return сохранённая партия или null, если её нет или файл испорчен
     */
    public GameState load(boolean candidates) {
        if (!file.exists()) return null;
        try (DataInputStream in = new DataInputStream(
                new BufferedInputStream(new FileInputStream(file)))) {
            if (in.readInt() != VERSION) return null;

            GameState.Draft draft = GameState.draft();
            draft.level = Difficulty.byName(in.readUTF(), Difficulty.MEDIUM);

            int[] puzzle = readCells(in);
            int[] solution = readCells(in);
            int[] values = readCells(in);
            int[] notes = readCells(in);
            draft.board = Board.restored(puzzle, solution, values, notes);

            draft.selected = clamp(in.readInt());
            // Байт карандаша читается и выбрасывается: кнопки, которая его выключает, сейчас
            // нет, и партия, сохранённая с включённым карандашом, застряла бы в пометках
            // навсегда. Из формата байт не убран намеренно: механизм карандаша цел и ждёт
            // кнопку, так что вернуть его — одна строка здесь и две в разметке.
            in.readBoolean();
            draft.pencil = false;
            draft.mistakes = in.readInt();
            draft.hintsLeft = in.readInt();
            draft.hintsUsed = in.readInt();
            draft.seconds = in.readInt();

            boolean paused = in.readBoolean();
            draft.phase = paused ? GameState.Phase.PAUSED : GameState.Phase.PLAYING;
            draft.panel = paused;

            draft.history = History.of(readMoves(in));

            // Настройка кандидатов живёт в настройках, а не в партии: включив её один раз,
            // человек ждёт её и в старой игре тоже.
            draft.candidates = candidates;
            draft.best = Stats.NO_TIME;
            draft.record = false;
            return draft.build();
        } catch (IOException | RuntimeException e) {
            // Испорченный файл ничем не лучше отсутствующего: убираем и начинаем заново.
            clear();
            return null;
        }
    }

    /** Законченная партия не сохраняется: возвращаться в решённую доску незачем. */
    public void save(GameState state) {
        if (state.isOver() || state.phase == GameState.Phase.GENERATING) {
            clear();
            return;
        }
        try (DataOutputStream out = new DataOutputStream(
                new BufferedOutputStream(new FileOutputStream(file)))) {
            out.writeInt(VERSION);
            out.writeUTF(state.level.name());

            writeCells(out, state.board.puzzle());
            writeCells(out, state.board.solution());
            writeCells(out, state.board.values());
            writeCells(out, state.board.notesArray());

            out.writeInt(state.selected);
            out.writeBoolean(state.pencil);
            out.writeInt(state.mistakes);
            out.writeInt(state.hintsLeft);
            out.writeInt(state.hintsUsed);
            out.writeInt(state.seconds);
            out.writeBoolean(state.phase == GameState.Phase.PAUSED);

            writeMoves(out, state.history.done());
        } catch (IOException e) {
            // Не смогли сохранить — игра от этого не ломается, партия просто не переживёт выход.
            clear();
        }
    }

    public void clear() {
        // Результат удаления не проверяем: файла может уже не быть, и это не беда.
        file.delete();
    }

    private static int[] readCells(DataInputStream in) throws IOException {
        int[] cells = new int[Cells.COUNT];
        for (int cell = 0; cell < Cells.COUNT; cell++) {
            cells[cell] = in.readInt();
        }
        return cells;
    }

    private static void writeCells(DataOutputStream out, int[] cells) throws IOException {
        for (int value : cells) {
            out.writeInt(value);
        }
    }

    private static List<Move> readMoves(DataInputStream in) throws IOException {
        int size = in.readInt();
        if (size < 0) throw new IOException();
        List<Move> moves = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            moves.add(Move.read(in));
        }
        return moves;
    }

    private static void writeMoves(DataOutputStream out, List<Move> moves) throws IOException {
        out.writeInt(moves.size());
        for (Move move : moves) {
            move.write(out);
        }
    }

    /** Выбранная клетка из чужого файла не должна ронять доску: всё вне доски — «не выбрано». */
    private static int clamp(int cell) {
        return cell >= 0 && cell < Cells.COUNT ? cell : -1;
    }
}
