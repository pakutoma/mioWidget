package pakutoma.iijmiocouponwidget.utility;

/**
 * Created by PAKUTOMA on 2016/12/10.
 */
public class AccountData {
    private String hdoServiceCode;
    private String number;
    private boolean regulation;
    private boolean couponUse;

    public AccountData(String hdoServiceCode,String number,boolean regulation,boolean couponUse) {
        this.hdoServiceCode = hdoServiceCode;
        this.number = number;
        this.regulation = regulation;
        this.couponUse = couponUse;
    }

    public String getHdoServiceCode() {
        return this.hdoServiceCode;
    }

    public String getNumber() {
        return this.number;
    }

    public boolean getRegulation() {
        return this.regulation;
    }

    public boolean getCouponUse() {
        return this.couponUse;
    }
}
