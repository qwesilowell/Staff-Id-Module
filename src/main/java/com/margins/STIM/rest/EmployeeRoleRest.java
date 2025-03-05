/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.service.EmployeeRole_Service;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;

@Path("/employeeRoles")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class EmployeeRoleRest {

    @Inject
    private EmployeeRole_Service employeeRoleService;

    // Create Employee Role
    @POST
    @Path("/newEmployeeRole")
    public Response createEmployeeRole(EmployeeRole employeeRole) {
        EmployeeRole createdRole = employeeRoleService.createEmployeeRole(employeeRole);
        System.out.println(">>>>>"+ createdRole.toString());
        return Response.status(Response.Status.CREATED).entity(createdRole).build();
    }

    // Retrieve All Employee Roles
    @GET
    @Path("/list")
    public Response getAllEmployeeRoles() {
        List<EmployeeRole> roles = employeeRoleService.findAllEmployeeRoles();
        return Response.ok(roles).build();
    }

    // Retrieve Employee Role by ID
    @GET
    @Path("/{id}")
    public Response getEmployeeRoleById(@PathParam("id") int id) {
        EmployeeRole role = employeeRoleService.findEmployeeRoleById(id);
        if (role != null) {
            return Response.ok(role).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Employee Role not found").build();
        }
    }

    // Update Employee Role
    @PUT
    @Path("/{id}")
    public Response updateEmployeeRole(@PathParam("id") int id, EmployeeRole employeeRole) {
        try {
            EmployeeRole updatedRole = employeeRoleService.updateEmployeeRole(id, employeeRole);
            return Response.ok(updatedRole).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Delete Employee Role
    @DELETE
    @Path("/{id}")
    public Response deleteEmployeeRole(@PathParam("id") int id) {
        employeeRoleService.deleteEmployeeRole(id);
        return Response.ok("Employee Role deleted successfully").build();
    }

  
}
