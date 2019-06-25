package ffa.exilent.systems.buildserver.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ClubVersions {
    @JsonProperty("adl")
    private List<ClubVersionInfo> adlVersions;
    @JsonProperty("bri")
    private List<ClubVersionInfo> briVersions;
    @JsonProperty("ccm")
    private List<ClubVersionInfo> ccmVersions;
    @JsonProperty("mcy")
    private List<ClubVersionInfo> mcyVersions;
    @JsonProperty("mvc")
    private List<ClubVersionInfo> mvcVersions;
    @JsonProperty("new")
    private List<ClubVersionInfo> newVersions;
    @JsonProperty("per")
    private List<ClubVersionInfo> perVersions;
    @JsonProperty("syd")
    private List<ClubVersionInfo> sydVersions;
    @JsonProperty("wel")
    private List<ClubVersionInfo> welVersions;
    @JsonProperty("wsw")
    private List<ClubVersionInfo> wswVersions;
    @JsonProperty("cbr")
    private List<ClubVersionInfo> cbrVersions;
}
