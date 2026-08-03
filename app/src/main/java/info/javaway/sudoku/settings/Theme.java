package info.javaway.sudoku.settings;

/**
 * Оформление приложения. По умолчанию — как в системе: телефон уже знает, когда у человека
 * вечер, и спрашивать об этом второй раз незачем. Выбор нужен тем, у кого система светлая,
 * а играть хочется в темноте, и наоборот.
 */
public enum Theme {

    SYSTEM,
    LIGHT,
    DARK;

    /**
     * Разбор сохранённого имени. Неизвестное имя — не повод падать: настройка просто
     * возвращается к системной.
     */
    public static Theme byName(String name, Theme fallback) {
        if (name != null) {
            for (Theme theme : values()) {
                if (theme.name().equals(name)) return theme;
            }
        }
        return fallback;
    }
}
