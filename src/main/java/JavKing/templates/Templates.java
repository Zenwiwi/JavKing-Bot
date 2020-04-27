package JavKing.templates;

public final class Templates {
    final public static class music {
        public static final EmbedTemplate added_to_queue = new EmbedTemplate();
        public static final Template playing_now = new Template(TemplateArgument.ARGS);
        public static final Template skipped_song = new Template(TemplateArgument.SKIP, TemplateArgument.ARGS);
        public static final Template repeat_song = new Template(TemplateArgument.REPEAT, TemplateArgument.ARGS);
        public static final Template shuffle_queue = new Template(TemplateArgument.SHUFFLE, TemplateArgument.ARGS);
        public static final Template paused_queue = new Template(TemplateArgument.PAUSE, TemplateArgument.ARGS);
        public static final Template resumed_queue = new Template(TemplateArgument.RESUME, TemplateArgument.ARGS);
    }

    final public static class command {
        public static final Template check_mark = new Template(TemplateArgument.CHECK, TemplateArgument.ARGS);
        public static final Template blue_check_mark = new Template(TemplateArgument.BLUE_CHECK, TemplateArgument.ARGS);
        public static final Template x_mark = new Template(TemplateArgument.X, TemplateArgument.ARGS);
        public static final Template o_mark = new Template(TemplateArgument.O, TemplateArgument.ARGS);
        public static final Template boom = new Template(TemplateArgument.BOOM, TemplateArgument.ARGS);
        public static final Template triumph = new Template(TemplateArgument.TRIUMPH, TemplateArgument.ARGS);
    }
}
