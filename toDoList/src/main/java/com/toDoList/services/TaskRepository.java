package com.toDoList.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

import com.toDoList.models.Tasks;
import com.toDoList.*;

import jakarta.annotation.PostConstruct;
//using streams instead of loops = more efficient and concise, streams process collections

@Repository
public class TaskRepository {

    private List<Tasks> tasks = new ArrayList<>();
    private int currentId = 0;

    // Find all tasks
    public List<Tasks> findAll(int page, int size, String sortBy, String filterBy, String priority, Boolean completed,
            String taskName) {

        Stream<Tasks> taskStream = tasks.stream();
        // Filtering by completion status
        if (completed != null) {
            taskStream = taskStream.filter(task -> task.getCompleted().equals(completed));
        }

        // Filtering by task name (partial match)
        if (taskName != null && !taskName.isEmpty()) {
            taskStream = taskStream.filter(task -> task.getTaskName().toLowerCase().contains(taskName.toLowerCase()));
        }

        // Filtering by priority
        if (priority != null) {
            try {
                TaskPriority taskPriority = TaskPriority.valueOf(priority.toUpperCase());
                taskStream = taskStream.filter(task -> task.getTaskPriority() == taskPriority);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("");
            }
        }

        // Sorting
        if ("priority".equalsIgnoreCase(sortBy)) {
            taskStream = taskStream.sorted(Comparator.comparing(Tasks::getTaskPriority));
        } else if ("dueDate".equalsIgnoreCase(sortBy)) {
            taskStream = taskStream.sorted(Comparator.comparing(Tasks::getTaskDueDate));
        }

        // Pagination
        return taskStream
                .skip(page * size)
                .limit(size)
                .collect(Collectors.toList());
    }

    // Create a new task
    public void create(Tasks task) {
        if (task != null && findById(task.getId()).isEmpty()) {
            currentId += 1;
            task.setId(currentId);
            tasks.add(task);
        } else {
            throw new IllegalArgumentException("");
        }
    }

    // Find a task by ID used for testing purposes xd
    public Optional<Tasks> findById(Integer id) {
        return tasks.stream()
                .filter(task -> task.getId().equals(id))
                .findFirst();
    }

    // Update a task (using patch because if we use put we need to retrieve the data
    // from the not-updated obj)
    public Tasks patchUpdate(Integer id, Tasks partialUpdate) {
        Optional<Tasks> optionalTask = findById(id);

        if (optionalTask.isEmpty()) {
            throw new IllegalArgumentException("ToDo not found");
        }

        Tasks existingTask = optionalTask.get();

        if (partialUpdate.getTaskName() != null) {
            existingTask.setTaskName(partialUpdate.getTaskName());
        }
        if (partialUpdate.getTaskPriority() != null) {
            existingTask.setTaskPriority(partialUpdate.getTaskPriority());
        }
        if (partialUpdate.getCompleted() != null) {
            existingTask.setCompleted(partialUpdate.getCompleted());
        }
        if (partialUpdate.getTaskDueDate() != null) {
            existingTask.setTaskDueDate(partialUpdate.getTaskDueDate()); // i think i could use streams here, check it
                                                                         // later
        }

        return existingTask;
    }

    // Mark a task as completed with ptchj
    public Optional<Tasks> markAsDone(Integer id) {
        return findById(id).map(task -> {
            if (Boolean.FALSE.equals(task.getCompleted())) {
                task.setCompleted(true);
            }
            return task;
        });
    }

    public Optional<Tasks> markAsUnDone(Integer id) {
        return findById(id).map(task -> {
            if (Boolean.TRUE.equals(task.getCompleted())) {
                task.setCompleted(false);
            }
            return task;
        });
    }

    // Delete a task by ID
    public boolean delete(Integer id) {
        return tasks.removeIf(task -> task.getId().equals(id));
    }

    // Initialize repository with sample data
    @PostConstruct
    private void init() {
        tasks.add(new Tasks(
                currentId,
                "Do a to-do list",
                TaskPriority.HIGH,
                false,
                LocalDate.now()));
    }
}
