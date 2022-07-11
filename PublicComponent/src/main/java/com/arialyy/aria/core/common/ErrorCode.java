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
package com.arialyy.aria.core.common;

public enum ErrorCode {
  ERROR_CODE_NORMAL(0, "normal"), ERROR_CODE_TASK_ID_NULL(1, "Error code with empty task id"),
  ERROR_CODE_URL_NULL(2, "url is empty"), ERROR_CODE_URL_INVALID(3, "url is invalid"),
  ERROR_CODE_PAGE_NUM(4, "page and num cannot be less than 1"), ERROR_CODE_GROUP_URL_NULL(5, "The combined task url list is empty"),
  ERROR_CODE_UPLOAD_FILE_NULL(7, "Upload file does not exist"),
  ERROR_CODE_MEMBER_WARNING(8, "To prevent memory leaks, use static member classes (public static class xxx) or file classes (A.java)"),
  ERROR_CODE_TASK_NOT_EXIST(9, "Mission information does not exist");

  public int code;
  public String msg;

  ErrorCode(int code, String msg) {
    this.code = code;
    this.msg = msg;
  }
}
