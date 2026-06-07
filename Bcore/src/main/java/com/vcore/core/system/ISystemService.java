package com.vcore.core.system;

/**
 * Lifecycle interface for BlackBox system services.
 * <p>
 * All core services (package manager, user manager, activity manager, etc.)
 * implement this interface to receive a readiness callback during system startup.
 */
public interface ISystemService {

    /**
     * Called when the BlackBox system has been fully initialized and the service
     * may begin its own initialization logic (e.g., loading persisted data,
     * registering monitors, etc.).
     */
    void systemReady();
}
