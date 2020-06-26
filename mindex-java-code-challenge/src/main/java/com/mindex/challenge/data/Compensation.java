package com.mindex.challenge.data;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

public class Compensation {

    private Employee employee;
    private Double salary;

    @JsonFormat(pattern="yyyy-MM-dd")
    private LocalDate effectiveDate;

    public Compensation() {
    }

    public Compensation(Employee employee, Double salary, LocalDate effectiveDate) {
        this.employee = employee;
        this.salary = salary;
        this.effectiveDate = effectiveDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }
}
