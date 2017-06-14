# Binder Queue

Binder queue is a simple master worker application. Master schedules jobs to multiple workers (job per worker) over RabbitMQ. Master waits for job parameters and ID of the optimization executable. When master recives parameters and ID over HTTP API, it uses RabbitMQ to send parameters to one of the free workers using Round Robin scheduling. When one of the workers recives job, it starts exe which needs to be in the given optimization dir and provides recived parameters to it. After the execution, worker recives results and sends them back to master which forwards them back to the client.

Executables are expected to have same name, and to be stored inside of the dirs named like optimization id.

### Requirements
  - RabbitMQ server

### Setup
  - Create RabbitMQ user for binder queue
  - Enter RabbitMQ host, port, username and password in master and worker properties
  - In `worker.properties` set path to the main dir (dir where dir containing optimizations is), optimization dir name (in which all optimization dirs are) and executable name.
  - In `master.properties` enter http server port
  - Start as many workers as you want
  - Start one master

### API
 ```
 POST /run
 ```
 ###### Parameters:
 - `params` (required) - list of parameters for the job (float).
 - `optimizationId` (required) - optimization id (int).
 
 
 ###### Response:
```JSON
{
  "result": [(float)],
  "status": "OK"
}
```

### Example
###### Request:
```JSON
{
	"params": [2.5, 5.0],
	"optimizationId": 1
}
```

###### Response:
```JSON
{
  "result": [7.5],
  "status": "OK"
}
````
