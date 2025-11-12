package valorless.havenbags.annotations;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

/**
 * Indicates that a method parameter must not be null.
 */
@Documented
@Target(PARAMETER)
public @interface NotNull {

	/**
	 * An optional description or reason for the NotNull annotation.
	 * 
	 * @return A string providing additional context (default is an empty string).
	 */
	public String value() default "";
	
}
