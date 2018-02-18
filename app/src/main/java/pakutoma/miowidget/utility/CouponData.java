package pakutoma.miowidget.utility;

/**
 * Created by PAKUTOMA on 2016/12/04.
 */
public class CouponData {
    private final int traffic;
    private final boolean sw;
    CouponData(int traffic,boolean sw) {
        this.traffic = traffic;
        this.sw = sw;
    }

    public int getTraffic() {
        return this.traffic;
    }

    public boolean getSwitch() {
        return this.sw;
    }
}
