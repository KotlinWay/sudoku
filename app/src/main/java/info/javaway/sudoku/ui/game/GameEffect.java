package info.javaway.sudoku.ui.game;

import info.javaway.sudoku.game.Difficulty;

/** Одноразовые команды наружу: то, что делает Activity, а не редьюсер. */
public abstract class GameEffect {

    /**
     * Отклик на действие: звук и отдача в палец. Одним эффектом, а не двумя, потому что это
     * одно событие — «ход принят», «так нельзя», «победа», — а чем именно его показать,
     * решает экран.
     */
    public enum Feedback { SELECT, PLACE, NOTE, ERASE, WRONG, HINT, WIN }

    /** Что показать на клетке. Анимация одноразовая, состоянием доски она не является. */
    public enum Animation { POP, SHAKE, REVEAL }

    /** Короткое сообщение поверх экрана. Текст выбирает Activity — в редьюсере строк нет. */
    public enum Message { NO_HINTS, BOARD_FULL }

    public static final class Generate extends GameEffect {
        public final Difficulty level;
        public final boolean daily;

        public Generate(Difficulty level, boolean daily) {
            this.level = level;
            this.daily = daily;
        }
    }

    public static final class Respond extends GameEffect {
        public final Feedback feedback;

        public Respond(Feedback feedback) {
            this.feedback = feedback;
        }
    }

    public static final class Animate extends GameEffect {
        public final Animation animation;
        public final int cell;

        public Animate(Animation animation, int cell) {
            this.animation = animation;
            this.cell = cell;
        }
    }

    public static final class Say extends GameEffect {
        public final Message message;

        public Say(Message message) {
            this.message = message;
        }
    }

    public static final class Celebrate extends GameEffect {
    }

    public static final class RecordWin extends GameEffect {
        public final Difficulty level;
        public final boolean daily;
        public final int seconds;
        public final int hints;

        public RecordWin(Difficulty level, boolean daily, int seconds, int hints) {
            this.level = level;
            this.daily = daily;
            this.seconds = seconds;
            this.hints = hints;
        }
    }

    public static final class RecordLoss extends GameEffect {
    }
}
