@Data

@AllArgsConstructor
public class ValidationResponse {

    private List<String> missingDependencies;

    private List<String> circularDependencies;

}
