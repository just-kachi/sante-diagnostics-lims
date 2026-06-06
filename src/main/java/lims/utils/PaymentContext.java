package lims.utils;

public class PaymentContext {

    private static int currentRequestId;

    private PaymentContext() {
    }

    public static int getCurrentRequestId() {
        return currentRequestId;
    }

    public static void setCurrentRequestId(int requestId) {
        currentRequestId = requestId;
    }

    public static void clear() {
        currentRequestId = 0;
    }
}