package ffa.exilent.systems.buildserver.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class IndexController {
    @GetMapping("/")
    public String hello() {
        return "index";
    }

    @GetMapping("/ios/{club_name}/{version}")
    public ResponseEntity<Resource> download(@PathVariable("club_name") String clubName ,@PathVariable("version") String version) {
        Resource resource = new ClassPathResource("test.txt");

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
