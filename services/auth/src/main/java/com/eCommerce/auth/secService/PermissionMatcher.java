package com.eCommerce.auth.secService;

import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class PermissionMatcher {
    // Cache để không phải compile regex mỗi lần
    private final Map<String, Pattern> cache = new ConcurrentHashMap<>();

    public boolean matches(String template, String actualPath) {
        Pattern pattern = cache.computeIfAbsent(template, this::buildPatternFromTemplate);
        return pattern.matcher(normalize(actualPath)).matches();
    }

    private Pattern buildPatternFromTemplate(String template) {
        // Chuẩn hóa path: bỏ // dư, bỏ / cuối
        String normalized = normalize(template);

        // Escape các kí tự regex đặc biệt (., ?, +, ...)
        String regex = Pattern.quote(normalized);

        // Bỏ quote quanh {...} để thay thành [^/]+
        // Pattern.quote tạo thành \Q...\E, nên ta xử lý đơn giản hơn:
        // Cách nhẹ nhàng: xử lý trên normalized trước khi quote:

        normalized = normalized.replaceAll("\\{[^/]+}", "[^/]+");

        // Bây giờ normalized đã chứa [^/]+ là regex, nên ta không quote toàn bộ mà chỉ:
        // - escape các ký tự đặc biệt trừ dấu [] và ^ / +
        // Để đỡ phức tạp, ta làm cách đơn giản hơn:

        String escaped = normalized
                .replace(".", "\\.")
                .replace("?", "\\?")
                .replace("+", "\\+"); // v.v... nếu cần thêm

        String finalRegex = "^" + escaped + "$";
        return Pattern.compile(finalRegex);
    }

    private String normalize(String path) {
        if (path == null || path.isBlank()) return "/";
        // bỏ bớt // thành /
        String normalized = path.replaceAll("/+", "/");
        // bỏ / ở cuối (trừ "/")
        if (normalized.length() > 1 && normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
