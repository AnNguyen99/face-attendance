package vn.bzcom.abis.faceattendance.event.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "event")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", length = 10)
    private String type;

    @CreationTimestamp
    @Column(name = "time", updatable = false)
    private LocalDateTime time;

    @Column(name = "message")
    private String message;

    @ManyToOne
    @JoinColumn(name = "device_id")
    private Device device;

    @Builder
    public Event(String type, String message, Device device) {
        this.type = type;
        this.message = message;
        this.device = device;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void  setDevice(Device device) {
        device.addEvent(this);
        this.device = device;
    }

}
