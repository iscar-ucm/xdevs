package xdevs.core.devstone;

public class DummyAtomicStats extends DummyAtomic {

    private int intCount , extCount;

    public DummyAtomicStats(String name, double intDelay, double extDelay) {
        this(name, intDelay, extDelay, true);
    }

    public DummyAtomicStats(String name, double intDelay, double extDelay, boolean addOutPort) {
        this(name, intDelay, extDelay, addOutPort, 0);
    }

    public DummyAtomicStats(String name, double intDelay, double extDelay, boolean addOutPort, double prepTime) {
        super(name, intDelay, extDelay, addOutPort, prepTime);
        this.intCount = 0;
        this.extCount = 0;
    }

    @Override
    public void deltint() {
        super.deltint();
        this.intCount += 1;
    }

    @Override
    public void deltext(double e) {
        super.deltext(e);
        this.extCount += 1;
    }

    public int getIntCount() {
        return intCount;
    }

    public int getExtCount() {
        return extCount;
    }
}
