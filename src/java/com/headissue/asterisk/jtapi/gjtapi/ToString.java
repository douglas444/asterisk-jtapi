/*
 *  (C) Copyright 2005 headissue GmbH; Jens Wilke
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.headissue.asterisk.jtapi.gjtapi;

import java.lang.reflect.Method;
import java.lang.reflect.Field;

/** 
 * A generic toString method, based on reflection.
 * Used for debugging.
 */
public class ToString {

  /** 
	* Return only the package local part of full qualified
	* class name. If the class is not in a package the
	* input string will be returned.
	*/
  public static String extractLocalClassName(String s) {
	 int i = s.lastIndexOf(".");
	 if (i>0) {
		return s.substring(i+1);
	 }
	 return s;
  }

  /** Escape non-displayable characters to unicode escapes */
  public static String escapeString(String s) {
	 StringBuffer sb = new StringBuffer();
	 char[] ca = s.toCharArray();
	 for (int i=0; i<ca.length; i++) {
		switch (ca[i]) {
		case '\t': sb.append("\\t"); break;
		case '\r': sb.append("\\r"); break;
		case '\n': sb.append("\\n"); break;
		case '\"': sb.append("\\\""); break;
		default:
		  if (ca[i]<32 || ca[i]>126) {
			 sb.append("\\u");
			 String n = Integer.toHexString(ca[i]);
			 sb.append("0000".substring(n.length()));
			 sb.append(n);
		  } else {
			 sb.append(ca[i]);
		  }
		}
	 }
	 return sb.toString();
  }

  public static String toHexDebug(byte[] ba) {
	 return toHexDebug(ba, 0, ba.length);
  }


  public static String toHexDebug(byte[] ba, int _idx, int _len) {
	 StringBuffer b = new StringBuffer();
	 for (int i=_idx; i<_len; i++) {
		if (i!=_idx) {
		  b.append(' ');
		}
		String s = Integer.toHexString(((int) ba[i])&0x0ff);
		b.append(s.length()==1 ? "0"+s : s);

	 }
	 return b.toString();
  }

  /**
	* Convert the object o in a usefull toString usable representation, by use of
	* public getXY, isXY and hasXY methods as well as public fields.
	*/
  public static String convert(Object o) {
	 Exception e;
	 return convert(o.getClass(), null, o);
  }

  /**
	* Identical to above, but use the (super)class c to find out the relevant
	* accessors.
	*/
  public static String convert(Class c, String[] _supressPrefixes, Object o) {
	 Exception e;
	 try {
		return new ToString(c, _supressPrefixes)._convert(o);
	 } catch (Exception ex) {
		e=ex;
	 }
	 return "<<ToString failure, got "+e+">>";
  }

  


  
  boolean more = false;
  boolean newlineFlag = false;
  StringBuffer buf = new StringBuffer();
  Object obj;
  Class aClass;
  String[] supressPrefixes;
  
  public ToString() {}

  public ToString(Class c) { aClass = c; }

  public ToString(Class c, String[] _supressPrefixes) { 
	 aClass = c; 
	 supressPrefixes = _supressPrefixes;
  }

  void newline() {
	 newlineFlag = true;
  }

  void append(String s) {
	 if (newlineFlag) {
		buf.append("\n   ");
	 }
	 buf.append(s);
	 newlineFlag=false;
  }

  void insertName(String n) {
	 if (n!=null) {
		append(n+"=");
	 }
  }

  public void insert(String _name, Object _value) {
	 String rs = null;
	 if (more) {
		buf.append(", ");
	 }
	 more = true;
	 if (_value==null) {
		insertName(_name);
		append("<null>");
		return;
	 }
         if (_value instanceof int[]) {
           insertName(_name);
           append("{");
           int[] ia = (int[]) _value;
           for (int i=0; i<ia.length;i++) {
             if (i>0) { append(","); }
             append(ia[i]+"");
           }
           append("}");
           return;
         }
	 if (_value instanceof Object[]) {
		insertName(_name);
		append("{");
		more = false;
		Object[] oa = (Object[]) _value;
		for (int i=0; i<oa.length; i++) {
		  insert(null, oa[i]);
		}
		append("}");
		return;
	 }
	 insertValue(_name, _value);
  }

