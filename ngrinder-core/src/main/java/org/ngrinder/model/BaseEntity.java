/*
 * Copyright (C) 2012 - 2012 NHN Corporation
 * All rights reserved.
 *
 * This file is part of The nGrinder software distribution. Refer to
 * the file LICENSE which is part of The nGrinder distribution for
 * licensing details. The nGrinder distribution is available on the
 * Internet at http://nhnopensource.org/ngrinder
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.ngrinder.model;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.ngrinder.common.exception.NGrinderRuntimeException;

/**
 * Base Entity. This has a long type ID field
 * 
 * @param <M>
 *            wrapped entity type
 * 
 * @author Liu Zhifei
 * @author JunHo Yoon
 * @since 3.0
 */
@MappedSuperclass
public class BaseEntity<M> implements Serializable {

	private static final long serialVersionUID = 8571113820348514692L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = false)
	private Long id;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	/**
	 * This function is used to check whether the entity id exist. It is not used to check the
	 * entity existence in DB. It can be used to check the entity in controller, which is passed
	 * from page.
	 * 
	 * @return true if exists
	 */
	public boolean exist() {
		return id != null && id.longValue() != 0;
	}

	/**
	 * Merge source entity into current entity.
	 * 
	 * Only not null value is merged.
	 * 
	 * @param source
	 *            merge source
	 * @return merged entity
	 */
	@SuppressWarnings("unchecked")
	public M merge(M source) {
		PropertyDescriptor forError = null;
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(getClass());
			// Iterate over all the attributes
			for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
				forError = descriptor;
				// Only copy writable attributes
				Method writeMethod = descriptor.getWriteMethod();
				if (writeMethod == null) {
					continue;
				}
				// Only copy values values where the source values is not null
				Method readMethod = descriptor.getReadMethod();
				if (readMethod == null) {
					continue;
				}

				Object defaultValue = readMethod.invoke(source);
				if (defaultValue == null) {
					continue;
				}

				if (writeMethod.getAnnotation(ForceMergable.class) != null 
								|| isNotBlankStringOrNotString(defaultValue)) {
					writeMethod.invoke(this, defaultValue);
				}
			}
			return (M) this;
		} catch (Exception e) {
			String displayName = (forError == null) ? "Empty" : forError.getDisplayName();
			throw new NGrinderRuntimeException(displayName + " - Exception occurs while merging entities from "
							+ source + " to " + this, e);
		}
	}

	private boolean isNotBlankStringOrNotString(Object aValue) {
		boolean isNotBlankString = aValue instanceof String && StringUtils.isNotBlank((String) aValue);
		boolean isNotString = !(aValue instanceof String);
		return isNotBlankString || isNotString;
	}
}
