package ffa.exilent.systems.buildserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import lombok.Data;

@Data
public class ClubVersionInfo {
    @JsonProperty("version_number")
    private String versionNumber;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("branch")
    private String branch;
    @JsonProperty("club")
    private String club;
}
