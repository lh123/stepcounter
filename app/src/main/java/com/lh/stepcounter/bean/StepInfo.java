package com.lh.stepcounter.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by home on 2017/1/12.
 */
@Entity
public class StepInfo {
    @Id(autoincrement = true)
    private Long id;
    private String date;
    private int step;
    @Generated(hash = 1584165430)
    public StepInfo(Long id, String date, int step) {
        this.id = id;
        this.date = date;
        this.step = step;
    }
    @Generated(hash = 1153084582)
    public StepInfo() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getDate() {
        return this.date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public int getStep() {
        return this.step;
    }
    public void setStep(int step) {
        this.step = step;
    }
    public void setId(Long id) {
        this.id = id;
    }
}
