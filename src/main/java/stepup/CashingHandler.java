package stepup;

import stepup.annotations.Cache;
import stepup.annotations.Mutator;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

public class CashingHandler<T> implements InvocationHandler, Runnable {
    private T obj;
    //  ConcurrentHashMap обеспечит корректный доступ нескольких потоков к Мапе без блокировок
    private ConcurrentHashMap<String, ConcurrentHashMap<Method, CashValue>> cashedObj = new ConcurrentHashMap<>();
    public CashingHandler(T obj)
    {
        this.obj = obj;
    }

    // Гененрируем ключ уникального состояния объекта
    private String getFildsKey(Field[] fields) throws IllegalAccessException {
        StringBuilder codeField = new StringBuilder();
        for (Field field : fields){
            field.setAccessible(true);
            codeField.append(field.getName()).append("=").append(field.get(this.obj).toString()).append(";");
        }
        return codeField.toString();
    }

    private void cashToString(){
        System.out.println("Cash:");
        cashedObj.forEach((hash, mapCashedValue)->{
            mapCashedValue.forEach((method, cashedValue)->{
                System.out.println("Key:'" + hash +
                        "' Method:'" + method.getName() +
                        "' Value:'" + cashedValue.getValue() +
                        "' Valid:'" + cashedValue.isValid() + "'");
            });
        });
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //System.out.println("It works");
        Class<?> objClass = obj.getClass();
        Method objMethod = objClass.getMethod(method.getName(), method.getParameterTypes());
        // Обработка аннотации Mutator
        if (objMethod.isAnnotationPresent(Mutator.class)) {
            System.out.println("Mutator");
            String objKey = getFildsKey(objClass.getDeclaredFields());
            System.out.println("objKey='"+objKey+"'");
            // Чистим все объекты текущего состояния так как состояние изменилось
            cashedObj.remove(objKey);
            cashToString();
        }
        // Обработка аннотации Cache
        if (objMethod.isAnnotationPresent(Cache.class)) {
            System.out.println("Casher");
            int lifeTime = objMethod.getAnnotation(Cache.class).lifeTime();
            ConcurrentHashMap<Method, CashValue> fieldsMap;
            //Получаем уникальный ключ состояния полей объекта
            String objKey = getFildsKey(objClass.getDeclaredFields());
            // Состояние есть в Кэше
            if (cashedObj.containsKey(objKey)) {
               fieldsMap = cashedObj.get(objKey);
               //Получаем MAP метода по значениям полей объекта
               if (!fieldsMap.isEmpty()) { // Мапа не пустая
                   if (!fieldsMap.containsKey(objMethod)) {// Метода в мапе нет добавляем
                       CashValue cashValue = new CashValue(method.invoke(obj, args), lifeTime);
                       fieldsMap.put(objMethod, cashValue);
                   }
               } else { // Мапа пустая - создает новую и объект помещаем в мапу
                   fieldsMap = new ConcurrentHashMap<>();
                   CashValue cashValue = new CashValue(method.invoke(obj, args), lifeTime);
                   fieldsMap.put(objMethod, cashValue);
               }
               CashValue cashValue = fieldsMap.get(objMethod);
               // Кэш валидный возвращаем значение и продлеваем интервал (считаем что кэш востребован)
               if (cashValue.isValid()) {
                   cashValue.setLifeTime(lifeTime);
                   cashToString();
                   return cashValue.getValue();
               };
               // Если не валидный вычисляем повторно и заносим в Кэш
               cashValue.setValue(method.invoke(obj, args), lifeTime);
               cashToString();
               return fieldsMap.get(objMethod).getValue();
            }
            // Мапы нет, создаем заносим объект в Кэш
            fieldsMap = new ConcurrentHashMap<>();
            CashValue cashValue = new CashValue(method.invoke(obj, args), lifeTime);
            fieldsMap.put(objMethod, cashValue);
            cashedObj.put(objKey, fieldsMap);
            cashToString();
            return fieldsMap.get(objMethod).getValue();
        }
        return method.invoke(obj, args);
    }

    // Логика очистки сводится к созданию отдельного фонового (Daemon) потока с минимальным приоритетом который
    // каждые 1000 мс пробегает по кэшу и вычищает в нем неактуальные значения
    // Копирования кэша при очистке не используется так как ConcurrentHashMap позволяет нескольким потокам безопасно работать с единым объектом
    public void run(){
        for (;;){
            if (!Thread.interrupted()) {
                cashedObj.forEach((hash, mapCashedValue) -> {
                    mapCashedValue.forEach((method, cashedValue) -> {
                        if (!cashedValue.isValid()) mapCashedValue.remove(method);
                    });
                });
                System.out.println("Cash Cleared");
                cashToString();
            } else {
                return;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
}
