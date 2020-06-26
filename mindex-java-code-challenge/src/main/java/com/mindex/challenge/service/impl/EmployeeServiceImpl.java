package com.mindex.challenge.service.impl;

import com.mindex.challenge.dao.CompensationRepository;
import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private static final Logger LOG = LoggerFactory.getLogger(EmployeeServiceImpl.class);

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private CompensationRepository compensationRepository;

    @Override
    public Employee create(Employee employee) {
        LOG.debug("Creating employee [{}]", employee);

        employee.setEmployeeId(UUID.randomUUID().toString());
        employeeRepository.insert(employee);

        return employee;
    }

    @Override
    public Employee read(String id) {
        LOG.debug("Reading employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        return employee;
    }

    @Override
    public Employee update(Employee employee) {
        LOG.debug("Updating employee [{}]", employee);

        return employeeRepository.save(employee);
    }

    @Override
    public ReportingStructure report(String id) {
        LOG.debug("Creating report of employee with id [{}]", id);

        Employee employee = employeeRepository.findByEmployeeId(id);

        if (employee == null) {
            throw new RuntimeException("Invalid employeeId: " + id);
        }

        // Find the number of distinct direct reports under this employee (direct and indirect)
        int numberOfReports = getNoOfDirectReports(employee);

        // Create ReportingStructure for this employee
        return new ReportingStructure(employee, numberOfReports);
    }

    @Override
    public Compensation createCompensation(Compensation compensation) {
        LOG.debug("Creating compensation [{}]", compensation);

        compensationRepository.insert(compensation);

        return compensation;
    }

    @Override
    public Compensation readCompensation(String id) {
        LOG.debug("Reading Compensation with employee id [{}]", id);

        Compensation employeeComp = compensationRepository.findByEmployeeEmployeeId(id);

        if (employeeComp == null) {
            throw new RuntimeException("Invalid employeeId for Compensation: " + id);
        }

        return employeeComp;
    }

    /***
     * Gets the No Of Distinct Direct Reports
     * Helper function for the recursive function countReporteeRecur()
     *
     * @param employee
     * @return
     */
    private int getNoOfDirectReports(Employee employee) {
        Set<String> reporteeIds = new HashSet<>();

        reporteeIds = countReporteeRecur(reporteeIds, employee);

        // To remove the employee himself from the set in case he is added
        reporteeIds.remove(employee.getEmployeeId());

        return reporteeIds.size();
    }

    /**
     * Recursive function to calculate direct reports
     *
     * @param reportedIds
     * @param employee
     * @return
     */
    private Set<String> countReporteeRecur(Set<String> reportedIds, Employee employee) {
        List<Employee> dReports = employee.getDirectReports();
        if (dReports == null) {
            // Base case
            reportedIds.add(employee.getEmployeeId());
        } else {
            // Recursive case
            for (Employee emp : dReports) {
                reportedIds.add(emp.getEmployeeId());
                Employee fullDetailsEmp = employeeRepository.findByEmployeeId(emp.getEmployeeId());
                reportedIds = countReporteeRecur(reportedIds, fullDetailsEmp);
            }
        }
        return reportedIds;
    }

}
