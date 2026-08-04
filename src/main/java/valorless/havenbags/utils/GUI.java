package valorless.havenbags.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.nexomc.nexo.api.NexoItems;
import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import valorless.havenbags.Lang;
import valorless.havenbags.datamodels.Placeholder;
import valorless.havenbags.enums.GUIAction;
import valorless.havenbags.persistentdatacontainer.PDC;
import valorless.valorlessutils.Server;
import valorless.valorlessutils.items.ItemUtils;
import valorless.valorlessutils.utils.Utils;

public class GUI {

    static final String PREV_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWM1YThhYThhNGMwMzYwMGEyYjVhNGViNmJlYjUxZDU5MDI2MGIwOTVlZTFjZGFhOTc2YjA5YmRmZTU2NjFjNiJ9fX0=";
    static final String NEXT_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFiOTVhODc1MWFlYWEzYzY3MWE4ZTkwYjgzZGU3NmEwMjA0ZjFiZTY1NzUyYWMzMWJlMmY5OGZlYjY0YmY3ZiJ9fX0=";
    static final String RETURN_TEXTURE = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTY5NjFhZDFmNWM3NmU5NzM1OGM0NDRmZTBlODNhMzk1NjRlNmI0ODEwOTE3MDk4NGE4NGVjYTVkY2NkNDI0In19fQ==";

    public static Inventory createPage(Player player, String title, int page, List<ItemStack> items, int rows) {
        int totalPages;

        rows = Utils.Clamp(rows, 2, 6);

        Inventory inventory = Bukkit.createInventory(player, 9 * rows, title);
        rows -= 1;

        totalPages = (int) Math.ceil((double) items.size() / (9 * rows));

        // Calculate start and end index for items on this page
        int startIndex = (page - 1) * 9 * rows;
        int endIndex = Math.min(startIndex + (9 * rows), items.size());

        // Add items to the inventory
        for (int i = startIndex; i < endIndex; i++) {
            if(items.get(i) == null) continue;
            inventory.addItem(items.get(i));
        }

        // Add navigation buttons to the bottom row
        int bottomRowStartSlot = (rows) * 9;
        int prevPageSlot = bottomRowStartSlot + 3;
        int indicatorSlot = bottomRowStartSlot + 4;
        int nextPageSlot = bottomRowStartSlot + 5;
        int goBackSlot = bottomRowStartSlot + 8;

        // Add "Previous" button
        if (page > 1) {
            inventory.setItem(prevPageSlot, createButton(GUIAction.PREV_PAGE, PREV_TEXTURE));
        }

        // Add "Go Back" button
        inventory.setItem(goBackSlot, createButton(GUIAction.RETURN, RETURN_TEXTURE));

        // Add "Next" button
        if (page < totalPages) {
            inventory.setItem(nextPageSlot, createButton(GUIAction.NEXT_PAGE, NEXT_TEXTURE));
        }

        inventory.setItem(indicatorSlot, createPageButton(page));


        return inventory;
    }

    private static ItemStack createButton(GUIAction action, String buttonTexture) {
        ItemStack button = HeadCreator.itemFromBase64(buttonTexture);
        //ItemStack button = new ItemStack(Material.DIRT);
        ItemMeta meta = button.getItemMeta();
        if(action.equals(GUIAction.NEXT_PAGE)) {
            meta.setDisplayName(Lang.get("next-page"));
        }else if(action.equals(GUIAction.PREV_PAGE)) {
            meta.setDisplayName(Lang.get("prev-page"));
        }else if(action.equals(GUIAction.RETURN)) {
            meta.setDisplayName(Lang.get("return"));
            List<String> r_lore = new ArrayList<String>();
            for(String line : Lang.lang.getStringList("return-lore")) {
                r_lore.add(Lang.parse(line, null));
            }
            //r_lore.add("§7Go back.");
            meta.setLore(r_lore);
        }

        button.setItemMeta(meta);

        // Store GUI action in NBT
        PDC.setString(button, "bag-action", action.toString());

        return button;
    }

