package org.example.elasticsearchtask1;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private final EmployeeService employeeService;

    @Autowired
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() throws IOException {
        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(@PathVariable("id") String id) throws IOException {
        Employee response = employeeService.getEmployeeById(id);
        return response == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<String> createEmployee(@RequestBody Employee employee) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(employee));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable("id") String id) throws IOException {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<Employee>> searchEmployees(@RequestParam(required = false) String field,
                                                          @RequestParam(required = false) String value) throws IOException {
        return ResponseEntity.ok(employeeService.searchEmployees(field, value));
    }

    @GetMapping("/aggregate")
    public ResponseEntity<Map<String, Object>> aggregateEmployees(@RequestParam String metric,
                                                                  @RequestParam String metricField) throws IOException {
        return ResponseEntity.ok(employeeService.aggregateEmployees(metric, metricField));
    }
}
