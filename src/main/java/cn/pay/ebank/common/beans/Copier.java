/*
 * www.ebank.pay.cn Inc.
 * Copyright (c) 2014 All Rights Reserved
 */

/*
 * 修订记录:
 * zhyang@ebank.pay.cn 2015-04-21 19:35 创建
 *
 */
package cn.pay.ebank.common.beans;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


import javassist.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cn.pay.ebank.common.exception.CommonException;
import cn.pay.ebank.common.beans.conversion.TypeConverterUtils;
import cn.pay.ebank.common.utils.ArrayUtils;
import cn.pay.ebank.common.utils.CollectionUtils;
import cn.pay.ebank.common.utils.PrimitiveUtils;
import cn.pay.ebank.common.utils.StringUtils;

/**
 * 对象属性复制工具类,采用javassit生成源代码实现属性复制.并能实现包装类型和基础类型之间的转换 <h3>Usage Examples</h3>
 * <p/>
 * 该工具使用 {@link TypeConverterUtils} 完成类型转换。
 * <p/>
 * <pre class="code">
 * {
 * &#064;code
 * TestBean testBean = new TestBean();
 * Copier.copy(TestBean1.createTest(), testBean);
 * Copier.copy(TestBean1.createTest(), testBean, &quot;t1&quot;);
 * }
 * </pre>
 * <p/>
 * 使用时请确保classpath中有:
 * <p/>
 * <p/>
 * <pre class="code">
 * {@code
 * <dependency>
 * <groupId>org.javassist</groupId>
 * <artifactId>javassist</artifactId>
 * </dependency>
 * }
 * </pre>
 *
 * @author zhyang@ebank.pay.cn
 */
public class Copier {

	private static final Logger logger = LoggerFactory.getLogger (Copier.class);

	private static final String packageName = getPackageName (Copier.class);
	/**
	 * 生成class dump路径
	 */
	public static String dumpClass = null;

	/**
	 * 打印生成源代码
	 */
	public static boolean logSource = false;

	/**
	 * Copy实现类对象缓存
	 */
	private static final Map<Key, Copy> copierMap = new ConcurrentHashMap<Key, Copy> ();

	public static enum CopyStrategy {
		/**
		 * 复制时忽略null对象，源对象属性为null，目标对象属性不会被设置为null
		 */
		IGNORE_NULL,
		/**
		 * 复制时包括null对象。源对象属性为null，目标对象属性会被设置为null
		 */
		CONTAIN_NULL
	}
	
	/**
	 * 当拷贝属性不兼容时的处理规则。
	 *
	 * @since 2.1
	 */
	public static enum NoMatchingRule {
		
		/**
		 * 忽略
		 */
		IGNORE,
		
		/**
		 * 抛出异常
		 */
		EXCEPTION;
		
	}

	public static interface Copy {
		void copy (Object source, Object target);
	}

	/**
	 * @param from            源对象
	 * @param toClass         目标类型
	 * @param ignorePropeties 忽略参数
	 * @return 复制结果
	 */
	public static <T> T copy (Object from, Class<T> toClass, String... ignorePropeties) {
		return copy (from, toClass, CopyStrategy.CONTAIN_NULL, ignorePropeties);
	}

	/**
	 * @param from            源对象
	 * @param toClass         目标类型
	 * @param strategy        复制策略
	 * @param ignorePropeties 忽略参数
	 * @return 复制结果
	 */
	public static <T> T copy (Object from, Class<T> toClass, CopyStrategy strategy, String... ignorePropeties) {
		notNull (toClass, "toClass不能为空");
		try {
			T target = toClass.newInstance ();
			copy (from, target, strategy, NoMatchingRule.IGNORE, ignorePropeties);
			return target;
		} catch (Exception e) {
			throw new CommonException (e);
		}
	}

	/**
	 * 属性复制，复制时会做类型转换，复制时包括null值
	 *
	 * @param from            源对象
	 * @param to              目标对象
	 * @param ignorePropeties 忽略属性
	 */
	public static void copy (Object from, Object to, String... ignorePropeties) {
		copy (from, to, CopyStrategy.CONTAIN_NULL, NoMatchingRule.IGNORE, ignorePropeties);
	}

