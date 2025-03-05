/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.Office;
import com.margins.STIM.service.Office_Service;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/offices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class OfficeRest {

    @Inject
    private Office_Service officeService;

    // Create Office
    @POST
    @Path("/newOffice")
    public Response createOffice(Office office) {
        Office createdOffice = officeService.createOffice(office);
        return Response.status(Response.Status.CREATED).entity(createdOffice).build();
    }

    // Retrieve All Offices
    @GET
    @Path("/list")
    public Response getAllOffices() {
        List<Office> offices = officeService.findAllOffices();
        return Response.ok(offices).build();
    }

    // Retrieve Office by ID
    @GET
    @Path("/{id}")
    public Response getOfficeById(@PathParam("id") Long id) {
        Office office = officeService.findOfficeById(id);
        if (office != null) {
            return Response.ok(office).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Office not found").build();
        }
    }

    // Update Office
    @PUT
    @Path("/{id}")
    public Response updateOffice(@PathParam("id") Long id, Office office) {
        try {
            Office updatedOffice = officeService.updateOffice(id, office);
            return Response.ok(updatedOffice).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Delete Office
    @DELETE
    @Path("/{id}")
    public Response deleteOffice(@PathParam("id") Long id) {
        officeService.deleteOffice(id);
        return Response.ok("Office deleted successfully").build();
    }
}
