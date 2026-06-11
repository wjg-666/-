package com.blog.utils;

import java.util.HashMap;
import java.util.Map;

public class Result extends HashMap<String, Object> {

    public static final int SUCCESS_CODE = 200;
    public static final int ERROR_CODE = 500;
    public static final int UNAUTHORIZED_CODE = 401;
    public static final int FORBIDDEN_CODE = 403;
    public static final int NOT_FOUND_CODE = 404;

    public Result() {
        put("code", SUCCESS_CODE);
        put("message", "操作成功");
    }

    public static Result ok() {
        return new Result();
    }

    public static Result ok(String message) {
        Result result = new Result();
        result.put("message", message);
        return result;
    }

    public static Result ok(Object data) {
        Result result = new Result();
        result.put("data", data);
        return result;
    }

    public static Result ok(String message, Object data) {
        Result result = new Result();
        result.put("message", message);
        result.put("data", data);
        return result;
    }

    public static Result error() {
        return error(ERROR_CODE, "操作失败");
    }

    public static Result error(String message) {
        return error(ERROR_CODE, message);
    }

    public static Result error(int code, String message) {
        Result result = new Result();
        result.put("code", code);
        result.put("message", message);
        return result;
    }

    public static Result unauthorized(String message) {
        return error(UNAUTHORIZED_CODE, message);
    }

    public static Result forbidden(String message) {
        return error(FORBIDDEN_CODE, message);
    }

    public static Result notFound(String message) {
        return error(NOT_FOUND_CODE, message);
    }

    public Result put(String key, Object value) {
        super.put(key, value);
        return this;
    }

    public Result data(Object value) {
        return this.put("data", value);
    }

    public Result message(String message) {
        return this.put("message", message);
    }
}