package szhzz.sectionmath;


import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-14
 * Time: 16:53:59
 * To change this template use File | Settings | File Templates.
 * <p/>
 * 固定长度、移动数据系列的计算器
 * 用于计算如 均线，累计值，等移动序列的数值
 */
public class SectionMathFloat implements Serializable {
    private static final long serialVersionUID = 1L;

    static DecimalFormat fT = new DecimalFormat("##0.000");
    protected float sumValue = 0f;
    LinkedList<Float> section;
    int sectionLength = 1;
    float lastValue = 0f;
    float max = 0f, min = 0f;
    float accumulate = 0f;
    Float droped = null;

    public SectionMathFloat(int sectionLength) {
        this.sectionLength = sectionLength;
        section = new LinkedList<Float>();
    }

    public void reset() {
        section.clear();
        sumValue = 0f;
        lastValue = 0f;
        max = 0f;
        min = 0f;
        accumulate = 0f;
        droped = null;
    }

    public void fill(float val) {
        add(val);
        while (sectionLength > section.size()) {
            add(val);
        }
    }

    public void add(double val) {
        add((float) val);
    }

    public void add(float val) {
        Float lastElement = 0f;

        if (sectionLength == section.size()) {
            lastElement = section.removeLast();
            droped = lastElement;
        }
        section.addFirst(val);
        sumValue += (val - lastElement);
        lastValue = val;
        accumulate += val;
    }

    public void addToEnd(float val) {
        Float lastElement = 0f;

        if (sectionLength == section.size()) {
            lastElement = section.removeFirst();
            droped = lastElement;
        }
        section.add(val);
        sumValue += (val - lastElement);
        lastValue = val;
        accumulate += val;
    }

    public void setFirt(float val) {
        if (section.size() == 0) return;

        float first = section.get(0);
        section.set(0, val);
        accumulate += (val - first);
        sumValue += (val - first);
        lastValue = val;
    }

    public void removeLast() {
        if (section.size() == 0) return;

        float last = section.removeLast();
        sumValue = 0f;
        for (Float v : section) {
            sumValue += v;
        }

        lastValue = section.getLast();
    }

    public void rollBack() {
        if (section.size() == 0) return;

        section.addLast(droped);
        section.removeFirst();
        lastValue = section.get(0);
        sumValue = 0f;
        for (Float v : section) {
            sumValue += v;
        }
    }

    public void replaceLast(float val) {
        Float lastElement = 0f;

        lastElement = section.get(0);
        section.set(0, val);

        sumValue += (val - lastElement);
        lastValue = val;
        accumulate += (val - lastElement);
    }

    public SectionMathFloat getCopy() {
        SectionMathFloat copy = new SectionMathFloat(sectionLength);
        for (int i = section.size() - 1; i >= 0; i--) {
            copy.add(section.get(i));
        }
//        copy.accumulate = accumulate;
//        copy.droped = droped;
//        copy.sumValue = sumValue;
        return copy;
    }

    public float getAccumulate() {
        return accumulate;
    }

    public void add(long val) {
        add((float) val);
    }

    public float getAvg() {
        if (section.size() > 0) {
            return sumValue / section.size();
        }
        return 0f;
    }

    public float getAbsAvg() {
        float eSum = 0f;
        if (section.size() == 0) {
            return 0;
        }
        for (int i = 0; i < section.size(); i++) {
            eSum += Math.abs(section.get(i));
        }
        return eSum / section.size();
    }

    public float getAvg(int elements) {
        float eSum = 0f;
        if (section.size() == 0) {
            return 0;
        }
        if (elements <= 0) {
            return section.get(0);
        }

        elements = Math.min(elements, section.size());
        for (int i = 0; i < elements; i++) {
            eSum += section.get(i);
        }
        return eSum / elements;
    }

    public float getAvg(int startElement, int elements) {
        float eSum = 0f;
        if (section.size() == 0) {
            return 0;
        }
        if (startElement < 0 || elements <= 0) {
            return section.get(0);
        }

        int endElement = Math.min(startElement + elements, section.size());
        for (int i = startElement; i < endElement; i++) {
            eSum += section.get(i);
        }
        return eSum / elements;
    }

    public float getPreVal(int step) {
        if (step < 0 || step > (section.size() - 1)) return 0;
        return section.get(step);

    }

    public float getLastVal() {
        return lastValue;
    }

