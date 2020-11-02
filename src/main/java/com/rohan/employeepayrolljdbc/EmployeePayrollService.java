package com.rohan.employeepayrolljdbc;

import java.util.List;

public class EmployeePayrollService {
	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	};

	public List<EmployeePayrollData> employeePayrollList;
	private EmployeePayrollDBService employeePayrollDBService;

	/**
	 * Default Constructor for caching employeePayrollDBService object 
	 */
	public EmployeePayrollService() {
		employeePayrollDBService = EmployeePayrollDBService.getInstance();
	}

	/**
	 * returns employeePayrollData object given name of employee
	 * 
	 * @param name
	 * @return
	 */
	private EmployeePayrollData getEmployeePayrollData(String name) {
		EmployeePayrollData employeePayrollData = this.employeePayrollList.stream()
				.filter(employeePayrollDataItem -> employeePayrollDataItem.name.equals(name)).findFirst().orElse(null);
		return employeePayrollData;
	}

	/**
	 * reads employee data from database and returns list of employee payroll data
	 * 
	 * @param ioService
	 * @return
	 */
	public List<EmployeePayrollData> readEmployeeData(IOService ioService) {
		try {
			if (ioService.equals(IOService.DB_IO)) {
				this.employeePayrollList = employeePayrollDBService.readData();
			}
		} catch (payrollServiceDBException exception) {
			System.out.println(exception.getMessage());
		}
		return this.employeePayrollList;
	}

	/**
	 * given name and updated salary of employee updates in the database
	 * 
	 * @param name
	 * @param salary
	 */
	public void updateEmployeePayrollSalary(String name, double salary) {
		int result = 0;
		try {
			result = employeePayrollDBService.updateEmployeeData(name, salary);
		} catch (payrollServiceDBException exception) {
			System.out.println(exception.getMessage());
		}
		if (result == 0) {
			return;
		}
		EmployeePayrollData employeePayrollData = this.getEmployeePayrollData(name);
		if (employeePayrollData != null) {
			employeePayrollData.salary = salary;
		}
	}

	/**
	 * checks if record matches with the updated record 
	 * 
	 * @param name
	 * @return
	 */
	public boolean checkEmployeePayrollInSyncWithDB(String name) {
		List<EmployeePayrollData> employeePayrollDataList = null;
		try {
			employeePayrollDataList = employeePayrollDBService.getEmployeePayrollData(name);
		} catch (payrollServiceDBException exception) {
			System.out.println(exception.getMessage());
		}
		return employeePayrollDataList.get(0).equals(getEmployeePayrollData(name));
	}

}