	/**
	 * 属性复制
	 *
	 * @param from            源对象
	 * @param to              目标对象
	 * @param strategy        复制策略
	 * @param ignorePropeties 忽略属性
	 */
	public static void copy (Object from, Object to, CopyStrategy strategy, NoMatchingRule noMatchingRule, String... ignorePropeties) {
		if (from == null) {
			return;
		}
		Key key = getKey (notNull (from, "源对象不能为空"), notNull (to, "目标对象不能为空"), notNull (strategy, "拷贝策略不能为空"), ignorePropeties);
		Copy copy = copierMap.get (key);
		if (copy == null) {
			synchronized (Copier.class) {
				copy = copierMap.get (key);
				if (copy == null) {
					Copier.Generator generator = new Copier.Generator ();
					generator.setSource (from.getClass ());
					generator.setTarget (to.getClass ());
					generator.setIgnorePropeties (ignorePropeties);
					generator.setNoMatchingRule (noMatchingRule);
					generator.setStrategy (strategy);
					try {
						copy = generator.generate ().newInstance ();
						copierMap.put (key, copy);
					} catch (Exception e) {
						throw new RuntimeException (e);
					}
				}
			}
		}
		copy.copy (from, to);
	}

	private static Key getKey (Object from, Object to, CopyStrategy strategy, String[] ignoreProperties) {
		Class<?> fromClass = from.getClass ();
		Class<?> toClass = to.getClass ();
		return new Key (fromClass, toClass, ignoreProperties);
	}

	private static String getPackageName (Class<?> clazz) {
		String className = clazz.getName ();
		int lastDotIndex = className.lastIndexOf (".");
		return (lastDotIndex != -1 ? className.substring (0, lastDotIndex) : "");
	}

	private static <T> T notNull (T obj, String message) {
		if (obj == null) {
			throw new NullPointerException (message);
		}
		return obj;
	}

	private static <T> T notNull (T obj) {
		return notNull (obj, null);
	}

	private static class Generator {
		private static AtomicInteger classNameIndex = new AtomicInteger (1000);
		private final static String SOURCE = "s";
		private final static String TARGET = "t";
		private Class<?> source;
		private Class<?> target;
		private String[] ignorePropeties = StringUtils.EMPTY_STRINGS;
		
		private NoMatchingRule noMatchingRule;

		public CopyStrategy getStrategy () {
			return strategy;
		}

		public void setStrategy (CopyStrategy strategy) {
			this.strategy = strategy;
		}

		private CopyStrategy strategy;

		private String beginSource;
		private List<String> propSources = new ArrayList<String> ();

		private String endSources;

		public void setSource (Class<?> source) {
			this.source = source;
		}

		public void setTarget (Class<?> target) {
			this.target = target;
		}

		public String[] getIgnorePropeties () {
			return ignorePropeties;
		}

		public void setIgnorePropeties (String[] ignorePropeties) {
			this.ignorePropeties = ignorePropeties;
		}

		public void setNoMatchingRule (NoMatchingRule noMatchingRule) {
			this.noMatchingRule = noMatchingRule;
		}

		private void generateBegin () {
			// 生成方法签名public void copy(TestBean s, TestBean1 t) {
			beginSource = "public void copy(Object " + SOURCE + "1 ,Object " + TARGET + "1){\n";
			// 强制转换源对象
			String convertSource = "\t" + source.getName () + " " + SOURCE + " =" + "(" + source.getName () + ")" + SOURCE + "1;\n";
			// 强制转换目标对象
			String convertTarget = "\t" + target.getName () + " " + TARGET + " =" + "(" + target.getName () + ")" + TARGET + "1;\n";
			//临时值
			String tmpValue = "\tObject tmpValue = null; \n";
			beginSource += convertSource + convertTarget + tmpValue;
			
		}

		private void generateEnd () {
			endSources = "\n}";
		}

