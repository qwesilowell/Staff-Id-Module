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
import com.margins.STIM.entity.EntityModel;
import com.margins.STIM.entity.Sub_Office;
import com.margins.STIM.service.Sub_Office_Service;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.persistence.EntityNotFoundException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/sub_offices")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)

public class Sub_OfficeRest extends EntityModel {

    @Inject
    private Sub_Office_Service subOfficeService;

    // Create Sub Office
    @POST
    @Path("/newSub_Office")
    public Response createSubOffice(Sub_Office subOffice) {
        Sub_Office createdSubOffice = subOfficeService.createSubOffice(subOffice);
        return Response.status(Response.Status.CREATED).entity(createdSubOffice).build();
    }

    // Retrieve All Sub Offices
    @GET
    @Path("/list")
    public Response getAllSubOffices() {
        List<Sub_Office> subOffices = subOfficeService.findAllSubOffices();
        return Response.ok(subOffices).build();
    }

    // Retrieve Sub Office by ID
    @GET
    @Path("/{id}")
    public Response getSubOfficeById(@PathParam("id") Long id) {
        Sub_Office subOffice = subOfficeService.findSubOfficeById(id);
        if (subOffice != null) {
            return Response.ok(subOffice).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Sub Office not found").build();
        }
    }

    // Update Sub Office
    @PUT
    @Path("/{id}")
    public Response updateSubOffice(@PathParam("id") Long id, Sub_Office subOffice) {
        try {
            Sub_Office updatedSubOffice = subOfficeService.updateSubOffice(id, subOffice);
            return Response.ok(updatedSubOffice).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        }
    }

    // Delete Sub Office
    @DELETE
    @Path("/{id}")
    public Response deleteSubOffice(@PathParam("id") Long id) {
        subOfficeService.deleteSubOffice(id);
        return Response.ok("Sub Office deleted successfully").build();
    }

    // Find Sub Offices by Department
    @GET
    @Path("/byDepartment/{departmentId}")
    public Response findSubOfficesByDepartment(@PathParam("departmentId") Long departmentId) {
        Department department = new Department(); 
        List<Sub_Office> subOffices = subOfficeService.findSubOfficesByDepartment(department);
        return Response.ok(subOffices).build();
    }
}