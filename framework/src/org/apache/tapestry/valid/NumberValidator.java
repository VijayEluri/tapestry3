/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2000-2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation", "Tapestry" 
 *    must not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache" 
 *    or "Tapestry", nor may "Apache" or "Tapestry" appear in their 
 *    name, without prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE TAPESTRY CONTRIBUTOR COMMUNITY
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

package org.apache.tapestry.valid;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.IMarkupWriter;
import org.apache.tapestry.IRequestCycle;
import org.apache.tapestry.Tapestry;
import org.apache.tapestry.form.IFormComponent;
import org.apache.tapestry.util.AdaptorRegistry;

/**
 *  Simple validation for standard number classes.  This is probably insufficient
 *  for anything tricky and application specific, such as parsing currency.  
 *
 *  @author Howard Lewis Ship
 *  @version $Id$
 *  @since 1.0.8
 *
 **/

public class NumberValidator extends BaseValidator
{
    private static final Map TYPES = new HashMap();

    static {
        TYPES.put("boolean", boolean.class);
        TYPES.put("Boolean", Boolean.class);
        TYPES.put("java.lang.Boolean", Boolean.class);
        TYPES.put("char", char.class);
        TYPES.put("Character", Character.class);
        TYPES.put("java.lang.Character", Character.class);
        TYPES.put("short", short.class);
        TYPES.put("Short", Short.class);
        TYPES.put("java.lang.Short", Short.class);
        TYPES.put("int", int.class);
        TYPES.put("Integer", Integer.class);
        TYPES.put("java.lang.Integer", Integer.class);
        TYPES.put("long", long.class);
        TYPES.put("Long", Long.class);
        TYPES.put("java.lang.Long", Long.class);
        TYPES.put("float", float.class);
        TYPES.put("Float", Float.class);
        TYPES.put("java.lang.Float", Float.class);
        TYPES.put("byte", byte.class);
        TYPES.put("Byte", Byte.class);
        TYPES.put("java.lang.Byte", Byte.class);
        TYPES.put("double", double.class);
        TYPES.put("Double", Double.class);
        TYPES.put("java.lang.Double", Double.class);
        TYPES.put("java.math.BigInteger", BigInteger.class);
        TYPES.put("java.math.BigDecimal", BigDecimal.class);
    }

    private Class _valueTypeClass = int.class;

    private boolean _zeroIsNull;
    private Number _minimum;
    private Number _maximum;

    private String _scriptPath = "/org/apache/tapestry/valid/NumberValidator.script";

    private String _invalidNumericFormatMessage;
    private String _numberTooSmallMessage;
    private String _numberTooLargeMessage;
    private String _numberRangeMessage;

    private static AdaptorRegistry _numberAdaptors = new AdaptorRegistry();

    private static abstract class NumberAdaptor
    {
        /**
         *  Parses a non-empty {@link String} into the correct subclass of
         *  {@link Number}.
         *
         *  @throws NumberFormatException if the String can not be parsed.
         **/

        abstract public Number parse(String value);

        public int compare(Number left, Number right)
        {
            Comparable lc = (Comparable) left;

            return lc.compareTo(right);
        }
    }

    private static class ByteAdaptor extends NumberAdaptor
    {
        public Number parse(String value)
        {
            return new Byte(value);
        }
    }

    private static class ShortAdaptor extends NumberAdaptor
    {
        public Number parse(String value)
        {
            return new Short(value);
        }
    }

    private static class IntAdaptor extends NumberAdaptor
    {
        public Number parse(String value)
        {
            return new Integer(value);
        }
    }

    private static class LongAdaptor extends NumberAdaptor
    {
        public Number parse(String value)
        {
            return new Long(value);
        }
    }

    private static class FloatAdaptor extends NumberAdaptor
    {
        public Number parse(String value)
        {
            return new Float(value);
        }
    }

    private static class DoubleAdaptor extends FloatAdaptor
    {
        public Number parse(String value)
        {
            return new Double(value);
        }
    }

    private static class BigDecimalAdaptor extends FloatAdaptor
    {
        public Number parse(String value)
        {
            return new BigDecimal(value);
        }
    }

    private static class BigIntegerAdaptor extends NumberAdaptor
    {
        public Number parse(String value)
        {
            return new BigInteger(value);
        }
    }

