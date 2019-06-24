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

    @GetMapping("/ios/{version}/{club}/manifest")
    public ResponseEntity<Resource> downloadManifest(@PathVariable("version") String version,@PathVariable("club") String club) {
        Resource resource = new ClassPathResource("builds/"+version+"/"+club+"/ios/manifest.plist");

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
    @GetMapping("/ios/{version}/{club}/ipa")
    public ResponseEntity<Resource> downloadIPA(@PathVariable("version") String version,@PathVariable("club") String club) {
        Resource resource = new ClassPathResource("builds/"+version+"/"+club+"/ios/"+club+".ipa");

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
    @GetMapping("/android/{version}/{club}/apk")
    public ResponseEntity<Resource> downloadApk(@PathVariable("version") String version,@PathVariable("club") String club) {
        Resource resource = new ClassPathResource("builds/android/"+version+"/"+club+"/"+club+"");

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
