package com.example.medilink.ModelClass;

import java.time.LocalDate;
import java.util.Date;

public class Day {
    private final LocalDate date;
    private final String name;
    private final int no;

    public Day(LocalDate date, String name, int no) {
        this.date = date;
        this.name = name;
        this.no = no;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getName() {
        return name;
    }

    public int getNo() {
        return no;
    }
}
