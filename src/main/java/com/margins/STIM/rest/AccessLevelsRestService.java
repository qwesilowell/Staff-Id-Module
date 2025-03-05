/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

import com.margins.STIM.entity.Access_Levels;
import com.margins.STIM.service.AccessLevelsService;
import java.util.List;
import jakarta.ejb.EJB;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author PhilipManteAsare
 */
@Path("/accessLevels")
@Produces("application/json")
@Consumes("application/json")
public class AccessLevelsRestService {

    @EJB
    private AccessLevelsService accessLevelService;

    @POST
    public Response createAccessLevel(Access_Levels accessLevel) {
        accessLevelService.createAccessLevel(accessLevel);
        return Response.ok("Access Level created successfully").build();
    }

    @GET
    public List<Access_Levels> getAllAccessLevels() {
        return accessLevelService.findAllAccessLevels();
    }

    @GET
    @Path("/{id}")
    public Response getAccessLevelById(@PathParam("id") int id) {
        Access_Levels accessLevel = accessLevelService.findAccessLevelById(id);
        if (accessLevel != null) {
            return Response.ok(accessLevel).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Access Level not found").build();
    }

    @PUT
    @Path("/{id}")
    public Response updateAccessLevel(@PathParam("id") int id, Access_Levels accessLevel) {
        try {
            Access_Levels updatedAccessLevel = accessLevelService.updateAccessLevel(id, accessLevel);
            return Response.ok(updatedAccessLevel).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteAccessLevel(@PathParam("id") int id) {
        try {
            accessLevelService.deleteAccessLevel(id);
            return Response.ok("Access Level deleted successfully").build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }
}
