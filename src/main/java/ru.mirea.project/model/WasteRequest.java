package ru.mirea.project.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Доменная сущность заявки на вывоз отходов.
 */
public class WasteRequest {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private Long id;
    private Long clientId;
    private String address;
    private WasteType wasteType;
    private double volumeM3;
    private RequestStatus status;
    private LocalDateTime createdAt;

    // Опциональное поле для связывания имени клиента при выводе в отчеты
    private String clientName;

    public WasteRequest() {
    }

    public WasteRequest(Long id, Long clientId, String address, WasteType wasteType,
                        double volumeM3, RequestStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.clientId = clientId;
        this.address = address;
        this.wasteType = wasteType;
        this.volumeM3 = volumeM3;
        this.status = status;
        this.createdAt = createdAt;
    }

    public WasteRequest(Long clientId, String address, WasteType wasteType, double volumeM3) {
        this(null, clientId, address, wasteType, volumeM3, RequestStatus.NEW, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public WasteType getWasteType() {
        return wasteType;
    }

    public void setWasteType(WasteType wasteType) {
        this.wasteType = wasteType;
    }

    public double getVolumeM3() {
        return volumeM3;
    }

    public void setVolumeM3(double volumeM3) {
        this.volumeM3 = volumeM3;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WasteRequest that = (WasteRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String dateStr = createdAt != null ? createdAt.format(FORMATTER) : "N/A";
        String clientInfo = clientName != null ? String.format(" [Клиент: %s (ID:%d)]", clientName, clientId) 
                                               : String.format(" [Клиент ID: %d]", clientId);
        return String.format("#%-3d | %-11s | %6.2f м³ | %-16s | %s | %s%s",
                id,
                status.getTitle(),
                volumeM3,
                wasteType.name(),
                dateStr,
                address,
                clientInfo);
    }
}
