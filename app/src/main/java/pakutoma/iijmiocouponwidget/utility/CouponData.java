package pakutoma.iijmiocouponwidget.utility;

/**
 * Created by PAKUTOMA on 2016/12/04.
 */
public class CouponData {
    private int traffic;
    private boolean sw;
    public CouponData(int traffic,boolean sw) {
        this.traffic = traffic;
        this.sw = sw;
    }

    public int getTraffic() {
        return this.traffic;
    }

    public boolean getSwitch() {
        return this.sw;
    }

    public void setSwitch(boolean sw) {
        this.sw = sw;
    }
}
