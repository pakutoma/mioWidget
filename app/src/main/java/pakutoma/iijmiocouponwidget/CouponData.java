package pakutoma.iijmiocouponwidget;

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

    public CouponData(boolean sw) {
        this.sw = sw;
        this.traffic = 0;
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
