package com.rohan.employeepayrolljdbc;

import java.time.LocalDate;

public class EmployeePayrollData {
	public int id;
	public String name;
	public String gender;
	public double salary;
	public LocalDate startDate;

	public EmployeePayrollData(int id, String name, double salary) {
		this.id = id;
		this.name = name;
		this.salary = salary;
	}

	public EmployeePayrollData(int id, String name, double salary, LocalDate startDate) {
		this(id, name, salary);
		this.startDate = startDate;
	}

	public EmployeePayrollData(int id, String name, String gender, double salary, LocalDate startDate) {
		this(id, name, salary, startDate);
		this.gender = gender;
	}

	public String toString() {
		return "id = " + id + ", name = " + name + ", Gender = " + gender + ", Salary = " + salary + ", Start Date = "
				+ startDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		EmployeePayrollData that = (EmployeePayrollData) o;
		return id == that.id && Double.compare(that.salary, salary) == 0 && name.equals(that.name);
	}
}
