package org.apereo.cas.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by anbug on 2018. 1. 16..
 */
@RestController
public class CustomController {
    @GetMapping("/health_check.html")
    public ResponseEntity<Void> healthCheck() {
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
