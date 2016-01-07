package humbleactivity.app;

import java.util.Objects;

public class Filter {
    public final String name;

    public Filter(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Filter filter = (Filter) o;
        return Objects.equals(name, filter.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
