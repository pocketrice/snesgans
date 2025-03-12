import java.util.Optional;

public interface Derivable {
    static <T> Optional<T> derive(int mode) {
        return Optional.empty();
    }
}
