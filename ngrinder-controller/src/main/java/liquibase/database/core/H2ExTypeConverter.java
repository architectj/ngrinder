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
package liquibase.database.core;

import liquibase.database.structure.type.BooleanType;
import liquibase.database.structure.type.FloatType;
import liquibase.database.typeconversion.core.H2TypeConverter;

/**
 * Liquibase H2 type converter.
 * 
 * @author JunHo Yoon
 * @since 3.0
 * 
 */
public class H2ExTypeConverter extends H2TypeConverter {
	@Override
	public int getPriority() {
		return super.getPriority() + 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getFloatType
	 * ()
	 */
	@Override
	public FloatType getFloatType() {
		return new FloatType("DOUBLE");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * liquibase.database.typeconversion.core.AbstractTypeConverter#getBooleanType
	 * ()
	 */
	@Override
	public BooleanType getBooleanType() {
		return new TrueOrFalseBooleanType();
	}
}