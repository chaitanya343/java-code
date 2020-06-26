package com.mindex.challenge.controller;

import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class EmployeeController {
    private static final Logger LOG = LoggerFactory.getLogger(EmployeeController.class);

    @Autowired
    private EmployeeService employeeService;

    @PostMapping("/employee")
    public Employee create(@RequestBody Employee employee) {
        LOG.debug("Received employee create request for [{}]", employee);

        return employeeService.create(employee);
    }

    @GetMapping("/employee/{id}")
    public Employee read(@PathVariable String id) {
        LOG.debug("Received employee read request for id [{}]", id);

        return employeeService.read(id);
    }

    @PutMapping("/employee/{id}")
    public Employee update(@PathVariable String id, @RequestBody Employee employee) {
        LOG.debug("Received employee update request for id [{}] and employee [{}]", id, employee);

        employee.setEmployeeId(id);
        return employeeService.update(employee);
    }

    // TASK 1
    /**
     *  REST endpoint to accept an employeeId and return the fully filled out ReportingStructure for the specified employeeId
     *
     * @param id
     * @return
     */
    @GetMapping("/employee/report/{id}")
    public ReportingStructure report(@PathVariable String id) {
        LOG.debug("Received employee report request for id [{}]", id);

        return employeeService.report(id);
    }

    // TASK 2
    /**
     *  REST endpoint to create a Compensation
     *
     * @param compensation
     * @return
     */
    @PostMapping("/employee/compensation")
    public Compensation createCompensation(@RequestBody Compensation compensation) {
        LOG.debug("Received employee compensation create request for [{}]", compensation);

        return employeeService.createCompensation(compensation);
    }

    /**
     * REST endpoint to read the Compensation for the specified employeeId
     *
     * @param id
     * @return
     */
    @GetMapping("/employee/compensation/{id}")
    public Compensation readCompensation(@PathVariable String id) {
        LOG.debug("Received employee compensation read request for id [{}]", id);

        return employeeService.readCompensation(id);
    }
}
