package info.javaway.sudoku.game;

/**
 * Уровень задаёт две вещи: сколько цифр открыто на старте и сколько ошибок прощается.
 * Чем меньше подсказок, тем дороже ошибка — иначе сложность превращается в перебор наугад.
 *
 * Открытых цифр больше, чем в прототипе на HTML (36/30/24/18). Прототип вынимал клетки как
 * попало и получал задачи с несколькими решениями; здесь {@link Generator} вынимает клетку
 * только пока решение остаётся единственным, а с этим условием до 18 подсказок дойти нельзя:
 * задач с единственным решением и таким числом открытых цифр почти не существует.
 */
public enum Difficulty {

    EASY(42, 3),
    MEDIUM(34, 3),
    HARD(28, 2),
    EXPERT(24, 1);

    /** Сколько цифр остаётся открытыми. Цель, а не обещание: ниже генератор может не спуститься. */
    public final int clues;

    public final int mistakeLimit;

    Difficulty(int clues, int mistakeLimit) {
        this.clues = clues;
        this.mistakeLimit = mistakeLimit;
    }

    /**
     * Разбор сохранённого имени. Неизвестное имя — не повод падать: старое сохранение
     * должно просто дать обычную игру.
     */
    public static Difficulty byName(String name, Difficulty fallback) {
        if (name != null) {
            for (Difficulty level : values()) {
                if (level.name().equals(name)) return level;
            }
        }
        return fallback;
    }
}
