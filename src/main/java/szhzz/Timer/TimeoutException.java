package szhzz.Timer;

public class TimeoutException extends RuntimeException {

    /**
     * εΊεεε·
     */
    private static final long serialVersionUID = -8078853655388692688L;

    public TimeoutException(String errMessage) {
        super(errMessage);
    }
}
