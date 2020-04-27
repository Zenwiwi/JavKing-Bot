package JavKing.templates;

public enum TemplateArgument {
    ARG("arg1", "First input argument", e -> e.arg[0] != null ? e.arg[0] : ""),
    ARG2("arg2", "Second argument", e -> e.arg[1] != null ? e.arg[1] : ""),
    ARG3("arg3", "Third argument", e -> e.arg[2] != null ? e.arg[2] : ""),
    ARGS("allargs", "All input arguments", e -> e.args != null ? e.args : ""),

    CHECK("check", "Check mark", e -> "✅"),
    BLUE_CHECK("blue_check", "Blue check mark", e -> "☑"),

    X("x", "X", e -> "❌"),
    O("o", "O", e -> "⭕"),
    BOOM("boom", "Boom", e -> "\uD83D\uDCA5"),

    TRIUMPH("triumph", "Triumph", e -> "\uD83D\uDE24"),

    REPEAT("repeat", "Repeat", e -> "\uD83D\uDD01"),
    SKIP("skip", "Skip", e -> "⏭"),
    SHUFFLE("shuffle", "Shuffle", e -> "\uD83D\uDD00"),
    PAUSE("pause", "Pause", e -> "⏸️"),
    RESUME("resume", "Resume", e -> "⏯️");

    private final String pattern;
    private final TemplateParser parser;
    private final String description;

    TemplateArgument(String pattern, String description, TemplateParser parser) {
        this.pattern = pattern;
        this.parser = parser;
        this.description = description;
    }

    public String getPattern() {
        return pattern;
    }

    public String parse(TemplateVariables vars) {
        return parser.apply(vars);
    }

    public String getDescription() {
        return description;
    }
}
