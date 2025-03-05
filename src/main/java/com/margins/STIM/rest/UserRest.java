/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

/**
 *
 * @author PhilipManteAsare
 */

import com.margins.STIM.entity.Users;
import com.margins.STIM.service.User_Service;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import jakarta.persistence.EntityNotFoundException;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserRest {

    @Inject
    private User_Service userService;

    // Create User
    @POST
    @Path("/newUser")
    public Response createUser(Users user) {
      try {
            Users createdUser = userService.createUser(user);
            return Response.status(Response.Status.CREATED).entity(createdUser).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to create new user please: " + e.getMessage()).build();
        }
    }

    // Retrieve All Users
    @GET
    @Path("/list")
    public Response getAllUsers() {
        List<Users> users = userService.findAllUsers();
        return Response.ok(users).build();
    }

     // Retrieve User by Ghana Card Number
    @GET
    @Path("/{ghanaCardNumber}")
    public Response getUserByGhanaCard(@PathParam("ghanaCardNumber") String ghanaCardNumber) {
        Users user = userService.findUserByGhanaCard(ghanaCardNumber);
        if (user != null) {
            return Response.ok(user).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found with Ghana Card number: " + ghanaCardNumber).build();
        }
    }

   // Update User
    @PUT
    @Path("/{ghanaCardNumber}")
    public Response updateUser(@PathParam("ghanaCardNumber") String ghanaCardNumber, Users user) {
        try {
            if (!ghanaCardNumber.equals(user.getGhanaCardNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Ghana Card number in path does not match user data").build();
            }
            Users updatedUser = userService.updateUser(ghanaCardNumber, user);
            return Response.ok(updatedUser).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND).entity(e.getMessage()).build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to update user: " + e.getMessage()).build();
        }
    }

 // Delete User
    @DELETE
    @Path("/{ghanaCardNumber}")
    public Response deleteUser(@PathParam("ghanaCardNumber") String ghanaCardNumber) {
        try {
            userService.deleteUser(ghanaCardNumber);
            return Response.ok("User deleted successfully with Ghana Card number: " + ghanaCardNumber).build();
        } catch (EntityNotFoundException e) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("User not found with Ghana Card number: " + ghanaCardNumber).build();
        }
    }
}
