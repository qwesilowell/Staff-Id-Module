/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

import com.margins.STIM.entity.EmployeeEntrance;
import com.margins.STIM.service.EmployeeEntranceService;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author PhilipManteAsare
 */
@Path("/employeeEntrances")
@Produces("application/json")
@Consumes("application/json")
public class EmployeeEntranceRest {

    @EJB
    private EmployeeEntranceService service;

    @POST
    public Response createEmployeeEntrance(EmployeeEntrance employeeEntrance) {
        service.createEmployeeEntrance(employeeEntrance);
        return Response.ok("Employee Entrance created successfully").build();
    }

    @GET
    public List<EmployeeEntrance> getAllEmployeeEntrances() {
        return service.getAllEmployeeEntrances();
    }

    @GET
    @Path("/employee/{ghanaCardNumber}")
    public List<EmployeeEntrance> getEntrancesByGhanaCardNumber(@PathParam("ghanaCardNumber") String ghanaCardNumber) {
        return service.getEmployeeEntrancesByGhanaCardNumber(ghanaCardNumber);
    }

    @PUT
    @Path("/{id}")
    public Response updateAccessLevel(@PathParam("id") Long id, @QueryParam("newAccessLevel") String newAccessLevel) {
        EmployeeEntrance updated = service.updateAccessLevel(id, newAccessLevel);
        if (updated != null) {
            return Response.ok("Access Level updated successfully").build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Employee Entrance not found").build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteEmployeeEntrance(@PathParam("id") Long id) {
        service.deleteEmployeeEntrance(id);
        return Response.ok("Employee Entrance deleted successfully").build();
    }
}
