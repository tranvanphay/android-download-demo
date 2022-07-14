/*
 * Copyright (C) 2016 AriaLyy(https://github.com/AriaLyy/Aria)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.arialyy.aria.http;

import android.text.TextUtils;
import com.arialyy.aria.core.common.RequestEnum;
import com.arialyy.aria.core.inf.ITaskOption;
import com.arialyy.aria.core.processor.IHttpFileLenAdapter;
import com.arialyy.aria.core.processor.IHttpFileNameAdapter;

import java.lang.ref.SoftReference;
import java.net.CookieManager;
import java.net.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Information set by the Http task, such as: cookies, request parameters
 */
public final class HttpTaskOption implements ITaskOption {

  private CookieManager cookieManager;

  /**
   * request parameter
   */
  private Map<String, String> params;

  /**
   * http request header
   */
  private Map<String, String> headers = new HashMap<>();

  /**
   * Character encoding, default is "utf-8"
   */
  private String charSet = "utf-8";

  /**
   * network request type
   */
  private RequestEnum requestEnum = RequestEnum.GET;

  /**
   * Whether to use the filename passed by the server through content-disposition, the content format is {@code attachment; filename="filename.jpg"} {@code true}
   * use
   */
  private boolean useServerFileName = false;

  /**
   * redirect link
   */
  private String redirectUrl = "";

  /**
   * Whether it is chunk mode
   */
  private boolean isChunked = false;
  /**
   * The key required for file upload
   */
  private String attachment;

  private Proxy proxy;
  /**
   * file upload form
   */
  private Map<String, String> formFields = new HashMap<>();

  private SoftReference<IHttpFileLenAdapter> fileLenAdapter;

  private SoftReference<IHttpFileNameAdapter> fileNameAdapter;

  public IHttpFileLenAdapter getFileLenAdapter() {
    return fileLenAdapter == null ? null : fileLenAdapter.get();
  }
  public IHttpFileNameAdapter getFileNameAdapter() {
    return fileNameAdapter == null ? null : fileNameAdapter.get();
  }
  /**
   * If it is an anonymous inner class, you need to set the adapter to be empty after completion, otherwise there will be a memory leak
   */
  public void setFileLenAdapter(IHttpFileLenAdapter fileLenAdapter) {
    this.fileLenAdapter = new SoftReference<>(fileLenAdapter);
  }
  public void setFileNameAdapter(IHttpFileNameAdapter fileNameAdapter) {
    this.fileNameAdapter = new SoftReference<>(fileNameAdapter);
  }
  public Map<String, String> getFormFields() {
    return formFields;
  }

  public void setFormFields(Map<String, String> formFields) {
    this.formFields = formFields;
  }

  public String getAttachment() {
    return TextUtils.isEmpty(attachment) ? "file" : attachment;
  }

  public void setAttachment(String attachment) {
    this.attachment = attachment;
  }

  public boolean isChunked() {
    return isChunked;
  }

  public void setChunked(boolean chunked) {
    isChunked = chunked;
  }

  public CookieManager getCookieManager() {
    return cookieManager;
  }

  public void setCookieManager(CookieManager cookieManager) {
    this.cookieManager = cookieManager;
  }

  public Proxy getProxy() {
    return proxy;
  }

  public void setProxy(Proxy proxy) {
    this.proxy = proxy;
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = headers;
  }

  public String getCharSet() {
    return TextUtils.isEmpty(charSet) ? "utf-8" : charSet;
  }

  public void setCharSet(String charSet) {
    this.charSet = charSet;
  }

  public RequestEnum getRequestEnum() {
    return requestEnum;
  }

  public void setRequestEnum(RequestEnum requestEnum) {
    this.requestEnum = requestEnum;
  }

  public boolean isUseServerFileName() {
    return useServerFileName;
  }

  public void setUseServerFileName(boolean useServerFileName) {
    this.useServerFileName = useServerFileName;
  }

  public String getRedirectUrl() {
    return redirectUrl;
  }

  public void setRedirectUrl(String redirectUrl) {
    this.redirectUrl = redirectUrl;
  }

  public Map<String, String> getParams() {
    return params;
  }

  public void setParams(Map<String, String> params) {
    this.params = params;
  }

}
