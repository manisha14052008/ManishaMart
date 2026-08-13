package com.manisha.manishamart.dto;

public class ApiResponse<T> {
    private boolean success;
    private T data;
    private ApiError error;

    public ApiResponse() {}

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = true;
        resp.data = data;
        resp.error = null;
        return resp;
    }

    public static <T> ApiResponse<T> fail(String code, String message) {
        ApiResponse<T> resp = new ApiResponse<>();
        resp.success = false;
        resp.data = null;
        resp.error = new ApiError(code, message);
        return resp;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
    public ApiError getError() { return error; }
    public void setError(ApiError error) { this.error = error; }

    public static class ApiError {
        private String code;
        private String message;

        public ApiError() {}

        public ApiError(String code, String message) {
            this.code = code;
            this.message = message;
        }

        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}
