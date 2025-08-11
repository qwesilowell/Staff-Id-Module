/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.rest;

/**
 *
 * @author PhilipManteAsare
 */
import com.margins.STIM.entity.BiometricData;
import com.margins.STIM.service.BiometricDataService;
import java.util.HashMap;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
@Path("/biometrics")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BiometricDataRest {

    @EJB
    private BiometricDataService biometricDataService;

    /**
     * Create new biometric data.
     *
     * @param biometricData The biometric data to create.
     * @return Response indicating the success or failure of the operation.
     */
    @POST
    @Path("/biometricdata")
    public Response createBiometricData(BiometricData biometricData) {
        try {
            BiometricData createdData = biometricDataService.createBiometricData(biometricData);
            return Response.status(Status.CREATED).entity(createdData).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error creating biometric data: " + e.getMessage()).build();
        }
    }
    
    
    @GET
    @Path("/hello")
    public String hello() {
        return "server up....";
    }
    
    @POST
    @Path("/biometricdata2")
    public Response createBiometricData2(String data) {
        try {
            
//            System.out.println("createBiometricData2 >>>>>>>>>>> " + data);
            JSONObject jsono = new JSONObject(data);
            JSONArray jsona = jsono.getJSONArray("finger_data");
            System.out.println("createBiometricData33333 >>>>>>>>>>> " + data);
            for (int i = 0; i < jsona.length(); i++) {
                JSONObject finger = jsona.optJSONObject(i);
                System.out.println("createBiometricData4444 >>>>>>>>>>> " + data);
                System.out.println(" FINGER DATA >>>>>>>>>>> " + finger.optString("data"));
            }
            System.out.println("createBiometricData5555 >>>>>>>>>>> " + data);
            //BiometricData createdData = biometricDataService.createBiometricData(biometricData);
//            return Response.status(Status.CREATED).entity(createdData).build();
            return Response.status(Status.CREATED).build();
        } catch (Exception e) {
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error creating biometric data: " + e.getMessage()).build();
        }
    }
    
    

    /**
     * Get a biometric data record by ID.
     *
     * @param id The ID of the biometric data.
     * @return Response containing the biometric data or an error message.
     */
    @GET
    @Path("/{id}")
    public Response getBiometricData(@PathParam("id") Long id) {
        BiometricData biometricData = biometricDataService.findBiometricDataById(id);
        if (biometricData != null) {
            return Response.ok(biometricData).build();
        }
        return Response.status(Status.NOT_FOUND).entity("Biometric data with ID " + id + " not found.").build();
    }

    /**
     * Get all biometric data records.
     *
     * @return Response containing a list of all biometric data records.
     */
    @GET
    @Path("/list")
    public Response getAllBiometricData() {
        List<BiometricData> biometricDataList = biometricDataService.findAllBiometricData();
        return Response.ok(biometricDataList).build();
    }

    /**
     * Update an existing biometric data record.
     *
     * @param id The ID of the biometric data to update.
     * @param updatedData The updated biometric data.
     * @return Response indicating the success or failure of the update.
     */
    @PUT
    @Path("/{id}")
    public Response updateBiometricData(@PathParam("id") Long id, BiometricData updatedData) {
        try {
            BiometricData updatedBiometricData = biometricDataService.updateBiometricData(id, updatedData);
            return Response.ok(updatedBiometricData).build();
        } catch (Exception e) {
            return Response.status(Status.NOT_FOUND).entity("Error updating biometric data: " + e.getMessage()).build();
        }
    }

    /**
     * Delete a biometric data record by ID.
     *
     * @param id The ID of the biometric data to delete.
     * @return Response indicating the success or failure of the deletion.
     */
    @DELETE
    @Path("/{id}")
    public Response deleteBiometricData(@PathParam("id") Long id) {
        try {
            biometricDataService.deleteBiometricData(id);
            return Response.status(Status.NO_CONTENT).build();
        } catch (Exception e) {
            return Response.status(Status.NOT_FOUND).entity("Error deleting biometric data: " + e.getMessage()).build();
        }
    }
}
