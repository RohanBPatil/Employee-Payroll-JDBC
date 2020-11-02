package com.rohan.employeepayrolljdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;

public class EmployeePayrollDBService {
	private static EmployeePayrollDBService employeePayrollDBService;
	private PreparedStatement employeePayrollDataPrepareStatement;

	private EmployeePayrollDBService() {

	}

	/**
	 * initiates employeePayrollDBService only once and returns same
	 * 
	 * @return
	 */
	public static EmployeePayrollDBService getInstance() {
		if (employeePayrollDBService == null) {
			employeePayrollDBService = new EmployeePayrollDBService();
		}
		return employeePayrollDBService;
	}

	/**
	 * returns established connection with database
	 * 
	 * @return
	 * @throws payrollServiceDBException
	 */
	private Connection getConnection() throws payrollServiceDBException {
		String jdbcURL = "jdbc:mysql://localhost:3306/payroll_service?useSSL=false";
		String userName = "root";
		String password = "rpatil";
		Connection connection = null;
		try {
			System.out.println("Connecting to database:" + jdbcURL);
			connection = DriverManager.getConnection(jdbcURL, userName, password);
			System.out.println("Connection is successful!" + connection);
		} catch (Exception exception) {
			throw new payrollServiceDBException("Connection is not successful");
		}

		return connection;
	}

	/**
	 * returns list of
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 * @throws payrollServiceDBException
	 */
	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) throws SQLException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		while (resultSet.next()) {
			int id = resultSet.getInt("id");
			String name = resultSet.getString("name");
			double salary = resultSet.getDouble("salary");
			LocalDate startDate = resultSet.getDate("start").toLocalDate();
			employeePayrollList.add(new EmployeePayrollData(id, name, salary, startDate));
		}

		return employeePayrollList;
	}

	private void prepareStatementForEmployeeData() throws payrollServiceDBException {
		try {
			Connection connection = this.getConnection();
			String sql = "Select * from employee_payroll where name = ?";
			employeePayrollDataPrepareStatement = (PreparedStatement) connection.prepareStatement(sql);
		} catch (SQLException | payrollServiceDBException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		}
	}

	/**
	 * updates data using statement
	 * 
	 * @param name
	 * @param salary
	 * @return
	 * @throws payrollServiceDBException
	 */
	private int updateEmployeeDataUsingStatement(String name, double salary) throws payrollServiceDBException {
		String sql = String.format("update employee_payroll set salary = %.2f where name = '%s';", salary, name);

		try (Connection connection = this.getConnection()) {
			Statement statement = (Statement) connection.createStatement();
			return statement.executeUpdate(sql);
		} catch (payrollServiceDBException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		} catch (SQLException exception) {
			throw new payrollServiceDBException("Error while updating data");
		}
	}

	/**
	 * updates data using prepared statement
	 * 
	 * @param name
	 * @param salary
	 * @return
	 * @throws payrollServiceDBException
	 */
	private int updateEmployeeDataUsingPreparedStatement(String name, double salary) throws payrollServiceDBException {
		try (Connection connection = this.getConnection()) {
			String sql = "Update employee_payroll set salary = ? where name = ? ; ";
			PreparedStatement prepareStatement = (PreparedStatement) connection.prepareStatement(sql);
			prepareStatement.setDouble(1, salary);
			prepareStatement.setString(2, name);
			return prepareStatement.executeUpdate();
		} catch (SQLException | payrollServiceDBException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		}
	}

	/**
	 * when passed query returns list of employee payroll data
	 * @param sql
	 * @return
	 * @throws payrollServiceDBException
	 */
	private List<EmployeePayrollData> getData(String sql) throws payrollServiceDBException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		try (Connection connection = this.getConnection()) {
			Statement statement = (Statement) connection.createStatement();
			ResultSet resultSet = statement.executeQuery(sql);
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException | payrollServiceDBException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		}

		return employeePayrollList;
	}

	/**
	 * reads data from database and returns it in list
	 * 
	 * @return
	 * @throws payrollServiceDBException
	 */
	public List<EmployeePayrollData> readData() throws payrollServiceDBException {
		String sql = "select *  from employee_payroll;";
		return this.getData(sql);
	}

	/**
	 * updates data and returns number of records updated
	 * 
	 * @param name
	 * @param salary
	 * @return
	 * @throws payrollServiceDBException
	 */
	public int updateEmployeeData(String name, double salary) throws payrollServiceDBException {
		return this.updateEmployeeDataUsingPreparedStatement(name, salary);
	}

	public List<EmployeePayrollData> getEmployeePayrollData(String name) throws payrollServiceDBException {
		List<EmployeePayrollData> employeePayrollList = null;
		try {
			if (this.employeePayrollDataPrepareStatement == null) {
				this.prepareStatementForEmployeeData();
			}
			employeePayrollDataPrepareStatement.setString(1, name);
			ResultSet resultSet = employeePayrollDataPrepareStatement.executeQuery();
			employeePayrollList = this.getEmployeePayrollData(resultSet);
		} catch (SQLException | payrollServiceDBException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		}
		return employeePayrollList;
	}

	/**
	 * when given date range returns list of employees who joined between dates
	 * @param start
	 * @param end
	 * @return
	 * @throws payrollServiceDBException
	 */
	public List<EmployeePayrollData> readDataForGivenDateRange(LocalDate start, LocalDate end) throws payrollServiceDBException {
		String sql = String.format("Select * from employee_payroll where start between '%s' and '%s' ;",
				Date.valueOf(start), Date.valueOf(end));
		return this.getData(sql);
	}

}
