package com.rohan.employeepayrolljdbc;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	 * returns list of employees
	 * 
	 * @param resultSet
	 * @return
	 * @throws SQLException
	 * @throws payrollServiceDBException
	 */
	private List<EmployeePayrollData> getEmployeePayrollData(ResultSet resultSet) throws SQLException {
		List<EmployeePayrollData> employeePayrollList = new ArrayList<>();
		Map<Integer, EmployeePayrollData> employeePayrollMap = new HashMap<>();
		while (resultSet.next()) {
			int id = resultSet.getInt("id");
			String deptName = resultSet.getString("departmentName");
			String name = resultSet.getString("name");
			String gender = resultSet.getString("gender");
			double salary = resultSet.getDouble("salary");
			double deductions = resultSet.getDouble("deductions");
			double taxable_pay = resultSet.getDouble("taxable_pay");
			double tax = resultSet.getDouble("tax");
			double netPay = resultSet.getDouble("net_pay");
			LocalDate startDate = resultSet.getDate("start").toLocalDate();
			PayrollDetails payrollDetails = new PayrollDetails(deductions, taxable_pay, tax, netPay);
			if (employeePayrollMap.containsKey(id)) {
				employeePayrollMap.get(id).departments.add(deptName);
			} else {
				employeePayrollMap.put(id, new EmployeePayrollData(id, name, gender, salary, startDate, payrollDetails,
						Arrays.asList(deptName)));
			}
		}
		employeePayrollList = employeePayrollMap.values().stream().collect(Collectors.toList());

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
	 * @throws SQLException
	 */
	@SuppressWarnings("static-access")
	private int updateEmployeeDataUsingStatement(String name, double salary)
			throws payrollServiceDBException, SQLException {
		int employeeId = -1;
		int rowAffected = 0;
		Connection connection = null;
		connection = this.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (SQLException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		}
		String sql = String.format("update employee_payroll set salary = %.2f where name = '%s';", salary, name);
		try (Statement statement = (Statement) connection.createStatement()) {
			rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
		} catch (SQLException exception) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new payrollServiceDBException(e.getMessage());
			}
			throw new payrollServiceDBException("Error while updating data");
		}
		this.addToPayrollDetails(connection, employeeId, salary); // adding to payroll_details table

		try {
			connection.commit();
		} catch (SQLException e) {
			throw new payrollServiceDBException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return rowAffected;
	}

	/**
	 * adding to payroll_details table
	 * 
	 * @param salary
	 * @throws payrollServiceDBException
	 */
	private void addToPayrollDetails(Connection connection, int employeeId, double salary)
			throws payrollServiceDBException {
		try (Statement statement = (Statement) connection.createStatement()) {
			double deductions = salary * 0.2;
			double taxable_pay = salary - deductions;
			double tax = taxable_pay * 0.1;
			double netPay = salary - tax;
			String sql = String.format(
					"insert into payroll_details (employeeId, basic_pay, deductions, taxable_pay, tax, net_pay) "
							+ "VALUES ('%s','%s','%s','%s','%s','%s')",
					employeeId, salary, deductions, taxable_pay, tax, netPay);
			statement.executeUpdate(sql);
		} catch (SQLException e) {
			try {
				connection.rollback();
			} catch (SQLException exception) {
				throw new payrollServiceDBException(exception.getMessage());
			}
			throw new payrollServiceDBException("Unable to add to database");
		}
	}

	/**
	 * when passed query returns list of employee payroll data
	 * 
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
		String sql = "select employee_payroll.id, employee_dept.departmentName, employee_payroll.name, employee.gender,"
				+ "payroll_details.salary, payroll_details.deductions, payroll_details.taxable_pay, payroll_details.tax, payroll_details.net_pay, employee_payroll.start"
				+ "from employee_payroll" + "inner join employee_dept on employee_payroll.id = employee_dept.employeeId"
				+ "inner join payroll_details on employee_payroll.id = payroll_details.employeeId;";
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
		try {
			return this.updateEmployeeDataUsingStatement(name, salary);
		} catch (SQLException e) {
			throw new payrollServiceDBException(e.getMessage());
		}
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
	 * 
	 * @param start
	 * @param end
	 * @return
	 * @throws payrollServiceDBException
	 */
	public List<EmployeePayrollData> readDataForGivenDateRange(LocalDate start, LocalDate end)
			throws payrollServiceDBException {
		String sql = String.format("Select * from employee_payroll where start between '%s' and '%s' ;",
				Date.valueOf(start), Date.valueOf(end));
		return this.getData(sql);
	}

	/**
	 * returns map of gender and calculated values when passed a function
	 * 
	 * @param function
	 * @return
	 * @throws payrollServiceDBException
	 */
	public Map<String, Double> getEmployeesByFunction(String function) throws payrollServiceDBException {
		Map<String, Double> genderComputedMap = new HashMap<>();
		String sql = String.format("Select gender, %s(salary) from employee_payroll group by gender ; ", function);
		try (Connection connection = this.getConnection()) {
			Statement statement = (Statement) connection.createStatement();
			ResultSet result = statement.executeQuery(sql);
			while (result.next()) {
				String gender = result.getString(1);
				Double salary = result.getDouble(2);
				genderComputedMap.put(gender, salary);
			}
		} catch (SQLException exception) {
			throw new payrollServiceDBException("Unable to find " + function);
		}
		return genderComputedMap;
	}

	/**
	 * adds employee details to database
	 * 
	 * @param name
	 * @param gender
	 * @param salary
	 * @param date
	 * @return
	 * @throws payrollServiceDBException
	 * @throws SQLException
	 */
	@SuppressWarnings("static-access")
	public EmployeePayrollData addEmployeeToPayroll(String name, String gender, double salary, LocalDate date,
			List<String> departments) throws payrollServiceDBException, SQLException {
		int employeeId = -1;
		Connection connection = null;
		EmployeePayrollData employee = null;
		connection = this.getConnection();
		try {
			connection.setAutoCommit(false);
		} catch (SQLException exception) {
			throw new payrollServiceDBException(exception.getMessage());
		}

		try (Statement statement = (Statement) connection.createStatement()) { // adding to employee_payroll table
			String sql = String.format(
					"insert into employee_payroll (name, gender, salary, start) values ('%s', '%s', '%s', '%s')", name,
					gender, salary, Date.valueOf(date));
			int rowAffected = statement.executeUpdate(sql, statement.RETURN_GENERATED_KEYS);
			if (rowAffected == 1) {
				ResultSet resultSet = statement.getGeneratedKeys();
				if (resultSet.next())
					employeeId = resultSet.getInt(1);
			}
			employee = new EmployeePayrollData(employeeId, name, gender, salary, date);
		} catch (SQLException exception) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new payrollServiceDBException(e.getMessage());
			}
			throw new payrollServiceDBException("Unable to add to database");
		}

		this.addToPayrollDetails(connection, employeeId, salary); // adding to payroll_details table

		try (Statement statement = (Statement) connection.createStatement()) { // adding to employee_dept table
			final int empId = employeeId;
			departments.forEach(dept -> {
				String sql = String.format("insert into employee_dept values (%s, '%s')", empId, dept);
				try {
					statement.executeUpdate(sql);
				} catch (SQLException e) {
				}
			});
			employee = new EmployeePayrollData(employeeId, name, gender, salary, date, departments);
		} catch (SQLException exception) {
			try {
				connection.rollback();
			} catch (SQLException e) {
				throw new payrollServiceDBException(e.getMessage());
			}
			throw new payrollServiceDBException("Unable to add to database");
		}

		try {
			connection.commit();
		} catch (SQLException e) {
			throw new payrollServiceDBException(e.getMessage());
		} finally {
			if (connection != null) {
				connection.close();
			}
		}
		return employee;
	}

	/**
	 * deletes employee record in cascade from both tables of database
	 * 
	 * @param id
	 * @throws payrollServiceDBException
	 */
	public void deleteEmployeeFromPayroll(int id) throws payrollServiceDBException {
		String sql = String.format("delete from employee_payroll where id = %s;", id);
		try (Connection connection = this.getConnection()) {
			Statement statement = (Statement) connection.createStatement();
			statement.executeUpdate(sql);
		} catch (SQLException exception) {
			throw new payrollServiceDBException("Unable to delete data");
		}
	}

}
