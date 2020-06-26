package com.mindex.challenge;

import com.mindex.challenge.dao.EmployeeRepository;
import com.mindex.challenge.data.Compensation;
import com.mindex.challenge.data.Employee;
import com.mindex.challenge.data.ReportingStructure;
import com.mindex.challenge.service.EmployeeService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ChallengeApplicationTests {

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private EmployeeService employeeService;

	@Test
	public void contextLoads() {
		// TASK 1
		Employee employee = employeeRepository.findByEmployeeId("16a596ae-edd3-4847-99fe-c4518e82c86f");
		ReportingStructure employeeReport = employeeService.report(employee.getEmployeeId());

		assertNotNull(employeeReport);
		assertEquals(4, employeeReport.getNumberOfReports());

		// TASK 2
		Compensation employeeComp = new Compensation(employee, 1000000.0, LocalDate.of(2020, 06, 03));
		Compensation savedEmployeeComp = employeeService.createCompensation(employeeComp);
		Compensation retrievedEmployeeComp = employeeService.readCompensation(employee.getEmployeeId());

		assertNotNull(savedEmployeeComp);
		assertNotNull(retrievedEmployeeComp);
		assertEquals(retrievedEmployeeComp.getEmployee().getEmployeeId(), savedEmployeeComp.getEmployee().getEmployeeId());
		assertEquals(retrievedEmployeeComp.getEmployee().getFirstName(), savedEmployeeComp.getEmployee().getFirstName());
		assertEquals(retrievedEmployeeComp.getEmployee().getLastName(), savedEmployeeComp.getEmployee().getLastName());
		assertEquals(retrievedEmployeeComp.getEmployee().getPosition(), savedEmployeeComp.getEmployee().getPosition());
		assertEquals(retrievedEmployeeComp.getEmployee().getDepartment(), savedEmployeeComp.getEmployee().getDepartment());
		assertEquals(retrievedEmployeeComp.getSalary(), savedEmployeeComp.getSalary());
		assertEquals(retrievedEmployeeComp.getEffectiveDate(), savedEmployeeComp.getEffectiveDate());
	}

}
