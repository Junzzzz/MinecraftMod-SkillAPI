package skillapi.api.gui.base;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.var;
import skillapi.common.SkillRuntimeException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Jun
 * @date 2021/3/20.
 */
@SideOnly(Side.CLIENT)
public final class ListenerRegistry {
    private final GenericGui base;
    private final Map<Class<? extends GenericListener>, Map<BaseComponent, List<GenericListener>>> local = new HashMap<>(8);
    private static final Map<Class<? extends GenericListener>, Map<BaseComponent, List<GenericListener>>> GLOBAL = new HashMap<>(8);

    private BaseComponent mappingComponent;

    public ListenerRegistry(GenericGui ui) {
        this.base = ui;
    }

    protected void onComponent(BaseComponent mappingComponent) {
        this.mappingComponent = mappingComponent;
    }

    /**
     * Can only be executed in BaseGui
     */
    protected void clear() {
        GLOBAL.clear();
        this.local.clear();
    }

//    private List<Pair<Class<? extends GenericListener>, List<GenericListener>>> extractListener() {
//        return this.local.entrySet().stream()
//                .filter(e -> !LocalListener.class.isAssignableFrom(e.getKey()))
//                .map(e -> new Pair<Class<? extends GenericListener>, List<GenericListener>>(e.getKey(), e.getValue().values().stream().flatMap(Collection::stream).collect(Collectors.toList())))
//                .collect(Collectors.toList());
//    }

//    protected void onChild() {
//        for (var e : mappingComponent.listenerRegistry.extractListener()) {
//            local.computeIfAbsent(e.getKey(), k -> new HashMap<>(8))
//                    .computeIfAbsent(mappingComponent, k -> new LinkedList<>())
//                    .addAll(e.getValue());
//        }
//    }

    @SuppressWarnings("unchecked")
    public void on(GenericListener listener) {
        final Class<?>[] interfaces = listener.getClass().getInterfaces();
        if (interfaces.length != 1) {
            throw new SkillRuntimeException("Unknown error");
        }
        final Class<? extends GenericListener> listenerClass = (Class<? extends GenericListener>) interfaces[0];
        if (listener instanceof LocalListener) {
            local.computeIfAbsent(listenerClass, k -> new HashMap<>(4))
                    .computeIfAbsent(mappingComponent, k -> new LinkedList<>())
                    .add(listener);
        } else {
            GLOBAL.computeIfAbsent(listenerClass, k -> new HashMap<>(4))
                    .computeIfAbsent(mappingComponent, k -> new LinkedList<>())
                    .add(listener);
        }
    }

    public void on(GenericListener... listener) {
        for (GenericListener l : listener) {
            on(l);
        }
    }


    @SuppressWarnings("unchecked")
    public <T extends GenericListener> void call(Class<T> clz, ComponentCaller<T> caller) {
        var map = GLOBAL;
        if (LocalListener.class.isAssignableFrom(clz)) {
            for (BaseComponent component : this.base.components) {
                component.listenerRegistry.call(clz, caller);
            }
            map = this.local;
        }
        final Map<BaseComponent, List<GenericListener>> m = map.get(clz);
        if (m == null) {
            return;
        }
        for (Map.Entry<BaseComponent, List<GenericListener>> e : m.entrySet()) {
            for (GenericListener l : e.getValue()) {
                caller.call(e.getKey(), (T) l);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public <T extends GenericListener> void call(Class<T> clz, Caller<T> caller) {
        var map = GLOBAL;
        if (LocalListener.class.isAssignableFrom(clz)) {
            for (BaseComponent component : this.base.components) {
                component.listenerRegistry.call(clz, caller);
            }
            map = this.local;
        }
        final Map<BaseComponent, List<GenericListener>> m = map.get(clz);
        if (m == null) {
            return;
        }
        final List<T> listeners = (List<T>) m.values().stream().flatMap(Collection::stream).collect(Collectors.toList());
        for (T listener : listeners) {
            caller.call(listener);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends GenericListener> void call(BaseComponent component, Class<T> clz, Caller<T> caller) {
        if (LocalListener.class.isAssignableFrom(clz)) {
            throw new SkillRuntimeException("Not supported.");
        }
        // Global only
        final Map<BaseComponent, List<GenericListener>> m = GLOBAL.get(clz);
        if (m == null) {
            return;
        }
        final List<T> listeners = (List<T>) m.get(component);
        if (listeners == null) {
            return;
        }
        for (T listener : listeners) {
            caller.call(listener);
        }
    }

    protected interface ComponentCaller<T> {
        void call(BaseComponent genericGui, T listener);
    }

    protected interface Caller<T> {
        void call(T listener);
    }
}
