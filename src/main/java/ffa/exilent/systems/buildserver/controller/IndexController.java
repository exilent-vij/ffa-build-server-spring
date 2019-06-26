package ffa.exilent.systems.buildserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ffa.exilent.systems.buildserver.model.ClubVersionInfo;
import ffa.exilent.systems.buildserver.model.ClubVersions;
import ffa.exilent.systems.buildserver.model.FeaturedBuilds;
import ffa.exilent.systems.buildserver.model.FeaturedBuildsInfo;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final String STAGING_BRANCH = "xi_staging";
    private final String MASTER_BRANCH = "xi_master";

    @GetMapping("/ios/{version}/{club}/manifest")
    public ResponseEntity downloadManifest(@PathVariable("version") String version, @PathVariable("club") String club) {
        String fileName = "/builds/" + version + "/" + club + "/ios/manifest.plist";
        File file = new File(fileName);
        FileInputStream fileInputStream = this.getFileContent(file);
        byte[] fileContent = new byte[(int) file.length()];
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
        String fileName = "/builds/" + version + "/" + club + "/ios/" + club + ".ipa";
        File file = new File(fileName);
        FileInputStream fileInputStream = this.getFileContent(file);
        byte[] fileContent = new byte[(int) file.length()];
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
        FileInputStream fileInputStream;
        byte fileContent[];
        try {
            fileInputStream = new FileInputStream(file);
            fileContent = new byte[(int) file.length()];
            fileInputStream.read(fileContent);
            return fileInputStream;
        } catch (IOException exception) {
            return null;
        }
    }

    @PostMapping("/newbuild")
    public ResponseEntity newVersionUpdated(@RequestBody FeaturedBuildsInfo newVersion) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map version;
            String buildJsonFile;
            if (newVersion.getBranch().equalsIgnoreCase(STAGING_BRANCH)) {
                buildJsonFile = "builds/staging_builds.json";

                version = mapper.readValue(new File(buildJsonFile), Map.class);
            } else {
                buildJsonFile = "builds/featured_builds.json";
                version = mapper.readValue(new File(buildJsonFile), Map.class);
            }
            ArrayList<FeaturedBuildsInfo> featuredBuildsInfo = (ArrayList<FeaturedBuildsInfo>) version.get("versions");
            Instant nowUtc = Instant.now();
            ZoneId australia = ZoneId.of("Australia/Sydney");
            ZonedDateTime nowAsiaSingapore = ZonedDateTime.ofInstant(nowUtc, australia);
            newVersion.setCreatedAt(Date.from(nowAsiaSingapore.toInstant()));
            featuredBuildsInfo.add(newVersion);
            mapper.writeValue(new File(buildJsonFile), version);
            this.addClubVersions(newVersion);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException exception) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping({"/featured-bills","/"})
    public ModelAndView getFeaturedBuilds() {
        ObjectMapper mapper = new ObjectMapper();
        FeaturedBuilds version;
        try {
            version = mapper.readValue(new File("builds/featured_builds.json"), FeaturedBuilds.class);
        } catch (IOException exception) {
            version = new FeaturedBuilds();
        }

        ModelAndView mav = new ModelAndView("featured_builds");
        mav.addObject("builds", version.getVersionInfo());
        return mav;
    }

    @GetMapping("/staging-bills")
    public ModelAndView getStagingBuilds() {
        ObjectMapper mapper = new ObjectMapper();
        FeaturedBuilds version;
        try {
            version = mapper.readValue(new File("builds/staging_builds.json"), FeaturedBuilds.class);
        } catch (IOException exception) {
            version = new FeaturedBuilds();
        }

        ModelAndView mav = new ModelAndView("featured_builds");
        mav.addObject("builds", version.getVersionInfo());
        return mav;
    }

    private void addClubVersions(FeaturedBuildsInfo versionInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ClubVersions versions = mapper.readValue(new File("builds/clubs.json"), ClubVersions.class);
            for (String clubName : versionInfo.getClubs()) {
                List<ClubVersionInfo> clubVersionInfos = this.getClubVersions(clubName, versions);

                ClubVersionInfo clubVersionInfo = new ClubVersionInfo();
                clubVersionInfo.setBranch(versionInfo.getBranch());
                clubVersionInfo.setCreatedAt(versionInfo.getCreatedAt());
                clubVersionInfo.setVersionNumber(versionInfo.getVersionNumber());
                clubVersionInfos.add(clubVersionInfo);
            }

            mapper.writeValue(new File("builds/clubs.json"), versions);
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    private List<ClubVersionInfo> getClubVersions(String clubName, ClubVersions versions) {

        List<ClubVersionInfo> versionInfos = null;
        if (clubName.equalsIgnoreCase("adl")) {
            versionInfos = versions.getAdlVersions();
        } else if (clubName.equalsIgnoreCase("bri")) {
            versionInfos = versions.getBriVersions();
        } else if (clubName.equalsIgnoreCase("mcy")) {
            versionInfos = versions.getMcyVersions();
        } else if (clubName.equalsIgnoreCase("ccm")) {
            versionInfos = versions.getCcmVersions();
        } else if (clubName.equalsIgnoreCase("mvc")) {
            versionInfos = versions.getMvcVersions();
        } else if (clubName.equalsIgnoreCase("new")) {
            versionInfos = versions.getNewVersions();
        } else if (clubName.equalsIgnoreCase("per")) {
            versionInfos = versions.getPerVersions();
        } else if (clubName.equalsIgnoreCase("syd")) {
            versionInfos = versions.getSydVersions();
        } else if (clubName.equalsIgnoreCase("wel")) {
            versionInfos = versions.getWelVersions();
        } else if (clubName.equalsIgnoreCase("wsw")) {
            versionInfos = versions.getWswVersions();
        } else if (clubName.equalsIgnoreCase("cbr")) {
            versionInfos = versions.getCbrVersions();
        } else {
            versionInfos = versions.getMvcVersions();
        }
        if (versionInfos == null) {
            versionInfos = new ArrayList<>();
        }
        return versionInfos;
    }
}