  void insertValue(String n, Object o) {
	if (o instanceof String) {
	  insertName(n);
	  append("\""+escapeString((String) o)+"\"");
	  return;
	}
	if (o instanceof Number) {
	  insertName(n);
	  append(o+"");
	  return;
	}
	if (o instanceof Boolean) {
	  insertName(n);
	  append(o+"");
	  return;
	 }
	 if (o instanceof java.util.Date) {
	   insertName(n);
	   append(o+"");
	   return;
	 }
	 if (o instanceof java.util.Enumeration) {
	   insertName(n);
	   append("Enumeration{");
	   more = false;
 	   try {
		 java.util.Enumeration enu = (java.util.Enumeration) o;
		 while (enu.hasMoreElements()) {
		   insert(null, enu.nextElement());
		 }
	   } catch (Throwable t) {
		 append("<OOPS: "+t+">");
	   }
	   return;
	 }
	 String rs = null;
	 try {
		rs = o.toString();
		String t = "";
		if (n!=null) {
		  t= n+"=";
		}
		newline();
		append(t+reparseToString(rs));
		newline();
	 } catch (Throwable t) {
		append("<OOPS: "+t+">");
	 }
  }
  
  /** Adds an additional space after each \n */
  String reparseToString(String s) {
	 char[] ca = s.toCharArray();
	 StringBuffer b = new StringBuffer();
	 for (int i=0; i<ca.length; i++) {
		if (ca[i]=='\n') {
		  b.append("\n   ");
		} else {
		  b.append(ca[i]);
		}
	 }
	 return b.toString();
  }

  public void insert(String _name, int _value) {
	 insert(_name, new Integer(_value));
  }

  boolean shouldBeSupressed(String n) {
	 if (supressPrefixes==null) {
		return false;
	 }
	 for (int j=0; j<supressPrefixes.length; j++) {
		// System.err.println(n +"?"+supressPrefixes[j]);
		if (n.startsWith(supressPrefixes[j])) {
		  return true;
		}
	 }
	 return false;
  }

  void doMethod(Method m, String n) {
	 if (shouldBeSupressed(m.getName())) {
		return;
	 }
	 Object res = null;
	 try {
		res = m.invoke(obj, null);
	 } catch(java.lang.reflect.InvocationTargetException ex) {
           Throwable t = ex.getTargetException();
           res = "Oops! On invoke: "+t;
         } catch (Exception ex) {
           res = "Oops! On invoke: "+ex;
	 }
	 insert(n,res);
  }

  void extractAccessors() throws Exception {
	 Method[] ma = aClass.getMethods();
	 for(int i = 0; i<ma.length; i++) {
		Method m = ma[i];
		if (m.getParameterTypes().length==0){
		  String n = m.getName();
		  boolean esc = true;
		  if (n.equals("getClass")) {
			 continue;
		  }
		  if (n.equals("getStickerContext")) {
			 continue;
		  }
		  if (n.startsWith("get")) {
			 doMethod(m, n.substring(3));
			 continue;
		  }
		  if (n.startsWith("is")) {
			 doMethod(m, n.substring(2));
			 continue;
		  }
		  if (n.startsWith("has") && !n.equals("hashCode")) {
			 doMethod(m, n.substring(3));
		  }
		}
	 }
  }

  void extractFields() throws Exception {
	 Field [] fa = aClass.getFields();
	 for(int i = 0; i<fa.length; i++) {
		Field f = fa[i];
		insert(f.getName(), f.get(obj));
	 }
  }


  String _convert(Object o) throws Exception {
	 obj = o;
	 if (aClass==null) {
		aClass = obj.getClass();
	 }
	 buf.append(extractLocalClassName(aClass.getName()));
	 buf.append("(");
	 extractAccessors();
	 extractFields();
	 buf.append(")");
	 return buf.toString();
  }

  public String toString() {	 
	 StringBuffer b = new StringBuffer();
	 b.append(extractLocalClassName(aClass.getName()));
	 b.append("(");
	 b.append(buf.toString());
	 b.append(")");
	 return b.toString();
  }

}
