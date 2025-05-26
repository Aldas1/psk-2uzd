package com.university.interceptor;

import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import java.io.Serializable;
import java.util.logging.Logger;

@Auditable
@Interceptor
public class AuditInterceptor implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AuditInterceptor.class.getName());

    @AroundInvoke
    public Object audit(InvocationContext context) throws Exception {
        String className = context.getTarget().getClass().getSimpleName();
        String methodName = context.getMethod().getName();
        Object[] parameters = context.getParameters();

        // Log method entry
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("AUDIT: ").append(className).append(".").append(methodName).append("(");

        if (parameters != null && parameters.length > 0) {
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) logMessage.append(", ");
                if (parameters[i] != null) {
                    logMessage.append(parameters[i].getClass().getSimpleName());
                } else {
                    logMessage.append("null");
                }
            }
        }
        logMessage.append(") - ENTRY");

        logger.info(logMessage.toString());

        long startTime = System.currentTimeMillis();

        try {
            Object result = context.proceed();
            long duration = System.currentTimeMillis() - startTime;

            logger.info("AUDIT: " + className + "." + methodName + "() - SUCCESS (duration: " + duration + "ms)");

            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;

            logger.severe("AUDIT: " + className + "." + methodName + "() - EXCEPTION: " + e.getMessage() + " (duration: " + duration + "ms)");

            throw e;
        }
    }
}