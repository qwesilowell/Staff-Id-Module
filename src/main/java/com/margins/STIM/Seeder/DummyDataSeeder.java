 /*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.margins.STIM.Seeder;

import com.margins.STIM.Interface.DeviceService;
import com.margins.STIM.entity.AccessLog;
import com.margins.STIM.entity.Devices;
import com.margins.STIM.entity.Employee;
import com.margins.STIM.entity.EmployeeEntranceState;
import com.margins.STIM.entity.EmployeeRole;
import com.margins.STIM.entity.Entrances;
import com.margins.STIM.entity.SystemUserRoles;
import com.margins.STIM.entity.ViewPermission;
import com.margins.STIM.entity.enums.DevicePosition;
import com.margins.STIM.entity.enums.EntranceMode;
import com.margins.STIM.entity.enums.LocationState;
import com.margins.STIM.service.AccessLogService;
import com.margins.STIM.service.EmployeeEntranceStateService;
import com.margins.STIM.service.EmployeeRole_Service;
import com.margins.STIM.service.Employee_Service;
import com.margins.STIM.service.EntrancesService;
import com.margins.STIM.service.TimeAccessRuleService;
import com.margins.STIM.service.UserRolesServices;
import com.margins.STIM.service.User_Service;
import jakarta.annotation.PostConstruct;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author PhilipManteAsare
 */
@Named("dummyDataSeeder")
@ViewScoped
@Getter
@Setter
public class DummyDataSeeder implements Serializable {

    @Inject
    private Employee_Service employeeService;
    @Inject
    private AccessLogService accessLogService;
    @Inject
    private DeviceService deviceService;
    @Inject
    private EntrancesService entranceService;
    @Inject
    private EmployeeEntranceStateService stateService;
    @Inject
    private User_Service userService;
    @Inject
    private TimeAccessRuleService timeAccessService;
    @Inject
    private EmployeeRole_Service roleService;
    @Inject
    private UserRolesServices userRoleService;

    private List<Employee> generatedEmployees = new ArrayList<>();
    private List<Devices> allDevices = new ArrayList<>();
    private String resultMessage;

    // Status tracking properties
    private int userCount = 0;
    private int roleCount = 0;
    private int pageCount = 0;

    private boolean setupComplete = false;

    private static final String[] FIRST_NAMES = {
        "Kwame", "Ama", "Kofi", "Akua", "Yaw", "Esi", "Kojo", "Afia", "Abena", "Nana",
        "Joseph", "Grace", "Michael", "Akosua", "Daniel", "Eunice", "Samuel", "Emmanuel", "Linda", "Yaw",
        "Kweku", "Adwoa", "Kwabena", "Akosua", "Kwadwo", "Yaa", "Fiifi", "Efua", "Kofi", "Araba",
        "Prince", "Patience", "Francis", "Comfort", "Isaac", "Mercy", "Paul", "Priscilla", "Peter", "Victoria",
        "Stephen", "Juliana", "David", "Beatrice", "John", "Mary", "Mark", "Elizabeth", "Luke", "Sarah",
        "Matthew", "Rebecca", "James", "Ruth", "Thomas", "Esther", "Andrew", "Hannah", "Philip", "Martha",
        "George", "Agnes", "Richard", "Joyce", "Robert", "Rose", "Charles", "Alice", "Edward", "Helen",
        "Anthony", "Margaret", "Benjamin", "Catherine", "Christopher", "Dorothy", "Nicholas", "Diana", "Timothy", "Janet"
    };

    private static final String[] LAST_NAMES = {
        "Mensah", "Owusu", "Boateng", "Addo", "Asare", "Appiah", "Tetteh", "Osei", "Darko", "Nyarko",
        "Agyeman", "Frimpong", "Dapaah", "Ampofo", "Agyei", "Sarpong", "Nkansah", "Annor", "Adomako", "Amankwah",
        "Johnson", "Brown", "Davis", "Miller", "Wilson", "Moore", "Taylor", "Anderson", "Thomas", "Jackson",
        "White", "Harris", "Martin", "Thompson", "Garcia", "Martinez", "Robinson", "Clark", "Rodriguez", "Lewis",
        "Lee", "Walker", "Hall", "Allen", "Young", "Hernandez", "King", "Wright", "Lopez", "Hill",
        "Scott", "Green", "Adams", "Baker", "Gonzalez", "Nelson", "Carter", "Mitchell", "Perez", "Roberts",
        "Turner", "Phillips", "Campbell", "Parker", "Evans", "Edwards", "Collins", "Stewart", "Sanchez", "Morris"
    };
    private static final String[] LOCATIONS = {
        "Octagon", "Pronto", "ICPS", "TechWing", "NonTechWing"
    };

