package com.rohan.employeepayrolljdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
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
		employeePayrollService.updateEmployeePayrollSalary("Raghav", 500000.00);
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
		employeePayrollService.addEmployeeToPayroll("Raghav", "M", 500000, LocalDate.now(),
				Arrays.asList("Sales", "Marketing"));
		employeePayrollData = employeePayrollService.readEmployeeData(IOService.DB_IO);
		boolean result = employeePayrollService.checkEmployeePayrollInSyncWithDB("Raghav");
		assertTrue(result);
	}

	@Test
	void givenEmployeeId_WhenDeleted_shouldDeleteCascadingly() {
		employeePayrollService.deleteEmployeeFromPayroll(11);
		employeePayrollData = employeePayrollService.readEmployeeData(IOService.DB_IO);
		assertEquals(3, employeePayrollData.size());
	}

	@Test
	void givenEmployeeId_WhenRemoved_shouldReturnNumberOfActiveEmployees() {
		List<EmployeePayrollData> onlyActiveList = employeePayrollService.removeEmployeeFromPayroll(2);
		assertEquals(3, onlyActiveList.size());
	}

	@Test
	public void given6Employees_WhenAddedToDB_ShouldMatchEmployeeCount() {
		EmployeePayrollData[] arrayOfEmp = {
				new EmployeePayrollData(0, "Jeff", "M", 100000.0, LocalDate.now(), Arrays.asList("Sales")),
				new EmployeePayrollData(0, "Bill", "M", 200000.0, LocalDate.now(), Arrays.asList("Marketing")),
				new EmployeePayrollData(0, "Mark ", "M", 150000.0, LocalDate.now(), Arrays.asList("Technical")),
				new EmployeePayrollData(0, "Sundar", "M", 400000.0, LocalDate.now(),
						Arrays.asList("Sales", "Technical")),
				new EmployeePayrollData(0, "Mukesh ", "M", 4500000.0, LocalDate.now(), Arrays.asList("Sales")),
				new EmployeePayrollData(0, "Anil", "M", 300000.0, LocalDate.now(), Arrays.asList("Sales")) };
		Instant start = Instant.now();
		employeePayrollService.addMultipleEmployeesToPayroll(Arrays.asList(arrayOfEmp));
		Instant end = Instant.now();
		System.out.println("Duration without Thread: " + Duration.between(start, end));
		Instant threadStart = Instant.now();
		employeePayrollService.addMultipleEmployeesToPayrollWithThreads(Arrays.asList(arrayOfEmp));
		Instant threadEnd = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(threadStart, threadEnd));
		employeePayrollData = employeePayrollService.readEmployeeData(IOService.DB_IO);
		assertEquals(15, employeePayrollData.size());
	}

	@Test
	public void givenMultipleEmployees_WhenUpdatedSalary_ShouldSyncWithDB() {
		Map<String, Double> salaryMap = new HashMap<>();
		salaryMap.put("Bill", 700000.0);
		salaryMap.put("Mukesh", 800000.0);
		Instant start = Instant.now();
		employeePayrollService.updateMultipleSalaries(salaryMap);
		Instant end = Instant.now();
		System.out.println("Duration with Thread: " + Duration.between(start, end));
		employeePayrollData = employeePayrollService.readEmployeeData(IOService.DB_IO);
		boolean result = employeePayrollService.checkEmployeeListSync(Arrays.asList("Bill", "Mukesh"));
		assertEquals(true, result);
	}
}
