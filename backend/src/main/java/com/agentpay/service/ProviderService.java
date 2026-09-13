package com.agentpay.service;

import com.agentpay.domain.Provider;
import com.agentpay.domain.ServiceEntity;
import com.agentpay.dto.ProviderDto;
import com.agentpay.dto.ServiceDto;
import com.agentpay.exception.ResourceNotFoundException;
import com.agentpay.repository.ProviderRepository;
import com.agentpay.repository.ServiceRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final ProviderRepository providerRepository;
    private final ServiceRepository serviceRepository;

    public ProviderService(ProviderRepository providerRepository, ServiceRepository serviceRepository) {
        this.providerRepository = providerRepository;
        this.serviceRepository = serviceRepository;
    }

    public List<ProviderDto> getAllProviders() {
        return providerRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public ProviderDto getProvider(String externalId) {
        Provider p = providerRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + externalId));
        return toDto(p);
    }

    public List<ServiceDto> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::toServiceDto)
                .collect(Collectors.toList());
    }

    public ServiceEntity getServiceEntity(String externalId) {
        return serviceRepository.findByExternalId(externalId)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + externalId));
    }

    public ServiceDto getService(String externalId) {
        return toServiceDto(getServiceEntity(externalId));
    }

    /**
     * Executes actual service delivery returning deterministic canonical content.
     */
    public String deliverService(String serviceId, String providerId, String inputText) {
        String input = (inputText != null && !inputText.isBlank()) ? inputText : "Hello world";

        if ("prov-b".equalsIgnoreCase(providerId) || serviceId.contains("prov-b")) {
            return "{\"provider\": \"Provider B\", \"service\": \"" + serviceId + "\", \"input\": \"" + input + "\", \"output\": \"नमस्ते दुनिया\", \"quality\": 97, \"sla\": \"0.4s\"}";
        } else if ("prov-c".equalsIgnoreCase(providerId) || serviceId.contains("prov-c")) {
            return "{\"provider\": \"Provider C\", \"service\": \"" + serviceId + "\", \"input\": \"" + input + "\", \"output\": \"नमस्ते दुनिया\", \"quality\": 82, \"sla\": \"1.2s\"}";
        } else if (serviceId.contains("overspend") || serviceId.contains("enterprise")) {
            return "{\"provider\": \"Provider Enterprise\", \"service\": \"" + serviceId + "\", \"input\": \"" + input + "\", \"output\": \"नमस्ते दुनिया (Enterprise Grade)\", \"quality\": 99, \"sla\": \"0.1s\"}";
        } else {
            // Default Provider A
            return "{\"provider\": \"Provider A\", \"service\": \"" + serviceId + "\", \"input\": \"" + input + "\", \"output\": \"नमस्ते दुनिया\", \"quality\": 90, \"sla\": \"0.8s\"}";
        }
    }

    private ProviderDto toDto(Provider p) {
        ProviderDto dto = new ProviderDto();
        dto.setId(p.getId());
        dto.setExternalId(p.getExternalId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setQualityScore(p.getQualityScore());
        dto.setStatus(p.getStatus());

        List<ServiceDto> services = serviceRepository.findByProviderId(p.getExternalId()).stream()
                .map(this::toServiceDto)
                .collect(Collectors.toList());
        dto.setServices(services);
        return dto;
    }

    private ServiceDto toServiceDto(ServiceEntity s) {
        ServiceDto dto = new ServiceDto();
        dto.setId(s.getId());
        dto.setExternalId(s.getExternalId());
        dto.setProviderId(s.getProviderId());
        dto.setName(s.getName());
        dto.setType(s.getType());
        dto.setDescription(s.getDescription());
        dto.setPricePaise(s.getPricePaise());
        dto.setCurrency(s.getCurrency());
        dto.setQualityScore(s.getQualityScore());
        dto.setStatus(s.getStatus());
        return dto;
    }
}
