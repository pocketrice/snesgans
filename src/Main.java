import java.io.File;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ROMReader romReader = new ROMReader(new File("data/smw.smc"));
    }
}