    static {
        NumberAdaptor byteAdaptor = new ByteAdaptor();
        NumberAdaptor shortAdaptor = new ShortAdaptor();
        NumberAdaptor intAdaptor = new IntAdaptor();
        NumberAdaptor longAdaptor = new LongAdaptor();
        NumberAdaptor floatAdaptor = new FloatAdaptor();
        NumberAdaptor doubleAdaptor = new DoubleAdaptor();

        _numberAdaptors.register(Byte.class, byteAdaptor);
        _numberAdaptors.register(byte.class, byteAdaptor);
        _numberAdaptors.register(Short.class, shortAdaptor);
        _numberAdaptors.register(short.class, shortAdaptor);
        _numberAdaptors.register(Integer.class, intAdaptor);
        _numberAdaptors.register(int.class, intAdaptor);
        _numberAdaptors.register(Long.class, longAdaptor);
        _numberAdaptors.register(long.class, longAdaptor);
        _numberAdaptors.register(Float.class, floatAdaptor);
        _numberAdaptors.register(float.class, floatAdaptor);
        _numberAdaptors.register(Double.class, doubleAdaptor);
        _numberAdaptors.register(double.class, doubleAdaptor);

        _numberAdaptors.register(BigDecimal.class, new BigDecimalAdaptor());
        _numberAdaptors.register(BigInteger.class, new BigIntegerAdaptor());
    }

    public String toString(IFormComponent field, Object value)
    {
        if (value == null)
            return null;

        if (_zeroIsNull)
        {
            Number number = (Number) value;

            if (number.doubleValue() == 0.0)
                return null;
        }

        return value.toString();
    }

    private NumberAdaptor getAdaptor(IFormComponent field)
    {
        NumberAdaptor result = (NumberAdaptor) _numberAdaptors.getAdaptor(_valueTypeClass);

        if (result == null)
            throw new ApplicationRuntimeException(
                Tapestry.getString(
                    "NumberValidator.no-adaptor-for-field",
                    field,
                    _valueTypeClass.getName()));

        return result;
    }

    public Object toObject(IFormComponent field, String value) throws ValidatorException
    {
        if (checkRequired(field, value))
            return null;

        NumberAdaptor adaptor = getAdaptor(field);
        Number result = null;

        try
        {
            result = adaptor.parse(value);
        }
        catch (NumberFormatException ex)
        {
            throw new ValidatorException(
                buildInvalidNumericFormatMessage(field),
                ValidationConstraint.NUMBER_FORMAT);
        }

        if (_minimum != null && adaptor.compare(result, _minimum) < 0)
            throw new ValidatorException(
                buildNumberTooSmallMessage(field),
                ValidationConstraint.TOO_SMALL);

        if (_maximum != null && adaptor.compare(result, _maximum) > 0)
            throw new ValidatorException(
                buildNumberTooLargeMessage(field),
                ValidationConstraint.TOO_LARGE);

        return result;
    }

    public Number getMaximum()
    {
        return _maximum;
    }

    public boolean getHasMaximum()
    {
        return _maximum != null;
    }

    public void setMaximum(Number maximum)
    {
        _maximum = maximum;
    }

    public Number getMinimum()
    {
        return _minimum;
    }

    public boolean getHasMinimum()
    {
        return _minimum != null;
    }

    public void setMinimum(Number minimum)
    {
        _minimum = minimum;
    }

    /**
     *  If true, then when rendering, a zero is treated as a non-value, and null is returned.
     *  If false, the default, then zero is rendered as zero.
     * 
     **/

    public boolean getZeroIsNull()
    {
        return _zeroIsNull;
    }

    public void setZeroIsNull(boolean zeroIsNull)
    {
        _zeroIsNull = zeroIsNull;
    }

    /** 
     * 
     *  @since 2.2
     * 
     **/

    public void renderValidatorContribution(
        IFormComponent field,
        IMarkupWriter writer,
        IRequestCycle cycle)
    {
        if (!isClientScriptingEnabled())
            return;

        if (!(isRequired() || _minimum != null || _maximum != null))
            return;

        Map symbols = new HashMap();

        if (isRequired())
            symbols.put("requiredMessage", buildRequiredMessage(field));

        symbols.put("formatMessage", buildInvalidNumericFormatMessage(field));

        if (_minimum != null || _maximum != null)
            symbols.put("rangeMessage", buildRangeMessage(field));

        processValidatorScript(_scriptPath, cycle, field, symbols);
    }

    private String buildRangeMessage(IFormComponent field)
    {
        if (_minimum != null && _maximum != null)
            return buildNumberRangeMessage(field);

        if (_maximum != null)
            return buildNumberTooLargeMessage(field);

        return buildNumberTooSmallMessage(field);
    }

