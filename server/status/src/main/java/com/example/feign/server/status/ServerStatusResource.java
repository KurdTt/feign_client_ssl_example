package com.example.feign.server.status;

import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ServerStatusResource {

    private static final UUID RESOURCE_ID = UUID.randomUUID();
    private static int COUNTER = 0;

    @GetMapping("/status")
    public ResponseEntity<UUID> getTestMessage() {
        if (COUNTER % 3 == 0) {
            log.info("Wywołanie {} status service", ++COUNTER);
            return ResponseEntity.ok(RESOURCE_ID);
        } else {
            log.info("Wywołanie {} status service z błędem", ++COUNTER);
            return ResponseEntity.internalServerError().body(null);
        }
    }

}