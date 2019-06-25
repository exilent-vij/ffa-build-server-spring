package ffa.exilent.systems.buildserver.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FeaturedBuilds {
    @JsonProperty("versions")
    private ArrayList<FeaturedBuildsInfo> versionInfo;
}
