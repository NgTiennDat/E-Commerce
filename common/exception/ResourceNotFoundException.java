import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final ResponseCode responseCode;

    public ResourceNotFoundException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
    }
}
