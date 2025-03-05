/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.Department;
import com.margins.STIM.service.Department_Service;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;

@Path("/departments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DepartmentRest {

    @Inject
    private Department_Service departmentService;

    // Create Department
    @POST
    @Path("/newDepartment")
    public Response createDepartment(Department department) {
        Department createdDepartment = departmentService.createDepartment(department);
        return Response.ok(createdDepartment).build();
    }

    // Retrieve All Departments
    @GET
    @Path("/list")
    public Response getAllDepartments() {
        List<Department> departments = departmentService.findAllDepartments();
        return Response.ok(departments).build();
    }

    // Retrieve Department by ID
    @GET
    @Path("/{id}")
    public Response getDepartmentById(@PathParam("id") Long id) {
        Department department = departmentService.findDepartmentById(id);
        if (department != null) {
            return Response.ok(department).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Department not found").build();
        }
    }

    // Update Department
    @PUT
    @Path("/{id}")
    public Response updateDepartment(@PathParam("id") Long id, Department department) {
        try {
            Department updatedDepartment = departmentService.updateDepartment(id, department);
            return Response.ok(updatedDepartment).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Delete Department
    @DELETE
    @Path("/{id}")
    public Response deleteDepartment(@PathParam("id") Long id) {
        Department existingDepartment = departmentService.findDepartmentById(id);
        if (existingDepartment != null) {
            departmentService.deleteDepartment(id);
            return Response.ok("Department deleted successfully").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Department not found").build();
        }
    }
}
