package com.example.customerservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

// Spring Cloud integrates Ribbon to provide a load balanced http client when using Feign.

@SpringBootApplication
@EnableFeignClients
public class CustomerServiceFeign {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceFeign.class, args);
    }

    @RestController
    @Slf4j
    static class CustomerController {

        private final OrderServiceClient orderServiceClient;
        private final CustomerRepository customerRepository;

        public CustomerController(OrderServiceClient orderServiceClient, CustomerRepository customerRepository) {
            this.orderServiceClient = orderServiceClient;
            this.customerRepository = customerRepository;
        }

        @GetMapping("/customers/{id}")
        public ResponseEntity<Customer> getCustomer(@PathVariable Long id) {
            log.info("getCustomer with id {}", id);
            Customer customer = customerRepository.getCustomer(id);
            if (customer == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Order order = orderServiceClient.getOrder(id);
            if (order != null) {
                customer.setOrder(order.getDetails());
            }
            return new ResponseEntity<>(customer, HttpStatus.OK);
        }

    }

    @FeignClient("order-service")
    interface OrderServiceClient {

        @GetMapping("/orders")
        Order getOrder(@RequestParam("customerId") Long id);
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
        String order;

    }

    @Data
    @NoArgsConstructor
    static class Order {
        String details;
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
