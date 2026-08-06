package valorless.havenbags.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Indicates that a method parameter must not be null.
 */
@Documented
@Target({ElementType.PARAMETER, ElementType.METHOD})
public @interface NotNull {

	/**
	 * An optional description or reason for the NotNull annotation.
	 * 
	 * @return A string providing additional context (default is an empty string).
	 */
	public String value() default "";
	
}
