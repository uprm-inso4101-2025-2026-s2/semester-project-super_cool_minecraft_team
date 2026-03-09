package com.inso.MinecraftProject;

import org.springframework.http.*;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Set;

@RestController
public class RedirectController {

    // Allowed domains (you can add more if needed)
    private static final Set<String> ALLOWLIST = Set.of(
            "modrinth.com",
            "www.modrinth.com",
            "curseforge.com",
            "www.curseforge.com"
    );

    @GetMapping("/r")
    public ResponseEntity<String> redirect(@RequestParam(name = "u", required = false) String u) {
        if (!StringUtils.hasText(u)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing 'u' parameter.");
        }

        final URI target;
        try {
            target = new URI(u);
        } catch (URISyntaxException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Malformed URL.");
        }

        String scheme = target.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Only http/https URLs are allowed.");
        }

        String host = target.getHost();
        if (!StringUtils.hasText(host)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("URL must include a host.");
        }

        String normalizedHost = IDN.toASCII(host).toLowerCase(Locale.ROOT);

        boolean allowed = ALLOWLIST.stream().anyMatch(d ->
                normalizedHost.equals(d) || normalizedHost.endsWith("." + d)
        );

        if (!allowed) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Blocked redirect: domain not allowlisted.");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(target);
        return new ResponseEntity<>(headers, HttpStatus.FOUND); // 302 redirect
    }
}
