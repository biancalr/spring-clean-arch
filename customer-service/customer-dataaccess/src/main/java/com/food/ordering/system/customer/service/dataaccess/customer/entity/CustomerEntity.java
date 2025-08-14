package com.food.ordering.system.customer.service.dataaccess.customer.entity;

import com.food.ordering.system.domain.valueobject.CustomerStatus;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "customers")
@Entity
public class CustomerEntity {
    @Id
    private UUID id;
    private String username;
    private String firstName;
    private String lastName;
    private ZonedDateTime createdAt;
    private CustomerStatus customerStatus;
}
