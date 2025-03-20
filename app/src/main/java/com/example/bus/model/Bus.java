package com.example.bus.model;

public class Bus {
    private String busName;
    private String assignedRoute;
    private double fare;
    private int totalTime;

    // ✅ Variables to store adjusted time & fare
    private transient int adjustedTime;
    private transient double adjustedFare;

    // ✅ Constructor
    public Bus(String busName, String assignedRoute, double fare, int totalTime) {
        this.busName = busName;
        this.assignedRoute = assignedRoute;
        this.fare = fare;
        this.totalTime = totalTime;
        this.adjustedFare = fare;  // Default = full fare
        this.adjustedTime = totalTime;  // Default = full time
    }

    // ✅ Copy Constructor
    public Bus(Bus other) {
        this.busName = other.busName;
        this.assignedRoute = other.assignedRoute;
        this.fare = other.fare;
        this.totalTime = other.totalTime;
        this.adjustedTime = other.adjustedTime;
        this.adjustedFare = other.adjustedFare;
    }

    // ✅ Methods to update fare and time dynamically
    public void updateAdjustedValues(int totalStops, int travelStops) {
        if (totalStops > 0) {
            this.adjustedFare = (this.fare / totalStops) * travelStops;
            this.adjustedTime = (this.totalTime / totalStops) * travelStops;
        }
    }

    // Getters and Setters
    public String getBusName() { return busName; }
    public String getAssignedRoute() { return assignedRoute; }
    public double getFare() { return adjustedFare; }  // Return updated fare
    public int getTotalTime() { return adjustedTime; }  // Return updated time
}
