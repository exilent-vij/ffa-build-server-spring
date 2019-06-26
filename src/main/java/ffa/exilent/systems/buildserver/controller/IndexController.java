package ffa.exilent.systems.buildserver.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import ffa.exilent.systems.buildserver.model.ClubVersionInfo;
import ffa.exilent.systems.buildserver.model.ClubVersions;
import ffa.exilent.systems.buildserver.model.FeaturedBuilds;
import ffa.exilent.systems.buildserver.model.FeaturedBuildsInfo;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
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
    private final int maxJsonObjectsInFile = 4;

    @GetMapping("/ios/{version}/{club}/manifest")
    public ResponseEntity<Resource> downloadManifest(@PathVariable("version") String version, @PathVariable("club") String club, HttpServletResponse response) {
        String fileName = "builds/" + version + "/" + club + "/ios/" + "manifest.plist";
        ByteArrayResource resource = this.getFileResource(fileName);
        String contentType = "application/xml";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + "manifest.plist" + "\"")
                .body(resource);
    }

    @GetMapping("/ios/{version}/{club}/ipa")
    public ResponseEntity<Resource> downloadIPA(@PathVariable("version") String version, @PathVariable("club") String club) {
        String fileName = "builds/" + version + "/" + club + "/ios/" + club + ".ipa";
        ByteArrayResource resource = this.getFileResource(fileName);
        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + club + ".ipa" + "\"")
                .body(resource);
//
    }

    @GetMapping("/android/{version}/{club}/apk")
    public ResponseEntity downloadApk(@PathVariable("version") String version, @PathVariable("club") String club) {
        String fileName = "builds/" + version + "/" + club + "/android/app-" + club + "-release.apk";
        Resource resource = this.getFileResource(fileName);
        String contentType = "application/octet-stream";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + club + ".apk" + "\"")
                .body(resource);
    }

    private ByteArrayResource getFileResource(String fileName) {
        File file = new File(fileName);
        Path path = Paths.get(file.getAbsolutePath());
        ByteArrayResource resource;
        try {
            resource = new ByteArrayResource(Files.readAllBytes(path));
        } catch (IOException exception) {
            return null;
        }
        return resource;
    }

    @PostMapping("/newbuild")
    public ResponseEntity newVersionUpdated(@RequestBody FeaturedBuildsInfo newVersion) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map version;
            String buildJsonFile;
            String fileNumber;
            if (newVersion.getBranch().equalsIgnoreCase(STAGING_BRANCH)) {
                try {
                    File file = new File("builds/current_staging_build_file_number.txt");
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    fileNumber = br.readLine();
                } catch (IOException exception) {
                    fileNumber = "1";
                    FileWriter fileWriter = new FileWriter("builds/current_staging_build_file_number.txt");
                    fileWriter.write(fileNumber);
                    fileWriter.close();
                }
                buildJsonFile = "builds/staging_builds_" + fileNumber + ".json";
                try {
                    mapper.readValue(new File(buildJsonFile), Map.class);
                } catch (IOException exception) {
                    FeaturedBuilds featuredBuilds = new FeaturedBuilds();
                    featuredBuilds.setVersionInfo(new ArrayList<>());
                    mapper.writeValue(new File(buildJsonFile), featuredBuilds);
                }
                buildJsonFile = "builds/staging_builds_" + fileNumber + ".json";
                version = mapper.readValue(new File(buildJsonFile), Map.class);
            } else {
                try {
                    File file = new File("builds/current_featured_build_file_number.txt");
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    fileNumber = br.readLine();
                } catch (IOException exception) {
                    fileNumber = "1";
                    FileWriter fileWriter = new FileWriter("builds/current_featured_build_file_number.txt");
                    fileWriter.write(fileNumber);
                    fileWriter.close();
                }

                buildJsonFile = "builds/featured_builds_" + fileNumber + ".json";
                try {
                    mapper.readValue(new File(buildJsonFile), Map.class);
                } catch (IOException exception) {
                    FeaturedBuilds featuredBuilds = new FeaturedBuilds();
                    featuredBuilds.setVersionInfo(new ArrayList<>());
                    mapper.writeValue(new File(buildJsonFile), featuredBuilds);
                }
                version = mapper.readValue(new File(buildJsonFile), Map.class);
            }
            ArrayList<FeaturedBuildsInfo> featuredBuildsInfo = (ArrayList<FeaturedBuildsInfo>) version.get("versions");
            Instant nowUtc = Instant.now();
            ZoneId australia = ZoneId.of("Australia/Sydney");
            ZonedDateTime nowAsiaSingapore = ZonedDateTime.ofInstant(nowUtc, australia);
            newVersion.setCreatedAt(Date.from(nowAsiaSingapore.toInstant()));
            featuredBuildsInfo.add(newVersion);
            if (featuredBuildsInfo.size() >= maxJsonObjectsInFile) {
                if (newVersion.getBranch().equalsIgnoreCase(STAGING_BRANCH)) {
                    this.createNewStagingBuildJson(fileNumber);
                } else {
                    this.createNewFeaturedBuildJson(fileNumber);
                }
            }
            mapper.writeValue(new File(buildJsonFile), version);
            this.addClubVersions(newVersion);
            return new ResponseEntity(HttpStatus.OK);
        } catch (IOException exception) {
            return new ResponseEntity(HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping({"/featured-bills", "/"})
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
            for (String clubName : versionInfo.getClubs()) {
                File file = new File("builds/" + clubName + "_build_file_number.txt");
                BufferedReader br = new BufferedReader(new FileReader(file));
                String fileNumber = br.readLine();
                ClubVersions versions;
                try {
                    versions = mapper.readValue(new File("builds/" + clubName + "_builds_" + fileNumber + ".json"), ClubVersions.class);
                } catch (IOException exception) {
                    versions = new ClubVersions();
                    versions.setClubVersionInfos(new ArrayList<>());
                }
                List<ClubVersionInfo> clubVersionInfos = versions.getClubVersionInfos();
                ClubVersionInfo clubVersionInfo = new ClubVersionInfo();
                clubVersionInfo.setBranch(versionInfo.getBranch());
                clubVersionInfo.setCreatedAt(versionInfo.getCreatedAt());
                clubVersionInfo.setVersionNumber(versionInfo.getVersionNumber());
                clubVersionInfos.add(clubVersionInfo);
                mapper.writeValue(new File("builds/" + clubName + "_builds_" + fileNumber + ".json"), versions);
                if (clubVersionInfos.size() >= maxJsonObjectsInFile) {
                    this.createNewClubsBuildJson(clubName, fileNumber);
                }
            }
        } catch (IOException exception) {

            System.out.println(exception.getMessage());
        }
    }

