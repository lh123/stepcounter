package com.lh.stepcounter.widget;

/**
 * Created by home on 2017/1/13.
 */

public class LineData {
    private String index;
    private float value;

    public LineData(String index, float value) {
        this.index = index;
        this.value = value;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
