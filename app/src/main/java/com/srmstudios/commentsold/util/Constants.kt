package com.srmstudios.commentsold.util

const val API_BASE_URL = "https://cscodetest.herokuapp.com"
const val DATABASE_NAME = "db_comment_sold"
const val DATABASE_VERSION = 1

const val AUTHORIZATION = "Authorization"
const val BEARER = "Bearer"

const val CODE_UNAUTHORIZED = 401
const val CODE_INTERNAL_SERVER_ERROR = 500

const val LOGIN_API_ERROR_CODE = 2
const val LOGIN_API_SUCCESS_CODE = 0
const val QUERY_PARAM_PAGE = "page"
const val QUERY_PARAM_LIMIT = "limit"
const val QUERY_PARAM_COLOR = "color"
const val QUERY_PARAM_SIZE = "size"
const val QUERY_PARAM_QUANTITY = "quantity"
const val PAGE_SIZE_PRODUCTS = 20
const val FIRST_PAGE_PRODUCTS_OFFSET = 0
const val NUM_COLUMNS_PRODUCTS = 2
const val PAGE_SIZE_INVENTORY = 20
const val FIRST_PAGE_INVENTORY_OFFSET = 0

val productSizes = listOf("XS", "S", "M", "L", "XL", "XXL")