//    private List<ClubVersionInfo> getClubVersions(String clubName, ClubVersions versions) {
//
//        List<ClubVersionInfo> versionInfos = null;
//        if (clubName.equalsIgnoreCase("adl")) {
//            versionInfos = versions.getAdlVersions();
//        } else if (clubName.equalsIgnoreCase("bri")) {
//            versionInfos = versions.getBriVersions();
//        } else if (clubName.equalsIgnoreCase("mcy")) {
//            versionInfos = versions.getMcyVersions();
//        } else if (clubName.equalsIgnoreCase("ccm")) {
//            versionInfos = versions.getCcmVersions();
//        } else if (clubName.equalsIgnoreCase("mvc")) {
//            versionInfos = versions.getMvcVersions();
//        } else if (clubName.equalsIgnoreCase("new")) {
//            versionInfos = versions.getNewVersions();
//        } else if (clubName.equalsIgnoreCase("per")) {
//            versionInfos = versions.getPerVersions();
//        } else if (clubName.equalsIgnoreCase("syd")) {
//            versionInfos = versions.getSydVersions();
//        } else if (clubName.equalsIgnoreCase("wel")) {
//            versionInfos = versions.getWelVersions();
//        } else if (clubName.equalsIgnoreCase("wsw")) {
//            versionInfos = versions.getWswVersions();
//        } else if (clubName.equalsIgnoreCase("cbr")) {
//            versionInfos = versions.getCbrVersions();
//        } else {
//            versionInfos = versions.getMvcVersions();
//        }
//        if (versionInfos == null) {
//            versionInfos = new ArrayList<>();
//        }
//        return versionInfos;
//    }

    private void createNewStagingBuildJson(String fileNumber) {
        fileNumber = Integer.toString(Integer.parseInt(fileNumber) + 1);
        FeaturedBuilds featuredBuilds = new FeaturedBuilds();
        featuredBuilds.setVersionInfo(new ArrayList<>());
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File("builds/staging_builds_" + fileNumber + ".json"), featuredBuilds);
        } catch (IOException exception) {

        }
    }

    private void createNewClubsBuildJson(String clubName, String fileNumber) {
        fileNumber = Integer.toString(Integer.parseInt(fileNumber) + 1);
        ClubVersions clubVersions = new ClubVersions();
        clubVersions.setClubVersionInfos(new ArrayList<>());
        ObjectMapper mapper = new ObjectMapper();
        try {
            FileWriter fw = new FileWriter("builds/" + clubName + "_build_file_number.txt", false);
            fw.write(fileNumber);
            fw.close();
            mapper.writeValue(new File("builds/" + clubName + "_builds_" + fileNumber + ".json"), clubVersions);
        } catch (IOException exception) {

        }
    }

    private void createNewFeaturedBuildJson(String fileNumber) {
        fileNumber = Integer.toString(Integer.parseInt(fileNumber) + 1);
        FeaturedBuilds featuredBuilds = new FeaturedBuilds();
        featuredBuilds.setVersionInfo(new ArrayList<>());
        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(new File("builds/featured_builds_" + fileNumber + ".json"), featuredBuilds);
        } catch (IOException exception) {

        }
    }
}
