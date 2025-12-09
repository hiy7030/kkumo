package com.kkumo.global.error

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }

        fun <T> error(errorCode: ErrorCode, message: String? = null): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorResponse(
                    code = errorCode.name,
                    message = message ?: errorCode.message
                )
            )
        }
    }

    data class ErrorResponse(
        val code: String,
        val message: String
    )
}
