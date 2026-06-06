/*
 * DMIS - Document Management Information System
 * Dr M H B Ariyaratne
 * buddhika.ari@gmail.com
 */
package lk.gov.health.phsp.ws.common;

/**
 * Standard envelope for all /api/* responses.
 * Success: {"status":"success","code":200,"data":{...}}
 * Error:   {"status":"error","code":401,"message":"..."}
 */
public class ApiResponseDto {

    private String status;
    private int code;
    private Object data;
    private String message;

    public static ApiResponseDto success(Object data) {
        ApiResponseDto r = new ApiResponseDto();
        r.status = "success";
        r.code = 200;
        r.data = data;
        return r;
    }

    public static ApiResponseDto error(int code, String message) {
        ApiResponseDto r = new ApiResponseDto();
        r.status = "error";
        r.code = code;
        r.message = message;
        return r;
    }

    public String getStatus() { return status; }
    public int getCode() { return code; }
    public Object getData() { return data; }
    public String getMessage() { return message; }

}
