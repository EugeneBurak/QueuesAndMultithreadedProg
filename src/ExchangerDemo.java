//: concurrency/ExchangerDemo.java
import java.util.concurrent.*;
import java.util.*;

class ExchangerProducer<T> implements Runnable {
    private Generator<T> generator;
    private Exchanger<List<T>> exchanger;
    private List<T> holder;
    ExchangerProducer(Exchanger<List<T>> exchg,
                      Generator<T> gen, List<T> holder) {
        exchanger = exchg;
        generator = gen;
        this.holder = holder;
    }
    public void run() {
        try {
            while(!Thread.interrupted()) {
                for(int i = 0; i < ExchangerDemo.size; i++)
                    holder.add(generator.next());
                // Заполненный контейнер заменяется пустым::
                holder = exchanger.exchange(holder);
            }
        } catch(InterruptedException e) {
            // Приемлемый способ завершения.
        }
    }
}

class ExchangerConsumer<T> implements Runnable {
    private Exchanger<List<T>> exchanger;
    private List<T> holder;
    private volatile T value;
    ExchangerConsumer(Exchanger<List<T>> ex, List<T> holder){
        exchanger = ex;
        this.holder = holder;
    }
    public void run() {
        try {
            while(!Thread.interrupted()) {
                holder = exchanger.exchange(holder);
                for(T x : holder) {
                    value = x; // Выборка значения
                    holder.remove(x); // Нормально для CopyOnWriteArrayList
                }
            }
        } catch(InterruptedException e) {
            // Приемлемый способ завершения.
        }
        System.out.println("Final value: " + value);
    }
}

public class ExchangerDemo {
    static int size = 10;
    static int delay = 5; // Секунды
    public static void main(String[] args) throws Exception {
        if(args.length > 0)
            size = new Integer(args[0]);
        if(args.length > 1)
            delay = new Integer(args[1]);
        ExecutorService exec = Executors.newCachedThreadPool();
        Exchanger<List<Fat>> xc = new Exchanger<List<Fat>>();
        List<Fat>
                producerList = new CopyOnWriteArrayList<Fat>(),
                consumerList = new CopyOnWriteArrayList<Fat>();
        exec.execute(new ExchangerProducer<Fat>(xc,
                BasicGenerator.create(Fat.class), producerList));
        exec.execute(
                new ExchangerConsumer<Fat>(xc,consumerList));
        TimeUnit.SECONDS.sleep(delay);
        exec.shutdownNow();
    }
}