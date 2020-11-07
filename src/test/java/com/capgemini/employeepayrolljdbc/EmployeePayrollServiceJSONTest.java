package com.capgemini.employeepayrolljdbc;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.jupiter.api.Test;

import com.capgemini.employeepayrolljdbc.EmployeePayrollService.IOService;
import com.google.gson.Gson;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

class EmployeePayrollServiceJSONTest {

	@Before
	public void setup() {
		RestAssured.baseURI = "http://localhost";
		RestAssured.port = 3000;
	}

	/**
	 * retrieving data from json server
	 * 
	 * @return
	 */
	private EmployeePayrollData[] getEmployeeList() {
		Response response = RestAssured.get("/employees");
		System.out.println("Employee payroll entries in JSONServer:\n" + response.asString());
		EmployeePayrollData[] arrayOfEmp = new Gson().fromJson(response.asString(), EmployeePayrollData[].class);
		return arrayOfEmp;
	}

	/**
	 * adding employee to json server
	 * 
	 * @param employee
	 * @return
	 */
	private Response addEmployeeToJsonServer(EmployeePayrollData employee) {
		String empJson = new Gson().toJson(employee);
		RequestSpecification request = RestAssured.given();
		request.header("Content-Type", "application/json");
		request.body(empJson);
		return request.post("/employees");
	}

	@Test
	public void givenNewEmployee_WhenAdded_ShouldMatch201ResponseAndCount() {
		EmployeePayrollData[] arrayOfEmp = getEmployeeList();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmp));
		EmployeePayrollData employee = new EmployeePayrollData(6, "Ratan Tata", "M", 9000000.0, LocalDate.now());
		Response response = addEmployeeToJsonServer(employee);
		int statusCode = response.getStatusCode();
		assertEquals(201, statusCode);
		employee = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
		employeePayrollService.addEmployeeToPayroll(employee);
		long count = employeePayrollService.countEntries(IOService.REST_IO);
		assertEquals(6, count);
	}
	
	@Test
	public void givenMultipleNewEmployees_WhenAdded_ShouldMatch201ResponseAndCount() {
		EmployeePayrollData[] arrayOfEmp = getEmployeeList();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(Arrays.asList(arrayOfEmp));
		EmployeePayrollData[] arrayOfEmployee = { new EmployeePayrollData(0, "Julia", "F", 6000000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Joe", "M", 7000000.0, LocalDate.now()),
				new EmployeePayrollData(0, "Kamala", "F", 5000000.0, LocalDate.now()) };
		List<EmployeePayrollData> employeeList = Arrays.asList(arrayOfEmployee);
		employeeList.forEach(employee -> {
			Runnable task = () -> {
				Response response = addEmployeeToJsonServer(employee);
				int statusCode = response.getStatusCode();
				assertEquals(201, statusCode);
				EmployeePayrollData newEmployee = new Gson().fromJson(response.asString(), EmployeePayrollData.class);
				employeePayrollService.addEmployeeToPayroll(newEmployee);
			};
			Thread thread = new Thread(task, employee.name);
			thread.start();
			try {
				thread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		});
		long count = employeePayrollService.countEntries(IOService.REST_IO);
		assertEquals(9, count);
	}
}
