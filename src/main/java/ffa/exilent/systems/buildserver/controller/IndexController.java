package ffa.exilent.systems.buildserver.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexController {
    @GetMapping("/")
    public String hello() {
        return "index";
    }

    @GetMapping("/ios/manifest")
    public ResponseEntity<Resource> downloadManifest() {
        Resource resource = new ClassPathResource("builds/ios/manifest.plist");

        // Try to determine file's content type
        String contentType = null;


        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @GetMapping("/ios/ipa")
    public ResponseEntity<Resource> downloadIPA() {
        Resource resource = new ClassPathResource("builds/ios/syd.ipa");

        // Try to determine file's content type
        String contentType = null;


        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
    @GetMapping("/android/apk")
    public ResponseEntity<Resource> downloadApk() {
        Resource resource = new ClassPathResource("builds/android/adl.apk");

        // Try to determine file's content type
        String contentType = null;


        // Fallback to the default content type if type could not be determined
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

}