		private HashMap<String, List<String>> findCopyFrom (Class<?> clazz) {
			HashMap<String, List<String>> fieldMap = CollectionUtils.newHashMap ();

			Class<?> superClass = clazz.getSuperclass ();
			if (superClass != null && !Object.class.equals (superClass)) {
				fieldMap.putAll (findCopyFrom (superClass));
			}

			Field[] fields = clazz.getDeclaredFields ();
			for (Field field : fields) {
				Annotation[] annotations = field.getDeclaredAnnotations ();
				if (annotations != null && annotations.length > 0) {
					for (Annotation annotation : annotations) {
						if (CopyFrom.class.equals (annotation.annotationType ())) {
							CopyFrom copyFrom = (CopyFrom) annotation;
							for (String value : copyFrom.value ()) {
								List<String> filedNames = fieldMap.get (field.getName ());
								if (filedNames == null) {
									filedNames = CollectionUtils.newArrayList ();
									fieldMap.put (field.getName (), filedNames);
								}
								filedNames.add (value);
							}
						}
					}
				}
			}
			return fieldMap;
		}

		private HashMap<String, String> findCopyTo (Class<?> clazz) {
			HashMap<String, String> fieldMap = CollectionUtils.newHashMap ();

			Class<?> superClass = clazz.getSuperclass ();
			if (superClass != null && !Object.class.equals (superClass)) {
				fieldMap.putAll (findCopyTo (superClass));
			}


			Field[] fields = clazz.getDeclaredFields ();
			for (Field field : fields) {
				Annotation[] annotations = field.getDeclaredAnnotations ();
				if (annotations != null && annotations.length > 0) {
					for (Annotation annotation : annotations) {
						if (CopyTo.class.equals (annotation.annotationType ())) {
							CopyTo copyFrom = (CopyTo) annotation;
							for (String value : copyFrom.value ()) {
								fieldMap.put (value, field.getName ());
							}
						}
					}
				}
			}
			return fieldMap;
		}

