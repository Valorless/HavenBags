package valorless.havenbags.annotations;

import java.lang.annotation.Documented;

/**
 * Annotation indicating that the annotated element is experimental and subject to change.
 * <p>
 * Elements marked with this annotation are considered unstable and may be modified or removed
 * in future versions without prior notice. Use with caution in production code and be
 * prepared to update your code if these APIs change.
 * </p>
 * <p>
 * This annotation can be applied to classes, methods, fields, and constructors to signify
 * that they are part of an experimental API or feature that is still under development or evaluation.
 * </p>
 *
 * @since 1.43.0
 */
@Documented
public @interface Experimental {

	/**
	 * Optional description providing context about the experimental status.
	 * <p>
	 * This can include reasons for the experimental designation, expected stability timeline,
	 * or migration guidance for future changes.
	 * </p>
	 *
	 * @return a string description (default is an empty string)
	 */
	String value() default "";

}
