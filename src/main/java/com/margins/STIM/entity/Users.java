/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.entity;

//import com.margins.STIM.enums.EntityModel;
import com.margins.STIM.entity.enums.UserType;
import java.io.Serializable;
import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Getter
@Setter
@Entity
@Table(name = "SYSTEM_USER")
@Cacheable(false)
public class Users extends EntityModel implements Serializable {
    
    @Column(name = "ADMINGHANA_CARD_NUMBER", nullable = false, unique = true, length = 15)
    private String ghanaCardNumber;
   
    @Column(name = "USERNAME", nullable = false, unique = true)
    private String username;

    @Enumerated(EnumType.STRING)
    @Column(name = "USER_ROLE")
    private UserType userType;
}
