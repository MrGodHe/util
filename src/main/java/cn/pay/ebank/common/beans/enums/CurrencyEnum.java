/**
 * www.peigen.info Inc.
 * Copyright (c) 2012 All Rights Reserved.
 */
package cn.pay.ebank.common.beans.enums;

import cn.pay.ebank.common.utils.StringUtils;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Map;

/**
 * 
 * @Version 1.0
 * 
 * @Author
 * 
 *
 * 
 *
 */

public enum CurrencyEnum {
	
	//RMB("RMB", "人民币", "", "156"),
	
	CNY("CNY", "人民币", "￥", "156"),
	
	USD("USD", "美元", "$", "840"),
	
	JPY("JPY", "日元", "￥", "392"),
	
	HKD("HKD", "港币", "HK$", "344"),
	
	GBP("GBP", "英镑", "￡", "826"),

	/*EUR("EUR", "欧元", "EUR", "978"),
	
	AUD("AUD", "澳元", "", "036"),
	
	CAD("CAD", "加元", "", "124"),
	
	SGD("SGD", "坡币", "", "702"),
	
	NZD("NZD", "新西", "", "554"),
	
	TWD("TWD", "台币", "", "901"),
	
	KRW("KRW", "韩元", "", "410"),
	
	DKK("DKK", "丹朗", "", "208"),
	
	TRY("TRY", "土拉", "", "949"),
	
	MYR("MYR", "马来", "", "458"),
	
	THB("THB", "泰铢", "", "764"),
	
	INR("INR", "印卢", "", "356"),
	
	PHP("PHP", "菲比", "", "608"),
	
	CHF("CHF", "瑞士", "", "756"),
	
	SEK("SEK", "瑞典", "", "752"),
	
	ILS("ILS", "以谢", "", "376"),
	
	ZAR("ZAR", "南非", "", "710"),
	
	RUB("RUB", "俄卢", "", "643"),
	
	NOK("NOK", "挪威克朗", "", "578"),
	
	AED("AED", "阿联酋", "", "784"),
	
	BRL("BRL", "巴西雷亚尔", "", "986"),
	
	IDR("IDR", "印尼卢比", "", "360"),
	
	SAR("SAR", "沙特里亚尔", "", "682"),
	
	MXN("MXN", "墨西哥比索", "", "484"),
	
	PLN("PLN", "波兰兹罗提", "", "985"),
	
	VND("VND", "越南盾", "", "704"),
	
	CLP("CLP", "智利比索", "", "152"),
	
	KZT("KZT", "哈萨克腾格", "", "398"),
	
	CZK("CZK", "捷克克朗", "", "203"),
	
	NGN("NGN", "尼日利亚奈拉", "", "566"),
	
	COP("COP", "哥伦比亚比索", "", "170"),
	
	EGP("EGP", "埃及镑", "", "818"),
	
	VEF("VEF", "委玻利瓦尔", "", "937"),
	
	KWD("KWD", "科威特第纳尔", "", "414"),
	
	ARS("ARS", "阿根廷比索", "", "032"),
	
	MOP("MOP", "澳门元", "", "446"),
	
	RON("RON", "罗马尼亚列伊", "", "946"),
	
	QAR("QAR", "卡塔尔里亚尔", "", "634"),
	
	UAH("UAH", "乌格里夫纳", "", "348"),
	
	HUF("HUF", "匈牙利福林", "", "348"),
	
	PKR("PKR", "巴基斯坦卢比", "", "586"),
	
	LBP("LBP", "黎巴嫩镑", "", "422"),
	
	JOD("JOD", "约旦第纳尔", "", "400"),
	
	PEN("PEN", "秘鲁新索尔", "", "566"),*/
;
	
	/** 币种简码 */
	private final String code;
	
	/** 币种描述 */
	private final String message;
	
	/** 币种符号 */
	private final String symbol;
	
	/** 币种标识 */
	private final String identify;
	
	/**
	 * 构建一个<code>CurrencyEnum.java</code>
	 * @param code
	 * @param message
	 * @param symbol
	 * @param identify
	 */
	private CurrencyEnum(String code, String message, String symbol, String identify) {
		this.code = code;
		this.message = message;
		this.symbol = symbol;
		this.identify = identify;
	}
	
	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		return code;
	}
	
	/**
	 * @return Returns the message.
	 */
	public String getMessage() {
		return message;
	}
	
	/**
	 * @return Returns the code.
	 */
	public String code() {
		return code;
	}
	
	/**
	 * @return Returns the message.
	 */
	public String message() {
		return message;
	}
	
	/**
	 * @return Returns the symbol
	 */
	public String getSymbol() {
		return symbol;
	}
	
	public String getIdentify() {
		return identify;
	}
	
	/**
	 * 通过枚举<code>code</code>获得枚举
	 * 
	 * @param code
	 * @return CurrencyEnum
	 */
	public static CurrencyEnum getByCode(String code) {
		for (CurrencyEnum _enum : values()) {
			if (StringUtils.equals(_enum.getCode(), code)) {
				return _enum;
			}
		}
		return null;
	}
	
	/**
	 * 通过枚举<code>identify</code>获得枚举
	 * 
	 * @param identify
	 * @return CurrencyEnum
	 */
	public static CurrencyEnum getByIdentify(String identify) {
		for (CurrencyEnum _enum : values()) {
			if (StringUtils.equals(_enum.getIdentify(), identify)) {
				return _enum;
			}
		}
		return null;
	}
	
	/**
	 * 获取全部枚举
	 * 
	 * @return List<CurrencyEnum>
	 */
	public List<CurrencyEnum> getAllEnum() {
		List<CurrencyEnum> list = new ArrayList<CurrencyEnum>();
		for (CurrencyEnum _enum : values()) {
			list.add(_enum);
		}
		return list;
	}
	
	/**
	 * 获取全部枚举值
	 * 
	 * @return List<String>
	 */
	public List<String> getAllEnumCode() {
		List<String> list = new ArrayList<String>();
		for (CurrencyEnum _enum : values()) {
			list.add(_enum.code());
		}
		return list;
	}

	/**
	 * Gets the default number of fraction digits used with this currency.
	 * For example, the default number of fraction digits for the Euro is 2,
	 * while for the Japanese Yen it's 0.
	 * In the case of pseudo-currencies, such as IMF Special Drawing Rights,
	 * -1 is returned.
	 *
	 * @return the default number of fraction digits used with this currency
	 */
	public int getDefaultFractionDigits() {
		return Currency.getInstance(getCode()).getDefaultFractionDigits();
	}
	
	/**
	 * 判断给定的枚举，是否在列表中
	 *
	 * @param value 检查的值
	 * @param values 列表
	 * @return
	 */
	public boolean isInList(CurrencyEnum value, CurrencyEnum... values) {
		for (CurrencyEnum e : values) {
			if (value == e) {
				return true;
			}
		}
		return false;
	}

	public static Map<String, String> mapping() {
		Map<String, String> map = Maps.newLinkedHashMap();
		for (CurrencyEnum type : values()) {
			map.put(type.getCode(), type.getMessage());
		}
		return map;
	}
}
