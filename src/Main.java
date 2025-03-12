import jdk.jshell.spi.ExecutionControl;

import java.io.*;

public class Main {
    public static void main(String[] args) throws IOException, ExecutionControl.NotImplementedException {
        //ROMReader romReader = new ROMReader(new File("data/smw.smc"));
//
//        BufferedReader romReader = new BufferedReader(new InputStreamReader(new FileInputStream("data/smw.smc")));
//        for (int i = 0; i <= 0x15 + 0x1FF; i++) {
//            System.out.println(romReader.read());
//        }
//
//        ROMReader.ROMType romtype = switch (romReader.read()) {
//            case 0x20 -> ROMReader.ROMType.LO_ROM;
//            case 0x21 -> ROMReader.ROMType.HI_ROM;
//            default -> throw new ExecutionControl.NotImplementedException("Bad");
//        };

        ROMReader rr = new ROMReader(new File("data/eb.sfc"));
        rr.flattenROM();
        rr.loadPage(0x00FF00);
        rr.checksum();
        rr.parseROM();
    }
}
