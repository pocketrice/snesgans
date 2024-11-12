import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ROMReader {
    byte[] romRaw;
    String romUnassembled, romAssembled;
    ROMType romType;

    public ROMReader(File romFile) throws IOException {
        romRaw = Files.readAllBytes(romFile.toPath());
        romUnassembled = new String(romRaw, StandardCharsets.UTF_8);
    }

    public void parseType() {
        // TODO
        romType = ROMType.LO_ROM;
    }

    public void parseROM() {
        // TODO
        romAssembled = romUnassembled;
    }

    private static char byteToChar(byte b) {
        return (char) (b & 0xFF);
    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(byteToChar(b));
        }

        return sb.toString();
    }


    private enum ROMType {
        LO_ROM(0),
        HI_ROM(1),
        SUPER_MMC(2),
        SAS(3),
        SFX(4),
        EX_HI_ROM(5),
        EX_LO_ROM(6);

        ROMType(int mode) {
        }
    }
}