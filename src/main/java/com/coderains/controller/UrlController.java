package com.coderains.controller;

import java.io.IOException;
import java.util.NoSuchElementException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.coderains.model.URL;
import com.coderains.model.URLRequest;
import com.coderains.services.UrlService;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

@AllArgsConstructor
@RestController
public class UrlController {

    private UrlService service;

    @PostMapping("/shorten")
    public ResponseEntity<Object> shortenUrl(@RequestBody URLRequest urlRequest) {
        var shortUrl = service.shortenUrl(urlRequest.Url());
        return ResponseEntity.ok(shortUrl);
    }

    @PostMapping("/update")
    public ResponseEntity<Object> updateShortUrl(@RequestBody(required = false) URL body) {
        System.out.println(body.toString());
        var response = service.validateURL(body.getOriginalUrl());
        if (response instanceof String) {
            response = service.updateShortUrl(body.getShortUrl(), (String) response);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortUrl}")
    public void redirectToOriginalUrl(HttpServletResponse response, @PathVariable String shortUrl) {
        try {
            String originalUrl = service.getOriginalUrl(shortUrl);
            response.sendRedirect(originalUrl);
        } catch (NoSuchElementException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "URL not found", e);
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Could not redirect to the original URL", e);
        }
    }

    @PostMapping("/update-expiry")
    public ResponseEntity<Boolean> updateExpiry(@RequestParam String shortUrl, @RequestParam int daysToAdd) {
        boolean updated = service.updateExpiry(shortUrl, daysToAdd);
        return ResponseEntity.ok(updated);
    }
}
