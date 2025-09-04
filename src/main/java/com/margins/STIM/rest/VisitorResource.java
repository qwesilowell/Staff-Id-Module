/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.DTO.VisitorAccessDTO;
import com.margins.STIM.DTO.VisitorCreateRequest;
import com.margins.STIM.DTO.VisitorResponse;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.Visitor;
import com.margins.STIM.entity.enums.NotificationPriority;
import com.margins.STIM.service.NotificationService;
import com.margins.STIM.service.VisitorService;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@Path("/visitors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class VisitorResource {

    @Inject
    private VisitorService visitorService;
    
    @Inject
    private NotificationService notificationService;

    @GET
    @Path("/{id}")
    public Response getVisitorById(@PathParam("id") int id) {
        Visitor visitor = visitorService.findVisitorById(id);
        if (visitor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Visitor not found")
                    .build();
        }
        VisitorResponse response = visitorService.visitorDetails(visitor);
        return Response.ok(response).build();
    }

    @GET
    public Response getAllVisitors() {
        List<Visitor> visitors = visitorService.findAllVisitors();

        List<VisitorResponse> responseList = visitors.stream()
                .map(visitorService::visitorDetails)
                .collect(Collectors.toList());

        return Response.ok(responseList).build();

    }

    //Create a visitor
    @POST
    @Path("createVisitor")
    public Response createVisitor(Visitor visitor) {
        Visitor savedVisitor = visitorService.createVisitor(visitor);
        VisitorResponse visitorResponse = visitorService.visitorDetails(savedVisitor);

        return Response.status(Response.Status.CREATED)
                .entity(visitorResponse)
                .build();
    }

    
    @POST
    @Path("visitorAccess")
    public Response createVisitorWithAccess(VisitorCreateRequest request) {
        try {
            if (request != null) {
                Employee employee = visitorService.createVisitorPerson(request);
                
                VisitorResponse response = visitorService.empVisitorDetails(employee);
                
                if (response.getAccessList() != null && !response.getAccessList().isEmpty()) {
                    for (VisitorAccessDTO access : response.getAccessList()) {
                        String message = String.format(
                                "Visitor %s %s assigned access to %s from %s to %s",
                                response.getForenames(),
                                response.getSurname(),
                                access.getEntranceName(),
                                access.getStartTime(),
                                access.getEndTime()
                        );

                        notificationService.createAdminBroadcastNotification(
                                "Visitor Access",
                                message,
                                NotificationPriority.LOW
                        );
                    }
                }

                return Response.status(Response.Status.CREATED)
                        .entity(response)
                        .build();          
            }
            else{
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Request cannot be null")
                        .build();            }
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("An error occurred while creating visitor access")
                    .build();
        }

    }

    // Deactivate ALL access for a visitor
    @PUT
    @Path("/{id}/deactivate")
    public Response deactivateVisitorAccess(@PathParam("id") int id) {
        boolean updated = visitorService.deactivateVisitorAccess(id);
        if (!updated) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Visitor not found or already inactive")
                    .build();
        }
        return Response.ok("All visitor access deactivated").build();
    }

    // Deactivate one specific access record
    @PUT
    @Path("/access/{accessId}/deactivate")
    public Response deactivateSingleAccess(@PathParam("accessId") int accessId) {
        boolean updated = visitorService.deactivateVisitorAccessById(accessId);
        if (!updated) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Visitor access not found or already inactive")
                    .build();
        }
        return Response.ok("Visitor access deactivated").build();
    }
}
