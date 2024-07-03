package com.coderains.services;

import java.net.URI;
import java.net.URLConnection;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

import com.coderains.model.URL;
import com.coderains.repository.URLRepository;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service
public class UrlService {

    private URLRepository repository;

    public Object shortenUrl(String urlRequest) {
        var result = validateURL(urlRequest);
        if (!(result instanceof String)) {
            return result;
        }
        String shortUrl = generateShortUrl();
        URL urlMapping = new URL();
        urlMapping.setShortUrl(shortUrl);
        urlMapping.setOriginalUrl(urlRequest);
        urlMapping.setExpiryDate(LocalDateTime.now().plusMonths(10));
        repository.save(urlMapping);
        return urlMapping;
    }


    // Will return url String or Invlid URL object
    public Object validateURL(String urlRequest) {
        if (urlRequest.startsWith("http://") || urlRequest.startsWith("https://")) {
            if (!isUrlAvailable(urlRequest)) {
                return invalidURL();
            }
        } else {
            String httpUrl = "http://" + urlRequest;
            String httpsUrl = "https://" + urlRequest;
            if (isUrlAvailable(httpsUrl)) {
                return httpsUrl;
            } else if (isUrlAvailable(httpUrl)) {
                return httpUrl;
            } else {
                return invalidURL();
            }
        }
        return urlRequest;
    }

    private Object invalidURL() {
        record errorRestponse(String message, int status) {
        }
        return new errorRestponse("Invalid URL", 400);
    }

    private boolean isUrlAvailable(String url) {
        try {
            java.net.URL urlObject = new URI(url).toURL();
            URLConnection connection = urlObject.openConnection();
            connection.setConnectTimeout(5000); // Timeout after 5 seconds
            connection.connect();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getOriginalUrl(String shortUrl) {
        URL urlMapping = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new NoSuchElementException("URL not found"));
        if (urlMapping.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new NoSuchElementException("URL has expired");
        }
        return urlMapping.getOriginalUrl();
    }

    public URL updateShortUrl(String shortUrl, String newOriginalUrl) {
        URL urlMapping = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new NoSuchElementException("URL not found"));
        urlMapping.setOriginalUrl(newOriginalUrl);
        repository.save(urlMapping);
        return urlMapping;
    }

    public boolean updateExpiry(String shortUrl, int daysToAdd) {
        URL urlMapping = repository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new NoSuchElementException("URL not found"));
        urlMapping.setExpiryDate(urlMapping.getExpiryDate().plusDays(daysToAdd));
        repository.save(urlMapping);
        return true;
    }

    private String generateShortUrl() {
        // Implement a URL shortening algorithm, e.g., Base62 encoding of a sequence or
        // a UUID.
        // Ensure the generated URL is unique.
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder shortUrl = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int randomIndex = (int) (Math.random() * chars.length());
            shortUrl.append(chars.charAt(randomIndex));
        }
        // Ensure uniqueness
        while (repository.findByShortUrl(shortUrl.toString()).isPresent()) {
            shortUrl.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return shortUrl.toString();
    }
}
