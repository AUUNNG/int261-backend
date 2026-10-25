package sit.int261.int261sampleexam.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.WebRequest;
import sit.int261.int261sampleexam.exceptions.InvalidRequestException;

import java.util.Map;
import java.util.Set;

public class RequestParamCheck {

    public static void allowOnly(WebRequest request, String... allowedNames) {
        Set<String> allowed = Set.of(allowedNames);
        for (Map.Entry<String, String[]> param : request.getParameterMap().entrySet()) {
            if (!allowed.contains(param.getKey())) {
                throw new InvalidRequestException("Unknown parameter: " + param.getKey());
            }
            if (param.getValue().length > 1) {
                throw new InvalidRequestException("Duplicate parameter: " + param.getKey());
            }
        }
    }

//    // แปลง query param เป็นเลขบวก: ว่าง/ตัวอักษร/overflow/0/ติดลบ = 400
//    public static Long toPositiveLong(String value, String name) {
//        if (value == null) return null;               // ไม่ได้ส่งมา
//        try {
//            long result = Long.parseLong(value);
//            if (result < 1) throw new InvalidRequestException(name + " must be a positive integer");
//            return result;
//        } catch (NumberFormatException e) {
//            throw new InvalidRequestException(name + " must be a positive integer");
//        }
//    }
}