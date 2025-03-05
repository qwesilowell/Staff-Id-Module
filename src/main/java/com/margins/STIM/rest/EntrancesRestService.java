/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

import com.margins.STIM.entity.Entrances;
import java.util.List;
import jakarta.ejb.Stateless;
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

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Create a new entrance.
     *
     * @param entrance The entrance to create.
     * @return Response indicating success or failure.
     */
    @POST
    public Response createEntrance(Entrances entrance) {
        entityManager.persist(entrance);
        return Response.ok("Entrance created successfully").build();
    }

    /**
     * Retrieve all entrances.
     *
     * @return A list of entrances.
     */
    @GET
    public List<Entrances> getAllEntrances() {
        return entityManager.createQuery("SELECT e FROM Entrances e", Entrances.class).getResultList();
    }

    /**
     * Retrieve a specific entrance by ID.
     *
     * @param id The ID of the entrance.
     * @return The entrance or a 404 response if not found.
     */
    @GET
    @Path("/{id}")
    public Response getEntranceById(@PathParam("id") String id) {
        Entrances entrance = entityManager.find(Entrances.class, id);
        if (entrance != null) {
            return Response.ok(entrance).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Entrance not found").build();
    }

    /**
     * Update an existing entrance.
     *
     * @param id       The ID of the entrance to update.
     * @param entrance The updated entrance details.
     * @return Response indicating success or failure.
     */
    @PUT
    @Path("/{id}")
    public Response updateEntrance(@PathParam("id") String id, Entrances entrance) {
        Entrances existingEntrance = entityManager.find(Entrances.class, id);
        if (existingEntrance != null) {
            existingEntrance.setEntrance_Device_ID(entrance.getEntrance_Device_ID());
            existingEntrance.setEntrance_Name(entrance.getEntrance_Name());
            existingEntrance.setEntrance_Location(entrance.getEntrance_Location());
            entityManager.merge(existingEntrance);
            return Response.ok("Entrance updated successfully").build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Entrance not found").build();
    }

    /**
     * Delete an entrance by ID.
     *
     * @param id The ID of the entrance to delete.
     * @return Response indicating success or failure.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteEntrance(@PathParam("id") String id) {
        Entrances entrance = entityManager.find(Entrances.class, id);
        if (entrance != null) {
            entityManager.remove(entrance);
            return Response.ok("Entrance deleted successfully").build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Entrance not found").build();
    }
}

