package net.minecraftforge.common.util;

/**
 * Compile-only stub for Forge's NonNullPredicate.
 * Needed so the compiler can resolve overloaded methods in CreateBlockEntityBuilder.visual().
 * At runtime, the real class is provided by the platform (Forge or porting_lib on Fabric).
 */
@FunctionalInterface
public interface NonNullPredicate<T> {
    boolean test(T t);
}
