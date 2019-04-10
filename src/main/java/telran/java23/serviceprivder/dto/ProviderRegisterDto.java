package telran.java23.serviceprivder.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import telran.java23.serviceprivder.model.Address;
import telran.java23.serviceprivder.model.Service;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProviderRegisterDto {
    String email;
    String password;
    String profession;
    String firstName;
    String lastName;
    Long telephone;
    Long whatsApp;
    Address address;
    Set<Service> services;
    Boolean isActive;

}
