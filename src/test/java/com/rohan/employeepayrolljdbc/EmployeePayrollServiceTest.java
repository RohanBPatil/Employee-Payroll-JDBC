package com.rohan.employeepayrolljdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.rohan.employeepayrolljdbc.EmployeePayrollService.IOService;

class EmployeePayrollServiceTest {
	public static EmployeePayrollService employeePayrollService;
	public static List<EmployeePayrollData> employeePayrollData;
	
	@BeforeAll
	static void setUp() {
		employeePayrollService = new EmployeePayrollService();
		employeePayrollData = employeePayrollService.readEmployeeData(IOService.DB_IO);
	}

	@Test
	public void givenEmployeePayrollDB_WhenRetrieved_ShouldMatchEmployeeCount() {
		assertEquals(4, employeePayrollData.size());
	}

	@Test
	public void givenNewSalaryForEmployee_WhenUpdated_ShouldSyncWithDb() {
		employeePayrollService.updateEmployeePayrollSalary("Raghav", 600000.00);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Raghav");
		assertEquals(4, employeePayrollData.size());
		assertTrue(result);
	}
	
	@Test
	public void givenDateRange_WhenRetrieved_ShouldMatchEmployeeCount() {
		List<EmployeePayrollData> employeeByDateList = null;
		LocalDate start = LocalDate.of(2018, 8, 10);
		LocalDate end = LocalDate.of(2020, 8, 10);
		employeeByDateList = employeePayrollService.getEmployeeByDate(start, end);
		assertEquals(3, employeeByDateList.size());
	}
}
