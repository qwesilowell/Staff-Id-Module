/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.Employee;
import com.margins.STIM.service.Employee_Service;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;

@Path("/employees")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmployeeRest {

    @Inject
    private Employee_Service employeeService;

    // Create Employee
    @POST
    @Path("/newEmployee")
    public Response createEmployee(Employee employee) {
        try {
            Employee createdEmployee = employeeService.saveEmployee(employee);
            return Response.status(Response.Status.CREATED)
                         .entity(createdEmployee)
                         .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                         .entity("Please there was an error in creating an employee: " + e.getMessage())
                         .build();
        }
    }

    // Retrieve All Employees
    @GET
    @Path("/list")
    public Response getAllEmployees() {
        List<Employee> employees = employeeService.findAllEmployees();
        return Response.ok(employees).build();
    }

    // Retrieve Employee by Ghana Card Number
    @GET
    @Path("/{ghanaCardNumber}")
    public Response getEmployeeByGhanaCard(@PathParam("ghanaCardNumber") String ghanaCardNumber) {
        Employee employee = employeeService.findEmployeeByGhanaCard(ghanaCardNumber);
        if (employee != null) {
            return Response.ok(employee).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Employee not found")
                    .build();
        }
    }

    // Update Employee
    @PUT
    @Path("/{ghanaCardNumber}")
    public Response updateEmployee(@PathParam("ghanaCardNumber") String ghanaCardNumber, 
                                 Employee employee) {
        try {
            // Ensure the Ghana Card number in the path matches the employee object
            if (!ghanaCardNumber.equals(employee.getGhanaCardNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                             .entity("Ghana Card number in path does not match employee data")
                             .build();
            }
            
            Employee updatedEmployee = employeeService.updateEmployee(ghanaCardNumber, employee);
            return Response.ok(updatedEmployee).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                         .entity("Employee not found with Ghana Card number: " + ghanaCardNumber)
                         .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                         .entity("Failed to update employee: " + e.getMessage())
                         .build();
        }
    }

    
    // Search Employee by Ghana Card Number (Partial match)
    @GET
    @Path("/search/{ghanaCardPattern}")
    public Response searchEmployeeByGhanaCard(@PathParam("ghanaCardPattern") String ghanaCardPattern) {
        try {
            List<Employee> matchingEmployees = employeeService.searchEmployeesByGhanaCard(ghanaCardPattern);
            if (matchingEmployees.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                             .entity("No employees found matching the Ghana Card pattern: " + ghanaCardPattern)
                             .build();
            }
            return Response.ok(matchingEmployees).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Error searching employees: " + e.getMessage())
                         .build();
        }
    }
    // Delete Employee
    @DELETE
    @Path("/{ghanaCardNumber}")
        public Response deleteEmployee(@PathParam("ghanaCardNumber") String ghanaCardNumber) {
        try {
            employeeService.deleteEmployee(ghanaCardNumber);
            return Response.ok("Employee with Ghana Card number " + ghanaCardNumber + " deleted successfully")
                         .build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                         .entity("Employee not found with Ghana Card number: " + ghanaCardNumber)
                         .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Failed to delete employee: " + e.getMessage())
                         .build();
        }
    }
    
        // Validate Ghana Card Number
    @GET
    @Path("/validate/{ghanaCardNumber}")
    public Response validateGhanaCard(@PathParam("ghanaCardNumber") String ghanaCardNumber) {
        try {
            // Add validation logic here
            boolean isValid = employeeService.validateGhanaCard(ghanaCardNumber);
            if (isValid) {
                return Response.ok("Ghana Card number is valid").build();
            } else {
                return Response.status(Response.Status.BAD_REQUEST)
                             .entity("Invalid Ghana Card number format")
                             .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                         .entity("Error validating Ghana Card: " + e.getMessage())
                         .build();
        }
    }
}


