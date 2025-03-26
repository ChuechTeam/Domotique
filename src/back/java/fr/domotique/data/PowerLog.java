package fr.domotique.data;

import java.time.LocalDateTime;

public class PowerLog {
    private int id;
    private int deviceId;
    private String status;
    private LocalDateTime time;

    public PowerLog() {
    }

    public PowerLog(int deviceId, String status, LocalDateTime time) {
        this.deviceId = deviceId;
        this.status = status;
        this.time = time;
    }

    public PowerLog(int id, int deviceId, String status, LocalDateTime time) {
        this.id = id;
        this.deviceId = deviceId;
        this.status = status;
        this.time = time;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }
    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getTime() {
        return time;
    }
    public void setTime(LocalDateTime time) {
        this.time = time;
    }
}