    private static ItemStack createPageButton(int page) {
        // PageNumberTextures only contains 31 numbers textures.
        // Beyond this, set the texture to the page 0 texture.
        ItemStack button = page > 31 ? HeadCreator.itemFromBase64(PageNumberTextures.getFirst()) :
                HeadCreator.itemFromBase64(PageNumberTextures.get(page));

        ItemMeta meta = button.getItemMeta();
        meta.setDisplayName(Lang.parse(Lang.get("page").replace("%page%", String.valueOf(page)), null));
        button.setItemMeta(meta);

        // Store GUI action in NBT
        PDC.setString(button, "bag-action", GUIAction.NONE.toString());

        return button;
    }

    public final static List<String> PageNumberTextures = new ArrayList<>() {
        private static final long serialVersionUID = 1L; {
            /*00*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2VmN2YzOWY3Y2JmZThhYjkzOGNkOWQxYmEyYzJkNzhjYWQ1ODcxZDBmM2FkZTEzYjk0ZDYzNzNiYTdjMWI0ZCJ9fX0=");
            /*01*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWZhZGUwYTgxNzIxYjNjMmY0ZWQyODI4Y2VjMDUyNDNkMTA3ZDIyN2Q0ZDE5NDcwNDAyOTE4NTM4ZTM2N2JjZiJ9fX0=");
            /*02*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Q2NjdhNjhhZjY1NWFlNmZmNjAxMDUxM2YxNDUzNTc5OWZjMzU3Y2UwODkxMDUxMDQ0YzQ5NWU3M2NlNmE1OCJ9fX0=");
            /*03*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODlkMDZkMWQ3MDhjM2I1ZWVkMTU4MDg4OWEyNjMzMTQyOTIyYWVhMGI4ZTQ5MmVhMmIzNjc1MGIyNGUwZThkYyJ9fX0=");
            /*04*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFiYmIwNmY5ZmIwMTg5NTNmYzU4NDdmN2E2NzI3YmY0OGMzMWYyOWE0ZDg0Nzc3YzU2ZmZjNWZlMzlkYjI3NCJ9fX0=");
            /*05*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjVjMmIzNWUxMThmODQ4MTgyMDhiMGRkNjRiMTNjOWJiZDg3MDk0MDZhYjkxNTk2ZjNlYmVjODE3NzU4ODM0YyJ9fX0=");
            /*06*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZkZmI3OWRkM2Q2ODNlODZlNjk0YzQ2MjY2OWZhYzA3OTRjNTg4NWUwNjIyZjRmYmE0YWE4ZGQzMzFhNzY4OCJ9fX0=");
            /*07*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmU2MGY4ODk1MTI4NGQxZDhkNzE5NWY5MzdkZDZhYjI3YzQ5YWI5OWJkOWUyN2VlZDQ3MmVhNjkyOWM3NjMyMCJ9fX0=");
            /*08*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjVmZDYzOGNlMDYzMjY0OGU3MWYzMmJiYTAzY2NmNTRjMjRhYzNkMTRiZThiODYyNmI1Y2M2MzY2OTQwZWRmOCJ9fX0=");
            /*09*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWQyMGI1MmE1M2MzZmQ1NTkzZTUxOTQyY2FjMWQ1NzkyOGQ0YzdiNzMxNTlmNzQ5MjI0MWFlM2RlOGVjM2U4NCJ9fX0=");
            /*10*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmIwYzQ0ZDA0MGYwYjllMzM3ZmMwNGU4MGJlMmQ1NmYzYjllNzEwZjU0MGE1ODcwMzBjM2Q2ZmQyYzhiMjFmNiJ9fX0=");
            /*11*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjFjN2QyMDU3OWU2ZmUyNWQ4MTJhN2VlNTU4NmM0ZmNkZTliODJmYmE3ZDIyYjNlMzI1YjkzZTBlOGQxNzMyMyJ9fX0=");
            /*12*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzNiMDhiMWVkZTJmNmQ5ZmIxMDYxNjc1ODgwNmU3OWNjZTQ3ZGVlMWFjZTlhMWQ5OWE2OWFjZGZlOTU2YTAxNyJ9fX0=");
            /*13*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGE5ZTVmN2QyNTNhMmQwZTM4MTM0OGY5NjZmNmE5MGE0MGFkMDY2NWE3M2E0ZDZkNTIwZWQ4MDkzOGE4MmI1MiJ9fX0=");
            /*14*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2MwMTFmODg0NWZhZjhmMjBlMWNjYTk2YzQ1MmVlMTAzZmY2YTk0NjNhZDk5YTk4NTdlMGRmMzY3Y2YwYzk2ZCJ9fX0=");
            /*15*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmU1NTYyODgxZTFmZjQ3MmY0ZDM1ZmZiNzM3NDYyMTM0MDk5YTdmYmZhNzIzMmNmMzM2OGMzMzM3NzUyMmYzOCJ9fX0=");
            /*16*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjk4MjRlZmQwMjJhMjhkYWU4N2MxNjcxOTFkN2YzOGJlMDgxMjc5NmY1MmViOGYzN2MzMzI2Yjg5MWUyNDM3NyJ9fX0=");
            /*17*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTBiNjFlYmRkMTk1NDdkNDYwMTNlYzNhZTllMTUzYjdmNzIwN2MzYTE5ZjZiZmMxYmM0MjhmMGI0N2VlNDU2MiJ9fX0=");
            /*18*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmI0YmFkYWIzN2IyZGYzZTZjZmQ0MjU2ZDhhODY3Y2ZmMzQxODYwOTZmNTYzZGYwOThmNGM4MTc3MGFmODRmNSJ9fX0=");
            /*19*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzQ3MjVjNWI0ODY5OWIxYmJkMGIzMjM5NDU5NjVmMzgwYzE2Njk4NWU3ODYyZjI4MTQ4NmVhZjM4ODhiMzQ2NyJ9fX0=");
            /*20*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2IwYjRhNDFlZjJlYjAxOTc3ZTgzYTM5MGMyNmY3ODcwZTViYmIwNjk3Y2ZjNmUyYTNhZjVjNGU3YmNjNWI5MCJ9fX0=");
            /*21*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzA4NjVhOWVkNWVjZDg0NWZlMDg0MTE0NjlkNTgyMDVjZTU5ZWFiOThiNzNlZjQ2Y2M0OTEwNGVmMDY3MWFjMyJ9fX0=");
            /*22*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTk3YzdmYWZmNzM0YjUzYzZjMzYzYTFjMjMxZDA0M2I2YzI1NjMzNWJiMjNlYjkzYWM4N2U3NWM4OWM3OWZlYSJ9fX0=");
            /*23*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzU2NjU2OTlmNmM3N2NhYjUyNmIyZmQ1MmRhNzIwYmJlNGJmYWNkZmJkNTZjOTZkYTEwYTA1ZTUxNzFlNzkyZiJ9fX0=");
            /*24*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODUwNjBmODZmOWY2MjMyOWFiYmQxMjg0MmQ1Njk5ZjhkNWI4ZWE5NzExMjI3YzBkNmU3NmY1MWJmYTRlYjNmZiJ9fX0=");
            /*25*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZmJkNDRmNTdhZTMxZWFlMmU3NDgyMGYyNjk0MDE5MmMzM2QxMmNhNjNlZmY3MDc5ODM3OWRiNzdlYTlmNDM4MiJ9fX0=");
            /*26*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTdiZTBiYmIyZmZmNDJmNzViYjYyMDcxMzVlYjI1NTlmMTdkNjBlMDEzYmZjN2JjMTE3YmJhOTlmM2Q3YjgifX19fX0=");
            /*27*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzM3NzNlMDUyZDM5MWU4NjllMGFmYWIyYTM0NTVmNDgwMzU0ZTQ4YWM0NjUyOGIxNDM4N2JjNjI4NDliMDYwZCJ9fX0=");
            /*28*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWJkOGRmYThiODViZGQ2MGY4MGU4ZjBhYTI0MGQ3NDU2ZGFmZjM0YjhhZDYxYTQxZjMzNzE0NTVkZTE1YmVkNyJ9fX0=");
            /*29*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGRjY2I4NDUwZTljZTcwNjlmOGFjNTI5NmQzNWMxN2UzYmI3ZGQ3MWNhMjQ2NzExMTU5MTY4ZGE5NGFkZjRjYyJ9fX0=");
            /*30*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2Q4ZTUyNDdjOWY5NDczMDA4N2MxOGFmNGMzZDMzODAwZmRlM2ZhZDRhZDFjZmQ2NmFiOWNhMzVjZDYwYWJkZiJ9fX0=");
            /*31*/add("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzhkYTAzZjMwY2JhYTQxY2Y0MTJjMTljYjRlYzE4ZWE5YmIxYWE5MTIzODk5OTI3OGYzNjkyNjAwNGNhNDVjYiJ9fX0=");
            // More more textures past 31.
        }
    };

