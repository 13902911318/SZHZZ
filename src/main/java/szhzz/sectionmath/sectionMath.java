package szhzz.sectionmath;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-15
 * Time: 20:27:29
 * To change this template use File | Settings | File Templates.
 */
public class sectionMath {
    static DecimalFormat floatFormatter = new DecimalFormat("#,##0");
    SectionMathFloat sectionFloat = null;
    SectionMathLong sectionLong = null;
    sectionMathBigDecimal sectionBigDecimal = null;
    private int sectionLength = 1;
    private boolean useFloat = false;
    private boolean useLong = false;
    private boolean useBigDecimal = false;

    sectionMath() {
    }

    public sectionMath(int sectionLength) {
        this.sectionLength = sectionLength;
        sectionFloat = new SectionMathFloat(this.sectionLength);
        useFloat = true;
    }

    public void setUseLong() {
        useLong = true;
        useFloat = false;
        useBigDecimal = false;
        sectionLong = new SectionMathLong(this.sectionLength);
        sectionFloat = null;
    }

    public SectionMathFloat getMathFloat() {
        return sectionFloat;
    }

    public void setUseBigDecima() {
        useBigDecimal = true;
        useFloat = false;
        useLong = false;

        sectionBigDecimal = new sectionMathBigDecimal(this.sectionLength);
        sectionFloat = null;
    }


    public void reset() {
        if (sectionFloat != null) sectionFloat.reset();
        if (sectionLong != null) sectionLong.reset();
        if (sectionBigDecimal != null) sectionBigDecimal.reset();
    }

    public void add(float val) {
        if (useBigDecimal) {
            sectionBigDecimal.add(BigDecimal.valueOf(val));
        } else if (useLong) {
            sectionLong.add(new Float(val).longValue());
        } else {
            sectionFloat.add(val);
        }
    }

    public Float getDroped() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getDroped().floatValue();
        } else if (useLong) {
            val = sectionLong.getDroped();
        } else {
            val = sectionFloat.getDroped();
        }
        return val;
    }

    public void add(long val) {
        if (useBigDecimal) {
            sectionBigDecimal.add(BigDecimal.valueOf(val));
        } else if (useLong) {
            sectionLong.add(val);
        } else {
            sectionFloat.add(val);
        }
    }

    public float getAccumulate() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getAccumulate().floatValue();
        } else if (useLong) {
            val = sectionLong.getAccumulate();
        } else {
            val = sectionFloat.getAccumulate();
        }
        return val;
    }

    public float getSumValue() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getSumValue().floatValue();
        } else if (useLong) {
            val = sectionLong.getSumValue();
        } else {
            val = sectionFloat.getSumValue();
        }
        return val;
    }

    public float getAvg() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getAvg().floatValue();
        } else if (useLong) {
            val = sectionLong.getAvg();
        } else {
            val = sectionFloat.getAvg();
        }
        return val;
    }

    public double getLastVal() {
        double val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getLastVal();
        } else if (useLong) {
            val = sectionLong.getLastVal();
        } else {
            val = sectionFloat.getLastVal();
        }
        return val;
    }


    public float getMax() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getMax().floatValue();
        } else if (useLong) {
            val = sectionLong.getMax();
        } else {
            val = sectionFloat.getMax();
        }
        return val;
    }

    public float getMin() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getMin().floatValue();
        } else if (useLong) {
            val = sectionLong.getMin();
        } else {
            val = sectionFloat.getMin();
        }
        return val;
    }

    public float getMaxAbs() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getMaxAbs().floatValue();
        } else if (useLong) {
            val = sectionLong.getMaxAbs();
        } else {
            val = sectionFloat.getMaxAbs();
        }
        return val;
    }


    public float getMinAbs() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getMaxAbs().floatValue();
        } else if (useLong) {
            val = sectionLong.getMaxAbs();
        } else {
            val = sectionFloat.getMaxAbs();
        }
        return val;
    }


    public float getPreVal(int step) {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.getPreVal(step).floatValue();
        } else if (useLong) {
            val = sectionLong.getPreVal(step);
        } else {
            val = sectionFloat.getPreVal(step);
        }
        return val;
    }

    public float sumSubSection(int last_n_Elements) {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.sumSubSection(last_n_Elements).floatValue();
        } else if (useLong) {
            val = sectionLong.sumSubSection(last_n_Elements);
        } else {
            val = sectionFloat.sumSubSection(last_n_Elements);
        }
        return val;
    }

    public int countContinuedLargeThan(float last_n_Elements) {
        int count = 0;
        if (useBigDecimal) {
            count = sectionBigDecimal.countContinuedLargeThan(last_n_Elements);
        } else if (useLong) {
            count = sectionLong.countContinuedLargeThan(last_n_Elements);
        } else {
            count = sectionFloat.countContinuedLargeThan(last_n_Elements);
        }
        return count;
    }


    public float avedev() {
        float val;
        if (useBigDecimal) {
            val = sectionBigDecimal.avedev().floatValue();
        } else if (useLong) {
            val = sectionLong.avedev();
        } else {
            val = sectionFloat.Avedev();
        }
        return val;
    }

    public int elementCount() {
        int val;
        if (useBigDecimal) {
            val = sectionBigDecimal.elementCount();
        } else if (useLong) {
            val = sectionLong.elementCount();
        } else {
            val = sectionFloat.elementCount();
        }
        return val;
    }

    public int getSectionLength() {
        return sectionLength;
    }
}