    public float getMax() {
        if (section.size() == 0) return 0f;
        max = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            max = Math.max(max, section.get(i));
        }
        return max;
    }

    public float getMax(int size) {
        if (section.size() == 0) return 0f;
        max = section.getFirst();
        int mSsize = Math.min(size, section.size());
        for (int i = 1; i < mSsize; i++) {
            max = Math.max(max, section.get(i));
        }
        return max;
    }

    public float getWave() {
        if (section.size() == 0) return 0f;
        max = section.getFirst();
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            max = Math.max(max, section.get(i));
        }
        return max;
    }

    public float getMaxAbs() {
        if (section.size() == 0) return 0f;
        max = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            max = Math.max(max, Math.abs(section.get(i)));
        }
        return max;
    }

    public float getMin() {
        if (section.size() == 0) return 0f;
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            min = Math.min(min, section.get(i));
        }
        return min;
    }

    public float getMin(int size) {
        if (section.size() == 0) return 0f;
        min = section.getFirst();
        int mSize = Math.min(size, section.size());
        for (int i = 1; i < mSize; i++) {
            min = Math.min(min, section.get(i));
        }
        return min;
    }

    public int getMinIndex() {
        int index = 0;
        if (section.size() == 0) return -1;
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            if (section.get(i) < min) {
                min = section.get(i);
                index = i;
            }
        }
        return index;
    }

    public int getMaxIndex() {
        int index = 0;
        if (section.size() == 0) return -1;
        max = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            if (section.get(i) > max) {
                max = section.get(i);
                index = i;
            }
        }
        return index;
    }

    public float getMinAbs() {
        if (section.size() == 0) return 0f;
        min = section.getFirst();
        for (int i = 1; i < section.size(); i++) {
            min = Math.min(min, Math.abs(section.get(i)));
        }
        return min;
    }

    public float getSumValue() {
        return sumValue;
    }

    public float getSumValue(int size) {
        float sum = 0;
        if (section.size() == 0) return 0f;
        int mSize = Math.min(size, section.size());
        for (int i = 0; i < mSize; i++) {
            sum += section.get(i);
        }
        return sum;
    }

    /**
     * 强化近期数值权重的合集
     *
     * @return
     */
    public float getSumValue_DC() {
        return getSumValue_DC(section.size());
    }

    public float getSumValue_DC(int size) {
        float sum = 0;
        if (section.size() == 0) return 0f;
        int mSize = Math.min(size, section.size());
        for (int i = 0; i < mSize; i++) {
            float inz = ((float) (mSize - i) / mSize);  //越后的数据对结果影响越小
            sum += section.get(i) * inz;
        }
        return sum;
    }

    public float getEndValue() {
        return section.getLast();
    }

    public String toString() {
        String s = "";
        for (Float aSection : section) {
            s += fT.format(aSection) + "\t";
        }
        return s.trim();
    }

    public void readString(String l) {
        String s = "";
        StringTokenizer tok = new StringTokenizer(l, "\t");
        while (tok.hasMoreTokens()) {
            this.add(Float.parseFloat(tok.nextToken().trim()));
        }
    }

    public float sumSubSection(int last_n_Elements) {
        int len = Math.min(last_n_Elements, section.size());
        float sum = 0;
        for (int i = 0; i < len; i++) {
            sum += section.get(i);
        }
        return sum;
    }

    public float Avedev() {
        float avrage = getAvg();
        float sumDiff = 0f;
        if (section.size() > 0) {
            for (int i = 0; i < section.size(); i++) {
                sumDiff += Math.abs(section.get(i) - avrage);
            }
            return sumDiff / section.size();
        }
        return sumDiff;
    }

    public float Avedev(int size) {
        float avrage = getAvg(size);
        float sumDiff = 0f;
        int mSize = Math.min(size, section.size());
        if (mSize > 0) {
            for (int i = 0; i < mSize; i++) {
                sumDiff += Math.abs(section.get(i) - avrage);
            }
            return sumDiff / mSize;
        }
        return sumDiff;
    }

    public double STDEV() {
        double avrage = this.getAvg();
        double sumDiff = 0d;
        if (section.size() > 0) {
            for (int i = 1; i < section.size(); i++) {
                sumDiff += Math.pow((section.get(i) - avrage), 2);
            }
            sumDiff = sumDiff / section.size();
            return Math.pow(sumDiff, 0.5d);
        }
        return 0d;
    }

    public Float getDroped() {
        return droped;
    }

    public int elementCount() {
        return section.size();
    }

    public int countContinuedLargeThan(float last_n_Elements) {
        int count = 0;
        for (int i = 0; i < section.size(); i++) {
            if (section.get(i) <= last_n_Elements) break;
            count++;
        }
        return count;
    }

    public int getLength() {
        return sectionLength;
    }
}
