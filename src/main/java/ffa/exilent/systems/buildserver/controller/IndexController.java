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
import java.util.*;
import javax.servlet.http.HttpServletResponse;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class IndexController {

    private final String STAGING_BRANCH = "xi_staging";
    private final int maxJsonObjectsInFile = 10;
    private final String stagingBuildType = "staging";
    private final String featureBuildType = "feature";
    private final String clubBuildType = "club";

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


    @GetMapping({"/featured-builds", "/"})
    public ModelAndView getFeaturedBuilds(@RequestParam("page_number") Optional<Integer> pageNumberOptional) {
        int pageNumber;
        ObjectMapper mapper = new ObjectMapper();
        FeaturedBuilds version;
        int totalPages = this.getTotalPages("builds/current_featured_build_file_number.txt", featureBuildType);
        pageNumber = this.getPageNumber(totalPages, pageNumberOptional);
        try {
            version = mapper.readValue(new File("builds/featured_builds_" + pageNumber + ".json"), FeaturedBuilds.class);
        } catch (IOException exception) {
            version = new FeaturedBuilds();
            version.setVersionInfo(new ArrayList<>());
        }
        if (version.getVersionInfo().size() < maxJsonObjectsInFile) {
            if (pageNumber > 1) {
                FeaturedBuilds extraVersions;
                pageNumber = pageNumber - 1;
                try {
                    extraVersions = mapper.readValue(new File("builds/featured_builds_" + pageNumber + ".json"), FeaturedBuilds.class);
                } catch (IOException exception) {
                    extraVersions = new FeaturedBuilds();
                    extraVersions.setVersionInfo(new ArrayList<>());
                }
                for (FeaturedBuildsInfo featuredBuildsInfo : extraVersions.getVersionInfo()) {
                    version.getVersionInfo().add(featuredBuildsInfo);
                }
            }
        }
        ModelAndView mav = new ModelAndView("featured_builds");
        mav.addObject("builds", version.getVersionInfo());
        mav.addObject("pages", totalPages);
        mav.addObject("build", "featured");
        return mav;
    }


    @GetMapping("/staging-builds")
    public ModelAndView getStagingBuilds(@RequestParam("page_number") Optional<Integer> pageNumberOptional) {
        int pageNumber;
        ObjectMapper mapper = new ObjectMapper();
        FeaturedBuilds version;
        int totalPages = this.getTotalPages("builds/current_staging_build_file_number.txt", stagingBuildType);
        pageNumber = this.getPageNumber(totalPages, pageNumberOptional);
        try {
            version = mapper.readValue(new File("builds/staging_builds_" + pageNumber + ".json"), FeaturedBuilds.class);
        } catch (IOException exception) {
            version = new FeaturedBuilds();
            version.setVersionInfo(new ArrayList());
        }
        if (version.getVersionInfo().size() < maxJsonObjectsInFile) {
            if (pageNumber > 1) {
                FeaturedBuilds extraVersions;
                pageNumber = pageNumber - 1;
                try {
                    extraVersions = mapper.readValue(new File("builds/staging_builds_" + pageNumber + ".json"), FeaturedBuilds.class);
                } catch (IOException exception) {
                    extraVersions = new FeaturedBuilds();
                    extraVersions.setVersionInfo(new ArrayList<>());
                }
                for (FeaturedBuildsInfo featuredBuildsInfo : extraVersions.getVersionInfo()) {
                    version.getVersionInfo().add(featuredBuildsInfo);
                }
            }
        }

        ModelAndView mav = new ModelAndView("featured_builds");
        mav.addObject("builds", version.getVersionInfo());
        mav.addObject("pages", totalPages);
        mav.addObject("build", "staging");
        return mav;
    }

    @GetMapping("/club-builds/{club}")
    public ModelAndView getClubBuilds(@PathVariable("club") String club, @RequestParam("page_number") Optional<Integer> pageNumberOptional) {
        int pageNumber;
        ObjectMapper mapper = new ObjectMapper();
        ClubVersions version;
        int totalPages = this.getTotalPages("builds/" + club + "_build_file_number.txt", clubBuildType, club);
        pageNumber = this.getPageNumber(totalPages, pageNumberOptional);
        try {
            version = mapper.readValue(new File("builds/" + club + "_builds_" + pageNumber + ".json"), ClubVersions.class);
        } catch (IOException exception) {
            version = new ClubVersions();
        }

        if (version.getClubVersionInfos().size() < maxJsonObjectsInFile) {
            if (pageNumber > 1) {
                ClubVersions extraVersions;
                pageNumber = pageNumber - 1;
                try {
                    extraVersions = mapper.readValue(new File("builds/" + club + "_builds_" + pageNumber + ".json"), ClubVersions.class);
                } catch (IOException exception) {
                    extraVersions = new ClubVersions();
                    extraVersions.setClubVersionInfos(new ArrayList<>());
                }
                for (ClubVersionInfo clubVersionInfo : extraVersions.getClubVersionInfos()) {
                    version.getClubVersionInfos().add(clubVersionInfo);
                }
            }
        }


        ModelAndView mav = new ModelAndView("club_builds");
        mav.addObject("builds", version.getClubVersionInfos());
        mav.addObject("pages", totalPages);
        mav.addObject("club", club);
        return mav;
    }

    private FeaturedBuilds getLastPaginatedJsonObject(String filePath) {
        FeaturedBuilds lastRecords;
        ObjectMapper mapper = new ObjectMapper();
        try {
            lastRecords = mapper.readValue(new File(filePath), FeaturedBuilds.class);
        } catch (IOException exception) {
            lastRecords = new FeaturedBuilds();
        }
        return lastRecords;
    }

    private int getPageNumber(int totalPages, Optional<Integer> pageNumberOptional) {
        int pageNumber;
        if (!pageNumberOptional.isPresent()) {
            pageNumber = totalPages;
        } else {
            pageNumber = totalPages - pageNumberOptional.get() + 1;
        }
        return pageNumber;
    }

    private int getPagination(String buildNumberFile) {
        String totalPages;
        try {
            File file = new File(buildNumberFile);
            BufferedReader br = new BufferedReader(new FileReader(file));
            totalPages = br.readLine();
        } catch (IOException exception) {
            totalPages = "1";
        }
        return Integer.parseInt(totalPages);
    }

    private int getTotalPages(String filePath, String type) {
        int totalPages = this.getPagination(filePath);
        FeaturedBuilds lastRecords;
        switch (type) {
            case featureBuildType:
                lastRecords = this.getLastPaginatedJsonObject("builds/featured_builds_" + totalPages + ".json");
                break;
            case stagingBuildType:
                lastRecords = this.getLastPaginatedJsonObject("builds/staging_builds_" + totalPages + ".json");
                break;
            default:
                lastRecords = this.getLastPaginatedJsonObject("builds/staging_builds_" + totalPages + ".json");
        }
        try{
            if (lastRecords.getVersionInfo().size() < maxJsonObjectsInFile && totalPages > 1) {
                totalPages = totalPages - 1;
            }
        }
        catch (Exception exception){
            totalPages=1;
        }

        return totalPages;
    }

    private int getTotalPages(String filePath, String type, String club) {
        int totalPages = this.getPagination(filePath);
        FeaturedBuilds lastRecords = this.getLastPaginatedJsonObject("builds/" + club + "_builds_" + totalPages + ".json");
        if (lastRecords.getVersionInfo().size() < maxJsonObjectsInFile && totalPages > 1) {
            totalPages = totalPages - 1;
        }
        return totalPages;
    }


    private void addClubVersions(FeaturedBuildsInfo versionInfo) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            for (String clubName : versionInfo.getClubs()) {
                String fileNumber;
                try {
                    File file = new File("builds/" + clubName + "_build_file_number.txt");
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    fileNumber = br.readLine();
                } catch (IOException exception) {
                    fileNumber = "1";
                    FileWriter fileWriter = new FileWriter("builds/" + clubName + "_build_file_number.txt");
                    fileWriter.write(fileNumber);
                    fileWriter.close();
                }

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
                clubVersionInfo.setClub(clubName);
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

    private void createNewStagingBuildJson(String fileNumber) {
        fileNumber = Integer.toString(Integer.parseInt(fileNumber) + 1);
        FeaturedBuilds featuredBuilds = new FeaturedBuilds();
        featuredBuilds.setVersionInfo(new ArrayList<>());
        ObjectMapper mapper = new ObjectMapper();
        try {
            FileWriter fileWriter = new FileWriter("builds/current_staging_build_file_number.txt");
            fileWriter.write(fileNumber);
            fileWriter.close();
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
            FileWriter fw = new FileWriter("builds/current_featured_build_file_number.txt");
            fw.write(fileNumber);
            fw.close();
            mapper.writeValue(new File("builds/featured_builds_" + fileNumber + ".json"), featuredBuilds);
        } catch (IOException exception) {

        }
    }
}
