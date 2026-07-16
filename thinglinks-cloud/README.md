<div align="center">

[![MQTTSNET Logo](./docs/images/logo.png)](https://mqttsnet.com)

</div>

## ThingLinks | [中文文档](README.zh-CN.md)

# Introduction to ThingLinks Platform

[![OSCS Status](https://www.oscs1024.com/platform/badge/mqttsnet/thinglinks.svg?size=small)](https://www.oscs1024.com/project/mqttsnet/thinglinks?ref=badge_small)

## Project Structure

```
thinglinks-cloud
├── thinglinks-dependencies-parent     // Project parent POM, dependency management
├── thinglinks-public                  // Business-related common modules
│   ├── thinglinks-common              // Common utilities
│   ├── thinglinks-common-config       // Common configuration
│   ├── thinglinks-model               // Data model
│   ├── thinglinks-file-sdk            // File SDK
│   ├── thinglinks-data-scope-sdk      // Data scope SDK
│   ├── thinglinks-tenant-datasource-init  // Tenant datasource initialization
│   ├── thinglinks-sa-token-ext        // SA-Token extension
│   └── thinglinks-login-user-facade   // Login user facade
├── thinglinks-gateway                 // Gateway service
│   ├── thinglinks-gateway-biz         // Business logic
│   ├── thinglinks-gateway-server      // Server startup module
│   └── thinglinks-sop-gateway-server  // SOP gateway server
├── thinglinks-oauth                   // Authentication service
├── thinglinks-system                  // System service (user, role, permission, etc.)
├── thinglinks-base                    // Basic service (product, device, etc.)
├── thinglinks-link                    // Device integration service (protocol adaptation)
├── thinglinks-broker                  // Broker service (MQTT broker)
├── thinglinks-rule                    // Rule engine service
├── thinglinks-tds                     // Time-series data service (device data storage)
├── thinglinks-mqs                     // Message governance service
├── thinglinks-openapi                 // Open API service
├── thinglinks-video                   // Video streaming service
├── thinglinks-card                    // IoT card business service
├── thinglinks-view                    // Visualization service
├── thinglinks-mobile                  // Mobile service
├── thinglinks-sop-admin               // SOP management service
├── thinglinks-generator               // Online code generator service
├── thinglinks-sdk                     // SDK module
│   ├── thinglinks-sdk-core            // Core SDK
│   └── thinglinks-simple-sdk          // Simple SDK
└── thinglinks-support                 // Support module
    ├── thinglinks-monitor-server      // Monitoring service
    ├── thinglinks-base-executor       // Base executor
    └── thinglinks-iot-executor        // IoT executor
```

### Module Structure Description

Each business module adopts a layered architecture, typically including:

- **facade**: Interface definition layer, provides RPC interfaces
- **entity**: Entity layer, defines data models
- **biz**: Business logic layer, implements core business logic
- **controller**: Controller layer, handles HTTP requests
- **server**: Service startup layer, Spring Boot application entry
