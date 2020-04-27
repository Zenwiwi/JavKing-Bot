package JavKing.templates;

public class ErrorTemplate {
    public static String formatFull(String vars, Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(vars).append("```java\n");
        for (StackTraceElement element : e.getStackTrace()) sb.append(element).append("\n");
        sb.append("\n```");
        return sb.toString();
    }
}