    public static class GUIButton {
        public final String name;
        public final List<String> lore;
        public final String material;
        public final int slot;
        public GUIButton(String name, List<String> lore, String material, int slot) {
            this.name = name;
            this.lore = lore;
            this.material = material;
            this.slot = slot;
        }
    }

    public static Inventory createCustomPage(Player player, String title, int page, List<ItemStack> items, int rows, boolean hideOnePage, HashMap<GUIAction, GUIButton> buttons) {
        int totalPages;

        rows = Utils.Clamp(rows, 2, 6);

        Inventory inventory = Bukkit.createInventory(player, 9 * rows, title);
        rows -= 1;

        totalPages = (int) Math.ceil((double) items.size() / (9 * rows));

        // Calculate start and end index for items on this page
        int startIndex = (page - 1) * 9 * rows;
        int endIndex = Math.min(startIndex + (9 * rows), items.size());

        // Add items to the inventory
        for (int i = startIndex; i < endIndex; i++) {
            if(items.get(i) == null) continue;
            inventory.addItem(items.get(i));
        }

        // Add navigation buttons to the bottom row
        int bottomRowStartSlot = (rows) * 9;
        int prevPageSlot = bottomRowStartSlot + (buttons.containsKey(GUIAction.PREV_PAGE) ? buttons.get(GUIAction.PREV_PAGE).slot : 3);
        int indicatorSlot = bottomRowStartSlot + (buttons.containsKey(GUIAction.PAGE_INDICATOR) ? buttons.get(GUIAction.PAGE_INDICATOR).slot : 4);
        int nextPageSlot = bottomRowStartSlot + (buttons.containsKey(GUIAction.NEXT_PAGE) ? buttons.get(GUIAction.NEXT_PAGE).slot : 5);
        int goBackSlot = bottomRowStartSlot + (buttons.containsKey(GUIAction.RETURN) ? buttons.get(GUIAction.RETURN).slot : 8);

        // Add "Go Back" button
        if (buttons.containsKey(GUIAction.RETURN)) {
            inventory.setItem(goBackSlot, createCustomButton(GUIAction.RETURN, buttons.get(GUIAction.RETURN)));
        } else {
            inventory.setItem(goBackSlot, createButton(GUIAction.RETURN, RETURN_TEXTURE));
        }

        if(hideOnePage && totalPages == 1) return inventory;

        // Add "Previous" button
        if (page > 1) {
            if (buttons.containsKey(GUIAction.PREV_PAGE)) {
                inventory.setItem(prevPageSlot, createCustomButton(GUIAction.PREV_PAGE, buttons.get(GUIAction.PREV_PAGE)));
            } else {
                inventory.setItem(prevPageSlot, createButton(GUIAction.PREV_PAGE, PREV_TEXTURE));
            }
        }

        // Add "Next" button
        if (page < totalPages) {
            if (buttons.containsKey(GUIAction.NEXT_PAGE)) {
                inventory.setItem(nextPageSlot, createCustomButton(GUIAction.NEXT_PAGE, buttons.get(GUIAction.NEXT_PAGE)));
            } else {
                inventory.setItem(nextPageSlot, createButton(GUIAction.NEXT_PAGE, NEXT_TEXTURE));
            }
        }

        // Add page indicator
        if (buttons.containsKey(GUIAction.PAGE_INDICATOR)) {
            inventory.setItem(indicatorSlot, createCustomPageButton(page, buttons.get(GUIAction.PAGE_INDICATOR)));
        } else {
            inventory.setItem(indicatorSlot, createPageButton(page));
        }

        return inventory;
    }

