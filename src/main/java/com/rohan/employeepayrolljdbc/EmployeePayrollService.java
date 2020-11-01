package com.rohan.employeepayrolljdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class EmployeePayrollService {
	public enum IOService {
		CONSOLE_IO, FILE_IO, DB_IO, REST_IO
	};

	public List<EmployeePayrollData> employeePayrollList;

	public EmployeePayrollService(List<EmployeePayrollData> employeePayrollList) {
		this.employeePayrollList = employeePayrollList;
	}

	public EmployeePayrollService() {
	}

	static Scanner scanner = new Scanner(System.in);

	public static void main(String[] args) throws SQLException {
		ArrayList<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		EmployeePayrollService employeePayrollService = new EmployeePayrollService(employeePayrollList);

		String choice = "yes";
		do {
			if (choice.equalsIgnoreCase("yes")) {
				employeePayrollService.readEmployeeData(IOService.CONSOLE_IO);
				scanner.nextLine();
				System.out.println("Want to enter new employee payroll data?");
				choice = scanner.nextLine();
			}
		} while (choice.equalsIgnoreCase("yes"));
		employeePayrollService.writeEmployeeData(IOService.FILE_IO);
		employeePayrollService.readEmployeeData(IOService.FILE_IO);

	}

	/**
	 * Takes employee input from user through console
	 * 
	 * @param scanner
	 * @return 
	 * @throws SQLException 
	 */
	public List<EmployeePayrollData> readEmployeeData(IOService ioService) throws SQLException {
		if (ioService.equals(IOService.CONSOLE_IO)) {
			System.out.println("Enter Employee ID : ");
			int id = scanner.nextInt();
			scanner.nextLine();
			System.out.println("Enter Employee Name : ");
			String name = scanner.nextLine();
			System.out.println("Enter Employee Salary : ");
			double salary = scanner.nextDouble();
			employeePayrollList.add(new EmployeePayrollData(id, name, salary));
		} else if (ioService.equals(IOService.FILE_IO)) {
			System.out.println("reading data from file");
			new EmployeePayrollFile().printData();
		} else if (ioService.equals(IOService.DB_IO)) {
			this.employeePayrollList = new EmployeePayrollDBService().readData();
		}

		return this.employeePayrollList;
	}

	/**
	 * Writes data to console or file
	 * 
	 * @param ioService
	 * @throws SQLException
	 */
	public void writeEmployeeData(IOService ioService) {
		if (ioService.equals(IOService.CONSOLE_IO))
			System.out.println("\nWriting Employee Payroll Roaster to Console\n" + employeePayrollList);
		else if (ioService.equals(IOService.FILE_IO)) {
			new EmployeePayrollFile().writeData(employeePayrollList);
		}
	}

	/**
	 * Prints data from file to console
	 * 
	 * @param ioService
	 */
	public void printData(IOService ioService) {
		if (ioService.equals(IOService.FILE_IO)) {
			new EmployeePayrollFile().printData();
		}
	}

	public long countEntries(IOService fileIo) {
		long entries = 0;
		if (fileIo.equals(IOService.FILE_IO)) {
			entries = new EmployeePayrollFile().countEntries();
		}
		return entries;
	}
}
