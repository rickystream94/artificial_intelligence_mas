package utils;

public class Memory {
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final double MB = 1024 * 1024;

    private static double used() {
        return (RUNTIME.totalMemory() - RUNTIME.freeMemory()) / MB;
    }

    private static double free() {
        return (RUNTIME.maxMemory() - RUNTIME.totalMemory() + RUNTIME.freeMemory()) / MB;
    }

    private static double max() {
        return RUNTIME.maxMemory() / MB;
    }

    public static String stringRep() {
        return String.format("Memory Usage: [Used: %.2f MB, Free: %.2f MB, MaxAlloc: %.2f MB]", used(), free(), max());
    }
}
