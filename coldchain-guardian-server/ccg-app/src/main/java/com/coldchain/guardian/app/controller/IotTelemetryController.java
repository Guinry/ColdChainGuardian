package com.coldchain.guardian.app.controller;

import com.coldchain.guardian.app.service.IotTelemetryService;
import com.coldchain.guardian.common.api.ApiResponse;
import com.coldchain.guardian.contract.dto.iot.IotTelemetryRequest;
import com.coldchain.guardian.contract.dto.iot.IotTelemetryResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/iot")
public class IotTelemetryController {

    @Autowired
    private IotTelemetryService iotTelemetryService;

    @PostMapping("/telemetry")
    public ApiResponse<IotTelemetryResponse> receiveTelemetry(@Valid @RequestBody IotTelemetryRequest request) {
        return ApiResponse.success(iotTelemetryService.receiveTelemetry(request));
    }
}
