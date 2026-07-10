package util;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Listeners<T> {

    private final List<T> list = new CopyOnWriteArrayList<T>();

    public void add(T listener) {
        if(listener != null && !list.contains(listener)){
            list.add(listener);
        }
    }

    public void remove(T listener){
        list.remove(listener);
    }

    public void fire(Consumer<T> action) {
        for(T el : list) {
            action.accept(el);
        }
    }
}
