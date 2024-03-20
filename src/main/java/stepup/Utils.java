package stepup;
import java.lang.reflect.Proxy;

public class Utils {
    // метод принимает объект обобщенного типа T и возвращает объект того же типа
    public static <T>T cache(T obj) {
        CashingHandler cashingHandler = new CashingHandler(obj);

        Thread thread = new Thread(cashingHandler, "Поток чистки кэша");
        thread.setDaemon(true); // Делаем поток служебным с минимальным приоритетом
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();

        return (T) Proxy.newProxyInstance(
                obj.getClass().getClassLoader(),
                obj.getClass().getInterfaces(),
                cashingHandler);
    }
}