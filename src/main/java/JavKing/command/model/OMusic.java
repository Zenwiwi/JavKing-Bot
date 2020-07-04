package JavKing.command.model;

import JavKing.command.meta.AbstractModel;

import java.lang.reflect.Field;

public class OMusic extends AbstractModel {
    public String uri = "";
    public String author = "";
    public String title = "";
    public long duration = 0;
    public String requestedBy = "";
    public String thumbnail = "";
    public String id = "";

    @Override
    public String toString() {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                System.out.println(field.getName() + ": " + field.get(this));
            } catch (IllegalAccessException e) {
                e.getMessage();
            }
        };
        return null;
    }
}