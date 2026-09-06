package com.haha.service.impl.service

class ServiceCreateException(implName: String, cause: Throwable) :
    RuntimeException("Failed to create service: $implName", cause)

class ServiceNotFoundException(interfaceName: String, detail: String = "") :
    RuntimeException(
        if (detail.isEmpty()) {
            "No service registered for $interfaceName"
        } else {
            "No service registered for $interfaceName: $detail"
        }
    )
