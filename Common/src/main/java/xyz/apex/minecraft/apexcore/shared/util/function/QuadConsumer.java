package xyz.apex.minecraft.apexcore.shared.util.function;

import java.util.Objects;

@FunctionalInterface
public interface QuadConsumer<A, B, C, D>
{
    void accept(A a, B b, C c, D d);

    default QuadConsumer<A, B, C, D> andThen(QuadConsumer<? super A, ? super B, ? super C, ? super D> after)
    {
        Objects.requireNonNull(after);

        return (a, b, c, d) -> {
            accept(a, b, c, d);
            after.accept(a, b, c, d);
        };
    }

    static <A, B, C, D> QuadConsumer<A, B, C, D> noop()
    {
        return (a, b, c, d) -> {};
    }
}
