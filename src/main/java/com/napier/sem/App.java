package com.napier.sem;

import com.sun.source.tree.TryTree;

import java.sql.*;
import java.util.ArrayList;

public class App
{

    /**
     * Connection to MySQL database.
     */
    private Connection con = null;

    public static void main(String[] args)
    {
        // Create new Application
        App a = new App();



        if(args.length < 1) {
            // Connect to local database
            a.connect("127.0.0.1:3306", 3000);
        }else
        {
            System.out.println("Connecting to Docker Database");
            a.connect(args[0], Integer.parseInt((args[1])));
        }

        // Get Employee
        Employee emp = a.getEmployee(255530);
        // Display results

        //get Salaries by Dept
    //    ArrayList<Employee> employees = a.getSalariesByDepartment("d007");
        a.displayEmployee(emp);

        // Disconnect from database
        a.disconnect();
    }


    /**
     * Connect to the MySQL database.
     */
    public void connect(String location, int delay)
    {
        try
        {
            // Load Database driver
            Class.forName("com.mysql.cj.jdbc.Driver");
        }
        catch (ClassNotFoundException e)
        {
            System.out.println("Could not load SQL driver");
            System.exit(-1);
        }

        int retries = 10;
        for (int i = 0; i < retries; ++i)
        {
            System.out.println("Connecting to database...");
            try
            {
                // Wait a bit for db to start
                Thread.sleep(10000);
                // Connect to database
                con = DriverManager.getConnection("jdbc:mysql://" + location
                                + "/employees?useSSL=false",
                        "root", "example");
                System.out.println("Successfully connected");
                break;
            }
            catch (SQLException sqle)
            {
                System.out.println("Failed to connect to database attempt " + i );
                System.out.println(sqle.getMessage());
            }
            catch (InterruptedException ie)
            {
                System.out.println("Thread interrupted? Should not happen.");
            }
        }
    }

    /**
     * Disconnect from the MySQL database.
     */
    public void disconnect()
    {
        if (con != null)
        {
            try
            {
                // Close connection
                con.close();
            }
            catch (Exception e)
            {
                System.out.println("Error closing connection to database");
            }
        }
    }

    public Employee getEmployee(int ID)
    {
        try
        {
            // Create an SQL statement
            Statement stmt = con.createStatement();
            // Create string for SQL statement
            String strSelect =
                    "SELECT emp_no, first_name, last_name "
                            + "FROM employees "
                            + "WHERE emp_no = " + ID;
            // Execute SQL statement
            ResultSet rset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            if (rset.next())
            {
                Employee emp = new Employee();
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                return emp;
            }
            else
                return null;
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
            System.out.println("Failed to get employee details");
            return null;
        }
    }

    public void displayEmployee(Employee emp)
    {
        if (emp != null)
        {
            System.out.println(
                    emp.emp_no + " "
                            + emp.first_name + " "
                            + emp.last_name + "\n"
                            + emp.title + "\n"
                            + "Salary:" + emp.salary + "\n"
                            + emp.dept_name + "\n"
                            + "Manager: " + emp.manager + "\n");
        }
    }

    public void printSalaries(ArrayList<Employee> employees)
    {
        // Check employees is not null
        if (employees == null)
        {
            System.out.println("No employees");
            return;
        }



        // Print header
        System.out.println(String.format("%-10s %-15s %-20s %-8s", "Emp No", "First Name", "Last Name", "Salary"));
        // Loop over all employees in the list
        for (Employee emp : employees)
        {
            if (emp == null)
                continue;
            String emp_string =
                    String.format("%-10s %-15s %-20s %-8s",
                            emp.emp_no, emp.first_name, emp.last_name, emp.salary);
            System.out.println(emp_string);
        }
    }

    public Department getDepartment(String dept_name)
    {
        Department dept = new Department();

        try
        {
            Statement stmt = con.createStatement();

            // Create string for SQL statement
            String strSelect =
                    "SELECT departments.dept_no, departments.dept_name, dept_manager.emp_no   "
                    + " FROM Department, Dept_Manager"
                    + " WHERE dept_name = '" + dept_name +"'"
                    + " AND Dept_Manager.dept_no = Deptartment.dept_no"
                    + " AND Dept_Manager.to_date = '9999-01-01'";

            ResultSet rs = stmt.executeQuery(strSelect);

            if(rs.next())
            {
                dept.dept_no = rs.getInt("dept_No");
                dept.dept_name = rs.getString("dept_name");
                dept.manager = new App().getEmployee(rs.getInt("emp_no"));

                return dept;
            }



        }catch(Exception e)
        {
            System.out.println(e.toString());
        }

        return null;
    }

    public ArrayList<Employee> getSalariesByDepartment(Department dept)
    {
        ArrayList<Employee> employees = new ArrayList<>();
        Employee emp = new Employee();

        try
        {
            Statement stmt = con.createStatement();

            // Create string for SQL statement
            String strSelect =

            "SELECT employees.emp_no, employees.first_name, employees.last_name, salaries.salary "
           +" FROM employees, salaries, dept_emp, departments "
           +" WHERE employees.emp_no = salaries.emp_no "
           +" AND employees.emp_no = dept_emp.emp_no "
           +" AND dept_emp.dept_no = departments.dept_no"
           +" AND salaries.to_date = '9999-01-01'"
           +" AND departments.dept_no = '<dept_no>'"
           +" ORDER BY employees.emp_no ASC";

            ResultSet rset = stmt.executeQuery(strSelect);
            // Return new employee if valid.
            // Check one is returned
            while (rset.next())
            {
                emp.emp_no = rset.getInt("emp_no");
                emp.first_name = rset.getString("first_name");
                emp.last_name = rset.getString("last_name");
                emp.salary = rset.getInt("salary");

                employees.add(emp);
            }


        }catch (Exception e)
        {
            System.out.println(e.getMessage());
        }


        return employees;

    }
}