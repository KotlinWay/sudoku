package info.javaway.sudoku.core;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Один фоновый поток на всё приложение: составление задачи и запись партии на диск.
 * Второй не нужен — обе работы идут над одной партией, а очередь из одного потока сама
 * гарантирует, что сохранение не обгонит генерацию.
 */
public final class Background {

    private static final ExecutorService WORKER = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    public static void work(Runnable task) {
        WORKER.execute(task);
    }

    public static void main(Runnable task) {
        MAIN.post(task);
    }

    private Background() {
    }
}
