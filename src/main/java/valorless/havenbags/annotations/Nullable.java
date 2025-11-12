package valorless.havenbags.annotations;

import static java.lang.annotation.ElementType.PARAMETER;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;

/**
 * Indicates that a method parameter can accept null values.
 */
@Documented
@Target(PARAMETER)
public @interface Nullable {

	/**
	 * An optional description or reason for the parameter being nullable.
	 * 
	 * @return A string providing additional context about the nullability.
	 */
	public String value() default "";
	
}