		private void generateBody () {

			PropertyDescriptor[] getters = getPropertyDescriptors (source);
			PropertyDescriptor[] setters = getPropertyDescriptors (target);

			Map<String, PropertyDescriptor> getterMap = new HashMap<String, PropertyDescriptor> ();
			for (PropertyDescriptor getter : getters) {
				getterMap.put (getter.getName (), getter);
			}

			HashMap<String, String> fromGetterMap = findCopyTo (this.source);
			HashMap<String, List<String>> toSetterMap = findCopyFrom (this.target);

			for (PropertyDescriptor setter : setters) {
				PropertyDescriptor getter = getterMap.get (setter.getName ());
				if (getter == null) {
					//find from
					List<String> sourceFields = toSetterMap.get (setter.getName ());
					if (CollectionUtils.isNotEmpty (sourceFields)) {
						for (String fromField : sourceFields) {
							getter = getterMap.get (fromField);
							if (getter != null) {
								break;
							}
						}
					}
				}
				if (getter == null) {
					String fromField = fromGetterMap.get (setter.getName ());
					getter = getterMap.get (fromField);
				}
				if (this.checkCanGenSource (setter, getter)) {
					Method readMethod = getter.getReadMethod ();
					Method writeMethod = setter.getWriteMethod ();
					String readMethodName = readMethod.getName ();
					String writerMethodName = writeMethod.getName ();
					String setterTypeName;
					Class<?> getterPropertyType = getter.getPropertyType ();
					if (compatible (getter, setter)) {
						propSources.add ("\t");
						if (strategy == CopyStrategy.IGNORE_NULL && !getterPropertyType.isPrimitive ()) {
							String source = genCheckWrapperIsNotNullSource (readMethod.getName ());
							source += "\t" + genPropertySource (writerMethodName, SOURCE + "." + readMethodName + "()");
							propSources.add (source);
						} else {
							propSources.add (genPropertySource (writerMethodName, SOURCE + "." + readMethodName + "()"));
						}
					} else {
						// 是否是包装类转换
						Class<?> setterPropertyType = setter.getPropertyType ();
						if (compatibleWrapper (getter, setter)) {
							Convertor convert = new Convertor (setterPropertyType, SOURCE, readMethod.getName ());
							String f = convert.convert ();
							if (f != null) {
								propSources.add ("\t");
								if (isWrapClass (getterPropertyType)) {
									String source = genCheckWrapperIsNotNullSource (readMethod.getName ());
									source += "\t" + genPropertySource (writerMethodName, f);
									propSources.add (source);
								} else {
									propSources.add (genPropertySource (writerMethodName, f));
								}
								continue;
							}
						} else {
							StringBuilder sb = new StringBuilder (400);

							String typeName = buildTypeName (setter, false);
							//gettter 是否基础类型
							boolean isGetterPrimitive = getterPropertyType.isPrimitive ();

							String s = TARGET + "." + writerMethodName + "(";

							String getValue = SOURCE + "." + readMethodName + "()";
							if (!isGetterPrimitive) {
								getValue = "tmpValue";
							}
							String value;
							if (setterPropertyType.isPrimitive ()) {
								value= PrimitiveUtils.class.getName () + ".value((" + PrimitiveUtils.getWrapperClass (setterPropertyType.getName ()).getName () + ") " + TypeConverterUtils.class.getName () + ".convertValue(" + (getterPropertyType.isPrimitive () ? PrimitiveUtils.class.getName () + ".value(" + getValue + ")" : buildTypeName (getter, false) + ".class.cast(" + getValue + ")") + ", " + typeName + ".class))";
							} else {
								value= "(" + typeName + ")" + typeName + ".class.cast( " + TypeConverterUtils.class.getName () + ".convertValue(" + (getterPropertyType.isPrimitive () ? PrimitiveUtils.class.getName () + ".value(" + getValue + ")" : buildTypeName (getter, false) + ".class.cast(" + getValue + ")") + ", " + typeName + ".class))";
							}
							s+= value +");\n";

							switch (this.noMatchingRule) {
								case IGNORE:
									String retract ="";
									if (!isGetterPrimitive) {
										sb.append ("\ttmpValue = "+  SOURCE + "." + readMethodName + "()" + ";\n");
										sb.append ("\tif(tmpValue != null) {\n");
										retract ="\t";
									}

									sb.append (retract).append ("\ttry {\n");
									sb.append (retract).append ("\t\t").append (s);
									sb.append (retract).append ("\t} catch (java.lang.ClassCastException e) {\n");
									sb.append (retract).append ("\t\n");
									sb.append (retract).append ("\t}\n");
									if (!isGetterPrimitive) {
										sb.append ("\t}\n");
									}
									break;
								case EXCEPTION:
									sb.append (s);
									break;
								default:
									throw new InternalError ();
							}
//							String writerMethodValue = TypeConverterUtils.class.getName () + ".convertValue(" + TypeConverterUtils.getCaseToObjectStr (getterType, SOURCE + "." + readMethodName + "()") + ", " + typeName + ".class)";
//							String writerMethod = "tmpValue = "+ writerMethodValue + ";\n";
//							writerMethod += "\tif(tmpValue != null) {\n\t\t";
//							String s = writerMethod + TARGET + "." + writerMethodName + "(" + TypeConverterUtils.getCaseFromObjectStr (setterType, "tmpValue") + ");\n\t}\n";
//							propSources.add (s);
							this.propSources.add (sb.toString ());
							continue;
						}
						warnCantConvert (setter, getter);
					}
				}
			}
		}

		private String buildTypeName (PropertyDescriptor setter, boolean isPrimitive) {
			String typeName;
			if (setter.getPropertyType ().isArray ()) {
				int dn = ArrayUtils.countDimension (setter.getPropertyType ());
				typeName = setter.getPropertyType ().getComponentType ().getName ();
				for (int i = 0; i < dn; i++) {
					typeName += "[]";
				}
			} else {
				typeName = setter.getPropertyType ().getName ();
			}
			return typeName;
		}

