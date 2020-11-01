package com.rohan.employeepayrolljdbc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class EmployeePayrollFile {
	public static String PAYROLL_FILE_NAME = "payroll.txt";

	/**
	 * writes data to file
	 * 
	 * @param list
	 */
	public void writeData(List<EmployeePayrollData> list) {
		StringBuffer employeeBuffer = new StringBuffer();
		list.forEach(employee -> {
			String empString = employee.toString().concat("\n");
			employeeBuffer.append(empString);
		});

		try {
			Files.write(Paths.get(PAYROLL_FILE_NAME), employeeBuffer.toString().getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Prints data from file to console
	 */
	public void printData() {
		try {
			Files.lines(new File(PAYROLL_FILE_NAME).toPath()).forEach(System.out::println);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * counts total entries
	 * 
	 * @return
	 */
	public long countEntries() {
		long totalEntries = 0;
		try {
			totalEntries = Files.lines(new File(PAYROLL_FILE_NAME).toPath()).count();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return totalEntries;
	}

	public List<EmployeePayrollData> readData() {
		List<EmployeePayrollData> list = new ArrayList<EmployeePayrollData>();
		try {
			Files.lines(new File(PAYROLL_FILE_NAME).toPath()).map(line -> line.trim()).forEach(line -> {
				String[] data = line.split("(, )");
				String[] newData = new String[10];
				int index = 0;
				for (String d : data) {
					String[] splitData = d.split("(=)");
					newData[index] = splitData[1];
					index++;
				}
				list.add(new EmployeePayrollData(Integer.parseInt(newData[0]), newData[1],
						Double.parseDouble(newData[2])));

			});
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}
}
