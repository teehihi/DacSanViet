package com.dacsanviet.controller;

import com.dacsanviet.dao.AddressDao;
import com.dacsanviet.dto.CreateAddressRequest;
import com.dacsanviet.dto.UpdateAddressRequest;
import com.dacsanviet.model.Address;
import com.dacsanviet.model.User;
import com.dacsanviet.repository.AddressRepository;
import com.dacsanviet.repository.UserRepository;
import com.dacsanviet.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * REST Controller for address management
 */
@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    
    @Autowired
    private AddressRepository addressRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all addresses for current user
     */
    @GetMapping
    public ResponseEntity<List<AddressDao>> getUserAddresses(Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            List<Address> addresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
            List<AddressDao> addressDaos = addresses.stream()
                .map(this::convertToDao)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(addressDaos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Get address by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<AddressDao> getAddress(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Verify user owns this address
            if (!address.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            
            return ResponseEntity.ok(convertToDao(address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Create new address
     */
    @PostMapping
    public ResponseEntity<AddressDao> createAddress(@Valid @RequestBody CreateAddressRequest request, 
                                                   Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            // If this is set as default, clear other default addresses
            if (request.getIsDefault() != null && request.getIsDefault()) {
                addressRepository.clearDefaultFlagForUser(userId);
            }
            
            Address address = new Address();
            address.setFullName(request.getFullName());
            address.setPhoneNumber(request.getPhoneNumber());
            address.setAddressLine1(request.getAddressLine1());
            address.setAddressLine2(request.getAddressLine2());
            address.setCity(request.getCity());
            address.setProvince(request.getProvince());
            address.setPostalCode(request.getPostalCode());
            address.setCountry(request.getCountry() != null ? request.getCountry() : "Vietnam");
            address.setIsDefault(request.getIsDefault() != null ? request.getIsDefault() : false);
            address.setUser(user);
            
            address = addressRepository.save(address);
            
            return ResponseEntity.ok(convertToDao(address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Update address
     */
    @PutMapping("/{id}")
    public ResponseEntity<AddressDao> updateAddress(@PathVariable Long id, 
                                                   @Valid @RequestBody UpdateAddressRequest request,
                                                   Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Verify user owns this address
            if (!address.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            
            // If this is set as default, clear other default addresses
            if (request.getIsDefault() != null && request.getIsDefault()) {
                addressRepository.clearDefaultFlagForUser(userId);
            }
            
            // Update fields
            if (request.getFullName() != null) address.setFullName(request.getFullName());
            if (request.getPhoneNumber() != null) address.setPhoneNumber(request.getPhoneNumber());
            if (request.getAddressLine1() != null) address.setAddressLine1(request.getAddressLine1());
            if (request.getAddressLine2() != null) address.setAddressLine2(request.getAddressLine2());
            if (request.getCity() != null) address.setCity(request.getCity());
            if (request.getProvince() != null) address.setProvince(request.getProvince());
            if (request.getPostalCode() != null) address.setPostalCode(request.getPostalCode());
            if (request.getCountry() != null) address.setCountry(request.getCountry());
            if (request.getIsDefault() != null) address.setIsDefault(request.getIsDefault());
            
            address = addressRepository.save(address);
            
            return ResponseEntity.ok(convertToDao(address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Delete address
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Verify user owns this address
            if (!address.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            
            // Don't allow deleting default address if it's the only one
            if (address.getIsDefault()) {
                long addressCount = addressRepository.countByUserId(userId);
                if (addressCount > 1) {
                    // Set another address as default
                    List<Address> otherAddresses = addressRepository.findByUserIdOrderByIsDefaultDescCreatedAtDesc(userId);
                    for (Address otherAddr : otherAddresses) {
                        if (!otherAddr.getId().equals(id)) {
                            otherAddr.setIsDefault(true);
                            addressRepository.save(otherAddr);
                            break;
                        }
                    }
                }
            }
            
            addressRepository.delete(address);
            
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Set address as default
     */
    @PostMapping("/{id}/set-default")
    public ResponseEntity<AddressDao> setDefaultAddress(@PathVariable Long id, Authentication authentication) {
        try {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            Long userId = userPrincipal.getId();
            
            Address address = addressRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Address not found"));
            
            // Verify user owns this address
            if (!address.getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).build();
            }
            
            // Set this address as default and clear others
            addressRepository.setDefaultAddress(userId, id);
            
            // Reload address to get updated state
            address = addressRepository.findById(id).orElse(address);
            
            return ResponseEntity.ok(convertToDao(address));
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Convert Address entity to DAO
     */
    private AddressDao convertToDao(Address address) {
        AddressDao dao = new AddressDao();
        dao.setId(address.getId());
        dao.setFullName(address.getFullName());
        dao.setPhoneNumber(address.getPhoneNumber());
        dao.setAddressLine1(address.getAddressLine1());
        dao.setAddressLine2(address.getAddressLine2());
        dao.setCity(address.getCity());
        dao.setProvince(address.getProvince());
        dao.setPostalCode(address.getPostalCode());
        dao.setCountry(address.getCountry());
        dao.setIsDefault(address.getIsDefault());
        dao.setUserId(address.getUser().getId());
        dao.setCreatedAt(address.getCreatedAt());
        dao.setUpdatedAt(address.getUpdatedAt());
        return dao;
    }
}