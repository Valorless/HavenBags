package valorless.havenbags.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

/**
 * Annotation indicating that the annotated element is intended for testing purposes only.
 * <p>
 * This annotation can be applied to fields, methods, and constructors to signify that
 * they should only be used within test code and not in production code.
 * </p>
 */
@Documented
@Target({ FIELD, METHOD, CONSTRUCTOR })
public @interface TestOnly {

	/**
	 * Optional description providing context about the testing usage.
	 * <p>
	 * This can include reasons for the testing designation or any relevant notes.
	 * </p>
	 *
	 * @return a string description (default is an empty string)
	 */
	String value() default "";

}
