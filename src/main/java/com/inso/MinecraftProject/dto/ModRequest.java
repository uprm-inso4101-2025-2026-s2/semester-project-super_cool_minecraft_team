@Data

public class ModRequest {

    @NotBlank
    private String modId;

    @NotBlank
    private String version;

    private String minecraftVersion;

    private List<Dependency> dependencies;

}
