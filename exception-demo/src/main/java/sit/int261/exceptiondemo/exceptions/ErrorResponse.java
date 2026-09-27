package sit.int261.exceptiondemo.exceptions;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({"code", "message", "status", "violations"})
public class ErrorResponse {
    private String code;
    private String message;
    private int status;
    private List<Violation> violations;

    public ErrorResponse(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Violation {
        private String field;
        private String message;
    }

    public void addViolation(String field, String message) {
        if (this.violations == null) {
            this.violations = new ArrayList<>();
        }
        this.violations.add(new Violation(field, message));
    }
}
