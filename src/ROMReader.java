import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;

public class ROMReader {
    byte[] rom;
    int offset;
    boolean isCarriage; // Auto-move after read?
    Queue<Byte> loaded;
    ROMType romType;


    public ROMReader() {
        rom = new byte[4194304]; // <-- Just assuming a blank Lo/HiROM.
        loaded = new LinkedList<>();
        isCarriage = false;
        offset = 0x00;
    }

    public ROMReader(File romFile) throws IOException {
        this();
        rom = Files.readAllBytes(romFile.toPath());
    }

    // For visualization purposes; the actual SNES CPU has full θ(1) random access.
    public void loadBank(int address) {
        loaded.clear();
        int bankAddr = 0b110000 & address; // Bitmask bank (MSB x2)

        for (int i = bankAddr; i < bankAddr + 0x00FFFF; i++) {
            loaded.add(rom[i]);
        }
    }

    public void loadPage(int address) {
        loaded.clear();
        int pageAddr = 0b111100 & address;

        for (int i = pageAddr; i < pageAddr + 0x0000FF; i++) {
            loaded.add(rom[i]);
        }
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setCarriage(boolean isCarriage) {
        this.isCarriage = isCarriage;
    }

    // Fixes bytes (since they are signed in Java).
    public static byte[] affix(int... bytes) {
        byte[] result = new byte[bytes.length];

        for (int i = 0; i < bytes.length; i++) {
            result[i] = (byte) bytes[i];
        }

        return result;
    }

    // Technically can trivially do single-writes with direct array modification. Cannot write beyond ROM size.
    public void awrite(int addr, byte[] b) {
        assert addr + b.length < this.rom.length : "Violation of: cannot exceed ROM size";
        System.arraycopy(b, 0, rom, addr, b.length);
    }

    public void awrite(int addr, byte b) {
        rom[addr] = b;
    }

    // Random access read (absolute)
    public byte[] aread(int addr, int len) {
        byte[] result = Arrays.copyOfRange(rom, addr, addr + len);

        if (isCarriage) {
            offset += len;
        }

        return result;
    }

    public byte aread(int addr) {
        return aread(addr, 1)[0];
    }

    // Random access read (relative)
    public byte[] sread(int len) {
        return aread(offset, len);
    }

    public byte sread() {
        return sread(1)[0];
    }

    public void parseROM() {
        // Courtesy of https://snes.nesdev.org/wiki/ROM_header
        this.setOffset(0x00FFC0);
        this.setCarriage(true);

        String cartTitle = b2str(sread(21));
        ROMType mmMode = ROMType.derive(sread()).get(); // <-- derive guaranteed to work here
        byte chipset = sread();
        int romSize = 1 << sread(); // 2^⌈lg(n)⌉
        int ramSize = 1 << sread();
        byte country = sread();
        byte devID = sread();
        byte romVer = sread();
        int checksumComp = sread() << 4 + sread();
        int checksum = sread() << 4 + sread();
        byte[] interrupts = sread(32);

        boolean isVer3 = devID == 0x33;
        boolean isVer2 = !isVer3 && aread(0x00FFD4) == 0x00;
    }

    public int checksum() {
        byte[] romSum = aread(0x00FFDE, 2);
        byte[] romCompSum = aread(0x00FFDC, 2);
        assert ((romSum[0] << 4 + romSum[1]) ^ 0xFFFF) == romCompSum[0] << 4 + romSum[1] : "Violation of: bad checksum";

        // Passing assertion guarantees both add up to $FFFF!
        awrite(0xFFDC, affix(0x00, 0x00));
        awrite(0xFFDE, affix(0xFF, 0xFF));

        int rawSum = 0x00;
        for (byte b : rom) {
            rawSum += b;
        }

        // Restore patch
        awrite(0xFFDE, romSum);
        awrite(0xFFDC, romCompSum);

        return rawSum & 0x0000FFFF; // Keep LSB x4
    }

    // Converts other ROM type (e.g. .smc) to .sfc
    public void flattenROM() {
        // Use magic number to remove headers
        switch (rom[0]) {
            case 0x40 -> // .smc (512 byte header)
                    rom = Arrays.copyOfRange(rom, 512, rom.length);
            default -> System.err.println("use a .smc");
        }

    }
//
//    public String readStr(int len) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < len; i++) {
//            sb.append(loaded.remove());
//        }
//        return sb.toString();
//    }
//
//    public byte[] readBytes(int len) {
//        byte[] bytes = new byte[len];
//        for (int i = 0; i < len; i++) {
//            bytes[i] = loaded.remove();
//        }
//
//        return bytes;
//    }


    private static char b2ch(byte b) {
        return (char) (b & 0xFF);
    }

    private static String b2str(byte[] bytes) {
        StringBuilder sb = new StringBuilder();

        for (byte b : bytes) {
            sb.append(b2ch(b));
        }

        return sb.toString();
    }


    public enum ROMType implements Derivable {
        LO_ROM(0x20),
        HI_ROM(0x21),
        SUPER_MMC(0x23),
        SAS(0x30),
        SFX(0x31),
        EX_HI_ROM(0x32),
        EX_LO_ROM(0x35);

        private final int mode;

        static Optional<ROMType> derive(int mode) {
            return Arrays.stream(ROMType.values()).filter(r -> r.mode == mode).findFirst();
        }
        ROMType(int mode) {
            this.mode = mode;
        }
    }


}