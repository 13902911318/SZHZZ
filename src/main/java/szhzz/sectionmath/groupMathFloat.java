package szhzz.sectionmath;

import java.text.DecimalFormat;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: szhzz
 * Date: 2007-12-14
 * Time: 16:53:59
 * To change this template use File | Settings | File Templates.
 */
public class groupMathFloat {
    static DecimalFormat floatFormatter = new DecimalFormat("#,##0.000");

    LinkedList<SectionMathFloat> section = null;
    int goupLenth = 1;
    int sectionLength = 1;

    float lastValue = 0f;
    SectionMathFloat droped = null;

    public groupMathFloat(int goupLenth, int sectionLength) {
        this.goupLenth = goupLenth;
        this.sectionLength = sectionLength;
        section = new LinkedList<SectionMathFloat>();
        section.add(new SectionMathFloat(sectionLength));
    }

    public void addSection() {
        SectionMathFloat lastElement;
        if (goupLenth == section.size()) {
            lastElement = section.removeLast();
            droped = lastElement;
        }
        section.addFirst(new SectionMathFloat(sectionLength));
    }

    public SectionMathFloat getSectionMathFloat() {
        return section.getFirst();
    }

    public SectionMathFloat getSectionMathFloat(int i) {
        if (i > -1 && i < section.size()) {
            return section.get(i);
        }
        return null;
    }

    public void addToCurrentSection(float val) {
        getSectionMathFloat().add(val);
    }

    public float getAccumulate() {
        float accumulate = 0f;
        for (SectionMathFloat aSection : section) {
            accumulate += aSection.getAccumulate();
        }
        return accumulate;
    }

    public float getSumValue() {
        float sumValue = 0f;
        for (SectionMathFloat aSection : section) {
            sumValue += aSection.getSumValue();
        }
        return sumValue;
    }

    public float getAvg() {
        float avgValue = 0f;
        for (SectionMathFloat aSection : section) {
            avgValue += aSection.getAvg();
        }
        return avgValue / section.size();
    }

    public float getPreVal(int step) {
        return getSectionMathFloat().getPreVal(step);
    }

    public float getLastVal() {
        return getSectionMathFloat().getLastVal();
    }

    public float getMax() {
        float max = 0f;
        for (SectionMathFloat aSection : section) {
            max = Math.max(max, aSection.getMax());
        }
        return max;
    }

    public float getMaxAbs() {
        float max = 0f;
        for (SectionMathFloat aSection : section) {
            max = Math.max(max, Math.abs(aSection.getMax()));
        }
        return max;
    }

    public float getMin() {
        float min = 0f;
        min = getLastVal();
        for (SectionMathFloat aSection : section) {
            min = Math.min(min, aSection.getMin());
        }
        return min;
    }

    public float getMinAbs() {
        float min = 0f;
        min = getLastVal();
        for (SectionMathFloat aSection : section) {
            min = Math.min(min, Math.abs(aSection.getMin()));
        }
        return min;
    }


    public String toString() {
        String s = "";
        for (SectionMathFloat aSection : section) {
            s = aSection.toString() + "\n";
        }
        return s;
    }

    public Float getDroped() {
        return getSectionMathFloat().getDroped();
    }

    public int elementCount() {
        int count = 0;
        for (SectionMathFloat aSection : section) {
            count += aSection.elementCount();
        }
        return count;
    }

    public int getLength() {
        return sectionLength;
    }
}
