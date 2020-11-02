package com.rohan.employeepayrolljdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.rohan.employeepayrolljdbc.EmployeePayrollService.IOService;

class EmployeePayrollServiceTest {
	public static EmployeePayrollService employeeService;

	@Test
	public void givenEmployeePayrollDB_WhenRetrieved_ShouldMatchEmployeeCount() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeeData(IOService.DB_IO);
		assertEquals(4, employeePayrollData.size());
	}

	@Test
	public void givenNewSalaryForEmployee_WhenUpdated_ShouldSyncWithDb() {
		EmployeePayrollService employeePayrollService = new EmployeePayrollService();
		List<EmployeePayrollData> employeePayrollData = employeePayrollService.readEmployeeData(IOService.DB_IO);
		employeePayrollService.updateEmployeePayrollSalary("Raghav", 600000.00);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Raghav");
		assertEquals(4, employeePayrollData.size());
		assertTrue(result);
	}
}
