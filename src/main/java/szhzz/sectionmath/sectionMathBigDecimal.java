package szhzz.sectionmath;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2008-1-4
 * Time: 13:12:34
 * To change this template use File | Settings | File Templates.
 */
class sectionMathBigDecimal {
    static DecimalFormat floatFormatter = new DecimalFormat("#,##0");
    LinkedList<BigDecimal> section;

    int sectionLength = 1;
    BigDecimal lastValue;
    BigDecimal max, min;
    BigDecimal acumulate = BigDecimal.ZERO;
    BigDecimal droped = null;
    private BigDecimal sumValue = BigDecimal.ZERO;

    public sectionMathBigDecimal(int sectionLength) {
        this.sectionLength = sectionLength;
        section = new LinkedList<BigDecimal>();
    }


    public void reset() {
        section.clear();
        sumValue = BigDecimal.ZERO;
        lastValue = null;
        max = null;
        min = null;
        acumulate = BigDecimal.ZERO;
        droped = null;
    }


    public void add(BigDecimal val) {
        BigDecimal lastElement = null;

        if (sectionLength == section.size()) {
            lastElement = section.removeLast();
            droped = lastElement;
        }
        section.addFirst(val);
        if (lastElement == null) {
            sumValue = (sumValue.add(val));
        } else {
            sumValue = (sumValue.add(val).subtract(lastElement));
        }
        lastValue = val;
        acumulate = acumulate.add(val);
    }

    public BigDecimal getDroped() {
        BigDecimal val = null;
        if (droped != null)
            val = droped;
        return val;
    }

    public BigDecimal getAccumulate() {
        return acumulate;
    }

    public BigDecimal getSumValue() {
        return sumValue;
    }

    public BigDecimal getAvg() {
        if (section.size() > 0) {
            return sumValue.divide(BigDecimal.valueOf(section.size()), 16, BigDecimal.ROUND_HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    public double getLastVal() {
        return lastValue.doubleValue();  //To change body of implemented methods use File | Settings | File Templates.
    }


    public BigDecimal getMax() {
        if (section.size() == 0) return BigDecimal.ZERO;
        max = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            max = max.compareTo(section.get(i)) > 0 ? max : section.get(i);
        }
        return max;
    }

    public BigDecimal getMin() {
        if (section.size() == 0) return BigDecimal.ZERO;
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            min = min.compareTo(section.get(i)) < 0 ? min : section.get(i);
        }
        return min;
    }

    public BigDecimal getMaxAbs() {
        if (section.size() == 0) return BigDecimal.ZERO;
        max = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            max = max.abs().compareTo(section.get(i).abs()) > 0 ? max : section.get(i);
        }
        return max;
    }


    public BigDecimal getMinAbs() {
        if (section.size() == 0) return BigDecimal.ZERO;
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            min = min.abs().compareTo(section.get(i).abs()) < 0 ? min : section.get(i);
        }
        return min;
    }


    public BigDecimal getPreVal(int step) {
        return section.get((section.size() - 1) - step);
    }

    public BigDecimal sumSubSection(int last_n_Elements) {
        int len = Math.min(last_n_Elements, section.size());
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < len; i++) {
            sum = sum.add(section.get(i));
        }
        return sum;
    }

    public int countContinuedLargeThan(double last_n_Elements) {
        int count = 0;
        for (int i = 0; i < section.size(); i++) {
            if (section.get(i).doubleValue() <= last_n_Elements) break;
            count++;
        }
        return count;
    }


    public BigDecimal avedev() {
        BigDecimal avrage = getAvg();
        BigDecimal sumDiff = BigDecimal.ZERO;
        if (section.size() > 0) {
            for (int i = 1; i < section.size(); i++) {
                sumDiff = sumDiff.add(section.get(i).subtract(avrage));
            }
            BigDecimal sz = BigDecimal.valueOf(section.size());
            sumDiff = sumDiff.divide(sz, 16, BigDecimal.ROUND_HALF_UP);
            return sumDiff;
        }
        return sumDiff;
    }

    public double MSE() {
        BigDecimal avrage = getAvg();
        BigDecimal sumDiff = BigDecimal.ZERO;
        if (section.size() > 0) {
            for (int i = 1; i < section.size(); i++) {
                sumDiff = sumDiff.add(section.get(i).subtract(avrage).pow(2));
            }
            BigDecimal sz = BigDecimal.valueOf(section.size());
            sumDiff = sumDiff.divide(sz, 16, BigDecimal.ROUND_HALF_UP);
            return Math.pow(sumDiff.doubleValue(), 0.5d);
        }
        return 0d;
    }

    public int elementCount() {
        return section.size();
    }
}
