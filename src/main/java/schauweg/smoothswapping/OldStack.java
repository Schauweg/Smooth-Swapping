package schauweg.smoothswapping;

import net.minecraft.item.Item;

public record OldStack(Item item, int count) {

    public Item getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }

    public String toString() {
        return item.getName().getString() + " " + count;
    }
}
