package com.jatin.todomanager.model;


public class Task {

    int taskID;
    String title;
    String date;
    boolean selected = false;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {

        return selected;
    }

    public int getTaskID() {
        return taskID;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public Task(int id, String title, String date) {
        this.taskID = id;
        this.title = title;
        this.date = date;
    }
}
