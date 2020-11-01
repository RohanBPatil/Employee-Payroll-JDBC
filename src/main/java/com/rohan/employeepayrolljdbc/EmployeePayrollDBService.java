package com.rohan.employeepayrolljdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.Statement;

public class EmployeePayrollDBService {
	private Connection getConnection() 
	{
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "rpatil";
		Connection connection = null;
		
		try 
		{
			System.out.println("Connecting to database:" + jdbcURL);
			connection = DriverManager.getConnection(jdbcURL, userName, password);
			System.out.println("Connection is successful!" + connection);					
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		return connection;
	}
	
	public List<EmployeePayrollData> readData() throws SQLException
	{
		String sql = "select *  from employee_payroll;";
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		
		Connection connection = this.getConnection();
		Statement statement = (Statement) connection.createStatement();
		ResultSet resultSet = statement.executeQuery(sql);
		
		while(resultSet.next())
		{
			int id = resultSet.getInt("id");
			String name = resultSet.getString("name");
			double salary = resultSet.getDouble("salary");
			LocalDate startDate = resultSet.getDate("start").toLocalDate();
			employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
		}
		
		return employeePayrollList;
	}

}
