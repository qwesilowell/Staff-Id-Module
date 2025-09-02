/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.service;

import com.margins.STIM.DTO.VisitorAccessDTO;
import com.margins.STIM.DTO.VisitorCreateRequest;
import com.margins.STIM.DTO.VisitorResponse;
import com.margins.STIM.entity.CustomTimeAccess;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.EmploymentStatus;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.Visitor;
import com.margins.STIM.entity.VisitorAccess;
import com.margins.STIM.util.DateFormatter;
import jakarta.ejb.Schedule;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author PhilipManteAsare
 */
@Stateless
@Transactional
public class VisitorService {

    @Inject
    private EntrancesService entrancesService;

    @Inject
    private EmployeeRole_Service roleService;

    @Inject
    private Employee_Service employeeService;

    @PersistenceContext
    private EntityManager em;

    public Visitor findVisitorById(int id) {
        return em.find(Visitor.class, id);
    }

    public Visitor createVisitor(Visitor visitor) {
        em.persist(visitor);
        return visitor;
    }

    public List<Visitor> findAllVisitors() {
        return em.createQuery("SELECT v FROM Visitor v", Visitor.class)
                .getResultList();
    }

    public Visitor findVisitorByGhanaCard(String number) {
        try {
            return em.createQuery(
                    "SELECT v FROM Visitor v WHERE v.nationalId = :number", Visitor.class)
                    .setParameter("number", number)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

//    //Assign Access to a single entrance
//    public VisitorAccess assignAccess(Visitor visitor, Entrances entrance,
//            LocalTime start, LocalTime end) {
//        VisitorAccess access = new VisitorAccess();
//        access.setVisitor(visitor);
//        access.setEntrance(entrance);
//        access.setStartTime(start);
//        access.setEndTime(end);
//        access.setActive(true);
//
//        em.persist(access);
//        return access;
//    }

    public boolean hasValidAccess(int visitorId, int entranceId) {
        LocalDateTime now = LocalDateTime.now();

        String jpql = "SELECT va FROM VisitorAccess va "
                + "WHERE va.visitor.id = :visitorId "
                + "AND va.entrance.id = :entranceId "
                + "AND va.active = true "
                + "AND :now BETWEEN va.startTime AND va.endTime";

        List<VisitorAccess> results = em.createQuery(jpql, VisitorAccess.class)
                .setParameter("visitorId", visitorId)
                .setParameter("entranceId", entranceId)
                .setParameter("now", now)
                .getResultList();

        return !results.isEmpty();
    }

    @Schedule(hour = "23", minute = "59", persistent = false)
    public void deactivateExpiredAccess() {
        LocalDateTime now = LocalDateTime.now();

        String jpql = "UPDATE VisitorAccess va "
                + "SET va.active = false "
                + "WHERE va.endTime < :now AND va.active = true";

        em.createQuery(jpql)
                .setParameter("now", now)
                .executeUpdate();
    }

    public boolean deactivateVisitorAccess(int visitorId) {
        Visitor visitor = findVisitorById(visitorId);
        if (visitor == null) {
            return false;
        }

        int updated = em.createQuery("UPDATE VisitorAccess va "
                + "SET va.active = false "
                + "WHERE va.visitor = :visitor AND va.active = true")
                .setParameter("visitor", visitor)
                .executeUpdate();
        return updated > 0;
    }

    public boolean deactivateVisitorAccessById(int accessId) {
        VisitorAccess va  = em.find(VisitorAccess.class, accessId);
        if (va  != null && va.isActive()) {
            va.setActive(false);
            em.merge(va);
            return true;
        }
        return false;
    }

    public Visitor createVisitorWithAccess(VisitorCreateRequest request) {
        Visitor visitor = new Visitor();
        visitor.setForenames(request.getForenames());
        visitor.setSurname(request.getSurname());
        visitor.setNationalId(request.getNationalId());
        visitor.setDateOfBirth(request.getDateOfBirth());
        visitor.setGender(request.getGender());
        visitor.setEmail(request.getEmail());
        visitor.setPhoneNumber(request.getPhoneNumber());
        createVisitor(visitor);

        if (request.getEntranceIds() != null && !request.getEntranceIds().isEmpty()) {
            List<Entrances> entrance = entrancesService.findEntranceByIds(request.getEntranceIds());

            for (Entrances ent : entrance) {
                VisitorAccess access = new VisitorAccess();
                access.setEntrance(ent);
                access.setVisitor(visitor);
                access.setStartTime(request.getStartTime());
                access.setEndTime(request.getEndTime());
                access.setActive(true);

                em.persist(access);

                visitor.getAccessList().add(access);
            }

        }

        return visitor;
    }

    public Employee createVisitorPerson(VisitorCreateRequest request) {
        
        Employee employee = new Employee();
        employee.setFirstname(request.getForenames());
        employee.setLastname(request.getSurname());
        employee.setGhanaCardNumber(request.getNationalId());
        employee.setDateOfBirth(request.getDateOfBirth());
        employee.setGender(request.getGender());
        employee.setAddress("N/A");
        employee.setEmail(request.getEmail());
        employee.setPrimaryPhone(request.getPhoneNumber());
        EmployeeRole role = roleService.findVisitorRole();
        //Add method to create role and empStatus visitor on fresh login
        employee.setRole(role);
        EmploymentStatus status = employeeService.findStatusVisitor();
        employee.setEmploymentStatus(status);
        List<Entrances> entrance = entrancesService.findEntranceByIds(request.getEntranceIds());
        employee.setCustomEntrances(entrance);
        
        
        
        for (Entrances ent : entrance) {
            CustomTimeAccess customAccess = new CustomTimeAccess();
            customAccess.setDayOfWeek(request.getDayofWeek());
            customAccess.setEmployee(employee);
            customAccess.setEntrances(ent);
            customAccess.setEndTime(DateFormatter.toDate(request.getEndTime()));
            customAccess.setStartTime(DateFormatter.toDate(request.getStartTime()));       
            
            employee.getCustomAccessList().add(customAccess); 
        }
        employeeService.saveEmployee(employee);

        return employee;
    }

    public VisitorResponse visitorDetails(Visitor v) {
        VisitorResponse visit = new VisitorResponse();
        visit.setForenames(v.getForenames());
        visit.setSurname(v.getSurname());
        visit.setNationalId(v.getNationalId());
        visit.setEmail(v.getEmail());
        visit.setPhoneNumber(v.getPhoneNumber());
        visit.setId(v.getId());

        List<VisitorAccessDTO> accessDtos = v.getAccessList().stream()
                .map(access -> {
                    VisitorAccessDTO vad = new VisitorAccessDTO();
                    vad.setEntranceName(access.getEntrance().getEntranceName());
                    vad.setStartTime(DateFormatter.formatLocalDateAsTimeString(access.getStartTime()));
                    vad.setEndTime(DateFormatter.formatLocalDateAsTimeString(access.getEndTime()));
                    return vad;
                })
                .collect(Collectors.toList());

        visit.setAccessList(accessDtos);

        return visit;
    }
    
    
    public VisitorResponse empVisitorDetails(Employee v) {
        VisitorResponse visit = new VisitorResponse();
        visit.setForenames(v.getFirstname());
        visit.setSurname(v.getLastname());
        visit.setNationalId(v.getGhanaCardNumber());
        visit.setEmail(v.getEmail());
        visit.setPhoneNumber(v.getPrimaryPhone());
        visit.setId(v.getId());

        List<VisitorAccessDTO> accessDtos = v.getCustomAccessList().stream()
                .map(access -> {
                    VisitorAccessDTO vad = new VisitorAccessDTO();
                    vad.setEntranceName(access.getEntrances().getEntranceName());
                    vad.setStartTime(DateFormatter.formatDateAsTimeString(access.getStartTime()));
                    vad.setEndTime(DateFormatter.formatDateAsTimeString(access.getEndTime()));
                    return vad;
                })
                .collect(Collectors.toList());

        visit.setAccessList(accessDtos);

        return visit;
    }
}
