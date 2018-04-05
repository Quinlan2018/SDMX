package it.bancaditalia.oss.sdmx.api;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import it.bancaditalia.oss.sdmx.util.Utils.BiFunction;
import it.bancaditalia.oss.sdmx.util.Utils.Function;

/**
 * An immutable observation in a series.
 * This is the base class implementation for an observation of a generic type.
 * 
 * The extending class is required only to implement the {@link #getValue()} method.
 * 
 * @author Valentino Pinna
 */
public abstract class BaseObservation<T> implements Serializable, Comparable<BaseObservation<?>>
{
	private static final long			serialVersionUID	= 1L;

	protected final String				timeslot;
	protected final Map<String, String>	obsAttributes;

	/**
	 * Creates an immutable observation from given values.
	 * 
	 * @param timeslot The timestamp of the observation.
	 * @param obsAttributes A map of observation-level attributes.
	 */
	protected BaseObservation(String timeslot, Map<String, String> obsAttributes)
	{
		if (timeslot == null || timeslot.isEmpty())
			throw new InvalidParameterException("The timeslot for an observation cannot be null or empty.");

		this.timeslot = timeslot;
		this.obsAttributes = obsAttributes == null ? new HashMap<String, String>() : obsAttributes;
	}

	/**
	 * Creates a new observation that is a copy of this Observation.
	 * 
	 * @param other The Observation to copy.
	 */
	protected  BaseObservation(BaseObservation<?> other)
	{
		this.timeslot = other.timeslot;
		this.obsAttributes = other.obsAttributes;
	}

	/**
	 * Creates a new Observation equal to this, with the value set equal to the result of applying a function to this
	 * Observation value.
	 * 
	 * @param mapper The function mapping the value.
	 * @return A new Observation
	 */
	public <U> BaseObservation<U> mapValue(Function<? super T, U> mapper)
	{
		return new Observation<>(this, mapper.apply(getValue()));
	}

	/**
	 * Creates a new Observation equal to this, with the value set equal to the result of applying a function to this
	 * and another Observation values. Note: String values are automatically converted to Double.
	 * 
	 * @param mapper The function mapping the value.
	 * @return A new Observation
	 */
	public <U, R> BaseObservation<R> combine(BaseObservation<U> other, BiFunction<? super T, ? super U, R> combiner)
	{
		return new Observation<>(this, combiner.apply(getValue(), other.getValue()));
	}

	/**
	 * @return This observation's timestamp
	 */
	public String getTimeslot()
	{
		return timeslot;
	}

	/**
	 * @return This observation's value
	 */
	public abstract T getValue();

	/**
	 * Get this observation's value as a {@code double} number.<br/>
	 * If either value is null, is not a {@link Number} instance, or if its {@link T#toString()} method returns an
	 * invalid number representation, a {@link Double#NaN} is returned instead.
	 * <p />
	 * <b>IMPLEMENTATION NOTE:</b> This method will cause major boxing/unboxing performance impact. It was implemented
	 * as a compatibility layer for other tools. Please use the specialized instances for series based on primitive types.
	 * 
	 * @see DoubleObservation
	 * @see LongObservation
	 * 
	 * @return This observation's value
	 * 
	 * @throws NumberFormatException if the value is not a valid double number (see {@link Double#parseDouble(String)})
	 *
	 */
	public double getValueAsDouble()
	{
		if (getValue() instanceof Number)
			return ((Number) getValue()).doubleValue();
		else
			try
			{
				return Double.parseDouble(getValue().toString());
			}
			catch (RuntimeException e)
			{
				return Double.NaN;
			}
	}

	/**
	 * @return This observation's value
	 */
	public String getValueAsString()
	{
		return getValue().toString();
	}

	/**
	 * @return This observation's attributes
	 */
	public Map<String, String> getAttributes()
	{
		return Collections.unmodifiableMap(obsAttributes);
	}

	/**
	 * @param attrName A name of an observation-level attribute
	 * @return This observation's attribute value corresponding to given name, or null.
	 */
	public String getAttributeValue(String attrName)
	{
		return obsAttributes.get(attrName);
	}

	@Override
	public int compareTo(BaseObservation<?> other)
	{
		return timeslot.compareToIgnoreCase(other.timeslot);
	}

}