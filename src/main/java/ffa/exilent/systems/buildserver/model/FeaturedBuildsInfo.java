package ffa.exilent.systems.buildserver.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class FeaturedBuildsInfo {
    @JsonProperty("version_number")
    private String versionNumber;
    @JsonProperty("created_at")
    private Date createdAt;
    @JsonProperty("branch")
    private String branch;
    @JsonProperty("clubs")
    private List<String> clubs;


}
