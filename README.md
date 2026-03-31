# TaskFlow Service

TaskFlow is a lightweight task management microservice designed to handle the complete lifecycle of tasks within a distributed system.

### Local Launch
In the root directory, open the console and run command:
`docker compose up -d`

The application runs on port 8080.

### Swagger
http://localhost:8080/tasks/swagger-ui.html


### Create a task 

Creates a new task and send event to kafka.

**Endpoint:** `POST /api/v1/tasks`

**Request Body:**
```json
{
  "name": "sum",        
  "description": "2 + 2 = ?"
}
```
**Constraints:**
- Parameter `name` must be between 3 and 30 characters and cannot be empty.
- Parameter `text` cannot exceed 1000 characters and cannot be empty.

**Response 201**
```json
{
  "taskId": "550e8400-e29b-41d4-a716-446655440000"
}
```
**Response 400**
```json
{
  "message": "Description cannot exceed 1000 characters",
  "errorCode": "400",
  "timestamp": "2026-02-18T14:51:52.7853999",
  "path": "http://localhost:8080/api/v1/tasks"
}
```

### Getting task

Gets task and they assigned user  by its ID.

**Endpoint:** `GET /api/v1/tasks/{taskId}`

**Constraints:**
- Parameter `taskId` must be UUID

**Response 200**

Successful response:
```json
{
  "name": "sum",
  "description": "2 + 2 = ?",
  "status": "COMPLETED",
  "user": {
    "userId": "33815145-ce49-450f-995d-33e84553ff77",
    "name": "user",
    "email": "user@gmai.com"
  }
}
```
**Response 400**
```json
{
  "message": "Method parameter 'taskId': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: 32w34432452345234545rgtfre4tfge4r",
  "errorCode": "400",
  "timestamp": "2026-02-18T15:08:47.0176812",
  "path": "http://localhost:8080/api/v1/tasks/32w34432452345234545rgtfre4tfge4r"
}
```

**Response 404**
```json
{
  "message": "Task with id: 3c52b478-590a-4665-b64f-49cc63caba18 not found",
  "errorCode": "404",
  "timestamp": "2026-02-18T15:09:48.4383468",
  "path": "http://localhost:8080/api/v1/tasks/3c52b478-590a-4665-b64f-49cc63caba18"
}
```

### Getting all tasks
Gets all tasks with pagination.

**Endpoint:** `GET /api/v1/tasks`

**Constraints:**
- Parameter `page` must be >= 0
- Parameter `size` must be >= 1

**Response 200**

Successful response:
```json
[
  {
    "taskId": "550e8400-e29b-41d4-a716-446655440000",
    "name": "sum",
    "description": "2 + 2 = ?",
    "status": "COMPLETED"
  },
  {
    "taskId": "e31c4cd3-bf13-4bbf-a241-820136e122b9",
    "name": "sum",
    "description": "2 + 2 = ?",
    "status": "IN_PROGRESS"
  }
]
```

### Change task status
Change status to task.

**Endpoint:** `PUT /api/v1/tasks/{taskId}/status`

**Request Body:**
```json
{
  "status": "FAILED"
}
```

**Constraints:**
- Parameter `status` cannot be null
- Parameter `taskId` must be uuid

**Response 200**
Void

**Response 400**
```json
{
"message": "Method parameter 'taskId': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: 32w34432452345234545rgtfre4tfge4r",
"errorCode": "400",
"timestamp": "2026-02-18T15:08:47.0176812",
"path": "http://localhost:8080/api/v1/tasks/32w34432452345234545rgtfre4tfge4r/status"
}
```

**Response 404**
```json
{
  "message": "Task with id: 3c52b478-590a-4665-b64f-49cc63caba18 not found",
  "errorCode": "404",
  "timestamp": "2026-02-18T15:09:48.4383468",
  "path": "http://localhost:8080/api/v1/tasks/3c52b478-590a-4665-b64f-49cc63caba18/status"
}
```


### Assign user to task
Assign user to task and send event to kafka

**Endpoint:** `PUT /api/v1/tasks/{taskId}/assignee`

**Request Body:**
```json
{
  "userId": "33815145-ce49-450f-995d-33e84553ff77"
}
```
**Constraints:**
- Parameter `userId` cannot be null and must be uuid
- Parameter `taskId` must be uuid

**Response 200**
Void

**Response 400**
```json
{
"message": "Method parameter 'taskId': Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: 32w34432452345234545rgtfre4tfge4r",
"errorCode": "400",
"timestamp": "2026-02-18T15:08:47.0176812",
"path": "http://localhost:8080/api/v1/tasks/32w34432452345234545rgtfre4tfge4r/assignee"
}
```

**Response 404**
```json
{ 
"message": "Task or user not found",
"errorCode": "404",
"timestamp": "2026-02-18T15:08:47.0176812",
"path": "http://localhost:8080/api/v1/tasks/3c52b478-590a-4665-b64f-49cc63caba18/assignee"
}
```
