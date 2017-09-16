package szhzz.sectionmath;

import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * 用于计算长系列数值的移动平均值
 * <p/>
 * Created by IntelliJ IDEA.
 * User: vmuser
 * Date: 2007-11-28
 * Time: 20:46:27
 * To change this template use File | Settings | File Templates.
 */
public class SectionMathLong {
    static DecimalFormat floatFormatter = new DecimalFormat("#,##0");
    LinkedList<Long> section;
    int sectionLength = 1;
    long lastValue = 0;
    long max = 0, min = 0;
    long acumulate = 0l;
    Long droped = null;
    private long sumValue = 0;

    public SectionMathLong(int sectionLength) {
        this.sectionLength = sectionLength;
        section = new LinkedList<Long>();
    }

    public void reset() {
        section.clear();
        sumValue = 0;
        lastValue = 0;
        max = 0;
        min = 0;
        acumulate = 0l;
        droped = null;
    }

    public void add(long val) {

        if (sectionLength == section.size()) {
            droped = section.removeLast();
        }
        section.addFirst(val);
        sumValue = (sumValue + val);
        if (droped != null) sumValue -= droped;

        lastValue = val;
        acumulate += val;
    }

    public Long getDroped() {
        return droped;
    }

    public long getAccumulate() {
        return acumulate;
    }

    public long getAvg() {
        if (section.size() > 0) {
            return sumValue / section.size();
        }
        return 0l;
    }

    public float getLastVal() {
        return lastValue;
    }

    public float getPreVal(int step) {
        return section.get((section.size() - 1) - step);
    }

    public float sumSubSection(int last_n_Elements) {
        int len = Math.min(last_n_Elements, section.size());
        long sum = 0;
        for (int i = 0; i < len; i++) {
            sum += section.get(i);
        }
        return sum;
    }

    public int countContinuedLargeThan(float last_n_Elements) {
        int count = 0;
        for (int i = 0; i < section.size(); i++) {
            if (section.get(i) <= last_n_Elements) break;
            count++;
        }
        return count;
    }

    public float getMax() {
        if (section.size() == 0) return 0;
        max = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            max = Math.max(max, section.get(i));
        }
        return max;
    }

    public float getMaxAbs() {
        if (section.size() == 0) return 0;
        max = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            max = Math.max(max, Math.abs(section.get(i)));
        }
        return max;
    }

    public float getMin() {
        if (section.size() == 0) return 0;
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            min = Math.min(min, section.get(i));
        }
        return min;
    }

    public float getMinAbs() {
        if (section.size() == 0) return 0;
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            min = Math.min(min, Math.abs(section.get(i)));
        }
        return min;
    }

    public String toString() {
        String s = "";
        for (Long aSection : section) {
            s += floatFormatter.format(aSection) + "; ";
        }
        return s;

    }

    public float getSumValue() {
        return sumValue;
    }

    public float avedev() {
        float avrage = getAvg();
        float sumDiff = 0f;
        if (section.size() > 0) {
            for (int i = 1; i < section.size(); i++) {
                sumDiff += (section.get(i) - avrage);
            }
            return sumDiff / section.size();
        }
        return sumDiff;
    }

    public int elementCount() {
        return section.size();
    }
}
