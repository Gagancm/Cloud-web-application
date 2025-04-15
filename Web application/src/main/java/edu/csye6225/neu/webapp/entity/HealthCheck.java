package edu.csye6225.neu.webapp.entity;

import jakarta.persistence.*;
import java.util.Date;

@Entity
@Table(name="health_check")
public class HealthCheck {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "check_id")
    private Long checkId;

    @Column(name = "datetime", nullable = false)
    private Date datetime = new Date();
}