    /**
     *  @since 2.2
     * 
     **/

    public String getScriptPath()
    {
        return _scriptPath;
    }

    /**
     *  Allows a developer to use the existing validation logic with a different client-side
     *  script.  This is often sufficient to allow application-specific error presentation
     *  (perhaps by using DHTML to update the content of a &lt;span&gt; tag, or to use
     *  a more sophisticated pop-up window than <code>window.alert()</code>).
     * 
     *  @since 2.2
     * 
     **/

    public void setScriptPath(String scriptPath)
    {
        _scriptPath = scriptPath;
    }

    /** Sets the value type from a string type name.  The name may be
     *  a scalar numeric type, a fully qualified class name, or the name
     *  of a numeric wrapper type from java.lang (with the package name omitted).
     * 
     * @since 3.0 
     * 
     **/

    public void setValueType(String typeName)
    {
        Class typeClass = (Class) TYPES.get(typeName);

        if (typeClass == null)
            throw new ApplicationRuntimeException(
                Tapestry.getString("NumberValidator.unknown-type", typeName));

        _valueTypeClass = typeClass;
    }

    /** @since 3.0 **/

    public void setValueTypeClass(Class valueTypeClass)
    {
        _valueTypeClass = valueTypeClass;
    }

    /** 
     *  
     *  Returns the value type to convert strings back into.  The default is int.
     * 
     *  @since 3.0 
     * 
     **/

    public Class getValueTypeClass()
    {
        return _valueTypeClass;
    }

    /** @since 3.0 */

    public String getInvalidNumericFormatMessage()
    {
        return _invalidNumericFormatMessage;
    }

    /** @since 3.0 */

    public String getNumberRangeMessage()
    {
        return _numberRangeMessage;
    }

    /** @since 3.0 */

    public String getNumberTooLargeMessage()
    {
        return _numberTooLargeMessage;
    }

    /** @since 3.0 */

    public String getNumberTooSmallMessage()
    {
        return _numberTooSmallMessage;
    }

    /** 
     * Overrides the <code>invalid-numeric-format</code> bundle key.
     * Parameter {0} is the display name of the field.
     * 
     * @since 3.0
     */

    public void setInvalidNumericFormatMessage(String string)
    {
        _invalidNumericFormatMessage = string;
    }

    /** @since 3.0 */

    protected String buildInvalidNumericFormatMessage(IFormComponent field)
    {
        String pattern =
            getPattern(
                getInvalidNumericFormatMessage(),
                "invalid-numeric-format",
                field.getPage().getLocale());

        return formatString(pattern, field.getDisplayName());
    }

    /** 
     * Overrides the <code>number-range</code> bundle key.
     * Parameter [0} is the display name of the field.
     * Parameter {1} is the minimum value.
     * Parameter {2} is the maximum value.
     * 
     * @since 3.0
     */

    public void setNumberRangeMessage(String string)
    {
        _numberRangeMessage = string;
    }

    protected String buildNumberRangeMessage(IFormComponent field)
    {
        String pattern =
            getPattern(_numberRangeMessage, "number-range", field.getPage().getLocale());

        return formatString(pattern, new Object[] { field.getDisplayName(), _minimum, _maximum });
    }

    /**
     *  Overrides the <code>number-too-large</code> bundle key.
     *  Parameter {0} is the display name of the field.
     *  Parameter {1} is the maximum allowed value.
     *  
     *  @since 3.0
     */

    public void setNumberTooLargeMessage(String string)
    {
        _numberTooLargeMessage = string;
    }

    /** @since 3.0 */

    protected String buildNumberTooLargeMessage(IFormComponent field)
    {
        String pattern =
            getPattern(_numberTooLargeMessage, "number-too-large", field.getPage().getLocale());

        return formatString(pattern, field.getDisplayName(), _maximum);
    }

    /**
     *  Overrides the <code>number-too-small</code> bundle key.
     *  Parameter {0} is the display name of the field.
     *  Parameter {1} is the minimum allowed value.
     * 
     *  @since 3.0
     * 
     */

    public void setNumberTooSmallMessage(String string)
    {
        _numberTooSmallMessage = string;
    }

    /** @since 3.0 */

    protected String buildNumberTooSmallMessage(IFormComponent field)
    {
        String pattern =
            getPattern(_numberTooSmallMessage, "number-too-small", field.getPage().getLocale());

        return formatString(pattern, field.getDisplayName(), _minimum);
    }

}