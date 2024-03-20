package stepup;

import org.junit.jupiter.api.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class AppTest 
{
    Fraction fraction;
    Fractionable num;
    ByteArrayOutputStream arrayOutStream;
    PrintStream defaultPrintStream = System.out;
    PrintStream printStream;

    @BeforeEach
    void beforeEach() {
        fraction = new Fraction((int)(Math.random() * 100), (int)(Math.random() * 100));
        System.out.println("Создана новая дробь: "+ fraction);
        num = Utils.cache(fraction);
        System.out.println("Создан proxy интерфейс: ");
        // Переопределяем вывод
        arrayOutStream = new ByteArrayOutputStream();
        printStream = new PrintStream(arrayOutStream);
    }

    @AfterEach
    void afterEach(){
        num = null;
    }

    // Количество вхождений подстроки в строку
    public static int count(String str, String target) {
        return (str.length() - str.replace(target, "").length()) / target.length();
    }

    @Test
    @DisplayName("Проверяем однократный вызов метода с аннотацией Cache")
    public void testAnnotationMethodCached () throws NoSuchFieldException {
        System.setOut(printStream);
        num.doubleValue();
        System.setOut(defaultPrintStream);
        String result = arrayOutStream.toString();
        System.out.println("Результат вызова proxy: '"+ result + "'");
        Assertions.assertEquals(count(result, "invoke double value"), 1);
    }

    @Test
    @DisplayName("Проверяем что вызов метода с аннотацией Cache повторно не производиться")
    public void testAnnotationMethodOneCached () {
        System.setOut(printStream);
        num.doubleValue();
        num.doubleValue();
        System.setOut(defaultPrintStream);
        String result = arrayOutStream.toString();
        System.out.println("Результат вызова proxy: '"+ result + "'");
        Assertions.assertEquals(count(result, "invoke double value"), 1);
    }

    @Test
    @DisplayName("Проверяем что вызов метода с аннотацией Cache воспроизводиться после очистки кэша")
    public void testAnnotationMethodOneClearCached (){
        System.setOut(printStream);
        num.doubleValue();
        try {
            Thread.sleep(1100); // Выставляем задержку
        }
            catch (InterruptedException e) {
                Assertions.fail();
        }
        num.doubleValue();
        System.setOut(defaultPrintStream);
        String result = arrayOutStream.toString();
        System.out.println("Результат вызова proxy: '"+ result + "'");
        Assertions.assertEquals(count(result, "invoke double value"), 2);
        Assertions.assertTrue(count(result, "Cash Cleared") > 0);
    }

    @Test
    @DisplayName("Проверяем что объект очищается после выполнения методов Mutator")
    public void testAnnotationMethodCachedMutatorClear ()   {
        num.doubleValue();
        num.doubleValue();
        System.setOut(printStream);
        num.setNum((int)(Math.random() * 100));
        System.setOut(defaultPrintStream);
        String result = arrayOutStream.toString();
        System.out.println("Результат вызова proxy: '"+ result + "'");
        Assertions.assertTrue(count(result, "Method:'doubleValue'") == 0);
    }

    @Test
    @DisplayName("Проверяем что кэш метода со временем жизни 0 не очищается по таймауту")
    public void testAnnotationMethodCachedTimeNoClear ()   {
        System.out.println(num.toString());
        try {
            Thread.sleep(1100); // Выставляем задержку
        }
        catch (InterruptedException e) {
            Assertions.fail();
        }
        System.setOut(printStream);
        num.doubleValue();
        System.setOut(defaultPrintStream);
        String result = arrayOutStream.toString();
        System.out.println("Результат вызова proxy: '"+ result + "'");
        Assertions.assertEquals(count(result, "Method:'toString'"), 1);
    }

    @Test
    @DisplayName("Проверяем что кэш метода со временем жизни 0 очищается по Mutatator")
    public void testAnnotationMethodCachedMutatatorClear ()   {
        System.out.println(num.toString());
        try {
            Thread.sleep(1100); // Выставляем задержку
        }
        catch (InterruptedException e) {
            Assertions.fail();
        }
        System.setOut(printStream);
        num.setNum((int)(Math.random() * 100));
        System.setOut(defaultPrintStream);
        String result = arrayOutStream.toString();
        System.out.println("Результат вызова proxy: '"+ result + "'");
        Assertions.assertEquals(count(result, "Method:'toString'"), 0);
    }

    @Test
    @DisplayName("Проверяем что объект повторно кэшируется после Mutator")
    public void testAnnotationMethodCachedNoClear ()   {
        System.setOut(printStream);
        num.doubleValue();
        num.doubleValue();
        num.setNum((int)(Math.random() * 100));
        num.setDenum((int)(Math.random() * 100));
        num.doubleValue();
        System.setOut(defaultPrintStream);
        String result = arrayOutStream.toString();
        System.out.println("Результат вызова proxy: '"+ result + "'");
        Assertions.assertEquals(count(result, "invoke setNum"), 1);
        Assertions.assertEquals(count(result, "invoke setDenum"), 1);
        Assertions.assertEquals(count(result, "invoke double value"), 2);
    }

}
