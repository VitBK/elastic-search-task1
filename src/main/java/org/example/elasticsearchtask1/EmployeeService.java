package org.example.elasticsearchtask1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeService {

    private final RestClient client;
    private final ObjectMapper objectMapper;

    public EmployeeService() {
        RestClientBuilder builder = RestClient.builder(
                new HttpHost("localhost", 9200, "http"));
        this.client = builder.build();
        this.objectMapper = new ObjectMapper();
    }

    public List<Employee> getAllEmployees() throws IOException {
        Request request = new Request("GET", "/employees/_search?size=100");
        return getEmployees(request);
    }

    public Employee getEmployeeById(String id) throws IOException {
        Request request = new Request("GET", "/employees/_doc/" + id);
        Response response = client.performRequest(request);

        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        if (!responseNode.get("found").asBoolean()) {
            return null;
        }

        Employee employee = objectMapper.treeToValue(responseNode.get("_source"), Employee.class);
        employee.setId(responseNode.get("_id").asText());
        return employee;
    }

    public String createEmployee(Employee employee) throws IOException {
        Request request = new Request("POST", "/employees/_doc");
        request.setEntity(new NStringEntity(objectMapper.writeValueAsString(employee), ContentType.APPLICATION_JSON));
        Response response = client.performRequest(request);

        Map<String, Object> responseMap = objectMapper.readValue(response.getEntity().getContent(), HashMap.class);
        return (String) responseMap.get("_id");
    }

    public ResponseEntity<Void> deleteEmployee(String id) throws IOException {
        Request request = new Request("DELETE", "/employees/_doc/" + id);
        client.performRequest(request);
        return ResponseEntity.noContent().build();
    }

    public List<Employee> searchEmployees(String field, String value) throws IOException {
        Request request = new Request("POST", "/employees/_search");
        ObjectNode queryNode = objectMapper.createObjectNode();

        if (field != null && value != null) {

            ObjectNode filter = objectMapper.createObjectNode();
            filter.put(field, value);
            ObjectNode match = objectMapper.createObjectNode();
            match.set("match", filter);
            queryNode.set("query", match);
        }

        request.setEntity(new NStringEntity(queryNode.toString(), ContentType.APPLICATION_JSON));
        return getEmployees(request);
    }

    public Map<String, Object> aggregateEmployees(String metric, String metricField) throws IOException {
        Request request = new Request("POST", "/employees/_search");
        ObjectNode avgAggr = objectMapper.createObjectNode();
        if ("avg".equalsIgnoreCase(metric)) {
            ObjectNode avgNode = objectMapper.createObjectNode();
            avgNode.put("field", metricField);
            ObjectNode agsNode = objectMapper.createObjectNode();
            agsNode.set(metric, avgNode);
            avgAggr.set("average_experience", agsNode);
        } else {
            // Add support for other metric types if needed
            throw new IllegalArgumentException("Unsupported metric type: " + metric);
        }

        ObjectNode queryNode = objectMapper.createObjectNode();
        queryNode.put("size", 0);
        queryNode.set("aggs", avgAggr);
        request.setEntity(new NStringEntity(queryNode.toString(), ContentType.APPLICATION_JSON));

        Response response = client.performRequest(request);
        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());

        Map<String, Object> result = new HashMap<>();
        JsonNode agsResponse = responseNode.get("aggregations");
        if (agsResponse != null) {
            JsonNode avgResponse = agsResponse.get("average_experience");
            result.put(metric, avgResponse.get("value").asDouble());
        }
        return result;
    }

    private List<Employee> getEmployees(Request request) throws IOException {
        Response response = client.performRequest(request);

        JsonNode responseNode = objectMapper.readTree(response.getEntity().getContent());
        List<Employee> employees = new ArrayList<>();
        for (JsonNode hit : responseNode.get("hits").get("hits")) {
            Employee employee = objectMapper.treeToValue(hit.get("_source"), Employee.class);
            employee.setId(hit.get("_id").asText());
            employees.add(employee);
        }
        return employees;
    }
}
