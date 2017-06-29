/**
 * Created by java_dev on 31.05.17.
 */
public class BasicGenerator<T> implements Generator{
    private Class<T> type;

    public BasicGenerator(Class<T> type) {
        this.type = type;
    }

    public T next() {
        try {
            // Assumes type is a public class:
            return type.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Produce a Default generator given a type token:
    public static <T> Generator<T> create(Class<T> type) {
        return new BasicGenerator<T>(type);
    }
}
