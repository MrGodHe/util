package cn.pay.ebank.common.beans.conversion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import cn.pay.ebank.common.utils.CollectionUtils;
import cn.pay.ebank.common.utils.PrimitiveUtils;

/**
 * short 的类型转换器。
 * 
 * @author Agreal·Lee (e-mail:lixiang@ebank.pay.cn)
 * 
 */
public class ShortPrimitiveTypeConverter extends AbstractTypeConverter<Short> {
	
	public Class<Short> getTargetType() {
		return Short.TYPE;
	}
	
	public List<Class<?>> getSupportedSourceTypes() {
		List<Class<?>> classes = new ArrayList<Class<?>>();
		CollectionUtils.add(classes, PrimitiveUtils.getAllPrimitiveClasses());
		CollectionUtils.add(classes, PrimitiveUtils.getAllWrapperClasses());
		classes.add(Object[].class);
		classes.add(Collection.class);
		classes.add(CharSequence.class);
		classes.add(CharSequence[].class);
		return classes;
	}
	
	public Short convert(Object value, Class<? extends Short> toType) {
		try {
			return shortValue(value);
		} catch (Exception e) {
			throw new TypeConversionException(e);
		}
	}
	
	public static short shortValue(Object value) throws NumberFormatException {
		return (short) IntTypeConverter.intValue(value);
	}
}
