package valorless.havenbags.annotations;

import static java.lang.annotation.ElementType.CONSTRUCTOR;
import static java.lang.annotation.ElementType.METHOD;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

/**
 * Indicates that a method or constructor is not intended to be called directly.
 * <p>
 * This annotation serves as a warning to developers that the annotated element
 * should not be invoked, as it may lead to unexpected behavior or is reserved
 * for internal use only.
 * </p>
 */
@Documented
@Target({ METHOD, CONSTRUCTOR })
public @interface DoNotCall {

	/**
	 * An optional message providing additional context about why the element
	 * should not be called.
	 *
	 * @return a string message, or an empty string if no message is provided
	 */
	String value() default "";
	
}
