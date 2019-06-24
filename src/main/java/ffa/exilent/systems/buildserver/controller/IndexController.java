package ffa.exilent.systems.buildserver.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
    public ResponseEntity downloadManifest(@PathVariable("version") String version, @PathVariable("club") String club) {
        String fileName = "/builds/" + version + "/" + club + "/ios/manifest.plist";
        File file = new File(fileName);
        FileInputStream fileInputStream = this.getFileContent(file);
        byte fileContent[] = null;
        try {
            fileInputStream.read(fileContent);
        } catch (IOException exception) {

        }
        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(fileContent);
    }

    @GetMapping("/ios/{version}/{club}/ipa")
    public ResponseEntity downloadIPA(@PathVariable("version") String version, @PathVariable("club") String club) {
        String fileName = "/builds/" + version + "/" + club + "/ios/"+club+".ipa";
        File file = new File(fileName);
        FileInputStream fileInputStream = this.getFileContent(file);
        byte fileContent[] = null;
        try {
            fileInputStream.read(fileContent);
        } catch (IOException exception) {

        }
        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(fileContent);
    }

    @GetMapping("/android/{version}/{club}/apk")
    public ResponseEntity downloadApk(@PathVariable("version") String version, @PathVariable("club") String club) {
        String fileName = "/builds/" + version + "/" + club + "/android/app-" + club + "-release.apk";
        System.out.println("File Path is "+fileName);
        File file = new File(fileName);
        FileInputStream fileInputStream = this.getFileContent(file);
        byte[] fileContent = new byte[(int) file.length()];
        try {
            fileInputStream.read(fileContent);
        } catch (IOException exception) {
            System.out.println("Error");
        }

        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .body(fileContent);
    }

    private FileInputStream getFileContent(File file) {
        FileInputStream fileInputStream = null;
        byte fileContent[] = null;
        try {
            fileInputStream = new FileInputStream(file);
            fileContent = new byte[(int) file.length()];
            fileInputStream.read(fileContent);
            return fileInputStream;
        } catch (IOException exception) {
            return null;
        }
    }
}
