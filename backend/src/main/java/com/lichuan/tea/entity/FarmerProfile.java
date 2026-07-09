package com.lichuan.tea.entity;

import lombok.Data;
import javax.persistence.*;

@Data
@Entity
@Table(name = "farmer_profiles")
public class FarmerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    @Column(nullable = false)
    private String realName;

    private String address; // Add address field

    @Column(nullable = false)
    private String origin;

    @Lob
    private String story;

    private String teaGardenUrl;
}
