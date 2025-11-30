package valorless.havenbags.annotations;

import java.lang.annotation.Documented;

/**
 * Marks a field, method, or constructor as currently unused.
 * <p>
 * This annotation serves as an indicator to developers that the annotated element
 * is not actively utilized in the codebase at present.
 * </p>
 */
@Documented
public @interface Unused {

	/**
	 * Optional description providing context about the unused status.
	 * <p>
	 * This can include reasons for why the element is unused,
	 * or plans for future use or removal.
	 * </p>
	 *
	 * @return a string description (default is an empty string)
	 */
	String value() default "";
}
