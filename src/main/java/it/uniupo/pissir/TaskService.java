package it.uniupo.pissir;

import static spark.Spark.*;
import com.google.gson.Gson;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

/**
 * The main REST service for handling tasks: it starts the server and handles all the HTTP requests.
 * @author <a href="mailto:luigi.derussis@uniupo.it">Luigi De Russis</a>
 * @version 1.2 (04/04/2019)
 */
public class TaskService {

    public static void main(String[] args) {
        // init
        Gson gson = new Gson();
        String baseURL = "/api/v1.0";
        TaskDao taskDao = new TaskDao();

        // enable CORS
        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Methods", "GET, POST");
            response.header("Access-Control-Allow-Headers", "Content-Type");
        });

        // get all the tasks
        get(baseURL + "/tasks", "application/json", (request, response) -> {
            // set a proper response code and type
            response.type("application/json");
            response.status(200);
            // get all tasks from the DB
            List<Task> allTasks = taskDao.getAllTasks();
            // prepare the JSON-related structure to return
            Map<String, List<Task>> finalJson = new HashMap<>();
            finalJson.put("tasks", allTasks);

            return gson.toJson(finalJson);
        });


        // get a single task
        get(baseURL + "/tasks/:id", "application/json", (request, response) -> {
            // get the id from the URL
            Task task = taskDao.getTask(Integer.valueOf(request.params(":id")));

            // no task? 404!
            if(task==null)
                halt(404);

            // prepare the JSON-related structure to return
            // and the suitable HTTP response code and type
            Map<String, Task> finalJson = new HashMap<>();
            finalJson.put("task", task);
            response.status(200);
            response.type("application/json");

            return finalJson;
        }, gson::toJson);

        // add a new task
        post(baseURL + "/tasks", "application/json", (request, response) -> {
            // get the body of the HTTP request
            Map addRequest = gson.fromJson(request.body(), Map.class);
            Task task;
            Map<String, Task> finalJson = new HashMap<>();

            // check whether everything is in place
            if(addRequest!=null && addRequest.containsKey("description") && addRequest.containsKey("urgent")) {
                String description = String.valueOf(addRequest.get("description"));
                // gson convert JSON num in double, but we need an int
                int urgent = ((Double) addRequest.get("urgent")).intValue();

                // add the task into the DB
                task = taskDao.addTask(new Task(description, urgent));

                // no task? 404!
                if(task==null)
                    halt(404);

                // prepare the JSON-related structure to return
                // and the suitable HTTP response code and type
                finalJson.put("task", task);
                response.type("application/json");
                // if success, prepare a suitable HTTP response code
                response.status(201);
            }
            else {
                halt(403);
            }

            return finalJson;
        },gson::toJson);

        // delete a single task
        delete(baseURL + "/tasks/:id", "application/json", (request, response) -> {
            // get the id from the URL
            int exitval = taskDao.deleteTask(Integer.valueOf(request.params(":id")));

            // no task? 404!
            if(exitval ==0 )
                halt(404);

            // return the suitable HTTP response code and type
            response.status(200);
            response.type("application/json");

            return "";
        });


    }

}
