/*
 * Copyright 2002-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.beans;

import java.io.Serializable;

import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * 对象来保存单个bean属性的信息和值。
 * 在这里使用一个对象，而不是仅仅将所有属性存储在一个按属性名键控的映射中，这样可以提供更大的灵活性，
 * 并能够以优化的方式处理索引属性等。
 *
 * Object to hold information and value for an individual bean property.
 * Using an object here, rather than just storing all properties in
 * a map keyed by property name, allows for more flexibility, and the
 * ability to handle indexed properties etc in an optimized way.
 *
 * 注意，这个值不需要是最终的必需类型:{@link BeanWrapper}实现应该处理任何必要的转换，
 * 因为这个对象不知道它将应用到的对象的任何信息。
 *
 * <p>Note that the value doesn't need to be the final required type:
 * A {@link BeanWrapper} implementation should handle any necessary conversion,
 * as this object doesn't know anything about the objects it will be applied to.
 *
 * @author Rod Johnson
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 13 May 2001
 * @see PropertyValues
 * @see BeanWrapper
 */
@SuppressWarnings("serial")
public class PropertyValue extends BeanMetadataAttributeAccessor implements Serializable {

	private final String name;

	@Nullable
	private final Object value;

	private boolean optional = false;

	private boolean converted = false;

	@Nullable
	private Object convertedValue;

	/**
	 * 包可见字段，指示是否需要转换。
	 *
	 * Package-visible field that indicates whether conversion is necessary.
	 */
	@Nullable
	volatile Boolean conversionNecessary;

	/**
	 * 用于缓存已解析的属性路径令牌的包可见字段。
	 *
	 * Package-visible field for caching the resolved property path tokens.
	 */
	@Nullable
	transient volatile Object resolvedTokens;


	/**
	 * Create a new PropertyValue instance.
	 * @param name the name of the property (never {@code null})
	 * @param value the value of the property (possibly before type conversion)
	 */
	public PropertyValue(String name, @Nullable Object value) {
		Assert.notNull(name, "Name must not be null");
		this.name = name;
		this.value = value;
	}

	/**
	 * Copy constructor.
	 * @param original the PropertyValue to copy (never {@code null})
	 */
	public PropertyValue(PropertyValue original) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = original.getValue();
		this.optional = original.isOptional();
		this.converted = original.converted;
		this.convertedValue = original.convertedValue;
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original.getSource());
		copyAttributesFrom(original);
	}

	/**
	 * Constructor that exposes a new value for an original value holder.
	 * The original holder will be exposed as source of the new holder.
	 * @param original the PropertyValue to link to (never {@code null})
	 * @param newValue the new value to apply
	 */
	public PropertyValue(PropertyValue original, @Nullable Object newValue) {
		Assert.notNull(original, "Original must not be null");
		this.name = original.getName();
		this.value = newValue;
		this.optional = original.isOptional();
		this.conversionNecessary = original.conversionNecessary;
		this.resolvedTokens = original.resolvedTokens;
		setSource(original);
		copyAttributesFrom(original);
	}


	/**
	 * 返回属性的名称。
	 *
	 * Return the name of the property.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * 返回属性的值。注意，这里不会发生类型转换。执行类型转换是BeanWrapper实现的职责。
	 *
	 * Return the value of the property.
	 * <p>Note that type conversion will <i>not</i> have occurred here.
	 * It is the responsibility of the BeanWrapper implementation to
	 * perform type conversion.
	 */
	@Nullable
	public Object getValue() {
		return this.value;
	}

	/**
	 * 返回此值持有者的原始PropertyValue实例。
	 *
	 * Return the original PropertyValue instance for this value holder.
	 * @return the original PropertyValue (either a source of this
	 * value holder or this value holder itself).
	 */
	public PropertyValue getOriginalPropertyValue() {
		PropertyValue original = this;
		Object source = getSource();
		while (source instanceof PropertyValue && source != original) {
			original = (PropertyValue) source;
			source = original.getSource();
		}
		return original;
	}

	/**
	 * 设置该值是否为可选值，即在目标类上不存在相应属性时忽略该值。
	 *
	 * Set whether this is an optional value, that is, to be ignored
	 * when no corresponding property exists on the target class.
	 * @since 3.0
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * 返回该值是否为可选值，即在目标类上不存在相应属性时忽略该值。
	 *
	 * Return whether this is an optional value, that is, to be ignored
	 * when no corresponding property exists on the target class.
	 * @since 3.0
	 */
	public boolean isOptional() {
		return this.optional;
	}

	/**
	 * 返回该容器是否已经包含转换后的值({@code true})，或者该值是否仍然需要转换({@code false})。
	 *
	 * Return whether this holder contains a converted value already ({@code true}),
	 * or whether the value still needs to be converted ({@code false}).
	 */
	public synchronized boolean isConverted() {
		return this.converted;
	}

	/**
	 * 设置处理类型转换后此属性值的转换值。
	 *
	 * Set the converted value of this property value,
	 * after processed type conversion.
	 */
	public synchronized void setConvertedValue(@Nullable Object value) {
		this.converted = true;
		this.convertedValue = value;
	}

	/**
	 * 处理类型转换后，返回此属性值的转换值。
	 *
	 * Return the converted value of this property value,
	 * after processed type conversion.
	 */
	@Nullable
	public synchronized Object getConvertedValue() {
		return this.convertedValue;
	}


	@Override
	public boolean equals(@Nullable Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof PropertyValue)) {
			return false;
		}
		PropertyValue otherPv = (PropertyValue) other;
		return (this.name.equals(otherPv.name) &&
				ObjectUtils.nullSafeEquals(this.value, otherPv.value) &&
				ObjectUtils.nullSafeEquals(getSource(), otherPv.getSource()));
	}

	@Override
	public int hashCode() {
		return this.name.hashCode() * 29 + ObjectUtils.nullSafeHashCode(this.value);
	}

	@Override
	public String toString() {
		return "bean property '" + this.name + "'";
	}

}
