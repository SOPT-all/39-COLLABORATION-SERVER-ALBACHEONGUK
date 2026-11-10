package org.sopt.alba.common.controller;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** CD 배포 후 헬스체크용 */
@Hidden
@RestController
public class HealthController {

    @GetMapping("/api/v1/health")
    public String health() {
        return "OK";
    }
}