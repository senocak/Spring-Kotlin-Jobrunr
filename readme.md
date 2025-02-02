# Spring Kotlin Jobrunr

This project is a Spring Boot application written in Kotlin that utilizes JobRunr for background job processing. It includes various configurations for data sources, job scheduling, and job execution.


## Getting Started

### Prerequisites

- Java 21
- Gradle
- PostgreSQL

### Building the Project

To build the project, run the following command:

```sh
./gradlew build
```

### Running the Application
To run the application, use the following command:
```sh
./gradlew bootRun
```

### Endpoints
The application provides several endpoints for job management:

- GET /jobs: Ping endpoint
- GET /jobs/simple-job: Enqueue a simple job
- GET /jobs/schedule-simple-job: Schedule a simple job
- GET /jobs/long-running-job: Enqueue a long-running job
- GET /jobs/long-running-job-with-job-context: Enqueue a long-running job with job context
- GET /jobs/delete-job: Delete a job by ID