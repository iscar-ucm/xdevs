package mitris.sim.core.lib.examples.performance;

import java.util.ArrayList;
import mitris.sim.core.modeling.InPort;

/**
 * Coupled model to study the performance HO DEVStone models
 *
 * @author José Luis Risco Martín
 */
public class DevStoneCoupledHOmod extends DevStoneCoupled {

    public InPort<Integer> iInAux = new InPort<>("inAux");

    public DevStoneCoupledHOmod(String prefix, int width, int depth, DevStoneProperties properties) {
        super(prefix + (depth - 1));
        super.addInPort(iInAux);
        if (depth == 1) {
            DevStoneAtomic atomic = new DevStoneAtomic("A1_" + name, properties);
            super.addComponent(atomic);
            super.addCoupling(iIn, atomic.iIn);
            super.addCoupling(atomic.oOut, oOut);
        } else {
            DevStoneCoupledHOmod coupled = new DevStoneCoupledHOmod(prefix, width, depth - 1, properties);
            super.addComponent(coupled);
            super.addCoupling(iIn, coupled.iIn);
            super.addCoupling(coupled.oOut, oOut);
            // First layer of atomic models:
            ArrayList<DevStoneAtomic> prevLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL1_" + (i + 1) + "_" + name, properties);
                super.addComponent(atomic);
                super.addCoupling(iInAux, atomic.iIn);
                super.addCoupling(atomic.oOut, coupled.iInAux);
                prevLayer.add(atomic);
            }
            // Second layer of atomic models:
            ArrayList<DevStoneAtomic> currentLayer = new ArrayList<>();
            for (int i = 0; i < (width - 1); ++i) {
                DevStoneAtomic atomic = new DevStoneAtomic("AL2_" + (i + 1) + "_" + name, properties);
                super.addComponent(atomic);
                if (i == 0) {
                    super.addCoupling(iInAux, atomic.iIn);
                }
                currentLayer.add(atomic);
            }
            for (int i = 0; i < currentLayer.size(); ++i) {
                DevStoneAtomic fromAtomic = currentLayer.get(i);
                for (int j = 0; j < prevLayer.size(); ++j) {
                    DevStoneAtomic toAtomic = prevLayer.get(j);
                    super.addCoupling(fromAtomic.oOut, toAtomic.iIn);
                }
            }

            // Rest of the tree
            prevLayer = currentLayer;
            currentLayer = new ArrayList<>();
            int level = 3;
            while (prevLayer.size() > 1) {
                for (int i = 0; i < prevLayer.size() - 1; ++i) {
                    DevStoneAtomic atomic = new DevStoneAtomic("AL" + level + "_" + (i + 1) + "_" + name, properties);
                    super.addComponent(atomic);
                    if (i == 0) {
                        super.addCoupling(iInAux, atomic.iIn);
                    }
                    currentLayer.add(atomic);
                }
                for (int i = 0; i < currentLayer.size(); ++i) {
                    DevStoneAtomic fromAtomic = currentLayer.get(i);
                    DevStoneAtomic toAtomic = prevLayer.get(i + 1);
                    super.addCoupling(fromAtomic.oOut, toAtomic.iIn);
                }
                prevLayer = currentLayer;
                currentLayer = new ArrayList<>();
                level++;
            }
        }
    }

    @Override
    public int getNumDeltExts(int maxEvents, int width, int depth) {
        int Gamma1 = width - 1;
        int Gamma2 = width * (width - 1) / 2;
        int Gamma3 = Gamma1;
        int Delta1 = depth - 1;
        int Delta3 = (depth - 1) * (depth - 2) / 2;
        return maxEvents * (1 + Delta1 * Gamma1 * Gamma1 + (Delta1 + Gamma3 * Delta3) * (Gamma1 + Gamma2));
    }

    @Override
    public int getNumDeltInts(int maxEvents, int width, int depth) {
        return getNumDeltExts(maxEvents, width, depth);
    }

    @Override
    public long getNumOfEvents(int maxEvents, int width, int depth) {
        long numEvents = 1;
        int gamma1 = getGammaI(1, width);
        int k = 1; // k for level 1
        long[] bag = new long[1 + 1 + depth * gamma1]; // Maximum number of propagations (asumming first index is 1, index 0 is not used)
        bag[1] = 1; // bag for level 1
        for (int d = 1; d < depth; ++d) {
            // Update numEvents
            for (int c = 1; c <= (k + gamma1); ++c) {
                if ((c <= k) && (c < gamma1)) {
                    long popSum1 = 0;
                    long popSum2 = 0;
                    for (int i = 1; i <= c; ++i) {
                        popSum1 += getGammaI(i, width) * bag[c - i + 1];
                        popSum2 += getGammaI(1, width) * bag[c - i + 1];
                    }
                    numEvents += (popSum1 + popSum2);
                } else if ((c <= k) && (c >= gamma1)) {
                    long popSum1 = 0;
                    long popSum2 = 0;
                    for (int i = 1; i <= gamma1; ++i) {
                        popSum1 += getGammaI(i, width) * bag[c - i + 1];
                        popSum2 += getGammaI(1, width) * bag[c - i + 1];
                    }
                    numEvents += (popSum1 + popSum2);
                } else if (c > k) {
                    long popSum1 = getGammaI(c - k + 1, width) * bag[k];
                    long popSum2 = 0;
                    for (int i = c - k; i <= gamma1; ++i) {
                        if (c - i > 0) {
                            popSum2 += getGammaI(1, width) * bag[c - i];
                        }
                    }
                    numEvents += (popSum1 + popSum2);
                }
            }
            // Update bag for the next level
            long[] nextBag = new long[1 + 1 + depth * gamma1]; // Maximum number of propagations (asumming first index is 1, index 0 is not used)
            for (int c = 1; c <= (k + gamma1); ++c) {
                if ((c <= k) && (c < gamma1)) {
                    long popSum = 0;
                    for (int i = 1; i <= c; ++i) {
                        popSum += bag[c - i + 1];
                    }
                    nextBag[c] = gamma1 * popSum;
                } else if ((c <= k) && (c >= gamma1)) {
                    long popSum = 0;
                    for (int i = 1; i <= gamma1; ++i) {
                        popSum += bag[c - i + 1];
                    }
                    nextBag[c] = gamma1 * popSum;
                } else if (c > k) {
                    long popSum = 0;
                    for (int i = c - k; i <= gamma1; ++i) {
                        if (c - i > 0) {
                            popSum += bag[c - i];
                        }
                    }
                    nextBag[c] = gamma1 * popSum;
                }
            }
            bag = nextBag;
            // Compute k for the next level
            k += gamma1;
        }
        return numEvents;
    }

    private int getGammaI(int i, int width) {
        int res = (width - i);
        if (res < 0) {
            return 0;
        }
        return res;
    }
}
