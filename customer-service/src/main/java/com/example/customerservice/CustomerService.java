package com.example.customerservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class CustomerService {

    public static void main(String[] args) {
        SpringApplication.run(CustomerService.class, args);
    }

    @Configuration
    static class CustomerConfig {

        @Bean
        @LoadBalanced
        // Annotation to mark a RestTemplate bean to be configured to use a LoadBalancerClient which represents a client side load balancer.
        // see LoadBalancerAutoConfiguration
        public RestTemplate restTemplate() {
            return new RestTemplate();
        }

    }

    @RestController
    @Slf4j
    static class CustomerController {

        private static final String TEMPLATE = UriComponentsBuilder.fromUriString("//order-service/orders")
                .queryParam("customerId", "{customerId}").build().toUriString();

        private final RestTemplate restTemplate;
        private final CustomerRepository customerRepository;

        public CustomerController(RestTemplate restTemplate, CustomerRepository customerRepository) {
            this.restTemplate = restTemplate;
            this.customerRepository = customerRepository;
        }

        @GetMapping("/customers/{id}")
        public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
            log.info("getCustomer with id {}", id);
            Customer customer = customerRepository.getCustomer(id);
            if (customer == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Order order = restTemplate.getForObject(TEMPLATE, Order.class, id);
            if (order != null) {
                customer.setOrder(new Order(order.getDetails(), order.getTime()));
            }
            return new ResponseEntity<>(customer, HttpStatus.OK);
        }

    }

    @EnableWebSecurity
    static class WebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.authorizeRequests()
                    .anyRequest().permitAll()
                .and().csrf().disable();
        }
    }

    @Data
    @AllArgsConstructor
    static class Customer {

        Long id;
        String name;
        Order order;

    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class Order {
        String details;
        LocalDateTime time;
    }

    @Component
    static class CustomerRepository {

        private Map<Long, Customer> customers = new HashMap<>();

        public CustomerRepository() {
            customers.put(1L, new Customer(1L, "Paul Molive", null));
            customers.put(2L, new Customer(2L, "Gail Forcewind", null));
            customers.put(3L, new Customer(3L, "Paige Turner", null));
        }

        public Customer getCustomer(Long id) {
            return customers.get(id);
        }
    }

}
