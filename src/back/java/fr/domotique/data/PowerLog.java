package fr.domotique.data;

import fr.domotique.base.data.*;

import java.time.*;

public class PowerLog {
    private int id;
    private int deviceId;
    private String status; // POWER_ON or POWER_OFF
    private double energyConsumption;
    private Instant time;

    public PowerLog() {
    }

    public PowerLog(int deviceId, String status, double energyConsumption, Instant time) {
        this.deviceId = deviceId;
        this.status = status;
        this.time = time;
        this.energyConsumption = energyConsumption;
    }

    public PowerLog(int id, int deviceId, String status, double energyConsumption, Instant time) {
        this.id = id;
        this.deviceId = deviceId;
        this.status = status;
        this.time = time;
        this.energyConsumption = energyConsumption;
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

    public Instant getTime() {
        return time;
    }
    public void setTime(Instant time) {
        this.time = time;
    }

    public double getEnergyConsumption() {
        return energyConsumption;
    }
    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }

    public static final EntityInfo<PowerLog> ENTITY = new EntityInfo<>(
        PowerLog.class,
        (r, s) -> new PowerLog(
            r.getInteger(s.next()),
            r.getInteger(s.next()),
            r.getString(s.next()),
            r.getDouble(s.next()),
            r.getLocalDateTime(s.next()).toInstant(ZoneOffset.UTC)
        ),
        new EntityColumn<>("id", PowerLog::getId, ColumnType.GENERATED_KEY),
        new EntityColumn<>("deviceId", PowerLog::getDeviceId),
        new EntityColumn<>("status", PowerLog::getStatus),
        new EntityColumn<>("energyConsumption", PowerLog::getEnergyConsumption),
        new EntityColumn<>("time", x -> LocalDateTime.ofInstant(x.getTime(), ZoneOffset.UTC))
    );
}
