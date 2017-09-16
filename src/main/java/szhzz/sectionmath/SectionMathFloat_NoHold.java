package szhzz.sectionmath;

import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-14
 * Time: 16:53:59
 * To change this template use File | Settings | File Templates.
 */
public class SectionMathFloat_NoHold extends SectionMathFloat {
    float sumDiff = 0f;
    private int elements = 0;

    public SectionMathFloat_NoHold(int sectionLength) {
        super(sectionLength);
    }


    public void reset() {
        sumValue = 0f;
        lastValue = 0f;
        max = 0f;
        min = 0f;
        accumulate = 0f;
        droped = null;
        elements = 0;
        sumDiff = 0;
    }

    public void add(float val) {
        sumValue += val;
        lastValue = val;
        accumulate = sumValue;
        elements++;
        if (elements == 1) {
            max = val;
            min = val;
        } else {
            max = Math.max(max, val);
            min = Math.min(min, val);
        }
        calculateAvedev(val);
    }

    public void removeLast() {
    }

    public void setFirt(float val) {
//        sumValue += (val - lastValue);
//        lastValue = val;
//        accumulate = sumValue;
    }

    public void rollBack() {
    }

    public void replaceLast(float val) {
    }

    public SectionMathFloat_NoHold getCopy() {
        return null;
    }

    public float getAccumulate() {
        return accumulate;
    }

    public void add(long val) {
        add((float) val);
    }

    public float getAvg() {
        if (elements > 0) {
            return sumValue / elements;
        }
        return 0f;
    }

    public float getPreVal(int step) {
        return 0f;
    }

    public float getLastVal() {
        return lastValue;
    }

    public float getMax() {
        return max;
    }

    public float getMaxAbs() {
        return Math.max(Math.abs(max), Math.abs(min));
    }

    public float getMin() {
        return min;
    }

    public float getMinAbs() {
        return 0;
    }

    public float getSumValue() {
        return sumValue;
    }


    public String toString() {
        StringBuffer sb = new StringBuffer();

        sb.append(fT.format(accumulate));
        sb.append("\t");
        sb.append(elements);
        sb.append("\t");
        sb.append(fT.format(sumValue));
        sb.append("\t");
        sb.append(fT.format(lastValue));
        sb.append("\t");
        sb.append(fT.format(max));
        sb.append("\t");
        sb.append(fT.format(min));

        return sb.toString();
    }


    public void readString(String l) {
        StringTokenizer tok = new StringTokenizer(l, "\t");
        if (tok.hasMoreTokens())
            accumulate = (Float.parseFloat(tok.nextToken().trim()));
        if (tok.hasMoreTokens())
            elements = (Integer.parseInt(tok.nextToken().trim()));
        if (tok.hasMoreTokens())
            sumValue = (Float.parseFloat(tok.nextToken().trim()));
        if (tok.hasMoreTokens())
            lastValue = (Float.parseFloat(tok.nextToken().trim()));
        if (tok.hasMoreTokens())
            max = (Float.parseFloat(tok.nextToken().trim()));
        if (tok.hasMoreTokens())
            min = (Float.parseFloat(tok.nextToken().trim()));
    }

    public float sumSubSection(int last_n_Elements) {
        return 0f;
    }


    void calculateAvedev(float val) {
        float avrage = getAvg();
        sumDiff += Math.abs(val - avrage);
    }

    public float Avedev() {
        return 0;
//        sumDiff / elements;
    }

    public Float getDroped() {
        return droped;
    }

    public int elementCount() {
        return elements;
    }

    public int countContinuedLargeThan(float last_n_Elements) {
        return 0;
    }

    public int getLength() {
        return elements;
    }
}
