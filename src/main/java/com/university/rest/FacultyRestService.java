package com.university.rest;

import com.university.entity.Faculty;
import com.university.service.FacultyService;
import jakarta.inject.Inject;
import jakarta.persistence.OptimisticLockException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.logging.Logger;

@Path("/faculties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FacultyRestService {
    @Inject
    private FacultyService facultyService;

    @GET
    public Response getAllFaculties() {
        try {

            List<Faculty> faculties = facultyService.getAllFacultiesJpa();

            return Response.ok(faculties).build();
        } catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving faculties", e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    public Response getFacultyById(@PathParam("id") Long id) {
        try {

            Faculty faculty = facultyService.getFacultyByIdJpa(id);

            if (faculty == null) {

                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Faculty not found", "Faculty with ID " + id + " does not exist"))
                        .build();
            }


            return Response.ok(faculty).build();
        } catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving faculty", e.getMessage()))
                    .build();
        }
    }

    @POST
    public Response createFaculty(Faculty faculty) {
        try {


            // Validate required fields
            if (faculty.getName() == null || faculty.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Validation error", "Faculty name is required"))
                        .build();
            }

            if (faculty.getDepartment() == null || faculty.getDepartment().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Validation error", "Department is required"))
                        .build();
            }

            // Create a new Faculty entity to ensure it's properly detached
            Faculty newFaculty = new Faculty();
            newFaculty.setName(faculty.getName().trim());
            newFaculty.setDepartment(faculty.getDepartment().trim());
            // Don't set ID or version - they should remain null for new entities

            facultyService.saveFacultyJpa(newFaculty);


            return Response.status(Response.Status.CREATED)
                    .entity(newFaculty)
                    .build();

        } catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error creating faculty", e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateFaculty(@PathParam("id") Long id, Faculty faculty) {
        try {


            // Validate required fields
            if (faculty.getName() == null || faculty.getName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Validation error", "Faculty name is required"))
                        .build();
            }

            if (faculty.getDepartment() == null || faculty.getDepartment().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(new ErrorResponse("Validation error", "Department is required"))
                        .build();
            }

            // Check if faculty exists
            Faculty existingFaculty = facultyService.getFacultyByIdJpa(id);
            if (existingFaculty == null) {

                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Faculty not found", "Faculty with ID " + id + " does not exist"))
                        .build();
            }

            // Set the correct ID and version for optimistic locking
            faculty.setId(id);

            // If version is not provided in request, use current version from database
            if (faculty.getVersion() == null) {
                faculty.setVersion(existingFaculty.getVersion());
            }

            facultyService.updateFacultyJpa(faculty);

            // Retrieve updated faculty to return latest data
            Faculty updatedFaculty = facultyService.getFacultyByIdJpa(id);


            return Response.ok(updatedFaculty).build();

        } catch (OptimisticLockException ole) {

            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Concurrent modification",
                            "Faculty was modified by another user. Please refresh and try again."))
                    .build();

        } catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error updating faculty", e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response deleteFaculty(@PathParam("id") Long id) {
        try {


            // Check if faculty exists
            Faculty existingFaculty = facultyService.getFacultyByIdJpa(id);
            if (existingFaculty == null) {

                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Faculty not found", "Faculty with ID " + id + " does not exist"))
                        .build();
            }

            facultyService.deleteFacultyJpa(id);


            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (Exception e) {

            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error deleting faculty", e.getMessage()))
                    .build();
        }
    }

    public static class ErrorResponse {
        private String error;
        private String message;
        private long timestamp;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        public String getError() { return error; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}