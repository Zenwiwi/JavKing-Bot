package JavKing.command.model;

import JavKing.command.meta.AbstractModel;

import java.util.ArrayList;
import java.util.List;

public class OPlaylist extends AbstractModel {
    public List<OMusic> items = new ArrayList<>();
    public String uri = "";
    public String title = "";
    public int length = 0;
    public String thumbnail = "";
    public String id = "";
    public String requestedBy = "";
}
