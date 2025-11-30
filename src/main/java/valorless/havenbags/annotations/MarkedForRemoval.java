package valorless.havenbags.annotations;

import java.lang.annotation.Documented;

/**
 * Marks a field, method, or constructor as slated for removal in a future release.
 * <p>
 * This annotation serves as a warning to developers that the annotated element
 * is deprecated and will be removed in upcoming versions of the codebase.
 * </p>
 */
@Documented
public @interface MarkedForRemoval {
	
	/**
	 * Optional description providing context about the removal.
	 * <p>
	 * This can include reasons for the removal, suggested alternatives,
	 * or the version in which the removal is planned.
	 * </p>
	 *
	 * @return a string description (default is an empty string)
	 */
	String value() default "";
	
}
