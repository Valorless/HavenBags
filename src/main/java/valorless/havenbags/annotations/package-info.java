/**
 * Annotations used throughout HavenBags to document API contracts and usage.
 * <p>
 * This package provides compile-time/documentation annotations such as:
 * <ul>
 *   <li>{@link NotNull} and {@link Nullable} to declare nullability contracts.</li>
 *   <li>{@link DoNotCall} to mark APIs that must not be invoked by plugins.</li>
 *   <li>{@link TestOnly} to indicate elements intended only for testing.</li>
 *   <li>{@link MarkedForRemoval} to flag elements scheduled for removal in a future release.</li>
 * </ul>
 * These annotations aid static analysis and improve generated documentation; they typically have no
 * runtime effect unless explicitly processed.
 */
package valorless.havenbags.annotations;