    private static final String[] ENTRANCE_NAMES = {
        "Main Entrance", "Tech Zone", "Server Room", "NonTech Zone", "Food Court", "Finance Office", "Cyber Security",
        "IT Office", "Software Entrance", "HR OFFICE", "Project Management Entrance"
    };

    @PostConstruct
    public void init() {
        refreshStatus();
    }

    public void generatePages() {
        userService.seedViewPermissions();
    }

    public void completeSetup() {
        try {
            System.out.println("[Seeder] Starting complete setup...");

            // Create roles first
            createSystemRole();
            System.out.println("[Seeder]  System roles created");

            // Create admin user
            createUser();
            System.out.println("[Seeder]  Admin user created");

            // Scan and load pages
            generatePages();
            System.out.println("[Seeder]  Pages scanned and loaded");

            // Refresh status
            refreshStatus();

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "🎉 Setup Complete!",
                    "System has been initialized successfully! Admin user created and pages loaded.");
            FacesContext.getCurrentInstance().addMessage(null, msg);

        } catch (Exception e) {
            System.err.println("[Seeder] ❌ Setup failed: " + e.getMessage());
            e.printStackTrace();

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "❌ Setup Failed!",
                    "Error during setup: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void createAdminUser() {
        try {
            // Make sure roles exist first
            if (userRoleService.findUserRoleByName("SUPER ADMIN") == null) {
                createSystemRole();
            }
            if (roleService.findVisitorRole() == null){
            createVisitorRole();
            }

            createUser();

            refreshStatus();

            if (resultMessage != null && resultMessage.equals("User created successfully.")) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                        "👤 User Created!",
                        "Super Admin user has been created successfully!");
                FacesContext.getCurrentInstance().addMessage(null, msg);
            }

        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "User Creation Failed!",
                    "Error creating admin user: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void createSystemRoles() {
        try {
            createSystemRole();
            refreshStatus();

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "🔧 Roles Created!",
                    "System roles have been created successfully!");
            FacesContext.getCurrentInstance().addMessage(null, msg);

        } catch (Exception e) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "❌ Role Creation Failed!",
                    "Error creating roles: " + e.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
        }
    }

    public void refreshStatus() {
        try {
            userCount = userService.findAllUsers().size();
            roleCount = userRoleService.getAllUserRoles().size();
            pageCount = userService.findAll().size(); // this gets ViewPermissions

            // Check if setup is complete (has admin user and some pages)
            setupComplete = userCount > 0 && pageCount > 0 && roleCount > 0;

        } catch (Exception e) {
            System.err.println("[Seeder] Error refreshing status: " + e.getMessage());
            userCount = roleCount = pageCount = 0;
            setupComplete = false;
        }
    }

    public void generateAll() {

        createSystemRole();
        System.out.println("[Seeder] Users Roles>>>>>>>>  created: " + userRoleService.getAllUserRoles().size());

        createUser();
        System.out.println("[Seeder] Users>>>>>>>>  created: " + userService.findAllUsers().size());

        createRoles();                        // Needed for employees and role-based access
        System.out.println("[Seeder] Roles created: " + roleService.findAllEmployeeRoles().size());

        createEntrances();                   // Needed before assigning to devices, roles
        System.out.println("[Seeder] Entrances created: " + entranceService.findAllEntrances().size());

        createDevices();                     // Needed for access logs (ENTRY/EXIT points)
        System.out.println("[Seeder] Devices created: " + deviceService.getAllDevices().size());

        linkRolesToEntrances();             // Must come before role time access
        System.out.println("[Seeder] Roles linked to entrances.");

        createEmployees();                   // Must come after roles (employees need roles)
        System.out.println("[Seeder] Employees created: " + generatedEmployees.size());

        assignRolesToEmployees();           // After employees exist
        System.out.println("[Seeder] Roles assigned to employees.");

        assignCustomTimeAccess();           // After employees and entrances exist
        long customRuleCount = timeAccessService.countCustomTimeAccessRules();
        System.out.println("[Seeder] Custom time access rules created: " + customRuleCount);

        assignRoleTimeAccess();             // After roles and their entrance links exist
        long roleRuleCount = timeAccessService.countRoleTimeAccessRules();
        System.out.println("[Seeder] Role time access rules created: " + roleRuleCount);

        createEmployeeEntranceStates();     // After employees and entrances exist
        long entranceStateCount = stateService.findAllEmployeeEntranceStates().size();
        System.out.println("[Seeder] Employee entrance states created: " + entranceStateCount);

        createAccessLogs();                 // Must be last — it relies on all the above
        long logCount = accessLogService.countAllLogs();
        System.out.println("[Seeder] Access logs created: " + logCount);

        System.out.println("Seeder>>>>>>>>>>>  finished successfully.");
    }
    
    public void createVisitorRole(){
    
    }

    private void deleteAllExistingData() {
        // TODO: Call your service deleteAll() methods
        // Example:
        // accessLogService.deleteAll();
        // employeeService.deleteAll();
        // ...

    }

    public void createSystemRole() {
        if (userRoleService.findUserRoleByName("SUPER ADMIN") != null) {
            return; // Already seeded
        }

        SystemUserRoles userRoles = new SystemUserRoles();
        userRoles.setUserRolename("SUPER ADMIN");

        generatePages();
        List<ViewPermission> allPermissions = userService.findAll();
        userRoles.setPermissions(new HashSet<>(allPermissions));
        userRoleService.createUserRole(userRoles);
    }

    public void createUser() {
        SystemUserRoles userRole = userRoleService.findUserRoleByName("SUPER ADMIN");

        resultMessage = userService.createSystemUser(userRole);
    }

    private void createRoles() {
        String[] roleNames = {
            "CEO", "CTO", "CFO", "HR Manager", "Software Developer",
            "Marketing Manager", "Sales Representative", "Accountant", "Operations Manager", "Security Guard",
            "Product Manager", "Data Analyst", "UX Designer", "DevOps Engineer", "Quality Assurance Tester",
            "Business Analyst", "Customer Support Representative", "Legal Counsel", "Project Manager", "IT Support Specialist",
            "Content Writer", "Graphic Designer", "Finance Manager", "Procurement Officer", "Training Coordinator",
            "Network Administrator", "Database Administrator", "Social Media Manager", "Research Analyst", "Administrative Assistant"
        };

        String[] roleDescriptions = {
            "Chief Executive Officer - Overall company leadership",
            "Chief Technology Officer - Technology strategy and development",
            "Chief Financial Officer - Financial planning and management",
            "Human Resources Manager - Employee relations and recruitment",
            "Software Developer - Design and develop software applications",
            "Marketing Manager - Brand promotion and market analysis",
            "Sales Representative - Customer acquisition and sales",
            "Accountant - Financial record keeping and reporting",
            "Operations Manager - Daily operations and process management",
            "Security Guard - Building and personnel security",
            "Product Manager - Product strategy and roadmap planning",
            "Data Analyst - Data analysis and business intelligence",
            "UX Designer - User experience and interface design",
            "DevOps Engineer - Development operations and infrastructure",
            "Quality Assurance Tester - Software testing and quality control",
            "Business Analyst - Business process analysis and improvement",
            "Customer Support Representative - Customer service and support",
            "Legal Counsel - Legal advice and compliance management",
            "Project Manager - Project planning and execution",
            "IT Support Specialist - Technical support and troubleshooting",
            "Content Writer - Content creation and copywriting",
            "Graphic Designer - Visual design and branding",
            "Finance Manager - Financial analysis and budgeting",
            "Procurement Officer - Vendor management and purchasing",
            "Training Coordinator - Employee training and development",
            "Network Administrator - Network infrastructure management",
            "Database Administrator - Database management and optimization",
            "Social Media Manager - Social media strategy and engagement",
            "Research Analyst - Market research and data analysis",
            "Administrative Assistant - Administrative and clerical support"
        };

        for (int i = 0; i < roleNames.length; i++) {
            EmployeeRole role = new EmployeeRole();
            role.setRoleName(roleNames[i]);
            role.setRoleDescription(roleDescriptions[i]);
            roleService.createEmployeeRole(role);
        }
    }

    private void createEntrances() {

        for (int i = 0; i < ENTRANCE_NAMES.length; i++) {
            String id = "ENTRANCE-" + (1000 + i);
            String name = ENTRANCE_NAMES[i];
            String location = LOCATIONS[i % LOCATIONS.length];
            
            Entrances entrance = new Entrances();
            entrance.setEntranceId(id);
            entrance.setEntranceName(name);
            entrance.setEntranceLocation(location);
            // Randomize mode: 60% STRICT, 40% LENIENT, 10% FULL_ACCESS
            int r = new Random().nextInt(100);
            if (r < 60) {
                entrance.setEntranceMode(EntranceMode.STRICT);
            } else if (r < 90) {
                entrance.setEntranceMode(EntranceMode.LENIENT);
            } else {
                entrance.setEntranceMode(EntranceMode.FULL_ACCESS);
            }
            entranceService.addEntrance(entrance);
        }
    }

    private void createDevices() {
        List<Entrances> entrances = entranceService.findAllEntrances();
        int counter = 1;

        for (Entrances entrance : entrances) {
            Devices entryDevice = new Devices();
            entryDevice.setDeviceId("DEV-E-" + counter);
            entryDevice.setDeviceName(entrance.getEntranceName() + " Entry");
            entryDevice.setDevicePosition(DevicePosition.ENTRY);
            entryDevice.setEntrance(entrance);
            deviceService.registerDevice(entryDevice);

            Devices exitDevice = new Devices();
            exitDevice.setDeviceId("DEV-X-" + counter);
            exitDevice.setDeviceName(entrance.getEntranceName() + " Exit");
            exitDevice.setDevicePosition(DevicePosition.EXIT);
            exitDevice.setEntrance(entrance);
            deviceService.registerDevice(exitDevice);

            counter++;
        }

        allDevices = deviceService.getAllDevices();
    }

    private void createEmployees() {
        List<EmployeeRole> allRoles = roleService.findAllEmployeeRoles(); // Get roles once
        Random rand = new Random();
        
        for (int i = 0; i < 100; i++) {
            String first = random(FIRST_NAMES);
            String last = random(LAST_NAMES);
            String ghanaCard = "GHA-" + (700000000 + i) + "-" + (i % 10);

            Employee emp = new Employee();
            emp.setFirstname(first);
            emp.setLastname(last);
            emp.setGhanaCardNumber(ghanaCard);
            emp.setGender(i % 2 == 0 ? "Male" : "Female");
            emp.setPrimaryPhone("0244" + (100000 + i));
            emp.setSecondaryPhone("0208" + (100000 + i));
            emp.setEmail((first + "." + last + i + "@test.com").toLowerCase());
            emp.setAddress("Test Address " + i);
            emp.setDateOfBirth(LocalDate.of(1990 + (i % 10), 1 + (i % 12), 1 + (i % 28)));

            EmployeeRole randomRole = allRoles.get(rand.nextInt(allRoles.size()));
            emp.setRole(randomRole);


            employeeService.saveEmployee(emp);
            generatedEmployees.add(emp);
        }
    }

    private void assignRolesToEmployees() {
        List<EmployeeRole> allRoles = roleService.findAllEmployeeRoles();
        Random rand = new Random();
        for (Employee emp : generatedEmployees) {
            EmployeeRole randomRole = allRoles.get(rand.nextInt(allRoles.size()));
            emp.setRole(randomRole);
            employeeService.updateEmployee(emp.getGhanaCardNumber(), emp);
        }
    }

    private void linkRolesToEntrances() {
        List<EmployeeRole> roles = roleService.findAllEmployeeRoles();
        List<Entrances> entrances = entranceService.findAllEntrances();
        Random rand = new Random();

        for (EmployeeRole role : roles) {
            Set<Entrances> assigned = new HashSet<>();
            for (Entrances entrance : entrances) {
                if (rand.nextBoolean()) {
                    assigned.add(entrance);

                    // Optional: update inverse side
                    Set<EmployeeRole> allowedRoles = entrance.getAllowedRoles();
                    if (allowedRoles == null) {
                        allowedRoles = new HashSet<>();
                    }
                    allowedRoles.add(role);
                    entrance.setAllowedRoles(allowedRoles);
                }
            }

            // Set the owning side
            role.setAccessibleEntrances(assigned);
            roleService.save(role); //Persist owning side
        }
    }

    private void assignCustomTimeAccess() {
        List<Entrances> allEntrances = entranceService.findAllEntrances();
        Random random = new Random();

        for (Employee emp : generatedEmployees) {
            if (random.nextInt(100) < 50) { // 50% of employees
                try {
                    Entrances entrance = allEntrances.get(random.nextInt(allEntrances.size()));

                    // Get current custom entrances or create new list
                    List<Entrances> currentCustomEntrances = emp.getCustomEntrances() != null
                            ? new ArrayList<>(emp.getCustomEntrances())
                            : new ArrayList<>();

                    // Add entrance if not already present
                    boolean entranceExists = currentCustomEntrances.stream()
                            .anyMatch(e -> e.getEntranceId().equals(entrance.getEntranceId()));

                    if (!entranceExists) {
                        currentCustomEntrances.add(entrance);

                        // Update employee entrances
                        Employee updatedEmployee = employeeService.updateEmployeeEntrances(emp, currentCustomEntrances);

                        // Create time access for the entrance
                        Map<String, LocalTime> startTimes = new HashMap<>();
                        Map<String, LocalTime> endTimes = new HashMap<>();
                        List<String> selectedDays = new ArrayList<>();

                        for (DayOfWeek day : DayOfWeek.values()) {
                            String dayStr = day.name();
                            startTimes.put(dayStr, LocalTime.of(8, 0));
                            endTimes.put(dayStr, LocalTime.of(17, 0));
                            selectedDays.add(dayStr);
                        }

                        timeAccessService.saveOrUpdateCustomTimeAccess(updatedEmployee, entrance, startTimes, endTimes, selectedDays);

                        System.out.println("Assigned entrance " + entrance.getEntranceName() + " to " + emp.getFullName());
                    }

                } catch (Exception e) {
                    System.err.println("Error processing employee " + emp.getGhanaCardNumber() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private void createTimeAccessRules() {
        // TODO: Generate some RoleTimeAccess or CustomTimeAccess rules
    }

    private void createAccessLogs() {
        if (allDevices.isEmpty()) {
            allDevices = deviceService.getAllDevices();
        }

        System.out.println("Total devices available: " + allDevices.size());
        Random random = new Random();

        for (int i = 0; i < 1000; i++) {
            Employee emp = generatedEmployees.get(random.nextInt(generatedEmployees.size()));

            // Pick a random entrance from devices list
            Devices anyDevice = allDevices.get(random.nextInt(allDevices.size()));
            Entrances entrance = anyDevice.getEntrance();

            // Get entry and exit devices for that entrance
            List<Devices> entryDevices = allDevices.stream()
                    .filter(d -> d.getEntrance().equals(entrance) && d.getDevicePosition() == DevicePosition.ENTRY)
                    .toList();
            List<Devices> exitDevices = allDevices.stream()
                    .filter(d -> d.getEntrance().equals(entrance) && d.getDevicePosition() == DevicePosition.EXIT)
                    .toList();

            if (entryDevices.isEmpty() && exitDevices.isEmpty()) {
                continue; // No devices assigned
            }

            int choice = random.nextInt(100); // 0-99

            if (choice < 50) {
                // 50% case: ENTRY + EXIT
                if (!entryDevices.isEmpty() && !exitDevices.isEmpty()) {
                    Devices entryDev = entryDevices.get(random.nextInt(entryDevices.size()));
                    Devices exitDev = exitDevices.get(random.nextInt(exitDevices.size()));

                    LocalDateTime entryTime = randomDateTimeInLast30Days();
                    LocalDateTime exitTime = entryTime.plusMinutes(random.nextInt(120) + 1); // 1–120 min later

                    logAccess(emp, entryDev, entryTime);
                    logAccess(emp, exitDev, exitTime);
                }
            } else if (choice < 75) {
                // 25% case: ENTRY only
                if (!entryDevices.isEmpty()) {
                    logAccess(emp, entryDevices.get(random.nextInt(entryDevices.size())), randomDateTimeInLast30Days());
                }
            } else {
                // 25% case: EXIT only
                if (!exitDevices.isEmpty()) {
                    logAccess(emp, exitDevices.get(random.nextInt(exitDevices.size())), randomDateTimeInLast30Days());
                }
            }
        }
    }

    private void logAccess(Employee emp, Devices device, LocalDateTime time) {
        Random random = new Random();
        boolean granted = random.nextInt(100) < 60; // 60% granted
        String result = granted ? "GRANTED" : "DENIED";

        AccessLog log = new AccessLog();
        log.setEmployee(emp);
        log.setDevice(device);
        log.setTimestamp(time);
        log.setResult(result);
        log.setVerificationTime(0.5 + (random.nextDouble() * 2.0));

        accessLogService.logAccess(log);
    }

    private void assignRoleTimeAccess() {
        List<EmployeeRole> roles = roleService.findAllEmployeeRoles();
        Random rand = new Random();

        for (EmployeeRole role : roles) {
            Set<Entrances> allowedEntrances = role.getAccessibleEntrances();
            if (allowedEntrances == null || allowedEntrances.isEmpty()) {
                continue;
            }

            for (Entrances entrance : allowedEntrances) {
                for (DayOfWeek day : DayOfWeek.values()) {
                    if (rand.nextInt(100) < 30) { // 30% chance to assign rule
                        LocalTime start = LocalTime.of(8, 0);
                        LocalTime end = LocalTime.of(17, 0);

                        Map<String, LocalTime> startTimes = new HashMap<>();
                        Map<String, LocalTime> endTimes = new HashMap<>();
                        startTimes.put(day.name(), start);
                        endTimes.put(day.name(), end);

                        List<String> selectedDays = List.of(day.name());

                        timeAccessService.saveOrUpdateRoleTimeAccess(role, entrance, startTimes, endTimes, selectedDays);
                    }
                }
            }
        }
    }

    private void createEmployeeEntranceStates() {
        List<Entrances> strictEntrances = entranceService.findByMode(EntranceMode.STRICT);
        Random rand = new Random();
        Map<String, Devices> lastUsedDevices = new HashMap<>();

        for (Employee emp : generatedEmployees) {
            for (Entrances entrance : strictEntrances) {
                if (rand.nextBoolean()) {
                    EmployeeEntranceState existing = stateService.findByEmployeeAndEntrance(emp.getId(), entrance.getId());
                    String key = emp.getId() + "-" + entrance.getId();

                    DevicePosition nextPosition = (existing == null)
                            ? DevicePosition.ENTRY
                            : (existing.getCurrentState() == LocationState.INSIDE ? DevicePosition.EXIT : DevicePosition.ENTRY);

                    Devices selectedDevice = null;

                    if (nextPosition == DevicePosition.ENTRY) {
                        List<Devices> entryDevices = entrance.getDevices().stream()
                                .filter(d -> d.getDevicePosition() == DevicePosition.ENTRY && !d.isDeleted())
                                .collect(Collectors.toList());
                        if (!entryDevices.isEmpty()) {
                            selectedDevice = entryDevices.get(rand.nextInt(entryDevices.size()));
                            lastUsedDevices.put(key, selectedDevice);
                        }
                    } else {
                        Devices lastEntryDevice = lastUsedDevices.get(key);
                        if (lastEntryDevice != null) {
                            List<Devices> exitDevices = entrance.getDevices().stream()
                                    .filter(d -> d.getDevicePosition() == DevicePosition.EXIT && !d.isDeleted())
                                    .collect(Collectors.toList());
                            if (!exitDevices.isEmpty()) {
                                // Match based on naming, ID pair logic, or just random for now
                                selectedDevice = exitDevices.get(rand.nextInt(exitDevices.size()));
                            }
                        }
                    }

                    if (selectedDevice != null) {
                        stateService.recordEntryOrExit(emp, entrance, nextPosition, "Seeder", selectedDevice);
                    }
                }
            }
        }
    }

    private String random(String[] array) {
        return array[new Random().nextInt(array.length)];
    }

    private LocalDateTime randomDateTimeInLast30Days() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime start = now.minusDays(30);
        long startEpoch = start.toEpochSecond(java.time.ZoneOffset.UTC);
        long endEpoch = now.toEpochSecond(java.time.ZoneOffset.UTC);
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return LocalDateTime.ofEpochSecond(randomEpoch, 0, java.time.ZoneOffset.UTC);
    }
}
