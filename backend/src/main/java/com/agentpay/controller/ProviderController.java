package com.agentpay.controller;

import com.agentpay.dto.ProviderDto;
import com.agentpay.dto.ServiceDto;
import com.agentpay.service.ProviderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProviderController {

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    @GetMapping("/providers")
    public ResponseEntity<List<ProviderDto>> getAllProviders() {
        return ResponseEntity.ok(providerService.getAllProviders());
    }

    @GetMapping("/providers/{id}")
    public ResponseEntity<ProviderDto> getProviderById(@PathVariable String id) {
        return ResponseEntity.ok(providerService.getProvider(id));
    }

    @GetMapping("/providers/{id}/services")
    public ResponseEntity<List<ServiceDto>> getServicesByProvider(@PathVariable String id) {
        ProviderDto provider = providerService.getProvider(id);
        return ResponseEntity.ok(provider.getServices());
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceDto>> getAllServices() {
        return ResponseEntity.ok(providerService.getAllServices());
    }

    @GetMapping("/services/{id}")
    public ResponseEntity<ServiceDto> getServiceById(@PathVariable String id) {
        return ResponseEntity.ok(providerService.getService(id));
    }
}
