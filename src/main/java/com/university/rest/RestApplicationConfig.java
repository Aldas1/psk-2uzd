package com.university.rest;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * JAX-RS Application configuration
 * Maps REST services to /api/* path
 */
@ApplicationPath("/api")
public class RestApplicationConfig extends Application {
    // No additional configuration needed
    // This class enables JAX-RS and maps all REST services to /api/* path
}