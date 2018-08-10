package com.example.orderservice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class OrderService {

    public static void main(String[] args) {
        SpringApplication.run(OrderService.class, args);
    }

    @Data
    @AllArgsConstructor
    static class Order {
        String details;
        LocalDateTime time;
    }

    @RestController
    @Slf4j
    static class OrderController {

        private final OrderRepository orderRepository;

        public OrderController(OrderRepository orderRepository) {
            this.orderRepository = orderRepository;
        }

        @GetMapping("/orders")
        public ResponseEntity<Order> getCustomerOrders(@RequestParam("customerId") Long customerId) {
           log.info("getCustomerOrders with customerId {}", customerId);
           Order order = orderRepository.getOrder(customerId);
           if (order == null) {
               return new ResponseEntity(HttpStatus.NOT_FOUND);
           }
           return new ResponseEntity(order, HttpStatus.OK);
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

    @Component
    static class OrderRepository {

        private Map<Long, Order> orders = new HashMap<>();

        public OrderRepository() {
            orders.put(1L, new Order("Grilled Chicken Sandwich", LocalDateTime.now().minusHours(1)));
            orders.put(2L, new Order("Roasted Duck Breast", LocalDateTime.now().minusMinutes(30)));
        }

        public Order getOrder(Long customerId) {
            return orders.get(customerId);
        }
    }



}