		private String genCheckWrapperIsNotNullSource (String readName) {
			return "if(" + SOURCE + "." + readName + "()!=null)\n";
		}

		private String genPropertySource (String writerMethodName, String getterSource) {
			return TARGET + "." + writerMethodName + "(" + getterSource + ");\n";
		}

		private void warnCantConvert (PropertyDescriptor setter, PropertyDescriptor getter) {
			logger.debug ("[对象属性复制]属性类型转换失败{}.{}({})->{}.{}({})", getter.getReadMethod ().getDeclaringClass ().getSimpleName (), getter.getName (), getter.getPropertyType (), setter.getWriteMethod ().getDeclaringClass ().getSimpleName (), setter.getName (), setter.getPropertyType ());
		}

		/**
		 * 检查是否可以生成源代码
		 */
		private boolean checkCanGenSource (PropertyDescriptor setter, PropertyDescriptor getter) {
			// 是否被忽略
			if (ignorePropeties != null && isIgnoredProperty (setter)) {
				return false;
			}
			// 检查getter是否存在
			if (getter == null) {
				logger.debug ("[对象属性复制]原对象[{}.{}]getter方法不存在", source.getCanonicalName (), setter.getName ());
				return false;
			}
			// 检查getter的读方法是否存在
			if (getter.getReadMethod () == null) {
				logger.debug ("[对象属性复制]原对象[{}.{}]getter方法不存在", source.getCanonicalName (), getter.getName ());
				return false;
			}
			// 检查setter的写方法是否存在
			if (setter.getWriteMethod () == null) {
				logger.debug ("[对象属性复制]目标对象[{}.{}]setter方法不存在", target.getCanonicalName (), setter.getName ());
				return false;
			}
			return true;
		}

		private boolean compatibleWrapper (PropertyDescriptor getter, PropertyDescriptor setter) {

			return isWrapClass (getter.getPropertyType (), setter.getPropertyType ()) || isWrapClass (setter.getPropertyType (), getter.getPropertyType ());
		}

		private boolean isIgnoredProperty (PropertyDescriptor descriptor) {
			if (this.ignorePropeties == null) {
				return false;
			}
			if (ArrayUtils.isEmpty (this.ignorePropeties)) {
				return false;
			}
			
			String propertyName = descriptor.getName ();
			for (String ignorePropety : ignorePropeties) {
				if (ignorePropety.equals (propertyName)) {
					return true;
				}
			}
			return false;
		}

		@SuppressWarnings({"unchecked"})
		public Class<Copy> generate () {
			generateBegin ();
			generateBody ();
			generateEnd ();
			StringBuilder sb = new StringBuilder ();
			sb.append (beginSource);
			for (String propSource : propSources) {
				sb.append (propSource);
			}
			sb.append (endSources);
			String source = sb.toString ();
			if (logSource) {
				logger.info ("\n{}", source);
			}
			ClassPool pool = ClassPool.getDefault ();
			ClassClassPath classPath = new ClassClassPath (this.getClass ());
			pool.insertClassPath (classPath);
			CtClass cc = pool.makeClass (packageName + ".CopierImpl" + classNameIndex.incrementAndGet ());

			Class<Copy> copyClass = null;
			try {
				cc.addInterface (pool.get (Copy.class.getName ()));
				CtMethod m = CtNewMethod.make (source, cc);
				cc.addMethod (m);
				if (dumpClass != null) {
					CtClass.debugDump = dumpClass;
				}
				ClassLoader classLoader = getDefaultClassLoader ();
				logger.debug ("classloader:{}", classLoader);
				copyClass = cc.toClass (classLoader, null);
			} catch (Exception e) {
				throw new RuntimeException (e);
			}
			return copyClass;
		}

		private boolean compatible (PropertyDescriptor getter, PropertyDescriptor setter) {
			return setter.getPropertyType ().isAssignableFrom (getter.getPropertyType ());
		}

