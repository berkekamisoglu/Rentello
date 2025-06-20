package com.example.rentello.entity;

import java.io.Serializable;
import java.util.Objects;

public class ViewRevenueSummaryId implements Serializable {
    
    private Integer year;
    private Integer month;
    
    public ViewRevenueSummaryId() {}
    
    public ViewRevenueSummaryId(Integer year, Integer month) {
        this.year = year;
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ViewRevenueSummaryId that = (ViewRevenueSummaryId) o;
        return Objects.equals(year, that.year) && Objects.equals(month, that.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, month);
    }
} 