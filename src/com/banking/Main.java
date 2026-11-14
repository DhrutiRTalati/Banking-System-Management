package com.banking;

/**
 * Master Application Entry Point.
 * Launches the Swing GUI by default, or the CLI Console if `--cli` is specified.
 */
public class Main {

    public static void main(String[] args) {
        boolean useCli = false;
        for (String arg : args) {
            if ("--cli".equalsIgnoreCase(arg) || "--console".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                useCli = true;
                break;
            }
        }

        if (useCli) {
            BankingCLI.main(args);
        } else {
            BankingGUI.main(args);
        }
    }
}
