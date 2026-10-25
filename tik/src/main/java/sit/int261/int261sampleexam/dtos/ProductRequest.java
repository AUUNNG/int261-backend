package sit.int261.int261sampleexam.dtos;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class ProductRequest {

    @NotNull
    @Size(min = 3, max = 30)
    @Pattern(regexp = "[A-Z0-9-]+")
    private String sku;

    @NotBlank
    @Size(min = 1, max = 100)
    private String name;

    @NotNull
    @Positive
    private Integer brandId;

    @NotNull
    @DecimalMin(value = "0.01", inclusive = true)
    @DecimalMax(value = "99999999.99", inclusive = true)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal price;

    @NotNull
    @Min(0)
    @Max(100000)
    private Integer stock;

    @NotNull
    @AllowedValues(value = {2, 4, 6, 8, 12, 16, 24}, message = "must be one of 2, 4, 6, 8, 12, 16, 24")
    private Integer ramGb;

    @NotNull
    @AllowedValues(value = {32, 64, 128, 256, 512, 1024}, message = "must be one of 32, 64, 128, 256, 512, 1024")
    private Integer storageGb;

    @NotEmpty
    @Size(min = 1, max = 40)
    private String color;

    @Size(max = 500)
    private String description;

    @NotNull
    private Boolean active;

    public void setSku(Object sku) {
        this.sku = toTrimmedString(sku, "sku");
    }

    public void setName(Object name) {
        this.name = toTrimmedString(name, "name");
    }

    public void setColor(Object color) {
        this.color = toTrimmedString(color, "color");
    }

    public void setDescription(Object description) {
        if (description != null && !(description instanceof String)) {
            throw new IllegalArgumentException("description must be a string");
        }
        this.description = (String) description;      // description ไม่ต้อง trim ตามโจทย์
    }

    private static String toTrimmedString(Object value, String field) {
        if (value == null) return null;               // ให้ @NotBlank จัดการ
        if (!(value instanceof String s)) {
            throw new IllegalArgumentException(field + " must be a string");
        }
        return s.strip();
    }

    // ==========================================================
    // Custom Constraint Annotation & Validator สำหรับ RAM และ Storage
    // ==========================================================
    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.RUNTIME)
    @Constraint(validatedBy = AllowedValuesValidator.class)
    public @interface AllowedValues {
        String message() default "invalid value";
        Class<?>[] groups() default {};
        Class<? extends Payload>[] payload() default {};
        int[] value();
    }

    public static class AllowedValuesValidator implements ConstraintValidator<AllowedValues, Integer> {
        private Set<Integer> allowed;

        @Override
        public void initialize(AllowedValues annotation) {
            allowed = Arrays.stream(annotation.value()).boxed().collect(Collectors.toSet());
        }

        @Override
        public boolean isValid(Integer value, ConstraintValidatorContext context) {
            return value == null || allowed.contains(value);
        }
    }
}
