package com.openclaw.observer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtlpResponse {
    
    private boolean success;
    private String type;
    private String receivedAt;
    private Integer size;
    private String error;
    
    public static OtlpResponse success(String type, int size) {
        return OtlpResponse.builder()
                .success(true)
                .type(type)
                .receivedAt(LocalDateTime.now().toString())
                .size(size)
                .build();
    }
    
    public static OtlpResponse error(String errorMessage) {
        return OtlpResponse.builder()
                .success(false)
                .error(errorMessage)
                .build();
    }
    
    public ResponseEntity<OtlpResponse> toResponseEntity() {
        if (success) {
            return ResponseEntity.ok(this);
        } else {
            return ResponseEntity.status(500).body(this);
        }
    }
}
