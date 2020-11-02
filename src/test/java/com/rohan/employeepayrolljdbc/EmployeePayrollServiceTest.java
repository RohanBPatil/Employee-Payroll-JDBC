package com.rohan.employeepayrolljdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
		LocalDate end = LocalDate.now();
		employeeByDateList = employeePayrollService.getEmployeeByDate(start, end);
		assertEquals(3, employeeByDateList.size());
	}

	@Test
	public void givenEmployees_WhenRetrievedAverage_ShouldReturnComputedMap() {
		Map<String, Double> genderComputedMap = employeePayrollService.getEmployeeAverageByGender();
		assertTrue(genderComputedMap.get("M").equals(300000.0));
		assertTrue(genderComputedMap.get("F").equals(500000.0));
	}

	@Test
	public void givenEmployees_WhenRetrievedMaximumSalaryByGender_ShouldReturnComputedMap() {
		Map<String, Double> genderComputedMap = employeePayrollService.getEmployeeMaximumSalaryByGender();
		System.out.println(genderComputedMap.get("M"));
		assertTrue(genderComputedMap.get("M").equals(500000.0));
		assertTrue(genderComputedMap.get("F").equals(500000.0));
	}

	@Test
	public void givenEmployees_WhenRetrievedMinimumSalaryByGender_ShouldReturnComputedMap() {
		Map<String, Double> genderComputedMap = employeePayrollService.getEmployeeMinimumSalaryByGender();
		assertTrue(genderComputedMap.get("M").equals(100000.0));
		assertTrue(genderComputedMap.get("F").equals(500000.0));
	}

	@Test
	public void givenEmployees_WhenRetrievedSumByGender_ShouldReturnComputedMap() {
		Map<String, Double> genderComputedMap = employeePayrollService.getEmployeeSumByGender();
		assertTrue(genderComputedMap.get("M").equals(900000.0));
		assertTrue(genderComputedMap.get("F").equals(500000.0));
	}

	@Test
	public void givenEmployees_WhenRetrievedCountByGender_ShouldReturnComputedMap() {
		Map<String, Double> genderComputedMap = employeePayrollService.getEmployeeCountByGender();
		assertTrue(genderComputedMap.get("M").equals(3.0));
		assertTrue(genderComputedMap.get("F").equals(1.0));
	}

	@Test
	public void givenNewEmployee_WhenAdded_ShouldSincWithDB() {
		employeePayrollService.addEmployeeToPayroll("Raghav", "M", 500000, LocalDate.now());
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Raghav");
		assertTrue(result);
	}
}
