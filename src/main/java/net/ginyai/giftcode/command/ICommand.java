package net.ginyai.giftcode.command;


import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.util.annotation.NonnullByDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@NonnullByDefault
public interface ICommand {

    CommandCallable getCallable();

    String getName();

    String[] getAlias();

    default List<String> getNameList() {
        List<String> list = new ArrayList<>();
        list.add(getName());
        list.addAll(Arrays.asList(getAlias()));
        return list;
    }
}