    private static ItemStack createCustomButton(GUIAction action, GUIButton guiButton) {
        ItemStack button = guiButton.material.startsWith("nexo:") ?
                NexoItems.itemFromId(guiButton.material.replace("nexo:", "")).build() :
                guiButton.material.startsWith("oraxen:") ?
                        OraxenItems.getItemById(guiButton.material.replace("oraxen:", "")).build() :
                        guiButton.material.equalsIgnoreCase("default") ?
                            createButton(action, action.equals(GUIAction.NEXT_PAGE) ?
                                 NEXT_TEXTURE : action.equals(GUIAction.PREV_PAGE) ? PREV_TEXTURE : RETURN_TEXTURE) :
                            new ItemStack(Material.valueOf(guiButton.material.toUpperCase()));

        if(!guiButton.material.startsWith("nexo:") && !guiButton.material.startsWith("oraxen:")) {
            ItemMeta meta = button.getItemMeta();
            meta.setDisplayName(Lang.parse(guiButton.name));
            List<String> lore = new ArrayList<>();
            for (String line : guiButton.lore) {
                lore.add(Lang.parse(line));
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
        }

        // Store GUI action in NBT
        PDC.setString(button, "bag-action", action.toString());

        return button;
    }

    private static ItemStack createCustomPageButton(int page, GUIButton guiButton) {
        ItemStack button = guiButton.material.startsWith("nexo:") ?
                NexoItems.itemFromId(guiButton.material.replace("nexo:", "")).build() :
                guiButton.material.startsWith("oraxen:") ?
                        OraxenItems.getItemById(guiButton.material.replace("oraxen:", "")).build() :
                        guiButton.material.equalsIgnoreCase("default") ?
                            createPageButton(page) :
                            new ItemStack(Material.valueOf(guiButton.material.toUpperCase()));

        if(guiButton.material.startsWith("nexo:") || guiButton.material.startsWith("oraxen:")) {
            if (Server.VersionHigherOrEqualTo(Server.Version.v1_20_5)) {
                // ItemUtils load and apply ItemMeta, therefore is done before/after own ItemMeta manipulation.
                if (ItemUtils.HasItemName(button)) {
                    String name = ItemUtils.GetItemName(button).replace("%page%", String.valueOf(page));
                    ItemUtils.SetItemName(button, name);
                }
            }

            ItemMeta meta = button.getItemMeta();
            if (meta.hasItemName() && meta.getItemName().contains("%page%")) {
                String name = meta.getItemName().replace("%page%", String.valueOf(page));
                meta.setItemName(name);
            }
            if (meta.hasDisplayName() && meta.getDisplayName().contains("%page%")) {
                String name = meta.getDisplayName().replace("%page%", String.valueOf(page));
                meta.setDisplayName(name);
            }

            if (meta.hasLore() && meta.getLore().stream().anyMatch(line -> line.contains("%page%"))) {
                List<String> lore = meta.getLore();
                lore.replaceAll(s -> s.replace("%page%", String.valueOf(page)));
                meta.setLore(lore);
            }

            button.setItemMeta(meta);
        }else{
            ItemMeta meta = button.getItemMeta();
            meta.setDisplayName(Lang.parse(guiButton.name.replace("%page%", String.valueOf(page))));
            List<String> lore = new ArrayList<>();
            for (String line : guiButton.lore) {
                lore.add(Lang.parse(line.replace("%page%", String.valueOf(page))));
            }
            meta.setLore(lore);
            button.setItemMeta(meta);
        }

        // Store GUI action in NBT
        PDC.setString(button, "bag-action", GUIAction.NONE.toString());

        return button;
    }
}
