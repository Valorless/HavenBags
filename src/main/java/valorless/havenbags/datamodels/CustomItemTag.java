package valorless.havenbags.datamodels;

import org.bukkit.Material;

import java.util.List;

public class CustomItemTag {

    public final String tag;
    public final List<String> items;

    public CustomItemTag(String tag, List<String> items) {
        this.tag = tag;
        this.items = items;
    }

}
