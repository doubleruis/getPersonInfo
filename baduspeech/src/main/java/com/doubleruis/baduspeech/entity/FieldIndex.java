package com.doubleruis.baduspeech.entity;
/**
 * @author dihb
 * @time 2019年6月6日 上午9:46:45 
 * TODO  参数的索引
 */
public class FieldIndex {
	
	private String indexid;    //索引的编码
	
	private String fieldid;    //参数的编码
	
	private String indexword;  //索引的关键词
	
	private String indexkey;   //索引关键词对应的值

	public String getIndexid() {
		return indexid;
	}

	public void setIndexid(String indexid) {
		this.indexid = indexid;
	}

	public String getFieldid() {
		return fieldid;
	}

	public void setFieldid(String fieldid) {
		this.fieldid = fieldid;
	}

	public String getIndexword() {
		return indexword;
	}

	public void setIndexword(String indexword) {
		this.indexword = indexword;
	}

	public String getIndexkey() {
		return indexkey;
	}

	public void setIndexkey(String indexkey) {
		this.indexkey = indexkey;
	}
	
	
}
