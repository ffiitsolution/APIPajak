package com.ffi.api.pajak.config;

/**
 *
 * @author USER
 */
import java.io.PrintStream;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartupListener implements ApplicationListener<ApplicationReadyEvent> {

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        printBanner(System.out);
    }

    // ANSI escape codes for colors
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_YELLOW = "\u001B[33m";

    private void printBanner(PrintStream out) {
        out.println("API Pajak Running..");        
        out.println("IT Solution Department PT. Fast Food Indonesia, Tbk.");
        out.println(ANSI_YELLOW + " :: API Pajak ::    (Backend Ver.24.10.002)");
        out.println(ANSI_RESET);
    }
}
