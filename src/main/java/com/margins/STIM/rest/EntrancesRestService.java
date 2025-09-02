/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

import com.margins.STIM.DTO.EntranceDTO;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.service.EntrancesService;
import java.util.List;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
@Stateless
@Path("/entrances")
@Produces("application/json")
@Consumes("application/json")
public class EntrancesRestService {

    @Inject
    private EntrancesService entranceService;

    @PersistenceContext
    private EntityManager em;

    @POST
    public Response createEntrance(Entrances entrance) {
        entranceService.save(entrance);
        return Response.ok("Entrance created successfully").build();
    }

    @GET
    public List<EntranceDTO> getAllEntrances() {
        List<Entrances> entrances = entranceService.findAllEntrances(); 
        return entrances.stream()
                .map(EntranceDTO::new)
                .toList();
    }

    @GET
    @Path("/{id}")
    public Response getEntranceById(@PathParam("id") int id) {
        Entrances entrance = entranceService.findEntranceById(id);
        if (entrance != null) {
            return Response.ok(new EntranceDTO(entrance)).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Entrance not found").build();
    }

    @PUT
    @Path("/{id}")
    public Response updateEntrance(@PathParam("id") int id, Entrances entrance) {
        Entrances existing = entranceService.findEntranceById(id);
        if (existing != null) {
            existing.setEntranceId(entrance.getEntranceId());
            existing.setEntranceName(entrance.getEntranceName());
            existing.setEntranceLocation(entrance.getEntranceLocation());
            entranceService.updateEntrance(id, existing);
            return Response.ok("Entrance updated successfully").build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Entrance not found").build();
    }
}
