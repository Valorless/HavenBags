package valorless.havenbags.datamodels;

import java.util.List;

public class Filter {
    public String key;
    public String displayname;
    public List<String> entries;
    public String guiIcon;
    public List<String> guiLore = List.of(
            "&fItems:"
    );
    public String lineFormat = "&7 ⏵ %s";
    public String andMore = "&7... and %s more";
    public int loreLimit = 10;
    public boolean guiShow = true;


    public Filter(String key, String displayname, List<String> entries) {
        this.key = key;
        this.displayname = displayname;
        this.entries = entries;
        this.guiIcon = entries.isEmpty() ? "NAME_TAG" : entries.getFirst();
    }

    public Filter(String key, String displayname, List<String> entries, List<String> guiLore, String lineFormat) {
        this.key = key;
        this.displayname = displayname;
        this.entries = entries;
        this.guiIcon = entries.isEmpty() ? "NAME_TAG" : entries.getFirst();
        this.guiLore = guiLore;
        this.lineFormat = lineFormat;
    }
}