		private ClassLoader getDefaultClassLoader () {
			ClassLoader cl = null;
			try {
				cl = Thread.currentThread ().getContextClassLoader ();
			} catch (Exception ex) {
				// ignore
			}
			if (cl == null) {
				cl = this.getClass ().getClassLoader ();
			}
			return cl;
		}

		public PropertyDescriptor[] getPropertyDescriptors (Class<?> clazz) {
			BeanInfo beanInfo;
			try {
				beanInfo = Introspector.getBeanInfo (clazz);
				return beanInfo.getPropertyDescriptors ();
			} catch (IntrospectionException e) {
				throw new RuntimeException (e);
			}

		}

		/**
		 * 是否为包装类
		 */
		public static boolean isWrapClass (Class<?> clazz) {
			try {
				return ((Class<?>) clazz.getField ("TYPE").get (null)).isPrimitive ();
			} catch (Exception e) {
				return false;
			}
		}

		/**
		 * source对象类型是否是target对象类型的包装类
		 */
		public static boolean isWrapClass (Class<?> source, Class<?> target) {
			if (!target.isPrimitive ()) {
				return false;
			}
			try {
				return source.getField ("TYPE").get (null) == target;
			} catch (Exception e) {
				return false;
			}
		}

	}

	/**
	 * 把source转换为target需要的包装器类型或者原始类型
	 */
	private static class Convertor {
		private String sourceName;
		private String readMethodName;
		private Class<?> targetType;

		private Convertor (Class<?> targetType, String sourceName, String readMethodName) {
			this.targetType = notNull (targetType);
			this.sourceName = notNull (sourceName);
			this.readMethodName = notNull (readMethodName);
		}

		public String convert () {
			if (targetType.isPrimitive ()) {
				String f = getPrimitiveFormat ();
				return getterSource () + "." + f + "()";
			} else if (Generator.isWrapClass (targetType)) {
				String f = getWrapperFormat ();
				return f + "(" + getterSource () + ")";
			} else {
				return null;
			}
		}

		private String getterSource () {
			return sourceName + "." + readMethodName + "()";
		}

		private String getPrimitiveFormat () {
			return targetType.getName () + "Value";
		}

		private String getWrapperFormat () {
			return targetType.getSimpleName () + ".valueOf";
		}

	}

	private static class Key {
		private Class<?> fromClass;
		private Class<?> toClass;
		private String[] ignoreProperties;
		private CopyStrategy strategy;

		@SuppressWarnings("unused")
		public Key (Class<?> fromClass, Class<?> toClass) {
			this.fromClass = fromClass;
			this.toClass = toClass;
		}

		public Key (Class<?> fromClass, Class<?> toClass, String[] ignoreProperties) {
			super ();
			this.fromClass = fromClass;
			this.toClass = toClass;
			this.ignoreProperties = ignoreProperties;
		}

		@SuppressWarnings("unused")
		public Key (Class<?> fromClass, Class<?> toClass, CopyStrategy strategy, String[] ignoreProperties) {
			super ();
			this.fromClass = fromClass;
			this.toClass = toClass;
			this.ignoreProperties = ignoreProperties;
			this.strategy = strategy;
		}

		@Override
		public boolean equals (Object o) {
			if (this == o)
				return true;
			if (o == null || getClass () != o.getClass ())
				return false;

			Key key = (Key) o;

			if (fromClass != null ? !fromClass.equals (key.fromClass) : key.fromClass != null)
				return false;
			if (!Arrays.equals (ignoreProperties, key.ignoreProperties))
				return false;
			if (strategy != key.strategy)
				return false;
			if (toClass != null ? !toClass.equals (key.toClass) : key.toClass != null)
				return false;

			return true;
		}

		@Override
		public int hashCode () {
			int result = fromClass != null ? fromClass.hashCode () : 0;
			result = 31 * result + (toClass != null ? toClass.hashCode () : 0);
			result = 31 * result + (ignoreProperties != null ? Arrays.hashCode (ignoreProperties) : 0);
			result = 31 * result + (strategy != null ? strategy.hashCode () : 0);
			return result;
		}
	}
}
