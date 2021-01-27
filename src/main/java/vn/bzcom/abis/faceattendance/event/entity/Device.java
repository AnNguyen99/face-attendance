package vn.bzcom.abis.faceattendance.event.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "device")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private DeviceType deviceType;

    @Column(name = "model_no", length = 50)
    private String modelNo;

    @Column(name = "serial_no", length = 50)
    private String serialNo;

    @Embedded
    private Location location;

    // Smartphone device id
    @Column(name = "machine_id", nullable = true)
    private String machineId;

    @Column(name = "enabled", columnDefinition = "boolean default true")
    private boolean enabled;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Event> events = new ArrayList<>();

    public Device(DeviceType deviceType, String modelNo, String serialNo, Location location, String machineId, boolean enabled) {
        this.deviceType = deviceType;
        this.modelNo = modelNo;
        this.serialNo = serialNo;
        this.location = location;
        this.machineId = machineId;
        this.enabled = enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void addEvent(Event event) {
        if (events.contains(event)) {
            events.remove(event);
        }
        event.setDevice(this);
        events.add(event);
    }

}
