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

/**
 * RESTful web service for Faculty entity operations
 * Provides GET, POST, and PUT operations for Faculty management
 */
@Path("/faculties")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FacultyRestService {

    private static final Logger logger = Logger.getLogger(FacultyRestService.class.getName());

    @Inject
    private FacultyService facultyService;

    /**
     * GET /api/faculties
     * Retrieves all faculties
     *
     * @return List of all faculties
     */
    @GET
    public Response getAllFaculties() {
        try {
            logger.info("REST API: Getting all faculties");
            List<Faculty> faculties = facultyService.getAllFacultiesJpa();
            logger.info("REST API: Retrieved " + faculties.size() + " faculties");
            return Response.ok(faculties).build();
        } catch (Exception e) {
            logger.severe("REST API: Error retrieving faculties: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving faculties", e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/faculties/{id}
     * Retrieves a specific faculty by ID
     *
     * @param id Faculty ID
     * @return Faculty entity or 404 if not found
     */
    @GET
    @Path("/{id}")
    public Response getFacultyById(@PathParam("id") Long id) {
        try {
            logger.info("REST API: Getting faculty with ID: " + id);
            Faculty faculty = facultyService.getFacultyByIdJpa(id);

            if (faculty == null) {
                logger.warning("REST API: Faculty not found with ID: " + id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Faculty not found", "Faculty with ID " + id + " does not exist"))
                        .build();
            }

            logger.info("REST API: Found faculty: " + faculty.getName());
            return Response.ok(faculty).build();
        } catch (Exception e) {
            logger.severe("REST API: Error retrieving faculty: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error retrieving faculty", e.getMessage()))
                    .build();
        }
    }

    /**
     * POST /api/faculties
     * Creates a new faculty
     *
     * @param faculty Faculty entity to create
     * @return Created faculty with assigned ID
     */
    @POST
    public Response createFaculty(Faculty faculty) {
        try {
            logger.info("REST API: Creating new faculty: " + faculty.getName());

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

            logger.info("REST API: Faculty created successfully with ID: " + newFaculty.getId());
            return Response.status(Response.Status.CREATED)
                    .entity(newFaculty)
                    .build();

        } catch (Exception e) {
            logger.severe("REST API: Error creating faculty: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error creating faculty", e.getMessage()))
                    .build();
        }
    }

    /**
     * PUT /api/faculties/{id}
     * Updates an existing faculty
     *
     * @param id Faculty ID to update
     * @param faculty Updated faculty data
     * @return Updated faculty or error response
     */
    @PUT
    @Path("/{id}")
    public Response updateFaculty(@PathParam("id") Long id, Faculty faculty) {
        try {
            logger.info("REST API: Updating faculty with ID: " + id);

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
                logger.warning("REST API: Faculty not found for update with ID: " + id);
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

            logger.info("REST API: Faculty updated successfully. New version: " + updatedFaculty.getVersion());
            return Response.ok(updatedFaculty).build();

        } catch (OptimisticLockException ole) {
            logger.warning("REST API: Optimistic lock exception during update: " + ole.getMessage());
            return Response.status(Response.Status.CONFLICT)
                    .entity(new ErrorResponse("Concurrent modification",
                            "Faculty was modified by another user. Please refresh and try again."))
                    .build();

        } catch (Exception e) {
            logger.severe("REST API: Error updating faculty: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error updating faculty", e.getMessage()))
                    .build();
        }
    }

    /**
     * DELETE /api/faculties/{id}
     * Deletes a faculty by ID
     *
     * @param id Faculty ID to delete
     * @return Success or error response
     */
    @DELETE
    @Path("/{id}")
    public Response deleteFaculty(@PathParam("id") Long id) {
        try {
            logger.info("REST API: Deleting faculty with ID: " + id);

            // Check if faculty exists
            Faculty existingFaculty = facultyService.getFacultyByIdJpa(id);
            if (existingFaculty == null) {
                logger.warning("REST API: Faculty not found for deletion with ID: " + id);
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(new ErrorResponse("Faculty not found", "Faculty with ID " + id + " does not exist"))
                        .build();
            }

            facultyService.deleteFacultyJpa(id);

            logger.info("REST API: Faculty deleted successfully: " + existingFaculty.getName());
            return Response.status(Response.Status.NO_CONTENT).build();

        } catch (Exception e) {
            logger.severe("REST API: Error deleting faculty: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(new ErrorResponse("Error deleting faculty", e.getMessage()))
                    .build();
        }
    }

    /**
     * Error response class for consistent error handling
     */
    public static class ErrorResponse {
        private String error;
        private String message;
        private long timestamp;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
            this.timestamp = System.currentTimeMillis();
        }

        // Getters for JSON serialization
        public String getError() { return error; }
        public String getMessage() { return message; }
        public long getTimestamp() { return timestamp; }
    }
}