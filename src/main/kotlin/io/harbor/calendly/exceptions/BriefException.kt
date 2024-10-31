package io.harbor.calendly.exceptions

import io.harbor.calendly.util.MSG_ERR_GENERIC


class BriefException(val code: Int, msg: String = MSG_ERR_GENERIC) : Exception(msg) {

  constructor(code: Int, throwable: Throwable) : this(code)
}
